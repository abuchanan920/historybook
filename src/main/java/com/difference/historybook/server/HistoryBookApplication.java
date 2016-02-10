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

import java.nio.file.Paths;
import java.util.function.Predicate;

import com.difference.historybook.index.Index;
import com.difference.historybook.index.lucene.LuceneIndex;
import com.difference.historybook.proxy.Proxy;
import com.difference.historybook.proxy.ProxyFilterFactory;
import com.difference.historybook.proxy.ProxyTransactionInfo;
import com.difference.historybook.proxy.littleproxy.LittleProxy;
import com.difference.historybook.proxyfilter.IndexingProxyFilterFactory;
import com.difference.historybook.proxyfilter.IndexingProxyResponseInfoSelector;
import com.difference.historybook.resources.CollectionResource;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Main application 
 */
public class HistoryBookApplication extends Application<HistoryBookConfiguration>{
	public static void main(String[] args) throws Exception {
        new HistoryBookApplication().run(args);
    }
	
	@Override
    public String getName() {
        return "historybook";
    }

    @Override
    public void initialize(Bootstrap<HistoryBookConfiguration> bootstrap) {
		bootstrap.addBundle(new AssetsBundle("/assets/", "/search", "index.html"));		
    }

	@Override
	public void run(HistoryBookConfiguration configuration, Environment environment) throws Exception {
		final Index index = new LuceneIndex(Paths.get(configuration.getDataDirectory()));
		final ProxyFilterFactory filterFactory = new IndexingProxyFilterFactory(index, configuration.getDefaultCollection());
		final Predicate<ProxyTransactionInfo> selector = new IndexingProxyResponseInfoSelector();
		final Proxy proxy = new LittleProxy()
				.setPort(configuration.getProxyPort())
				.setFilterFactory(filterFactory)
				.setResponseFilterSelector(selector)
				.setMaxBufferSize(configuration.getMaxBufferSize());
		
		final CollectionResource collectionResource = new CollectionResource(index); 
		environment.jersey().register(collectionResource);
		
		CertManager.initialize(
				Paths.get(configuration.getKeyStorePath()),
				configuration.getKeyStorePassword(),
				configuration.getCertAlias(),
				configuration.getHost(),
				"HistoryBook",
				configuration.getCertDuration());

		environment.lifecycle().manage(new ManagedProxy(proxy));
	}
	
}
