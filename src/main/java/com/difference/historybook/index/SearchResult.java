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

/**
 * A container for the data related to a single search result 
 *
 */
public class SearchResult {
	private final String key;
	private final String collection;
	private final String title;
	private final String url;
	private final String domain;
	private final String timestamp;
	private final String snippet;
	private final String debugInfo;
	private final float score;
	
	public SearchResult(
			String key,
			String collection,
			String title,
			String url,
			String domain,
			String timestamp,
			String snippet,
			String debugInfo,
			float score) {
		this.key = key;
		this.collection = collection;
		this.title = title;
		this.url = url;
		this.domain = domain;
		this.timestamp = timestamp;
		this.snippet = snippet;
		this.debugInfo = debugInfo;
		this.score = score;
	}
	
	public String getKey() {
		return key;
	}

	public String getCollection() {
		return collection;
	}
	
	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}
	
	public String getDomain() {
		return domain;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getSnippet() {
		return snippet;
	}
	
	public String getDebugInfo() {
		return debugInfo;
	}
	
	public float getScore() {
		return score;
	}
	
}
