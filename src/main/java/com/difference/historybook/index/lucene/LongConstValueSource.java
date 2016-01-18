package com.difference.historybook.index.lucene;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.docvalues.LongDocValues;
import org.apache.lucene.queries.function.valuesource.ConstNumberSource;

class LongConstValueSource extends ConstNumberSource {
	final long constant;
	final double dv;
	final float fv;

	public LongConstValueSource(long constant) {
		this.constant = constant;
		this.dv = constant;
		this.fv = constant;
	}

	@Override
	public String description() {
		return "const(" + constant + ")";
	}

	@Override
	public FunctionValues getValues(@SuppressWarnings("rawtypes") Map context, LeafReaderContext readerContext) throws IOException {
		return new LongDocValues(this) {
			@Override
			public float floatVal(int doc) {
				return fv;
			}

			@Override
			public int intVal(int doc) {
				return (int) constant;
			}

			@Override
			public long longVal(int doc) {
				return constant;
			}

			@Override
			public double doubleVal(int doc) {
				return dv;
			}

			@Override
			public String toString(int doc) {
				return description();
			}
		};
	}

	@Override
	public int hashCode() {
		return (int) constant + (int) (constant >>> 32);
	}

	@Override
	public boolean equals(Object o) {
		if (LongConstValueSource.class != o.getClass()) return false;
		LongConstValueSource other = (LongConstValueSource) o;
		return this.constant == other.constant;
	}

	@Override
	public int getInt() {
		return (int)constant;
	}

	@Override
	public long getLong() {
		return constant;
	}

	@Override
	public float getFloat() {
		return fv;
	}

	@Override
	public double getDouble() {
		return dv;
	}

	@Override
	public Number getNumber() {
		return constant;
	}

	@Override
	public boolean getBool() {
		return constant != 0;
	}
}
