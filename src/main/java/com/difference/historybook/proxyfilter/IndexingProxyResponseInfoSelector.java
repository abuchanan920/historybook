package com.difference.historybook.proxyfilter;

import java.util.function.Predicate;

import com.difference.historybook.proxy.ProxyResponseInfo;

public class IndexingProxyResponseInfoSelector implements Predicate<ProxyResponseInfo> {

	@Override
	public boolean test(ProxyResponseInfo responseInfo) {
		if (responseInfo.getStatus() != 200) return false;

		String contentType = responseInfo.getHeaders().get("Content-Type");
		return contentType != null && contentType.startsWith("text/html");
	}

}
