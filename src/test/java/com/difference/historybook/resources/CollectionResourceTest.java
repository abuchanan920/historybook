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

package com.difference.historybook.resources;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Test;

import com.difference.historybook.index.Index;
import com.difference.historybook.index.IndexException;
import com.difference.historybook.index.SearchResult;
import com.difference.historybook.index.SearchResultWrapper;

public class CollectionResourceTest {

	@Test
	public void testPostContentToCollection() throws IndexException {
		Index index = mock(Index.class);
		CollectionResource resource = new CollectionResource(index);
		
		String collection = "testCollection";
		String url = "http://does.not.exist";
		String body = "This is the body";
		Response response = resource.postContentToCollection(collection, url, body);
		assertEquals(202, response.getStatus());
		verify(index).indexPage(eq(collection), eq(url), any(), eq(body));
	}

	@Test
	public void postContentToCollectionWithTimestamp() throws IndexException {
		Index index = mock(Index.class);
		CollectionResource resource = new CollectionResource(index);
		
		String collection = "testCollection";
		String url = "http://does.not.exist";
		String body = "This is the body";
		String timestampString = "2015-12-20T02:57:06Z";
		Instant timestamp = Instant.parse(timestampString);
		Response response = resource.postContentToCollectionWithTimestamp(collection, url, timestampString, body);
		assertEquals(202, response.getStatus());
		verify(index).indexPage(collection, url, timestamp, body);
	}

	@Test
	public void getSearchResult() throws NumberFormatException, IndexException {
		String collection = "testCollection";
		String query = "testing";
		String offsetString = "5";
		String sizeString = "20";

		Index index = mock(Index.class);
		List<SearchResult> results = new LinkedList<>();
		results.add(new SearchResult("key1", "testCollection", "title1", "http://does.not.exist/1", "not.exist", "timestamp1", "snippet1", null, 1.0f));
		results.add(new SearchResult("key2", "testCollection", "title2", "http://does.not.exist/2", "not.exist", "timestamp2", "snippet2", null, 0.7f));
		SearchResultWrapper wrapper = new SearchResultWrapper().setResults(results);
		when(index.search(collection, query, Integer.parseInt(offsetString), Integer.parseInt(sizeString), false)).thenReturn(wrapper);
		
		CollectionResource resource = new CollectionResource(index);
		
		Response response = resource.getSearchResult(collection, query, offsetString, sizeString, false);
		assertEquals(200, response.getStatus());
		verify(index).search(collection, query, Integer.parseInt(offsetString), Integer.parseInt(sizeString), false);
		assertEquals(wrapper, response.getEntity());
	}
}
