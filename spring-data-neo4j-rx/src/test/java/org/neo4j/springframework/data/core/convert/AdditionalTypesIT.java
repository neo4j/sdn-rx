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

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.springframework.data.test.Neo4jExtension;
import org.neo4j.springframework.data.test.Neo4jExtension.Neo4jConnectionSupport;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.convert.ConverterBuilder;

/**
 * @author Michael J. Simons
 */
@ExtendWith(Neo4jExtension.class)
class AdditionalTypesIT {

	public static final TypeDescriptor TYPE_DESCRIPTOR_OF_VALUE = TypeDescriptor.valueOf(Value.class);
	private static Neo4jConnectionSupport neo4jConnectionSupport;
	private static DefaultConversionService conversionService;

	@BeforeAll
	static void createConversionService() {

		conversionService = new DefaultConversionService();
		for (Object o : AdditionalTypes.CONVERTERS) {
			if (o instanceof Converter) {
				conversionService.addConverter((Converter) o);
			} else if (o instanceof GenericConverter) {
				conversionService.addConverter((GenericConverter) o);
			} else if (o instanceof ConverterBuilder.ConverterAware) {
				((ConverterBuilder.ConverterAware) o).getConverters().forEach(conversionService::addConverter);
			} else {
				throw new IllegalArgumentException(o.getClass().getName());
			}
		}

		try (Session session = neo4jConnectionSupport.getDriver().session()) {
			session.writeTransaction(w -> {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put("aByte", Values.value(new byte[] { 6 }));
				w.run("MATCH (n) detach delete n");
				w.run("CREATE (n:AdditionalTypes) SET "
					+ " n.booleanArray = [true, true, false],"
					+ " n.aByte = $aByte,"
					+ " n.aChar = 'x',"
					+ " n.charArray = ['x', 'y', 'z'],"
					+ " n.aDate = '2019-09-21T02:00:00.000+02:00',"
					+ " n.doubleArray = [1.1, 2.2, 3.3],"
					+ " n.aFloat = '23.42',"
					+ " n.floatArray = ['4.4', '5.5'],"
					+ " n.anInt = 42,"
					+ " n.intArray = [21, 9],"
					+ " n.aLocale = 'de_DE',"
					+ " n.longArray = [-9223372036854775808, 9223372036854775807],"
					+ " n.aShort = 127,"
					+ " n.shortArray = [-10, 10]"
					+ " RETURN n", parameters);
				w.success();
				return null;
			});
		}
	}

	@Nested
	class BooleanArray {

		@Test
		void read() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Value v = session.run("MATCH (n:AdditionalTypes) RETURN n.booleanArray as r").single().get("r");
				boolean[] r = (boolean[]) conversionService.convert(v, TypeDescriptor.valueOf(boolean[].class));
				assertThat(r).containsExactly(true, true, false);
			}
		}

		@Test
		void write() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {

				long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.booleanArray = $v RETURN COUNT(n) AS cnt",
					Collections.singletonMap("v", conversionService
						.convert(new boolean[] { true, true, false }, TYPE_DESCRIPTOR_OF_VALUE))).single()
					.get("cnt").asLong();
				assertThat(cnt).isEqualTo(1L);
			}
		}
	}

	@Nested
	class AByte {

		@Test
		void read() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Value v = session.run("MATCH (n:AdditionalTypes) RETURN n.aByte as r").single().get("r");

				byte r = (byte) conversionService.convert(v, TypeDescriptor.valueOf(byte.class));
				assertThat(r).isEqualTo((byte) 6);

				Byte rM = (Byte) conversionService.convert(v, TypeDescriptor.valueOf(Byte.class));
				assertThat(rM).isEqualTo(Byte.valueOf("6"));
			}
		}

		@Test
		void write() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put("v", conversionService.convert((byte) 6, TYPE_DESCRIPTOR_OF_VALUE));
				parameters.put("vM", conversionService.convert(Byte.valueOf("6"), TYPE_DESCRIPTOR_OF_VALUE));

				long cnt = session
					.run("MATCH (n:AdditionalTypes) WHERE n.aByte = $v and n.aByte = $vM RETURN COUNT(n) AS cnt",
						parameters)
					.single().get("cnt").asLong();
				assertThat(cnt).isEqualTo(1L);
			}
		}
	}

	@Nested
	class AChar {

		@Test
		void read() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Value v = session.run("MATCH (n:AdditionalTypes) RETURN n.aChar as r").single().get("r");

				char r = (char) conversionService.convert(v, TypeDescriptor.valueOf(char.class));
				assertThat(r).isEqualTo('x');

				Character rM = (Character) conversionService.convert(v, TypeDescriptor.valueOf(Character.class));
				assertThat(rM).isEqualTo(Character.valueOf('x'));
			}
		}

		@Test
		void write() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put("v", conversionService.convert('x', TYPE_DESCRIPTOR_OF_VALUE));
				parameters.put("vM", conversionService.convert(Character.valueOf('x'), TYPE_DESCRIPTOR_OF_VALUE));

				long cnt = session
					.run("MATCH (n:AdditionalTypes) WHERE n.aChar = $v and n.aChar = $vM RETURN COUNT(n) AS cnt",
						parameters)
					.single().get("cnt").asLong();
				assertThat(cnt).isEqualTo(1L);
			}
		}
	}

	@Nested
	class CharArray {

		@Test
		void read() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Value v = session.run("MATCH (n:AdditionalTypes) RETURN n.charArray as r").single().get("r");
				char[] r = (char[]) conversionService.convert(v, TypeDescriptor.valueOf(char[].class));
				assertThat(r).containsExactly('x', 'y', 'z');
			}
		}

		@Test
		void write() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {

				long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.charArray = $v RETURN COUNT(n) AS cnt",
					Collections.singletonMap("v", conversionService
						.convert(new char[] { 'x', 'y', 'z' }, TYPE_DESCRIPTOR_OF_VALUE))).single()
					.get("cnt").asLong();
				assertThat(cnt).isEqualTo(1L);
			}
		}
	}

	@Nested
	class ADate {

		@Test
		void read() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Value v = session.run("MATCH (n:AdditionalTypes) RETURN n.aDate as r").single().get("r");
				Date r = (Date) conversionService.convert(v, TypeDescriptor.valueOf(Date.class));
				assertThat(r).isEqualTo(Date.from(LocalDateTime.of(2019, 9, 21, 0, 0, 0).toInstant(ZoneOffset.UTC)));
			}
		}

		@Test
		void write() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {

				long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.aDate = $v RETURN COUNT(n) AS cnt",
					Collections.singletonMap("v", conversionService
						.convert(Date.from(LocalDateTime.of(2019, 9, 21, 0, 0, 0).toInstant(ZoneOffset.UTC)),
							TYPE_DESCRIPTOR_OF_VALUE))).single()
					.get("cnt").asLong();
				assertThat(cnt).isEqualTo(1L);
			}
		}
	}

	@Nested
	class DoubleArray {

		@Test
		void read() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Value v = session.run("MATCH (n:AdditionalTypes) RETURN n.doubleArray as r").single().get("r");
				double[] r = (double[]) conversionService.convert(v, TypeDescriptor.valueOf(double[].class));
				assertThat(r).containsExactly(1.1, 2.2, 3.3);
			}
		}

		@Test
		void write() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {

				long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.doubleArray = $v RETURN COUNT(n) AS cnt",
					Collections.singletonMap("v", conversionService
						.convert(new double[] { 1.1, 2.2, 3.3 }, TYPE_DESCRIPTOR_OF_VALUE))).single()
					.get("cnt").asLong();
				assertThat(cnt).isEqualTo(1L);
			}
		}
	}

	@Nested
	class AFloat {

		@Test
		void read() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Value v = session.run("MATCH (n:AdditionalTypes) RETURN n.aFloat as r").single().get("r");

				float r = (float) conversionService.convert(v, TypeDescriptor.valueOf(float.class));
				assertThat(r).isEqualTo(23.42F);

				Float rM = (Float) conversionService.convert(v, TypeDescriptor.valueOf(Float.class));
				assertThat(rM).isEqualTo(Float.valueOf(23.42F));
			}
		}

		@Test
		void write() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put("v", conversionService.convert(23.42F, TYPE_DESCRIPTOR_OF_VALUE));
				parameters.put("vM", conversionService.convert(Float.valueOf(23.42F), TYPE_DESCRIPTOR_OF_VALUE));

				long cnt = session
					.run("MATCH (n:AdditionalTypes) WHERE n.aFloat = $v and n.aFloat = $vM RETURN COUNT(n) AS cnt",
						parameters)
					.single().get("cnt").asLong();
				assertThat(cnt).isEqualTo(1L);
			}
		}
	}

	@Nested
	class FloatArray {

		@Test
		void read() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Value v = session.run("MATCH (n:AdditionalTypes) RETURN n.floatArray as r").single().get("r");
				float[] r = (float[]) conversionService.convert(v, TypeDescriptor.valueOf(float[].class));
				assertThat(r).containsExactly(4.4F, 5.5F);
			}
		}

		@Test
		void write() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {

				long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.floatArray = $v RETURN COUNT(n) AS cnt",
					Collections.singletonMap("v", conversionService
						.convert(new float[] { 4.4F, 5.5F }, TYPE_DESCRIPTOR_OF_VALUE))).single()
					.get("cnt").asLong();
				assertThat(cnt).isEqualTo(1L);
			}
		}
	}

	@Nested
	class AnInt {

		@Test
		void read() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Value v = session.run("MATCH (n:AdditionalTypes) RETURN n.anInt as r").single().get("r");

				int r = (int) conversionService.convert(v, TypeDescriptor.valueOf(int.class));
				assertThat(r).isEqualTo(42);

				Integer rM = (Integer) conversionService.convert(v, TypeDescriptor.valueOf(Integer.class));
				assertThat(rM).isEqualTo(Integer.valueOf(42));
			}
		}

		@Test
		void write() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put("v", conversionService.convert(42, TYPE_DESCRIPTOR_OF_VALUE));
				parameters.put("vM", conversionService.convert(Integer.valueOf(42), TYPE_DESCRIPTOR_OF_VALUE));

				long cnt = session
					.run("MATCH (n:AdditionalTypes) WHERE n.anInt = $v and n.anInt = $vM RETURN COUNT(n) AS cnt",
						parameters)
					.single().get("cnt").asLong();
				assertThat(cnt).isEqualTo(1L);
			}
		}
	}

	@Nested
	class IntArray {

		@Test
		void read() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Value v = session.run("MATCH (n:AdditionalTypes) RETURN n.intArray as r").single().get("r");
				int[] r = (int[]) conversionService.convert(v, TypeDescriptor.valueOf(int[].class));
				assertThat(r).containsExactly(21, 9);
			}
		}

		@Test
		void write() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {

				long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.intArray = $v RETURN COUNT(n) AS cnt",
					Collections.singletonMap("v", conversionService
						.convert(new int[] { 21, 9 }, TYPE_DESCRIPTOR_OF_VALUE))).single()
					.get("cnt").asLong();
				assertThat(cnt).isEqualTo(1L);
			}
		}
	}

	@Nested
	class ALocale {

		@Test
		void read() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Value v = session.run("MATCH (n:AdditionalTypes) RETURN n.aLocale as r").single().get("r");
				Locale r = (Locale) conversionService.convert(v, TypeDescriptor.valueOf(Locale.class));
				assertThat(r).isEqualTo(Locale.GERMANY);
			}
		}

		@Test
		void write() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {

				long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.aLocale = $v RETURN COUNT(n) AS cnt",
					Collections.singletonMap("v", conversionService
						.convert(Locale.GERMANY, TYPE_DESCRIPTOR_OF_VALUE))).single()
					.get("cnt").asLong();
				assertThat(cnt).isEqualTo(1L);
			}
		}
	}

	@Nested
	class LongArray {

		@Test
		void read() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Value v = session.run("MATCH (n:AdditionalTypes) RETURN n.longArray as r").single().get("r");
				long[] r = (long[]) conversionService.convert(v, TypeDescriptor.valueOf(long[].class));
				assertThat(r).containsExactly(Long.MIN_VALUE, Long.MAX_VALUE);
			}
		}

		@Test
		void write() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {

				long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.longArray = $v RETURN COUNT(n) AS cnt",
					Collections.singletonMap("v", conversionService
						.convert(new long[] { Long.MIN_VALUE, Long.MAX_VALUE }, TYPE_DESCRIPTOR_OF_VALUE))).single()
					.get("cnt").asLong();
				assertThat(cnt).isEqualTo(1L);
			}
		}
	}

	@Nested
	class AShort {

		@Test
		void read() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Value v = session.run("MATCH (n:AdditionalTypes) RETURN n.aShort as r").single().get("r");

				short r = (short) conversionService.convert(v, TypeDescriptor.valueOf(short.class));
				assertThat(r).isEqualTo((short) 127);

				Short rM = (Short) conversionService.convert(v, TypeDescriptor.valueOf(Short.class));
				assertThat(rM).isEqualTo(Short.valueOf((short)127));
			}
		}

		@Test
		void write() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put("v", conversionService.convert((short) 127, TYPE_DESCRIPTOR_OF_VALUE));
				parameters.put("vM", conversionService.convert(Short.valueOf((short)127), TYPE_DESCRIPTOR_OF_VALUE));

				long cnt = session
					.run("MATCH (n:AdditionalTypes) WHERE n.aShort = $v and n.aShort = $vM RETURN COUNT(n) AS cnt",
						parameters)
					.single().get("cnt").asLong();
				assertThat(cnt).isEqualTo(1L);
			}
		}
	}

	@Nested
	class ShortArray {

		@Test
		void read() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Value v = session.run("MATCH (n:AdditionalTypes) RETURN n.shortArray as r").single().get("r");
				short[] r = (short[]) conversionService.convert(v, TypeDescriptor.valueOf(short[].class));
				assertThat(r).containsExactly((short)-10, (short)10);
			}
		}

		@Test
		void write() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {

				long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.shortArray = $v RETURN COUNT(n) AS cnt",
					Collections.singletonMap("v", conversionService
						.convert(new short[] { -10, 10 }, TYPE_DESCRIPTOR_OF_VALUE))).single()
					.get("cnt").asLong();
				assertThat(cnt).isEqualTo(1L);
			}
		}
	}
}
