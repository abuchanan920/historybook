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

package com.difference.historybook.textutils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Interface to HTML text extraction functions 
 */
public class HtmlTextExtractor {
	
	private final Document jsoup;
	
	/**
	 * Constructor for HtmlTextExtractor
	 * 
	 * @param body the textual representation of the page entity
	 * @param url the url of the page. Useful if needing to canonicalize embedded URLs
	 */
	public HtmlTextExtractor(String body, String url) {
		jsoup = Jsoup.parse(body, url);
	}
	
	/**
	 * @return The title of the web page (or empty string if not found)
	 */
	public String getTitle() {
		return jsoup.title();
	}
	
	/**
	 * @return The text extracted from the web page content (removing HTML)
	 */
	public String getContent() {
		return jsoup.text();
	}

}
