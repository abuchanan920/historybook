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

package com.difference.historybook.index.lucene;

import java.time.Instant;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

/**
 * Provides a semantic interface to a Lucene document 
 */
public class IndexDocumentAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(IndexDocumentAdapter.class);

	public static final String FIELD_SEARCH = "body";
	private static final String FIELD_COLLECTION = "collection";
	private static final String FIELD_URL = "url";
	private static final String FIELD_DOMAIN = "domain";
	private static final String FIELD_TIMESTAMP = "timestamp";
	private static final String FIELD_TIMESTAMP_TEXT = "timestampText";
	private static final String FIELD_TITLE = "title";
	private static final String FIELD_KEY = "key";

	private final Document doc;
	
	/**
	 * Constructor for IndexDocumentAdapter that creates a backing Lucene doc that is initially empty
	 */
	public IndexDocumentAdapter() {
		this.doc = new Document();
	}

	/**
	 * Constructor for IndexDocumentAdapter that uses a provided Lucene doc
	 * @param doc A Lucene doc to use as the backing store for this instance
	 */
	public IndexDocumentAdapter(Document doc) {
		this.doc = doc;
	}

	/**
	 * @param collection the case-sensitive name of a collection to use in namespacing the index
	 * @return this for request chaining
	 */
	public IndexDocumentAdapter setCollection(String collection) {
		doc.add(new StringField(FIELD_COLLECTION, collection, Field.Store.YES));
		return this;
	}

	/**
	 * @return the case-sensitive name of the collection this backing document is in
	 */
	public String getCollection() {
		return doc.get(FIELD_COLLECTION);
	}
	
	/**
	 * @param url the complete URL for the page being indexed in this document
	 * @return this for method chaining
	 */
	public IndexDocumentAdapter setUrl(String url) {
		doc.add(new StringField(FIELD_URL, url, Field.Store.YES));
		setDomainField(url);
		return this;
	}
	
	/**
	 * @return the URL for the page indexed in this document
	 */
	public String getUrl() {
		return doc.get(FIELD_URL);
	}
	
	private void setDomainField(String url) {
		try {
			URL u = URL.parse(url);
			String hostString = u.host().toHumanString();
			String domain;
			if (!"localhost".equalsIgnoreCase(hostString) && !InetAddresses.isInetAddress(hostString)) {
				domain = InternetDomainName.from(hostString).topPrivateDomain().toString();
			} else {
				domain = hostString;
			}
			doc.add(new StringField(FIELD_DOMAIN, domain, Field.Store.YES));
		} catch (GalimatiasParseException e1) {
			LOG.error("Unable to parse url {}", url);
		}
	}

	/**
	 * @return the top level domain extracted from the page url. Useful for grouping/filtering results.
	 */
	public String getDomain() {
		return doc.get(FIELD_DOMAIN);
	}
	
	/**
	 * @param timestamp the timestamp for when the page was fetched from the source
	 * @return this for method chaining
	 */
	public IndexDocumentAdapter setTimestamp(Instant timestamp) {
		doc.add(new LongField(FIELD_TIMESTAMP, timestamp.getEpochSecond(), Field.Store.NO));		
		doc.add(new StoredField(FIELD_TIMESTAMP_TEXT, timestamp.toString()));
		return this;
	}
	
	/**
	 * @return a textual representation of the timestamp for when the page was fetched in ISO-8601 format
	 */
	public String getTimestampText() {
		return doc.get(FIELD_TIMESTAMP_TEXT);
	}

	/**
	 * @param title The title of the page
	 * @return this for method chaining
	 */
	public IndexDocumentAdapter setTitle(String title) {
		doc.add(new TextField(FIELD_TITLE, title, Field.Store.YES));
		return this;
	}
	
	/**
	 * @return The title of the page
	 */
	public String getTitle() {
		return doc.get(FIELD_TITLE);
	}

	/**
	 * @param content The textual content of the page
	 * @return this for method chaining
	 */
	public IndexDocumentAdapter setContent(String content) {
		doc.add(new TextField(FIELD_SEARCH, content, Field.Store.YES));

		String hash = Hashing.sha1().newHasher().putString(content, Charsets.UTF_8).hash().toString();
		doc.add(new StringField(FIELD_KEY, hash, Field.Store.YES));

		return this;
	}
	
	/**
	 * @return A unique key for the page content (a hash of the content in fact)
	 */
	public String getKey() {
		return doc.get(FIELD_KEY);
	}

	/**
	 * @return the underlying Lucene document
	 */
	public Document getAsDocument() {
		return doc;
	}
}
