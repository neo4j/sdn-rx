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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.exceptions.value.LossyCoercion;
import org.neo4j.driver.types.IsoDuration;
import org.neo4j.driver.types.Point;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.util.Assert;

/**
 * Built-in converters for Neo4j.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
abstract class Neo4jConverters {

	/**
	 * Converters for all the supported {@link org.neo4j.springframework.data.core.mapping.Neo4jSimpleTypes simple types}.
	 *
	 * @return the converters to be registered.
	 */
	static Collection<Object> getConvertersToRegister() {

		List<Object> converters = new ArrayList<>();

		converters.add(reading(Value.class, Boolean.class, Value::asBoolean).andWriting(Values::value));
		converters.add(reading(Value.class, Byte.class, Neo4jConverters::asByte).andWriting(Neo4jConverters::value));
		converters.add(
			reading(Value.class, Character.class, Neo4jConverters::asCharacter).andWriting(Neo4jConverters::value));
		converters.add(
			reading(Value.class, Date.class, Neo4jConverters::asDate).andWriting(Neo4jConverters::value));
		converters.add(reading(Value.class, Double.class, Value::asDouble).andWriting(Values::value));
		converters.add(new EnumConververt());
		converters.add(
			reading(Value.class, Float.class, Neo4jConverters::asFloat).andWriting(Neo4jConverters::value));
		converters.add(reading(Value.class, Integer.class, Value::asInt).andWriting(Values::value));
		converters.add(reading(Value.class, IsoDuration.class, Value::asIsoDuration).andWriting(Values::value));
		converters.add(reading(Value.class, LocalDate.class, Value::asLocalDate).andWriting(Values::value));
		converters.add(reading(Value.class, LocalDateTime.class, Value::asLocalDateTime).andWriting(Values::value));
		converters.add(reading(Value.class, LocalTime.class, Value::asLocalTime).andWriting(Values::value));
		converters.add(
			reading(Value.class, Locale.class, Neo4jConverters::asLocale).andWriting(Neo4jConverters::value));
		converters.add(reading(Value.class, Long.class, Value::asLong).andWriting(Values::value));
		converters.add(reading(Value.class, Map.class, Value::asMap).andWriting(Values::value));
		converters.add(reading(Value.class, OffsetTime.class, Value::asOffsetTime).andWriting(Values::value));
		converters.add(reading(Value.class, Point.class, Value::asPoint).andWriting(Values::value));
		converters.add(
			reading(Value.class, Short.class, Neo4jConverters::asShort).andWriting(Neo4jConverters::value));
		converters.add(reading(Value.class, String.class, Value::asString).andWriting(Values::value));
		converters.add(reading(Value.class, Void.class, v -> null).andWriting(v -> Values.NULL));
		converters.add(reading(Value.class, ZonedDateTime.class, Value::asZonedDateTime).andWriting(Values::value));
		converters.add(reading(Value.class, boolean.class, Value::asBoolean).andWriting(Values::value));
		converters
			.add(reading(Value.class, boolean[].class, Neo4jConverters::asBooleanArray).andWriting(Values::value));
		converters.add(reading(Value.class, byte.class, Neo4jConverters::asByte).andWriting(Neo4jConverters::value));
		converters.add(reading(Value.class, byte[].class, Value::asByteArray).andWriting(Values::value));
		converters.add(
			reading(Value.class, char.class, Neo4jConverters::asCharacter).andWriting(Neo4jConverters::value));
		converters.add(
			reading(Value.class, char[].class, Neo4jConverters::asCharArray).andWriting(Neo4jConverters::value));
		converters.add(reading(Value.class, double.class, Value::asDouble).andWriting(Values::value));
		converters.add(reading(Value.class, double[].class, Neo4jConverters::asDoubleArray).andWriting(Values::value));
		converters.add(
			reading(Value.class, float.class, Neo4jConverters::asFloat).andWriting(Neo4jConverters::value));
		converters.add(
			reading(Value.class, float[].class, Neo4jConverters::asFloatArray).andWriting(Neo4jConverters::value));
		converters.add(reading(Value.class, int.class, Value::asInt).andWriting(Values::value));
		converters.add(reading(Value.class, int[].class, Neo4jConverters::asIntArray).andWriting(Values::value));
		converters.add(reading(Value.class, long.class, Value::asLong).andWriting(Values::value));
		converters.add(reading(Value.class, long[].class, Neo4jConverters::asLongArray).andWriting(Values::value));
		converters.add(
			reading(Value.class, short.class, Neo4jConverters::asShort).andWriting(Neo4jConverters::value));
		converters.add(
			reading(Value.class, short[].class, Neo4jConverters::asShortArray).andWriting(Neo4jConverters::value));

		return converters;
	}

	private static Byte asByte(Value value) {
		byte[] bytes = value.asByteArray();
		Assert.isTrue(bytes.length == 1, "Expected a byte array with exactly 1 element.");
		return bytes[0];
	}

	private static Value value(Byte aByte) {
		if (aByte == null) {
			return Values.NULL;
		}

		return Values.value(aByte);
	}

	private static Character asCharacter(Value value) {
		char[] chars = value.asString().toCharArray();
		Assert.isTrue(chars.length == 1, "Expected a char array with exactly 1 element.");
		return chars[0];
	}

	private static Value value(Character aChar) {
		if (aChar == null) {
			return Values.NULL;
		}

		return Values.value(String.valueOf(aChar));
	}

	private static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

	private static Date asDate(Value value) {
		try {
			return new SimpleDateFormat(ISO_8601_DATE_FORMAT).parse(value.asString());
		} catch (ParseException e) {
			throw new IllegalArgumentException("Could not parse " + value.asString(), e);
		}
	}

	private static Value value(Date date) {
		if (date == null) {
			return Values.NULL;
		}

		return Values.value(new SimpleDateFormat(ISO_8601_DATE_FORMAT).format(date));
	}

	@ReadingConverter
	@WritingConverter
	static class EnumConververt implements GenericConverter {

		private final Set<ConvertiblePair> convertiblePairs;

		EnumConververt() {
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
				return ((Enum) source).name();
			}

		}
	}

	private static Float asFloat(Value value) {
		return Float.parseFloat(value.asString());
	}

	private static Value value(Float aFloat) {
		if (aFloat == null) {
			return Values.NULL;
		}

		return Values.value(aFloat.toString());
	}

	private static Locale asLocale(Value value) {
		return new Locale(value.asString());
	}

	private static Value value(Locale locale) {
		if (locale == null) {
			return Values.NULL;
		}

		return Values.value(locale.toString());
	}

	private static Short asShort(Value value) {
		long val = value.asLong();
		if (val > Short.MAX_VALUE || val < Short.MIN_VALUE) {
			throw new LossyCoercion(value.type().name(), "Java short");
		}
		return (short) val;
	}

	private static Value value(Short aShort) {
		if (aShort == null) {
			return Values.NULL;
		}

		return Values.value(aShort.longValue());
	}

	private static boolean[] asBooleanArray(Value value) {
		boolean[] array = new boolean[value.size()];
		int i = 0;
		for (Boolean v : value.values(Value::asBoolean)) {
			array[i++] = v;
		}
		return array;
	}

	private static char[] asCharArray(Value value) {
		char[] array = new char[value.size()];
		int i = 0;
		for (Character v : value.values(Neo4jConverters::asCharacter)) {
			array[i++] = v;
		}
		return array;
	}

	private static Value value(char[] aCharArray) {
		if (aCharArray == null) {
			return Values.NULL;
		}

		String[] values = new String[aCharArray.length];
		int i = 0;
		for (char v : aCharArray) {
			values[i++] = String.valueOf(v);
		}
		return Values.value(values);
	}

	private static double[] asDoubleArray(Value value) {
		double[] array = new double[value.size()];
		int i = 0;
		for (double v : value.values(Value::asDouble)) {
			array[i++] = v;
		}
		return array;
	}

	private static float[] asFloatArray(Value value) {
		float[] array = new float[value.size()];
		int i = 0;
		for (float v : value.values(Neo4jConverters::asFloat)) {
			array[i++] = v;
		}
		return array;
	}

	private static Value value(float[] aFloatArray) {
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

	private static int[] asIntArray(Value value) {
		int[] array = new int[value.size()];
		int i = 0;
		for (int v : value.values(Value::asInt)) {
			array[i++] = v;
		}
		return array;
	}

	private static long[] asLongArray(Value value) {
		long[] array = new long[value.size()];
		int i = 0;
		for (int v : value.values(Value::asInt)) {
			array[i++] = v;
		}
		return array;
	}

	private static short[] asShortArray(Value value) {
		short[] array = new short[value.size()];
		int i = 0;
		for (short v : value.values(Neo4jConverters::asShort)) {
			array[i++] = v;
		}
		return array;
	}

	private static Value value(short[] aShortArray) {
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

	private Neo4jConverters() {
	}
}
