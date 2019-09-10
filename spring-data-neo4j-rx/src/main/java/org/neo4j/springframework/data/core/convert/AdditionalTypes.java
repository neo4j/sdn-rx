/*
 * Copyright (c) 2019 "Neo4j,"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.springframework.data.core.convert;

import static org.springframework.data.convert.ConverterBuilder.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.exceptions.value.LossyCoercion;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Additional types that are supported out of the box.
 * Mostly all of {@link org.springframework.data.mapping.model.SimpleTypeHolder SimpleTypeHolder's} defaults.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
final class AdditionalTypes {

	static final List<?> CONVERTERS;

	static {

		List<Object> hlp = new ArrayList<>();
		hlp.add(reading(Value.class, boolean[].class, AdditionalTypes::asBooleanArray).andWriting(Values::value));
		hlp.add(reading(Value.class, Byte.class, AdditionalTypes::asByte).andWriting(AdditionalTypes::value));
		hlp.add(reading(Value.class, byte.class, AdditionalTypes::asByte).andWriting(AdditionalTypes::value));
		hlp.add(reading(Value.class, Character.class, AdditionalTypes::asCharacter).andWriting(Values::value));
		hlp.add(reading(Value.class, char.class, AdditionalTypes::asCharacter).andWriting(Values::value));
		hlp.add(reading(Value.class, char[].class, AdditionalTypes::asCharArray).andWriting(Values::value));
		hlp.add(reading(Value.class, Date.class, AdditionalTypes::asDate).andWriting(AdditionalTypes::value));
		hlp.add(reading(Value.class, double[].class, AdditionalTypes::asDoubleArray).andWriting(Values::value));
		hlp.add(new EnumConverter());
		hlp.add(reading(Value.class, Float.class, AdditionalTypes::asFloat).andWriting(AdditionalTypes::value));
		hlp.add(reading(Value.class, float.class, AdditionalTypes::asFloat).andWriting(AdditionalTypes::value));
		hlp.add(reading(Value.class, float[].class, AdditionalTypes::asFloatArray).andWriting(AdditionalTypes::value));
		hlp.add(reading(Value.class, Integer.class, Value::asInt).andWriting(Values::value));
		hlp.add(reading(Value.class, int.class, Value::asInt).andWriting(Values::value));
		hlp.add(reading(Value.class, int[].class, AdditionalTypes::asIntArray).andWriting(Values::value));
		hlp.add(reading(Value.class, Locale.class, AdditionalTypes::asLocale).andWriting(AdditionalTypes::value));
		hlp.add(reading(Value.class, long[].class, AdditionalTypes::asLongArray).andWriting(Values::value));
		hlp.add(reading(Value.class, Short.class, AdditionalTypes::asShort).andWriting(AdditionalTypes::value));
		hlp.add(reading(Value.class, short.class, AdditionalTypes::asShort).andWriting(AdditionalTypes::value));
		hlp.add(reading(Value.class, short[].class, AdditionalTypes::asShortArray).andWriting(AdditionalTypes::value));
		hlp.add(reading(Value.class, String[].class, AdditionalTypes::asStringArray).andWriting(Values::value));
		hlp.add(
			reading(Value.class, BigDecimal.class, AdditionalTypes::asBigDecimal).andWriting(AdditionalTypes::value));
		hlp.add(
			reading(Value.class, BigInteger.class, AdditionalTypes::asBigInteger).andWriting(AdditionalTypes::value));
		hlp.add(
			reading(Value.class, TemporalAmount.class, AdditionalTypes::asTemporalAmount).andWriting(AdditionalTypes::value));

		CONVERTERS = Collections.unmodifiableList(hlp);
	}

	static TemporalAmount asTemporalAmount(Value value) {
		return new TemporalAmountAdapter().apply(value.asIsoDuration());
	}

	static Value value(TemporalAmount temporalAmount) {
		return Values.value(temporalAmount);
	}

	static BigDecimal asBigDecimal(Value value) {
		return new BigDecimal(value.asString());
	}

	static Value value(BigDecimal bigDecimal) {
		if (bigDecimal == null) {
			return Values.NULL;
		}

		return Values.value(bigDecimal.toString());
	}

	static BigInteger asBigInteger(Value value) {
		return new BigInteger(value.asString());
	}

	static Value value(BigInteger bigInteger) {
		if (bigInteger == null) {
			return Values.NULL;
		}

		return Values.value(bigInteger.toString());
	}

	static Byte asByte(Value value) {
		byte[] bytes = value.asByteArray();
		Assert.isTrue(bytes.length == 1, "Expected a byte array with exactly 1 element.");
		return bytes[0];
	}

	static Value value(Byte aByte) {
		if (aByte == null) {
			return Values.NULL;
		}

		return Values.value(new Byte[] { aByte });
	}

	static Character asCharacter(Value value) {
		char[] chars = value.asString().toCharArray();
		Assert.isTrue(chars.length == 1, "Expected a char array with exactly 1 element.");
		return chars[0];
	}

	private static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

	static Date asDate(Value value) {
		try {
			return new SimpleDateFormat(ISO_8601_DATE_FORMAT).parse(value.asString());
		} catch (ParseException e) {
			throw new IllegalArgumentException("Could not parse " + value.asString(), e);
		}
	}

	static Value value(Date date) {
		if (date == null) {
			return Values.NULL;
		}

		return Values.value(new SimpleDateFormat(ISO_8601_DATE_FORMAT).format(date));
	}

	@ReadingConverter
	@WritingConverter
	static class EnumConverter implements GenericConverter {

		private final Set<ConvertiblePair> convertiblePairs;

		EnumConverter() {
			Set<ConvertiblePair> hlp = new HashSet<>();
			hlp.add(new ConvertiblePair(Enum.class, Value.class));
			hlp.add(new ConvertiblePair(Value.class, Enum.class));
			convertiblePairs = Collections.unmodifiableSet(hlp);
		}

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return this.convertiblePairs;
		}

		@Override
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			Class<?> concreteTargetType = targetType.getType();
			if (source == null) {
				return concreteTargetType == Value.class ? Values.NULL : null;
			}

			if (sourceType.getType() == Value.class) {
				return Enum.valueOf((Class<Enum>) concreteTargetType, ((Value) source).asString());
			} else {
				return Values.value(((Enum) source).name());
			}
		}
	}

	static Float asFloat(Value value) {
		return Float.parseFloat(value.asString());
	}

	static Value value(Float aFloat) {
		if (aFloat == null) {
			return Values.NULL;
		}

		return Values.value(aFloat.toString());
	}

	static Locale asLocale(Value value) {

		return StringUtils.parseLocale(value.asString());
	}

	static Value value(Locale locale) {
		if (locale == null) {
			return Values.NULL;
		}

		return Values.value(locale.toString());
	}

	static Short asShort(Value value) {
		long val = value.asLong();
		if (val > Short.MAX_VALUE || val < Short.MIN_VALUE) {
			throw new LossyCoercion(value.type().name(), "Java short");
		}
		return (short) val;
	}

	static Value value(Short aShort) {
		if (aShort == null) {
			return Values.NULL;
		}

		return Values.value(aShort.longValue());
	}

	static boolean[] asBooleanArray(Value value) {
		boolean[] array = new boolean[value.size()];
		int i = 0;
		for (Boolean v : value.values(Value::asBoolean)) {
			array[i++] = v;
		}
		return array;
	}

	static char[] asCharArray(Value value) {
		char[] array = new char[value.size()];
		int i = 0;
		for (Character v : value.values(AdditionalTypes::asCharacter)) {
			array[i++] = v;
		}
		return array;
	}

	static String[] asStringArray(Value value) {
		String[] array = new String[value.size()];
		return value.asList(Value::asString).toArray(array);
	}

	static double[] asDoubleArray(Value value) {
		double[] array = new double[value.size()];
		int i = 0;
		for (double v : value.values(Value::asDouble)) {
			array[i++] = v;
		}
		return array;
	}

	static float[] asFloatArray(Value value) {
		float[] array = new float[value.size()];
		int i = 0;
		for (float v : value.values(AdditionalTypes::asFloat)) {
			array[i++] = v;
		}
		return array;
	}

	static Value value(float[] aFloatArray) {
		if (aFloatArray == null) {
			return Values.NULL;
		}

		String[] values = new String[aFloatArray.length];
		int i = 0;
		for (float v : aFloatArray) {
			values[i++] = Float.toString(v);
		}
		return Values.value(values);
	}

	static int[] asIntArray(Value value) {
		int[] array = new int[value.size()];
		int i = 0;
		for (int v : value.values(Value::asInt)) {
			array[i++] = v;
		}
		return array;
	}

	static long[] asLongArray(Value value) {
		long[] array = new long[value.size()];
		int i = 0;
		for (long v : value.values(Value::asLong)) {
			array[i++] = v;
		}
		return array;
	}

	static short[] asShortArray(Value value) {
		short[] array = new short[value.size()];
		int i = 0;
		for (short v : value.values(AdditionalTypes::asShort)) {
			array[i++] = v;
		}
		return array;
	}

	static Value value(short[] aShortArray) {
		if (aShortArray == null) {
			return Values.NULL;
		}

		long[] values = new long[aShortArray.length];
		int i = 0;
		for (short v : aShortArray) {
			values[i++] = v;
		}
		return Values.value(values);
	}

	private AdditionalTypes() {
	}
}
