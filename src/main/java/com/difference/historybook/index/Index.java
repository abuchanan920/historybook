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

package com.difference.historybook.index;

import java.time.Instant;

/**
 * An abstraction of a searchable index of web pages
 * 
 */
public interface Index extends AutoCloseable {
	/**
	 * Adds a given page to an index collection
	 * 
	 * @param collection      a namespace for an index. Allows storing multiple indexes within the same backing store.
	 * @param url             the url of the page being indexed
	 * @param timestamp       the timestamp representing when the page was retrieved
	 * @param body            the textual content of the page (binaries are unsupported)
	 * @throws IndexException
	 */
	public void indexPage(String collection, String url, Instant timestamp, String body) throws IndexException;
	
	/**
	 * Executes a given query against the index in the specified collection namespace and returns up to the requested page size of results.
	 * 
	 * @param collection        a namespace for an index. Allows storing multiple indexes within the same backing store.
	 * @param query             the search query to execute. The supported syntax of this is determined by the underlying @Index implementation
	 * @param offset            the 0-based offset to begin retrieving results from. Specifying an offset greater than the actual number of results will result in an empty result.
	 * @param size              the maximum number of results to return. The actual number of results returned may be fewer.
	 * @return                  the search results for the specified query along with relevant metadata.
	 * @see SearchResultWrapper
	 * @throws IndexException
	 */
	default public SearchResultWrapper search(String collection, String query, int offset, int size) throws IndexException {
		return search(collection, query, offset, size, false);
	}

	/**
	 * Executes a given query against the index in the specified collection namespace and returns up to the requested page size of results.
	 * 
	 * @param collection        a namespace for an index. Allows storing multiple indexes within the same backing store.
	 * @param query             the search query to execute. The supported syntax of this is determined by the underlying @Index implementation
	 * @param offset            the 0-based offset to begin retrieving results from. Specifying an offset greater than the actual number of results will result in an empty result.
	 * @param size              the maximum number of results to return. The actual number of results returned may be fewer.
	 * @param includeDebug		include implementation dependent debug information for search result
	 * @return                  the search results for the specified query along with relevant metadata.
	 * @see SearchResultWrapper
	 * @throws IndexException
	 */
	public SearchResultWrapper search(String collection, String query, int offset, int size, boolean includeDebug) throws IndexException;
}
