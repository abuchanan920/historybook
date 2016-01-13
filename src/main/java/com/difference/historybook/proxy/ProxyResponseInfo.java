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

import java.util.Map;

/**
 * A container for the header information for a response
 */
public class ProxyResponseInfo {
	private final int status;
	private final Map<String,String> headers;
	
	public ProxyResponseInfo(int status, Map<String,String> headers) {
		this.status = status;
		this.headers = headers;
	}

	/**
	 * @return the numeric HTTP response code for the response
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @return a @Map of the HTTP headers associated with the response
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}

}
