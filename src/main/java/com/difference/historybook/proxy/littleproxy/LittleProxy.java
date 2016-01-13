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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.difference.historybook.proxy.Proxy;
import com.difference.historybook.proxy.ProxyFilter;
import com.difference.historybook.proxy.ProxyFilterFactory;
import com.difference.historybook.proxy.ProxyResponseInfo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
import io.netty.util.CharsetUtil;

/**
 * An implementation of @Proxy using LittleProxy which is based on a Netty core 
 */
public class LittleProxy implements Proxy {
	private static final Logger LOG = LoggerFactory.getLogger(LittleProxy.class);

	private ProxyFilterFactory filterFactory;
	private Predicate<ProxyResponseInfo> selector;
	
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
	public LittleProxy setResponseFilterSelector(Predicate<ProxyResponseInfo> selector) {
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
							
							if (selector != null && selector.test(new ProxyResponseInfo(response.getStatus().code(), headers))) {
						        bufferChannel = new EmbeddedChannel(
						        		new HttpResponseDecoder(), 
						        		new HttpContentDecompressor(), 
						        		new HttpObjectAggregator(maxBufferSize));
						        
						        StringBuffer headerBuffer = new StringBuffer();
						        String[] headerLines = response.toString().split("\\n");
						        for (int i = 1; i < headerLines.length; i++) {
						        	headerBuffer.append(headerLines[i] + "\r\n");
						        }
						        headerBuffer.append("\r\n");
						        String parsedHeader = headerBuffer.toString();
						        
						        ByteBuf buf = Unpooled.copiedBuffer(parsedHeader.getBytes(CharsetUtil.US_ASCII));
						        bufferChannel.writeInbound(buf);
							}
						} else if (httpObject instanceof DefaultHttpContent) {
							if (bufferChannel != null) {
								DefaultHttpContent httpContent = (DefaultHttpContent)httpObject;
								bufferChannel.writeInbound(Unpooled.wrappedBuffer(httpContent.content()).retain());
							}
						} else if (filter != null && httpObject instanceof FullHttpResponse) {
							filter.processResponse(new LittleProxyResponse((FullHttpResponse)httpObject));
						}
						return httpObject;
					};
					
					@Override
				    public void serverToProxyResponseReceived() {
						if (bufferChannel != null) {
							filter.processResponse(new LittleProxyResponse((FullHttpResponse)bufferChannel.readInbound()));
							bufferChannel.close();
							bufferChannel = null;
						}
				    }					
				};
			};			
		};
	}
}
