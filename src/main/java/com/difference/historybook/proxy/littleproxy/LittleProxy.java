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

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * An implementation of @Proxy using LittleProxy which is based on a Netty core 
 */
public class LittleProxy implements Proxy {
	private static final Logger LOG = LoggerFactory.getLogger(LittleProxy.class);

	private ProxyFilterFactory filterFactory;
	
	private HttpProxyServer proxy = null;
	
	@Override
	public Proxy setFilterFactory(ProxyFilterFactory filterFactory) {
		this.filterFactory = filterFactory;
		return this;
	}
	
	@Override
	public void start() {
		if (proxy == null) {
			proxy = DefaultHttpProxyServer.bootstrap()
					.withPort(8082)
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
		//TODO: Is this limiting large file downloads?
		return new HttpFiltersSourceAdapter() {
			@Override
			public HttpFilters filterRequest(HttpRequest originalRequest) {
				
				return new HttpFiltersAdapter(originalRequest) {
					private final ProxyFilter filter = filterFactory != null ? filterFactory.getInstance() : null;
					
					@Override
	                 public HttpResponse clientToProxyRequest(HttpObject httpObject) {
						if (filter != null && httpObject instanceof DefaultHttpRequest) {
							filter.processRequest(new LittleProxyRequest((DefaultHttpRequest)httpObject));
						}
						return null;
	                 }
					
					@Override
					public HttpObject proxyToClientResponse(HttpObject httpObject) {							
						if (filter != null && httpObject instanceof FullHttpResponse) {
							filter.processResponse(new LittleProxyResponse((FullHttpResponse)httpObject));
						}						
						return httpObject;
					};
					
//				    @Override
//				    public void proxyToServerConnectionSucceeded(ChannelHandlerContext serverCtx) {
//				        ChannelPipeline pipeline = serverCtx.pipeline();
//				        if (pipeline.get("inflater") != null) {
//				            pipeline.remove("inflater");
//				        }
//				        if (pipeline.get("aggregator") != null) {
//				            pipeline.remove("aggregator");
//				        }
//				        super.proxyToServerConnectionSucceeded(serverCtx);
//				    }
				};
			};
			
			@Override
			public int getMaximumRequestBufferSizeInBytes() {
				return 0;
			};

			@Override
			public int getMaximumResponseBufferSizeInBytes() {
				return 1 * 1024 * 1024;
			};
		};
	}
}
