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

import java.util.function.Predicate;

import com.difference.historybook.proxy.Proxy;
import com.difference.historybook.proxy.ProxyFilterFactory;
import com.difference.historybook.proxy.ProxyResponseInfo;

import io.dropwizard.lifecycle.Managed;

/**
 * Wrapper to make @Proxy implement @Managed
 * 
 *  Would not need this if Java had something like duck-typing, but such is life.
 *  Did not want Proxy to be dependent on the particular web service framework used.
 */
public class ManagedProxy implements Proxy, Managed {
	private final Proxy proxy;
	
	public ManagedProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	@Override
	public void start() throws Exception {
		proxy.start();
	}

	@Override
	public void stop() throws Exception {
		proxy.stop();
	}

	@Override
	public Proxy setFilterFactory(ProxyFilterFactory factory) {
		proxy.setFilterFactory(factory);
		return this;
	}

	@Override
	public Proxy setResponseFilterSelector(Predicate<ProxyResponseInfo> selector) {
		proxy.setResponseFilterSelector(selector);
		return this;
	}

}
