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
package org.neo4j.springframework.data.integration;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

import lombok.Builder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.springframework.data.core.convert.Neo4jConversions;
import org.neo4j.springframework.data.test.Neo4jExtension;
import org.neo4j.springframework.data.test.Neo4jExtension.Neo4jConnectionSupport;
import org.neo4j.springframework.data.types.CartesianPoint2d;
import org.neo4j.springframework.data.types.CartesianPoint3d;
import org.neo4j.springframework.data.types.GeographicPoint2d;
import org.neo4j.springframework.data.types.GeographicPoint3d;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.convert.ConverterBuilder;
import org.springframework.data.geo.Point;

/**
 * @author Michael J. Simons
 */
@ExtendWith(Neo4jExtension.class)
class Neo4jConversionsIt {

	public static final TypeDescriptor TYPE_DESCRIPTOR_OF_VALUE = TypeDescriptor.valueOf(Value.class);
	private static Neo4jConnectionSupport neo4jConnectionSupport;
	private static final DefaultConversionService DEFAULT_CONVERSION_SERVICE = new DefaultConversionService();

	@Builder
	static class ParamHolder {
		String name;
		double latitude;
		double longitude;

		Map<String, Object> toParameterMap() {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("name", this.name);
			parameters.put("latitude", this.latitude);
			parameters.put("longitude", this.longitude);
			return parameters;
		}

		Point asSpringPoint() {
			return new Point(latitude, longitude);
		}

		GeographicPoint2d asGeo2d() {
			return new GeographicPoint2d(latitude, longitude);
		}

		GeographicPoint3d asGeo3d(double height) {
			return new GeographicPoint3d(latitude, longitude, height);
		}
	}

	private static final ParamHolder NEO_HQ = ParamHolder.builder().latitude(55.612191).longitude(12.994823)
		.name("Neo4j HQ").build();
	private static final ParamHolder CLARION = ParamHolder.builder().latitude(55.607726).longitude(12.994243)
		.name("Clarion").build();
	private static final ParamHolder MINC = ParamHolder.builder().latitude(55.611496).longitude(12.994039).name("Minc")
		.build();

	@BeforeAll
	static void prepareDefaultConversionService() {
		new Neo4jConversions().registerConvertersIn(DEFAULT_CONVERSION_SERVICE);
	}

	@BeforeAll
	static void prepareData() {

		try (Session session = neo4jConnectionSupport.getDriver().session()) {
			session.writeTransaction(w -> {
				Map<String, Object> parameters;

				w.run("MATCH (n) detach delete n");

				parameters = new HashMap<>();
				parameters.put("aByteArray", "A thing".getBytes());
				w.run("CREATE (n:CypherTypes) SET "
					+ " n.aBoolean = true,"
					+ " n.aLong = 9223372036854775807,"
					+ " n.aDouble = 1.7976931348,"
					+ " n.aString = 'Hallo, Cypher',"
					+ " n.aByteArray = $aByteArray,"
					+ " n.aLocalDate = date('2015-07-21'),"
					+ " n.anOffsetTime  = time({ hour:12, minute:31, timezone: '+01:00' }),"
					+ " n.aLocalTime = localtime({ hour:12, minute:31, second:14 }),"
					+ " n.aZoneDateTime = datetime('2015-07-21T21:40:32-04[America/New_York]'),"
					+ " n.aLocalDateTime = localdatetime('2015202T21'),"
					+ " n.anIsoDuration = duration('P14DT16H12M'),"
					+ " n.aPoint = point({x:47, y:11})"
					+ " RETURN n", parameters);

				parameters = new HashMap<>();
				parameters.put("aByte", Values.value(new byte[] { 6 }));
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
					+ " n.shortArray = [-10, 10],"
					+ " n.aBigDecimal = '1.79769313486231570E+309',"
					+ " n.aBigInteger = '92233720368547758070',"
					+ " n.aPeriod = duration('P23Y4M7D'),"
					+ " n.aDuration = duration('PT26H4M5S')"
					+ " RETURN n", parameters);

				parameters = new HashMap<>();
				parameters.put("neo4j", NEO_HQ.toParameterMap());
				parameters.put("minc", MINC.toParameterMap());
				parameters.put("clarion", CLARION.toParameterMap());
				parameters.put("aByte", Values.value(new byte[] { 6 }));
				w.run("CREATE (n:SpatialTypes) SET "
					+ " n.sdnPoint = point({latitude: $neo4j.latitude, longitude: $neo4j.longitude}),"
					+ " n.geo2d = point({latitude: $minc.latitude, longitude: $minc.longitude}),"
					+ " n.geo3d = point({latitude: $clarion.latitude, longitude: $clarion.longitude, height: 27}),"
					+ " n.car2d = point({x: 10, y: 20}),"
					+ " n.car3d = point({x: 30, y: 40, z: 50})"
					+ " RETURN n", parameters);
				w.success();
				return null;
			});
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class CustomConversions {

		private final DefaultConversionService customConversionService = new DefaultConversionService();

		@BeforeAll
		void prepareDefaultConversionService() {
			ConverterBuilder.ConverterAware converterAware = ConverterBuilder
				.reading(Value.class, LocalDate.class, v -> {
					String s = v.asString();
					switch (s) {
						case "gestern":
							return LocalDate.now().minusDays(1);
						case "heute":
							return LocalDate.now();
						case "morgen":
							return LocalDate.now().plusDays(1);
						default:
							throw new IllegalArgumentException();
					}
				}).andWriting(d -> {
					if (d.isBefore(LocalDate.now())) {
						return Values.value("gestern");
					} else if (d.isAfter(LocalDate.now())) {
						return Values.value("morgen");
					} else {
						return Values.value("heute");
					}
				});
			new Neo4jConversions(converterAware.getConverters()).registerConvertersIn(customConversionService);
		}

		@ParameterizedTest
		@MethodSource("parameters")
		void read(String value, LocalDate expected) {

			assertThat(customConversionService.convert(Values.value(value), LocalDate.class)).isEqualTo(expected);
		}

		@ParameterizedTest
		@MethodSource("parameters")
		void write(String expected, LocalDate value) {

			assertThat(customConversionService.convert(value, TYPE_DESCRIPTOR_OF_VALUE))
				.isEqualTo(Values.value(expected));
		}

		Stream<Arguments> parameters() {
			return Stream.of(
				arguments("gestern", LocalDate.now().minusDays(1)),
				arguments("heute", LocalDate.now()),
				arguments("morgen", LocalDate.now().plusDays(1))
			);
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class CypherTypes {

		@Test
		void primitivesShouldBeRead() {
			boolean b = DEFAULT_CONVERSION_SERVICE.convert(Values.value(true), boolean.class);
			assertThat(b).isEqualTo(true);

			long l = DEFAULT_CONVERSION_SERVICE.convert(Values.value(Long.MAX_VALUE), long.class);
			assertThat(l).isEqualTo(Long.MAX_VALUE);

			double d = DEFAULT_CONVERSION_SERVICE.convert(Values.value(1.7976931348), double.class);
			assertThat(d).isEqualTo(1.7976931348);
		}

		Stream<Arguments> cypherTypes() {
			return Stream.of(
				arguments("aBoolean", true),
				arguments("aLong", Long.MAX_VALUE),
				arguments("aDouble", 1.7976931348),
				arguments("aString", "Hallo, Cypher"),
				arguments("aByteArray", "A thing".getBytes()),
				arguments("aPoint", Values.point(7203, 47, 11).asPoint()),
				arguments("aLocalDate", LocalDate.of(2015, 7, 21)),
				arguments("anOffsetTime", OffsetTime.of(12, 31, 0, 0, ZoneOffset.ofHours(1))),
				arguments("aLocalTime", LocalTime.of(12, 31, 14)),
				arguments("aZoneDateTime", ZonedDateTime
					.of(2015, 7, 21, 21, 40, 32, 0, TimeZone.getTimeZone("America/New_York").toZoneId())),
				arguments("aLocalDateTime", LocalDateTime.of(2015, 7, 21, 21, 0)),
				arguments("anIsoDuration", Values.isoDuration(0, 14, 58320, 0).asObject())
			);
		}

		@ParameterizedTest
		@MethodSource("cypherTypes")
		void read(String name, Object t) {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Value v = session.run("MATCH (n:CypherTypes) RETURN n." + name + " as r").single().get("r");

				Object converted = DEFAULT_CONVERSION_SERVICE.convert(v, t.getClass());
				assertThat(converted).isEqualTo(t);
			}
		}

		@ParameterizedTest
		@MethodSource("cypherTypes")
		void write(String name, Object t) {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put("v", DEFAULT_CONVERSION_SERVICE.convert(t, TYPE_DESCRIPTOR_OF_VALUE));

				long cnt = session
					.run("MATCH (n:CypherTypes) WHERE n." + name + " = $v RETURN COUNT(n) AS cnt",
						parameters)
					.single().get("cnt").asLong();
				assertThat(cnt).isEqualTo(1L);
			}
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class AdditionalTypes {
		@Test
		void primitivesShouldBeRead() {

			byte b = DEFAULT_CONVERSION_SERVICE.convert(Values.value(new byte[] { 6 }), byte.class);
			assertThat(b).isEqualTo((byte) 6);

			char c = DEFAULT_CONVERSION_SERVICE.convert(Values.value("x"), char.class);
			assertThat(c).isEqualTo('x');

			float f = DEFAULT_CONVERSION_SERVICE.convert(Values.value("23.42"), float.class);
			assertThat(f).isEqualTo(23.42F);

			int i = DEFAULT_CONVERSION_SERVICE.convert(Values.value(42), int.class);
			assertThat(i).isEqualTo(42);

			short s = DEFAULT_CONVERSION_SERVICE.convert(Values.value((short) 127), short.class);
			assertThat(s).isEqualTo((short) 127);
		}

		Stream<Arguments> additionalTypes() {
			return Stream.of(
				arguments("booleanArray", new boolean[] { true, true, false }),
				arguments("aByte", (byte) 6),
				arguments("aChar", 'x'),
				arguments("charArray", new char[] { 'x', 'y', 'z' }),
				arguments("aDate", Date.from(LocalDateTime.of(2019, 9, 21, 0, 0, 0).toInstant(ZoneOffset.UTC))),
				arguments("aBigDecimal", BigDecimal.valueOf(Double.MAX_VALUE).multiply(BigDecimal.TEN)),
				arguments("aBigInteger", BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN)),
				arguments("doubleArray", new double[] { 1.1, 2.2, 3.3 }),
				arguments("aFloat", 23.42F),
				arguments("floatArray", new float[] { 4.4F, 5.5F }),
				arguments("anInt", 42),
				arguments("intArray", new int[] { 21, 9 }),
				arguments("aLocale", Locale.GERMANY),
				arguments("longArray", new long[] { Long.MIN_VALUE, Long.MAX_VALUE }),
				arguments("aShort", (short) 127),
				arguments("shortArray", new short[] { -10, 10 }),
				arguments("aPeriod", Period.of(23, 4, 7)),
				arguments("aDuration", Duration.ofHours(25).plusMinutes(63).plusSeconds(65))
			);
		}

		@ParameterizedTest
		@MethodSource("additionalTypes")
		void read(String name, Object t) {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Value v = session.run("MATCH (n:AdditionalTypes) RETURN n." + name + " as r").single().get("r");

				Object converted = DEFAULT_CONVERSION_SERVICE.convert(v, t.getClass());
				assertThat(converted).isEqualTo(t);
			}
		}

		@ParameterizedTest
		@MethodSource("additionalTypes")
		void write(String name, Object t) {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put("v", DEFAULT_CONVERSION_SERVICE.convert(t, TYPE_DESCRIPTOR_OF_VALUE));

				long cnt = session
					.run("MATCH (n:AdditionalTypes) WHERE n." + name + " = $v RETURN COUNT(n) AS cnt",
						parameters)
					.single().get("cnt").asLong();
				assertThat(cnt).isEqualTo(1L);
			}
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class SpatialTypes {

		Stream<Arguments> spatialTypes() {
			return Stream.of(
				arguments("sdnPoint", NEO_HQ.asSpringPoint()),
				arguments("geo2d", MINC.asGeo2d()),
				arguments("car2d", new CartesianPoint2d(10, 20)),
				arguments("car3d", new CartesianPoint3d(30, 40, 50))
			);
		}

		@ParameterizedTest
		@MethodSource("spatialTypes")
		void read(String name, Object t) {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Value v = session.run("MATCH (n:SpatialTypes) RETURN n." + name + " as r").single().get("r");

				Object converted = DEFAULT_CONVERSION_SERVICE.convert(v, t.getClass());
				assertThat(converted).isEqualTo(t);
			}
		}

		@ParameterizedTest
		@MethodSource("spatialTypes")
		void write(String name, Object t) {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put("v", DEFAULT_CONVERSION_SERVICE.convert(t, TYPE_DESCRIPTOR_OF_VALUE));

				long cnt = session
					.run("MATCH (n:SpatialTypes) WHERE n." + name + " = $v RETURN COUNT(n) AS cnt",
						parameters)
					.single().get("cnt").asLong();
				assertThat(cnt).isEqualTo(1L);
			}
		}
	}
}
