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

package com.difference.historybook.proxy.littleproxy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.Test;

import com.google.common.base.Charsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

public class LittleProxyResponseTest {

	@Test
	public void testGetStatus() {
		FullHttpResponse response = mock(FullHttpResponse.class);
		when(response.getStatus()).thenReturn(HttpResponseStatus.ACCEPTED);
		
		LittleProxyResponse lpr = new LittleProxyResponse(response);
		assertEquals(HttpResponseStatus.ACCEPTED.code(), lpr.getStatus());
	}

	@Test
	public void testGetHeaders() {
		DefaultHttpHeaders headers = new DefaultHttpHeaders();
		headers.add("header1", "value1");
		headers.add("header2", "value2");
		
		FullHttpResponse response = mock(FullHttpResponse.class);
		when(response.headers()).thenReturn(headers);
		
		LittleProxyResponse lpr = new LittleProxyResponse(response);
		Map<String,String> responseHeaders = lpr.getHeaders();
		assertEquals(2, responseHeaders.size());
		assertEquals("value1", responseHeaders.get("header1"));
		assertEquals("value2", responseHeaders.get("header2"));
	}

	@Test
	public void testGetContent() {
		String msg = "This is a test";
		testGetContent(msg, UnpooledByteBufAllocator.DEFAULT.heapBuffer(msg.getBytes(Charsets.UTF_8).length));
	}
	
	@Test
	public void testGetContentWithNoBackingArray() {
		testGetContent("This is a test", PooledByteBufAllocator.DEFAULT.compositeBuffer());
	}
	
	private void testGetContent(String msg, ByteBuf buffer) {
		FullHttpResponse response = mock(FullHttpResponse.class);
		byte[] bytes = msg.getBytes(Charsets.UTF_8);
		buffer.writeBytes(bytes, 0, bytes.length);
		when(response.content()).thenReturn(buffer);
		
		LittleProxyResponse lpr = new LittleProxyResponse(response);
		assertArrayEquals(bytes, lpr.getContent());
	}
}
