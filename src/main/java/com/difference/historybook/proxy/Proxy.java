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

import java.util.function.Predicate;

/**
 * An abstract interface to a web proxy service
 */
public interface Proxy {
	
	/**
	 * Start the web proxy service
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception;
	
	/**
	 * Stop the web proxy service
	 * 
	 * @throws Exception
	 */
	public void stop() throws Exception;
	
	/**
	 * Specify a @ProxyFilterFactory to use in creating @ProxyFilter instances for each proxy request/response
	 * @param factory the @ProxyFilterFactory to use in creating @ProxyFilter instances for each proxy request/response
	 * @return        this for method call chaining
	 */
	public Proxy setFilterFactory(ProxyFilterFactory factory);
	
	/**
	 * Specify a @Predicate to use to determine whether a @ProxyResponse will be required.
	 * This is done early in the streaming of a response by peeking at the headers to determine
	 * whether the content will need to be buffered and decompressed for use.
	 * 
	 * @param selector a @Predicate that, given a @ProxyResponseInfo, determines whether a response should be buffered, decompressed, and passed to the filter for processing.
	 * @return	this for method call chaining
	 */
	public Proxy setResponseFilterSelector(Predicate<ProxyResponseInfo> selector);
}
