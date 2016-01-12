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
 * Abstract interface to a class that provides ProxyFilters of a particular kind
 */
public interface ProxyFilterFactory {
	/**
	 * Return a new @ProxyFilter for use with a new request/response pair.
	 * @return @ProxyFilter Note that this should be a new object with each call unless the given proxy filter is stateless.
	 */
	public ProxyFilter getInstance();
}
