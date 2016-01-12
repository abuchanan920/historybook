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
import java.util.Map.Entry;

import com.difference.historybook.proxy.ProxyResponse;

import io.netty.handler.codec.http.FullHttpResponse;

/**
 * An implementation of @ProxyResponse that wraps a Netty response 
 */
public class LittleProxyResponse implements ProxyResponse {
	private final FullHttpResponse response;
	private Map<String,String> headers = null;
	
	/**
	 * Constructor for LittleProxyResponse 
	 * @param httpObject the Netty response to extract from
	 */
	public LittleProxyResponse(FullHttpResponse httpObject) {
		this.response = httpObject;
	}

	@Override
	public int getStatus() {
		return response.getStatus().code();
	}

	@Override
	public Map<String, String> getHeaders() {
		if (headers == null) {
			headers = new HashMap<>();
			for (Entry<String,String> entry : response.headers()) {
				headers.put(entry.getKey(), entry.getValue());
			}
		}
		
		return headers;
	}

	@Override
	public byte[] getContent() {
		return response.content().array();
	}

}
