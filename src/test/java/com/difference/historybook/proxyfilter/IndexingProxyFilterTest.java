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

package com.difference.historybook.proxyfilter;

import static org.mockito.Mockito.*;

import java.nio.charset.Charset;
import java.time.Instant;

import org.junit.Test;

import com.difference.historybook.index.IndexException;
import com.difference.historybook.index.lucene.LuceneIndex;
import com.difference.historybook.proxy.ProxyFilter;
import com.difference.historybook.proxy.ProxyRequest;
import com.difference.historybook.proxy.ProxyResponse;
import com.difference.historybook.proxyfilter.IndexingProxyFilter;
import com.google.common.collect.ImmutableMap;

public class IndexingProxyFilterTest {
	private static final String DEFAULT_COLLECTION = "test";
	
	private static class RequestResponse {
		private final ProxyRequest request;
		private final ProxyResponse response;
		
		public RequestResponse(String url, int status, String contentType, String content) {
			request = mock(ProxyRequest.class);
			when(request.getUri()).thenReturn(url);

			response = mock(ProxyResponse.class);
			when(response.getStatus()).thenReturn(status);
			when(response.getHeaders()).thenReturn(ImmutableMap.of("Content-Type", contentType));
			when(response.getContentAsString(any(Charset.class))).thenReturn(content);			
		}
		
		public ProxyRequest getRequest() {
			return request;
		}
		
		public ProxyResponse getResponse() {
			return response;
		}
	}

	@Test
	public void testProxyFilterPassed() throws IndexException {
		String url = "http://does.not.exist.com";
		String content = "This is the content";
		
		RequestResponse reqRes = new RequestResponse(url, 200, "text/html", content);
		LuceneIndex index = processRequestResponse(reqRes, DEFAULT_COLLECTION);
		
		verify(index).indexPage(eq(DEFAULT_COLLECTION), eq(url), any(Instant.class), eq(content));
	}

	@Test
	public void testContentTypeFailed() throws IndexException {
		String url = "http://does.not.exist.com";
		
		RequestResponse reqRes = new RequestResponse(url, 200, "image/jpg", null);
		LuceneIndex index = processRequestResponse(reqRes, DEFAULT_COLLECTION);

		verifyZeroInteractions(index);
	}

	@Test
	public void testStatusFailed() throws IndexException {
		String url = "http://does.not.exist.com";
		String content = "This is the content";
		
		RequestResponse reqRes = new RequestResponse(url, 201, "text/html", content);
		LuceneIndex index = processRequestResponse(reqRes, DEFAULT_COLLECTION);
		
		verifyZeroInteractions(index);
	}
	
	private LuceneIndex processRequestResponse(RequestResponse reqRes, String defaultCollection) {
		LuceneIndex index = mock(LuceneIndex.class);
		
		ProxyFilter filter = new IndexingProxyFilter(index, defaultCollection);
		filter.processRequest(reqRes.getRequest());
		filter.processResponse(reqRes.getResponse());
		
		return index;
		
	}
}
