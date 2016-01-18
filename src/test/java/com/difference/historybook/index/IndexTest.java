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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

public abstract class IndexTest {
	
	public abstract Index getIndex() throws IndexException;

	@Test
	public void testIndexAndSearch() throws Exception {
		try (Index index = getIndex()){
			String collection = "test";
			
			String url1 = "http://www.difference.com";
			Instant timestamp1 = Instant.now();
			String body1 = "<html><head><title>Testing 1-2-3</title></head><body>This is a test of the emergency broadcast system.</body></html>";
			index.indexPage(collection, url1, timestamp1, body1);
			
			String url2 = "http://does.not.exist.com";
			Instant timestamp2 = Instant.now();
			String body2 = "<html><head><title>A great adventure...</title></head><body>A long time ago in a galaxy far, far away...</body></html>";
			index.indexPage(collection, url2, timestamp2, body2);
			
			SearchResultWrapper wrapper = index.search(collection, "broadcast", 0, 10);
			assertEquals(1, wrapper.getResults().size());
			
			assertEquals("broadcast", wrapper.getQuery());
			assertEquals(0, wrapper.getOffset());
			assertEquals(10, wrapper.getMaxResultsRequested());
			assertEquals(1, wrapper.getResultCount());
			
			SearchResult result = wrapper.getResults().get(0);
			assertEquals(collection, result.getCollection());
			assertEquals(url1, result.getUrl());
			assertEquals(timestamp1.toString(), result.getTimestamp());
			assertTrue(result.getScore() > 0);
			assertNotNull(result.getKey());
			assertEquals("difference.com", result.getDomain());
			assertEquals("Testing 1-2-3", result.getTitle());		
		} catch (IndexException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testNoResults() throws Exception {
		try (Index index = getIndex()){
			SearchResultWrapper wrapper = index.search("test", "broadcast", 0, 10);
			assertEquals(0, wrapper.getResults().size());
			assertEquals(0, wrapper.getResultCount());
		} catch (IndexException e) {
			fail(e.getLocalizedMessage());
		}
	}

	@Test
	public void testCollectionFiltering() throws Exception {
		try (Index index = getIndex()){
			Instant timestamp = Instant.now();

			String url1 = "http://www.difference.com";
			String collection1 = "collection1";
			String body1 = "Testing Doc1";
			index.indexPage(collection1, url1, timestamp, body1);
			
			String url2 = "http://does.not.exist.com";
			String collection2 = "collection2";
			String body2 = "Testing Doc2";
			index.indexPage(collection2, url2, timestamp, body2);
			
			SearchResultWrapper wrapper = index.search(collection1, "testing", 0, 10);
			assertEquals(1, wrapper.getResults().size());
			
			SearchResult result = wrapper.getResults().get(0);
			assertEquals(collection1, result.getCollection());
			assertEquals(url1, result.getUrl());
			assertTrue(result.getScore() > 0);
			
			wrapper = index.search(collection2, "testing", 0, 10);
			assertEquals(1, wrapper.getResults().size());
			
			result = wrapper.getResults().get(0);
			assertEquals(collection2, result.getCollection());
			assertEquals(url2, result.getUrl());
			assertTrue(result.getScore() > 0);
		} catch (IndexException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testDebugInfo() throws Exception {
		try (Index index = getIndex()){
			Instant timestamp = Instant.now();

			String url = "http://www.difference.com";
			String collection = "collection1";
			String body = "Testing Doc1";
			index.indexPage(collection, url, timestamp, body);
			
			SearchResultWrapper wrapper = index.search(collection, "testing", 0, 10);
			assertEquals(1, wrapper.getResults().size());
			assertTrue(wrapper.getDebugInfo() == null);
			
			SearchResult result = wrapper.getResults().get(0);
			assertTrue(result.getDebugInfo() == null);

			wrapper = index.search(collection, "testing", 0, 10, true);
			assertEquals(1, wrapper.getResults().size());
			assertNotNull(wrapper.getDebugInfo());

			result = wrapper.getResults().get(0);
			assertNotNull(result.getDebugInfo());
		} catch (IndexException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testResultGrouping() throws Exception {
		try (Index index = getIndex()){
			Instant timestamp = Instant.now();

			String url = "http://does.not.exist.com";
			String collection = "collection";
			
			String body1 = "Testing Doc1";
			index.indexPage(collection, url, timestamp, body1);
			
			String body2 = "Testing Doc2";
			index.indexPage(collection, url, timestamp, body2);
			
			SearchResultWrapper wrapper = index.search(collection, "testing", 0, 10);
			assertEquals(1, wrapper.getResults().size());
			
			SearchResult result = wrapper.getResults().get(0);
			assertEquals(collection, result.getCollection());
			assertEquals(url, result.getUrl());			
		} catch (IndexException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testAgeBoosting() throws Exception {
		try (Index index = getIndex()){
			String collection = "collection";
			String body = "Testing";

			String url1 = "http://current.com";
			Instant timestamp1 = Instant.now();
			index.indexPage(collection, url1, timestamp1, body);
			
			String url2 = "http://yearold.com";
			Instant timestamp2 = timestamp1.minus(365, ChronoUnit.DAYS);
			index.indexPage(collection, url2, timestamp2, body);
			
			String url3 = "http://twoyearsold.com";
			Instant timestamp3 = timestamp2.minus(365, ChronoUnit.DAYS);
			index.indexPage(collection, url3, timestamp3, body);

			SearchResultWrapper wrapper = index.search(collection, "testing", 0, 10);
			assertEquals(3, wrapper.getResults().size());
			
			SearchResult result1 = wrapper.getResults().get(0);
			SearchResult result2 = wrapper.getResults().get(1);
			SearchResult result3 = wrapper.getResults().get(2);
			
			assertEquals(url1, result1.getUrl());	
			assertEquals(url2, result2.getUrl());
			assertEquals(url3, result3.getUrl());
			
			assertTrue(result2.getScore() < result1.getScore());
			assertTrue(result3.getScore() < result2.getScore());
			
			float n2 = result2.getScore() * 1/(result1.getScore());
			float n3 = result3.getScore() * 1/(result1.getScore());
			
			assertTrue(n2 > .49F && n2 < .51F);
			assertTrue(n3 > .32F && n3 < .34F);
			
		} catch (IndexException e) {
			fail(e.getLocalizedMessage());
		}
	}
}
