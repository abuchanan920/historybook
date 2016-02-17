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
	private long certDuration = 365*24*3600L;

	// These seem to need to take effect in the constructor. 
	// They are ignored if configured later.
	private int apiPort = Integer.parseInt(System.getProperty("dw.apiPort", "8443"));
	private int adminPort = Integer.parseInt(System.getProperty("dw.adminPort", "8444"));
	private String host = System.getProperty("dw.host", "127.0.0.1");
	private String keyStorePath = System.getProperty("dw.keyStorePath", "historybook.jks");
	private String keyStorePassword = System.getProperty("dw.keyStorePassword", ")A[Yb;:ci_p@f_r+$%{7`6XSWf^,V-");
	private String certAlias = System.getProperty("dw.certAlias", "historybook");
	
	public HistoryBookConfiguration() {
		super();
		
		//overrides
		System.setProperty("dw.server.applicationConnectors[0].bindHost", host);
		System.setProperty("dw.server.applicationConnectors[0].type", "https");		
		System.setProperty("dw.server.applicationConnectors[0].port", new Integer(apiPort).toString());
		System.setProperty("dw.server.applicationConnectors[0].keyStorePath", keyStorePath);		
		System.setProperty("dw.server.applicationConnectors[0].keyStorePassword", keyStorePassword);		
		System.setProperty("dw.server.applicationConnectors[0].certAlias", certAlias);
		System.setProperty("dw.server.applicationConnectors[0].validateCerts", "false");		
		
		System.setProperty("dw.server.adminConnectors[0].bindHost", host);		
		System.setProperty("dw.server.adminConnectors[0].type", "https");		
		System.setProperty("dw.server.adminConnectors[0].port", new Integer(adminPort).toString());		
		System.setProperty("dw.server.adminConnectors[0].keyStorePath", keyStorePath);		
		System.setProperty("dw.server.adminConnectors[0].keyStorePassword", keyStorePassword);		
		System.setProperty("dw.server.adminConnectors[0].certAlias", certAlias);		
		System.setProperty("dw.server.adminConnectors[0].validateCerts", "false");		
	}
	
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
	 * @return port to run api service on
	 */
	@JsonProperty
	public int getApiPort() {
		return apiPort;
	}

	/**
	 * NOTE: This setting only works as a system parameter, not in a config file.
	 * 
	 * @param apiPort port to run api service on
	 */
	@JsonProperty
	public void setApiPort(int apiPort) {
		this.apiPort = apiPort;
	}

	/**
	 * @return port to run admin service on
	 */
	@JsonProperty
	public int getAdminPort() {
		return adminPort;
	}

	/**
	 * NOTE: This setting only works as a system parameter, not in a config file.
	 * 
	 * @param adminPort port to run admin service on
	 */
	@JsonProperty
	public void setAdminPort(int adminPort) {
		this.adminPort = adminPort;
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

	/**
	 * @return host to use for self-signed certificate
	 */
	@JsonProperty
	public String getHost() {
		return host;
	}

	/**
	 * NOTE: This setting only works as a system parameter, not in a config file.
	 * 
	 * @param host host to use for self-signed certificate
	 */
	@JsonProperty
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return path for self-signed certificate keystore
	 */
	@JsonProperty
	public String getKeyStorePath() {
		return keyStorePath;
	}

	/**
	 * NOTE: This setting only works as a system parameter, not in a config file.
	 * 
	 * @param keyStorePath to use for self-signed certificate
	 */
	@JsonProperty
	public void setKeyStorePath(String keyStorePath) {
		this.keyStorePath = keyStorePath;
	}

	/**
	 * @return password to use for self-signed certificate keystore
	 */
	@JsonProperty
	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	/**
	 * NOTE: This setting only works as a system parameter, not in a config file.
	 * 
	 * @param keyStorePassword password to use for self-signed certificate keystore
	 */
	@JsonProperty
	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	/**
	 * @return name to use for self-signed certificate
	 */
	@JsonProperty
	public String getCertAlias() {
		return certAlias;
	}

	/**
	 * NOTE: This setting only works as a system parameter, not in a config file.
	 * 
	 * @param certAlias name to use for self-signed certificate
	 */
	@JsonProperty
	public void setCertAlias(String certAlias) {
		this.certAlias = certAlias;
	}

	/**
	 * @return valid duration to use for self-signed certificate
	 */
	public long getCertDuration() {
		return certDuration;
	}

	/**
	 * @param certDuration valid duration to use for self-signed certificate
	 */
	public void setCertDuration(long certDuration) {
		this.certDuration = certDuration;
	}

}
