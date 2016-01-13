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

package com.difference.historybook.server;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

/**
 * Configuration settings for application
 */
public class HistoryBookConfiguration extends Configuration {
	@NotEmpty
	private String dataDirectory = System.getProperty("user.home") + "/Library/Application Support/HistoryBook";
	private String defaultCollection = "default";
	private int maxBufferSize = 1 * 1024 * 1024;
	private int proxyPort = 8082;
	
	/**
	 * @return Directory index will be created within
	 */
	@JsonProperty
	public String getDataDirectory() {
		return dataDirectory;
	}
	
	/**
	 * @param directory Directory to create index within
	 */
	@JsonProperty
	public void setDataDirectory(String directory) {
		this.dataDirectory = directory;
	}

	/**
	 * @return collection namespace to use when none has been specified
	 */
	@JsonProperty
	public String getDefaultCollection() {
		return defaultCollection;
	}

	/**
	 * @param defaultCollection collection namespace to use when none has been specified
	 */
	@JsonProperty
	public void setDefaultCollection(String defaultCollection) {
		this.defaultCollection = defaultCollection;
	}

	/**
	 * @return maximum amount of content to buffer for a page for indexing
	 */
	@JsonProperty
	public int getMaxBufferSize() {
		return maxBufferSize;
	}

	/**
	 * @param maxBufferSize maximum amount of content to buffer for a page for indexing
	 */
	@JsonProperty
	public void setMaxBufferSize(int maxBufferSize) {
		this.maxBufferSize = maxBufferSize;
	}

	/**
	 * @return port to run proxy service on
	 */
	@JsonProperty
	public int getProxyPort() {
		return proxyPort;
	}

	/**
	 * @param proxyPort port to run proxy service on
	 */
	@JsonProperty
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}
		
}
