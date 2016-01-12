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

import static org.junit.Assert.*;

import org.junit.Test;

public class HtmlTextExtractorTest {
	private static final String TEST_HTML = "<html><head><title>This is the title</title></head><body>This is the body</body></html>";

	@Test
	public void testGetTitle() {
		HtmlTextExtractor extractor = new HtmlTextExtractor(TEST_HTML, "http://does.not.exist.com");
		assertEquals("This is the title", extractor.getTitle());
	}

	@Test
	public void testGetContent() {
		HtmlTextExtractor extractor = new HtmlTextExtractor(TEST_HTML, "http://does.not.exist.com");
		assertEquals("This is the title This is the body", extractor.getContent());
	}
}
