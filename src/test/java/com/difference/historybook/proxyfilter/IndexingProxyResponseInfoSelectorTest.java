package com.difference.historybook.proxyfilter;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.difference.historybook.proxy.ProxyTransactionInfo;

public class IndexingProxyResponseInfoSelectorTest {

	@Test
	public void testStatus() {
		Map<String,String> headers = new HashMap<>();
		headers.put("Content-Type", "text/html");

		IndexingProxyResponseInfoSelector selector = new IndexingProxyResponseInfoSelector();
		ProxyTransactionInfo info1 = new ProxyTransactionInfo("http://does.not.exist", 200, headers);
		assertTrue(selector.test(info1));

		ProxyTransactionInfo info2 = new ProxyTransactionInfo("http://does.not.exist", 500, headers);
		assertFalse(selector.test(info2));
	}
	
	@Test
	public void testContentType() {
		IndexingProxyResponseInfoSelector selector = new IndexingProxyResponseInfoSelector();
		Map<String,String> headers = new HashMap<>();
		headers.put("Content-Type", "text/html; charset=utf-8");
		ProxyTransactionInfo info1 = new ProxyTransactionInfo("http://does.not.exist", 200, headers);
		assertTrue(selector.test(info1));

		headers.put("Content-Type", "image/jpg");
		ProxyTransactionInfo info2 = new ProxyTransactionInfo("http://does.not.exist", 200, headers);
		assertFalse(selector.test(info2));
	}
	
	@Test
	public void testHostname() {
		IndexingProxyResponseInfoSelector selector = new IndexingProxyResponseInfoSelector();
		Map<String,String> headers = new HashMap<>();
		headers.put("Content-Type", "text/html; charset=utf-8");
		ProxyTransactionInfo info1 = new ProxyTransactionInfo("http://does.not.exist", 200, headers);
		assertTrue(selector.test(info1));

		ProxyTransactionInfo info2 = new ProxyTransactionInfo("http://localhost", 200, headers);
		assertFalse(selector.test(info2));
	}

}
