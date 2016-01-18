package com.difference.historybook.index.lucene;

import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.DualFloatFunction;

public class DurationValueSource extends DualFloatFunction {
	
	public DurationValueSource(long ms1, ValueSource v2){
		super(new LongConstValueSource(ms1), v2);
	}

	@Override
	protected String name() {
		return "duration";
	}

	@Override
	protected float func(int doc, FunctionValues aVals, FunctionValues bVals) {
		long aVal = aVals.longVal(doc);
		long bVal = bVals.longVal(doc);
		long result = aVal - bVal;
		return result;
	}
}