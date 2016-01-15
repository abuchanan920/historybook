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

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.difference.historybook.index.Index;
import com.difference.historybook.index.IndexException;
import com.difference.historybook.proxy.ProxyFilter;
import com.difference.historybook.proxy.ProxyRequest;
import com.difference.historybook.proxy.ProxyResponse;
import com.difference.historybook.proxy.ProxyTransactionInfo;
import com.google.common.base.Charsets;

/**
 * An implementation of @ProxyFilter that indexes web pages on the fly
 */
public class IndexingProxyFilter implements ProxyFilter {
	private static final Logger LOG = LoggerFactory.getLogger(IndexingProxyFilter.class);

	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private final Index index;
	private final String defaultCollection;
	
	private String url;
	
	/**
	 * Constructor for IndexingProxyFilter
	 * 
	 * @param index The @Index to submit the indexing request to
	 * @param defaultCollection The collection namespace to use for indexing requests
	 */
	public IndexingProxyFilter(Index index, String defaultCollection) {
		this.index = index;
		this.defaultCollection = defaultCollection;
	}
	
	@Override
	public void processRequest(ProxyRequest request) {
		this.url = request.getUri();
	}

	@Override
	public void processResponse(ProxyResponse response) {
		if (new IndexingProxyResponseInfoSelector().test(new ProxyTransactionInfo(url, response.getStatus(), response.getHeaders()))) {
			String content = response.getContentAsString(Charsets.UTF_8); //TODO: Need to use actual charset...
			executor.submit(() -> {
				try {
					LOG.info("INDEXING {}", url);
					index.indexPage(defaultCollection, url, Instant.now(), content);
				} catch (IndexException e) {
					LOG.error(e.getLocalizedMessage());
				}
			});
		}
	}

}
