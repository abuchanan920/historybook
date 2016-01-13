package com.difference.historybook.proxyfilter;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.difference.historybook.proxy.ProxyResponseInfo;

public class IndexingProxyResponseInfoSelectorTest {

	@Test
	public void testStatus() {
		Map<String,String> headers = new HashMap<>();
		headers.put("Content-Type", "text/html");

		IndexingProxyResponseInfoSelector selector = new IndexingProxyResponseInfoSelector();
		ProxyResponseInfo info1 = new ProxyResponseInfo(200, headers);
		assertTrue(selector.test(info1));

		ProxyResponseInfo info2 = new ProxyResponseInfo(500, headers);
		assertFalse(selector.test(info2));
	}
	
	@Test
	public void testContentType() {
		IndexingProxyResponseInfoSelector selector = new IndexingProxyResponseInfoSelector();
		Map<String,String> headers = new HashMap<>();
		headers.put("Content-Type", "text/html; charset=utf-8");
		ProxyResponseInfo info1 = new ProxyResponseInfo(200, headers);
		assertTrue(selector.test(info1));

		headers.put("Content-Type", "image/jpg");
		ProxyResponseInfo info2 = new ProxyResponseInfo(200, headers);
		assertFalse(selector.test(info2));
	}

}
