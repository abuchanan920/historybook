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

/**
 * Abstract interface to a filter that can view proxy requests and responses
 */
public interface ProxyFilter {
	/**
	 * Perform arbitrary processing based on a given @ProxyRequest
	 * @param request 
	 */
	public void processRequest(ProxyRequest request);

	/**
	 * Perform arbitrary processing based on a given @ProxyResponse
	 * @param response 
	 */
	public void processResponse(ProxyResponse response);
}
