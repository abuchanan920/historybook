package com.difference.historybook.index.lucene;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

public class LongConstValueSourceTest {
	@Test
	public void test() {
		long l = new Date().getTime();
		LongConstValueSource vs = new LongConstValueSource(l);
		
		assertTrue(l == vs.getLong());
		assertTrue((float)l == vs.getFloat());
		assertTrue((double)l == vs.getDouble());
		assertTrue((int)l == vs.getInt());
	}
}
