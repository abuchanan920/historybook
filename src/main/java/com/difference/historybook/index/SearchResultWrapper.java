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

import java.util.List;

/**
 * A container representing a set of set of search results (a "page") 
 * along with relevant search result metadata.
 */
public class SearchResultWrapper {
	private String query;
	private int offset;
	private int maxResultsRequested;
	private int resultCount;
	private String debugInfo;
	private List<SearchResult> results;

	public String getQuery() {
		return query;
	}

	public SearchResultWrapper setQuery(String query) {
		this.query = query;
		return this;
	}

	public int getOffset() {
		return offset;
	}

	public SearchResultWrapper setOffset(int offset) {
		this.offset = offset;
		return this;
	}

	public int getMaxResultsRequested() {
		return maxResultsRequested;
	}

	public SearchResultWrapper setMaxResultsRequested(int maxResultsRequested) {
		this.maxResultsRequested = maxResultsRequested;
		return this;
	}

	public int getResultCount() {
		return resultCount;
	}

	public SearchResultWrapper setResultCount(int resultCount) {
		this.resultCount = resultCount;
		return this;
	}

	public List<SearchResult> getResults() {
		return results;
	}

	public SearchResultWrapper setResults(List<SearchResult> results) {
		this.results = results;
		return this;
	}

	public String getDebugInfo() {
		return debugInfo;
	}

	public SearchResultWrapper setDebugInfo(String debugInfo) {
		this.debugInfo = debugInfo;
		return this;
	}
	
}
