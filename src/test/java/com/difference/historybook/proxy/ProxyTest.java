/*
 * Copyright 2016 Andrew W. Buchanan (buchanan@difference.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.difference.historybook.proxy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


import org.junit.Test;

import com.difference.historybook.proxy.ProxyFilter;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Charsets;

public abstract class ProxyTest {
	private static final int PROXY_PORT = 8082;
	private static final int DUMMY_SERVER_PORT = 8089;
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(DUMMY_SERVER_PORT);
	
	public abstract Proxy getProxy();
	
	@Test
	public void testSmallFetch() {
		String body = "<html><head></head><body>Hello World!</body></html>";
		
		stubFor(get(urlEqualTo("/some/page"))
	            .willReturn(aResponse()
	                .withStatus(200)
	                .withHeader("Content-Type", "text/html")
	                .withBody(body)));
		
		Proxy proxy = getProxy();
		try {
			proxy.start();
			
			java.net.Proxy proxyServer = new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", PROXY_PORT));
			HttpURLConnection connection = (HttpURLConnection)new URL("http://localhost:" + DUMMY_SERVER_PORT + "/some/page").openConnection(proxyServer);
			byte[] fetchedContent = IOUtils.toByteArray(connection.getInputStream());
			assertArrayEquals(body.getBytes(Charsets.UTF_8), fetchedContent);
			
			proxy.stop();
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
		
	}

	@Test
	public void testLargeFetch() {
		byte[] body = new byte[100 * 1024];
		new Random().nextBytes(body);
		
		stubFor(get(urlEqualTo("/some/page"))
	            .willReturn(aResponse()
	                .withStatus(200)
	                .withHeader("Content-Type", "text/html")
	                .withBody(body)));
		
		Proxy proxy = getProxy();
		try {
			proxy.start();
			
			java.net.Proxy proxyServer = new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", PROXY_PORT));
			HttpURLConnection connection = (HttpURLConnection)new URL("http://localhost:" + DUMMY_SERVER_PORT + "/some/page").openConnection(proxyServer);
			byte[] fetchedContent = IOUtils.toByteArray(connection.getInputStream());
			assertArrayEquals(body, fetchedContent);
			
			proxy.stop();
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
		
	}
	
	@Test
	public void testSelector() {
		String body = "<html><head></head><body>Hello World!</body></html>";
		
		stubFor(get(urlEqualTo("/some/page"))
	            .willReturn(aResponse()
	                .withStatus(200)
	                .withHeader("Content-Type", "text/html")
	                .withBody(body)));
		
		stubFor(get(urlEqualTo("/some/other/page"))
	            .willReturn(aResponse()
	                .withStatus(201)
	                .withHeader("Content-Type", "text/html")
	                .withBody(body)));

		Proxy proxy = getProxy();
		
		ProxyFilter filter = mock(ProxyFilter.class);
		ProxyFilterFactory factory = mock(ProxyFilterFactory.class);
		when(factory.getInstance()).thenReturn(filter);
		proxy.setFilterFactory(factory);
		proxy.setResponseFilterSelector(t -> {
			return t.getStatus() == 200;
		});
		
		try {
			proxy.start();
			
			java.net.Proxy proxyServer = new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", PROXY_PORT));
			
			HttpURLConnection connection = (HttpURLConnection)new URL("http://localhost:" + DUMMY_SERVER_PORT + "/some/page").openConnection(proxyServer);
			byte[] fetchedContent = IOUtils.toByteArray(connection.getInputStream());
			assertArrayEquals(body.getBytes(Charsets.UTF_8), fetchedContent);
			
			verify(filter).processRequest(any());
			verify(filter).processResponse(any());
			verifyNoMoreInteractions(filter);
			
			connection = (HttpURLConnection)new URL("http://localhost:" + DUMMY_SERVER_PORT + "/some/other/page").openConnection(proxyServer);
			fetchedContent = IOUtils.toByteArray(connection.getInputStream());
			assertArrayEquals(body.getBytes(Charsets.UTF_8), fetchedContent);

			verify(filter, times(2)).processRequest(any());
			verifyNoMoreInteractions(filter);

			proxy.stop();
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
	}
}
