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

package com.difference.historybook.proxy.littleproxy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSource;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.impl.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.difference.historybook.proxy.Proxy;
import com.difference.historybook.proxy.ProxyFilter;
import com.difference.historybook.proxy.ProxyFilterFactory;
import com.difference.historybook.proxy.ProxyTransactionInfo;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.LastHttpContent;

/**
 * An implementation of @Proxy using LittleProxy which is based on a Netty core 
 */
public class LittleProxy implements Proxy {
	private static final Logger LOG = LoggerFactory.getLogger(LittleProxy.class);

	private ProxyFilterFactory filterFactory;
	private Predicate<ProxyTransactionInfo> selector;
	
	private HttpProxyServer proxy = null;
	private int port = 8080;
	private int maxBufferSize = 1 * 1024 * 1024;
	
	@Override
	public LittleProxy setPort(int port) {
		this.port = port;
		return this;
	}

	@Override
	public LittleProxy setFilterFactory(ProxyFilterFactory filterFactory) {
		this.filterFactory = filterFactory;
		return this;
	}
	
	@Override
	public LittleProxy setResponseFilterSelector(Predicate<ProxyTransactionInfo> selector) {
		this.selector = selector;
		return this;
	}
	
	/**
	 * Set the maximum page size to buffer for selected responses
	 * 
	 * @param size size in bytes
	 * @return this for method chaining
	 */
	public LittleProxy setMaxBufferSize(int size) {
		this.maxBufferSize = size;
		return this;
	}

	@Override
	public void start() {
		if (proxy == null) {
			proxy = DefaultHttpProxyServer.bootstrap()
					.withPort(port)
					.withFiltersSource(getFiltersSource())
					.start();
		}
	}
	
	@Override
	public void stop() {
		if (proxy != null) {
			LOG.info("Stopping proxy");
			proxy.stop();
			proxy = null;
		}
	}
	
	private HttpFiltersSource getFiltersSource() {
		return new HttpFiltersSourceAdapter() {
			@Override
			public HttpFilters filterRequest(HttpRequest originalRequest) {
				
				return new HttpFiltersAdapter(originalRequest) {
					private final ProxyFilter filter = filterFactory != null ? filterFactory.getInstance() : null;
					private EmbeddedChannel bufferChannel = null;
					
					@Override
	                public HttpResponse clientToProxyRequest(HttpObject httpObject) {
						if (filter != null && httpObject instanceof DefaultHttpRequest) {
							filter.processRequest(new LittleProxyRequest((DefaultHttpRequest)httpObject));
						}
						return null;
	                }
					
					@Override
					public HttpObject proxyToClientResponse(HttpObject httpObject) {
						if (httpObject instanceof DefaultHttpResponse) {
							DefaultHttpResponse response = (DefaultHttpResponse)httpObject;
							Map<String,String> headers = new HashMap<>();
							response.headers().forEach(e -> {
								headers.put(e.getKey(), e.getValue());
							});
							
							if (selector != null && selector.test(new ProxyTransactionInfo(originalRequest.getUri(), response.getStatus().code(), headers))) {
						        bufferChannel = new EmbeddedChannel(
						        		new HttpResponseDecoder(), 
						        		new HttpContentDecompressor(), 
						        		new HttpObjectAggregator(maxBufferSize));
						        
						        bufferChannel.writeInbound(response);
							}
						} else if (httpObject instanceof DefaultHttpContent && bufferChannel != null) {
							DefaultHttpContent httpContent = (DefaultHttpContent)httpObject;
							//TODO: Is there a way to do this without the copy?
							bufferChannel.writeInbound(httpContent.copy());
						} else if (httpObject instanceof LastHttpContent && bufferChannel != null) {
							if (ProxyUtils.isLastChunk(httpObject)) {
								LastHttpContent httpContent = (LastHttpContent)httpObject;
								//TODO: Is there a way to do this without the copy?
								bufferChannel.writeInbound(httpContent.copy());
								bufferChannel.finish();
								filter.processResponse(new LittleProxyResponse((FullHttpResponse)bufferChannel.readInbound()));
								bufferChannel.close();
								bufferChannel = null;
							}
						} else if (filter != null && httpObject instanceof FullHttpResponse) {
							filter.processResponse(new LittleProxyResponse((FullHttpResponse)httpObject));
						}
						return httpObject;
					};					
				};
			};			
		};
	}
}
