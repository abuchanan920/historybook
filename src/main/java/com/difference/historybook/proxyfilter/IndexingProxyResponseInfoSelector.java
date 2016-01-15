package com.difference.historybook.proxyfilter;

import java.util.function.Predicate;

import com.difference.historybook.proxy.ProxyTransactionInfo;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

public class IndexingProxyResponseInfoSelector implements Predicate<ProxyTransactionInfo> {

	@Override
	public boolean test(ProxyTransactionInfo transactionInfo) {
		if (transactionInfo.getStatus() != 200) return false;
		
		try {
			String hostname = URL.parse(transactionInfo.getUrl()).host().toString();
			if (hostname.equalsIgnoreCase("localhost") || hostname.equals("127.0.0.1")) return false;
		} catch (GalimatiasParseException e) {
			return false;
		}

		String contentType = transactionInfo.getHeaders().get("Content-Type");
		return contentType != null && contentType.startsWith("text/html");
	}

}
