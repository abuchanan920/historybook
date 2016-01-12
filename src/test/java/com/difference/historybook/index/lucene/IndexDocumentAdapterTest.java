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

import static org.junit.Assert.*;

import java.time.Instant;

import org.apache.lucene.document.Document;
import org.junit.Test;

public class IndexDocumentAdapterTest {

	@Test
	public void testSetGetCollection() {
		IndexDocumentAdapter adapter = new IndexDocumentAdapter();
		String collection = "test";
		adapter.setCollection(collection);
		assertEquals(collection, adapter.getCollection());
	}
	
	@Test
	public void testSetGetUrl() {
		IndexDocumentAdapter adapter = new IndexDocumentAdapter();
		String url = "http://does.not.exist.com";
		adapter.setUrl(url);
		assertEquals(url, adapter.getUrl());
	}
	
	@Test
	public void testGetDomain() {
		IndexDocumentAdapter adapter = new IndexDocumentAdapter();
		String url = "http://does.not.exist.co.uk";
		adapter.setUrl(url);
		assertEquals("exist.co.uk", adapter.getDomain());
	}

	@Test
	public void testSetGetTimestamp() {
		IndexDocumentAdapter adapter = new IndexDocumentAdapter();
		Instant now = Instant.now();
		adapter.setTimestamp(now);
		assertEquals(now.toString(), adapter.getTimestampText());
	}

	@Test
	public void testSetContentGetKey() {
		String content1 = "content1";
		String content2 = "content2";
		
		IndexDocumentAdapter adapter1 = new IndexDocumentAdapter().setContent(content1);
		IndexDocumentAdapter adapter2 = new IndexDocumentAdapter().setContent(content2);
		IndexDocumentAdapter adapter3 = new IndexDocumentAdapter().setContent(content1);
		
		assertNotNull(adapter1.getKey());
		assertNotNull(adapter2.getKey());
		assertNotNull(adapter3.getKey());

		assertNotEquals(adapter1.getKey(), adapter2.getKey());
		assertEquals(adapter1.getKey(), adapter3.getKey());
	}
	
	@Test
	public void testGetAsDocument() {
		Document doc = new Document();
		IndexDocumentAdapter adapter = new IndexDocumentAdapter(doc);
		assertEquals(doc, adapter.getAsDocument());
	}
}
