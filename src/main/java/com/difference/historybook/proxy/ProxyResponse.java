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

package com.difference.historybook.proxy;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * Abstract interface to extract required information from a given response implementation 
 */
public interface ProxyResponse {
	/**
	 * @return the numeric HTTP status code of the response
	 */
	public int getStatus();
	
	/**
	 * Note that this simplistic implementation ignores valid headers that may have multiple values
	 * as none are required for this app. This will undoubtably come back to bite me.
	 * 
	 * @return a @Map of header name/value string pairs.
	 */
	public Map<String,String> getHeaders();
	
	/**
	 * Note that this is a simplistic assumption that will perform badly (heap exhaustion)
	 * if called against a large response. This is somewhat mitigated by the fact that it
	 * is only called for text/html responses, but still should be reworked...
	 * 
	 * @return a raw byte array representation of the response entity.
	 */
	public byte[] getContent();
	
	/**
	 * @param charset The charset to use in converting the entity bytes to a String
	 * @return The entity in String format (i.e. textual)
	 */
	default String getContentAsString(Charset charset) {
		return new String(getContent(), charset);
	}
}
