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
	private List<SearchResult> results;

	public List<SearchResult> getResults() {
		return results;
	}

	public SearchResultWrapper setResults(List<SearchResult> results) {
		this.results = results;
		return this;
	}
	
}
