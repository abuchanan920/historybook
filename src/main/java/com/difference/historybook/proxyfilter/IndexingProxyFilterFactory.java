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

package com.difference.historybook.proxyfilter;

import com.difference.historybook.index.Index;
import com.difference.historybook.proxy.ProxyFilter;
import com.difference.historybook.proxy.ProxyFilterFactory;

/**
 * Implementation of @ProxyFilterFactory that returns IndexingProxyFilters 
 */
public class IndexingProxyFilterFactory implements ProxyFilterFactory {
	private final Index index;
	private final String defaultCollection;
	
	/**
	 * Constructor for IndexingProxyFilterFactory
	 * 
	 * @param index the @Index to submit the indexing request to
	 * @param defaultCollection The collection namespace to use for indexing requests
	 */
	public IndexingProxyFilterFactory(Index index, String defaultCollection) {
		this.index = index;
		this.defaultCollection = defaultCollection;
	}
	
	@Override
	public ProxyFilter getInstance() {
		return new IndexingProxyFilter(index, defaultCollection);
	}
}
