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
import java.util.Collections;
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
	class CypherTypes {
		@Nested
		class ABoolean {

			@Test
			void read() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Value v = session.run("MATCH (n:CypherTypes) RETURN n.aBoolean as r").single().get("r");

					boolean r = DEFAULT_CONVERSION_SERVICE.convert(v, boolean.class);
					assertThat(r).isEqualTo(true);

					Boolean rM = DEFAULT_CONVERSION_SERVICE.convert(v, Boolean.class);
					assertThat(rM).isEqualTo(Boolean.TRUE);
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("v", DEFAULT_CONVERSION_SERVICE.convert(true, TYPE_DESCRIPTOR_OF_VALUE));
					parameters.put("vM", DEFAULT_CONVERSION_SERVICE.convert(Boolean.TRUE, TYPE_DESCRIPTOR_OF_VALUE));

					long cnt = session
						.run("MATCH (n:CypherTypes) WHERE n.aBoolean = $v and n.aBoolean = $vM RETURN COUNT(n) AS cnt",
							parameters)
						.single().get("cnt").asLong();
					assertThat(cnt).isEqualTo(1L);
				}
			}
		}

		@Nested
		class ALong {

			@Test
			void read() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Value v = session.run("MATCH (n:CypherTypes) RETURN n.aLong as r").single().get("r");

					long r = DEFAULT_CONVERSION_SERVICE.convert(v, long.class);
					assertThat(r).isEqualTo(Long.MAX_VALUE);

					Long rM = DEFAULT_CONVERSION_SERVICE.convert(v, Long.class);
					assertThat(rM).isEqualTo(Long.valueOf(Long.MAX_VALUE));
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("v", DEFAULT_CONVERSION_SERVICE.convert(Long.MAX_VALUE, TYPE_DESCRIPTOR_OF_VALUE));
					parameters
						.put("vM",
							DEFAULT_CONVERSION_SERVICE.convert(Long.valueOf(Long.MAX_VALUE), TYPE_DESCRIPTOR_OF_VALUE));

					long cnt = session
						.run("MATCH (n:CypherTypes) WHERE n.aLong = $v and n.aLong = $vM RETURN COUNT(n) AS cnt",
							parameters)
						.single().get("cnt").asLong();
					assertThat(cnt).isEqualTo(1L);
				}
			}
		}

		@Nested
		class ADouble {

			@Test
			void read() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Value v = session.run("MATCH (n:CypherTypes) RETURN n.aDouble as r").single().get("r");

					double r = DEFAULT_CONVERSION_SERVICE.convert(v, double.class);
					assertThat(r).isEqualTo(1.7976931348);

					Double rM = DEFAULT_CONVERSION_SERVICE.convert(v, double.class);
					assertThat(rM).isEqualTo(Double.valueOf(1.7976931348));
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("v", DEFAULT_CONVERSION_SERVICE.convert(1.7976931348, TYPE_DESCRIPTOR_OF_VALUE));
					parameters
						.put("vM",
							DEFAULT_CONVERSION_SERVICE.convert(Double.valueOf(1.7976931348), TYPE_DESCRIPTOR_OF_VALUE));

					long cnt = session
						.run("MATCH (n:CypherTypes) WHERE n.aDouble = $v and n.aDouble = $vM RETURN COUNT(n) AS cnt",
							parameters)
						.single().get("cnt").asLong();
					assertThat(cnt).isEqualTo(1L);
				}
			}
		}

		@Nested
		class AString {

			@Test
			void read() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Value v = session.run("MATCH (n:CypherTypes) RETURN n.aString as r").single().get("r");

					String convert = DEFAULT_CONVERSION_SERVICE.convert(v, String.class);
					assertThat(convert).isEqualTo("Hallo, Cypher");
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("v", DEFAULT_CONVERSION_SERVICE.convert("Hallo, Cypher", TYPE_DESCRIPTOR_OF_VALUE));

					long cnt = session
						.run("MATCH (n:CypherTypes) WHERE n.aString = $v RETURN COUNT(n) AS cnt",
							parameters)
						.single().get("cnt").asLong();
					assertThat(cnt).isEqualTo(1L);
				}
			}
		}

		@Nested
		class AByteArray {

			@Test
			void read() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Value v = session.run("MATCH (n:CypherTypes) RETURN n.aByteArray as r").single().get("r");

					byte[] convert = DEFAULT_CONVERSION_SERVICE.convert(v, byte[].class);
					assertThat(convert).isEqualTo("A thing".getBytes());
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Map<String, Object> parameters = new HashMap<>();
					parameters
						.put("v", DEFAULT_CONVERSION_SERVICE.convert("A thing".getBytes(), TYPE_DESCRIPTOR_OF_VALUE));

					long cnt = session
						.run("MATCH (n:CypherTypes) WHERE n.aByteArray = $v RETURN COUNT(n) AS cnt",
							parameters)
						.single().get("cnt").asLong();
					assertThat(cnt).isEqualTo(1L);
				}
			}
		}

		@Nested
		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
		class Temporals {

			@ParameterizedTest
			@MethodSource("cypherTemporals")
			void read(String name, Object t) {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Value v = session.run("MATCH (n:CypherTypes) RETURN n." + name + " as r").single().get("r");

					Object converted = DEFAULT_CONVERSION_SERVICE.convert(v, t.getClass());
					assertThat(converted).isEqualTo(t);
				}
			}

			@ParameterizedTest
			@MethodSource("cypherTemporals")
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

			Stream<Arguments> cypherTemporals() {
				return Stream.of(
					arguments("aLocalDate", LocalDate.of(2015, 7, 21)),
					arguments("anOffsetTime", OffsetTime.of(12, 31, 0, 0, ZoneOffset.ofHours(1))),
					arguments("aLocalTime", LocalTime.of(12, 31, 14)),
					arguments("aZoneDateTime", ZonedDateTime
						.of(2015, 7, 21, 21, 40, 32, 0, TimeZone.getTimeZone("America/New_York").toZoneId())),
					arguments("aLocalDateTime", LocalDateTime.of(2015, 7, 21, 21, 0)),
					arguments("anIsoDuration", Values.isoDuration(0, 14, 58320, 0).asObject())
				);
			}
		}

		@Nested
		class APoint {

			@Test
			void read() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Value v = session.run("MATCH (n:CypherTypes) RETURN n.aPoint as r").single().get("r");

					org.neo4j.driver.types.Point convert = DEFAULT_CONVERSION_SERVICE
						.convert(v, org.neo4j.driver.types.Point.class);
					assertThat(convert).isEqualTo(Values.point(7203, 47, 11).asPoint());
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("v",
						DEFAULT_CONVERSION_SERVICE
							.convert(Values.point(7203, 47, 11).asPoint(), TYPE_DESCRIPTOR_OF_VALUE));

					long cnt = session
						.run("MATCH (n:CypherTypes) WHERE n.aPoint = $v RETURN COUNT(n) AS cnt",
							parameters)
						.single().get("cnt").asLong();
					assertThat(cnt).isEqualTo(1L);
				}
			}
		}
	}

	@Nested
	class AdditionalTypes {

		@Nested
		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
		class Temporals {

			@ParameterizedTest
			@MethodSource("additionalTemporals")
			void read(String name, Object t) {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Value v = session.run("MATCH (n:AdditionalTypes) RETURN n." + name + " as r").single().get("r");

					Object converted = DEFAULT_CONVERSION_SERVICE.convert(v, t.getClass());
					assertThat(converted).isEqualTo(t);
				}
			}

			@ParameterizedTest
			@MethodSource("additionalTemporals")
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

			Stream<Arguments> additionalTemporals() {
				return Stream.of(
					arguments("aPeriod", Period.of(23, 4, 7)),
					arguments("aDuration", Duration.ofHours(25).plusMinutes(63).plusSeconds(65))
				);
			}
		}

		@Nested
		class BooleanArray {

			@Test
			void read() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Value v = session.run("MATCH (n:AdditionalTypes) RETURN n.booleanArray as r").single().get("r");
					boolean[] r = DEFAULT_CONVERSION_SERVICE.convert(v, boolean[].class);
					assertThat(r).containsExactly(true, true, false);
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {

					long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.booleanArray = $v RETURN COUNT(n) AS cnt",
						Collections.singletonMap("v", DEFAULT_CONVERSION_SERVICE
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

					byte r = DEFAULT_CONVERSION_SERVICE.convert(v, byte.class);
					assertThat(r).isEqualTo((byte) 6);

					Byte rM = DEFAULT_CONVERSION_SERVICE.convert(v, Byte.class);
					assertThat(rM).isEqualTo(Byte.valueOf("6"));
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("v", DEFAULT_CONVERSION_SERVICE.convert((byte) 6, TYPE_DESCRIPTOR_OF_VALUE));
					parameters
						.put("vM", DEFAULT_CONVERSION_SERVICE.convert(Byte.valueOf("6"), TYPE_DESCRIPTOR_OF_VALUE));

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

					char r = DEFAULT_CONVERSION_SERVICE.convert(v, char.class);
					assertThat(r).isEqualTo('x');

					Character rM = DEFAULT_CONVERSION_SERVICE.convert(v, Character.class);
					assertThat(rM).isEqualTo(Character.valueOf('x'));
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("v", DEFAULT_CONVERSION_SERVICE.convert('x', TYPE_DESCRIPTOR_OF_VALUE));
					parameters.put("vM",
						DEFAULT_CONVERSION_SERVICE.convert(Character.valueOf('x'), TYPE_DESCRIPTOR_OF_VALUE));

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
					char[] r = DEFAULT_CONVERSION_SERVICE.convert(v, char[].class);
					assertThat(r).containsExactly('x', 'y', 'z');
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {

					long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.charArray = $v RETURN COUNT(n) AS cnt",
						Collections.singletonMap("v", DEFAULT_CONVERSION_SERVICE
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
					Date r = DEFAULT_CONVERSION_SERVICE.convert(v, Date.class);
					assertThat(r)
						.isEqualTo(Date.from(LocalDateTime.of(2019, 9, 21, 0, 0, 0).toInstant(ZoneOffset.UTC)));
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {

					long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.aDate = $v RETURN COUNT(n) AS cnt",
						Collections.singletonMap("v", DEFAULT_CONVERSION_SERVICE
							.convert(Date.from(LocalDateTime.of(2019, 9, 21, 0, 0, 0).toInstant(ZoneOffset.UTC)),
								TYPE_DESCRIPTOR_OF_VALUE))).single()
						.get("cnt").asLong();
					assertThat(cnt).isEqualTo(1L);
				}
			}
		}

		@Nested
		class ABigDecimal {

			@Test
			void read() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Value v = session.run("MATCH (n:AdditionalTypes) RETURN n.aBigDecimal as r").single().get("r");
					BigDecimal r = DEFAULT_CONVERSION_SERVICE.convert(v, BigDecimal.class);
					assertThat(r).isEqualTo(BigDecimal.valueOf(Double.MAX_VALUE).multiply(BigDecimal.TEN));
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {

					long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.aBigDecimal = $v RETURN COUNT(n) AS cnt",
						Collections.singletonMap("v", DEFAULT_CONVERSION_SERVICE
							.convert(BigDecimal.valueOf(Double.MAX_VALUE).multiply(BigDecimal.TEN),
								TYPE_DESCRIPTOR_OF_VALUE))).single()
						.get("cnt").asLong();
					assertThat(cnt).isEqualTo(1L);
				}
			}
		}

		@Nested
		class ABigInteger {

			@Test
			void read() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Value v = session.run("MATCH (n:AdditionalTypes) RETURN n.aBigInteger as r").single().get("r");
					BigInteger r = DEFAULT_CONVERSION_SERVICE.convert(v, BigInteger.class);
					assertThat(r).isEqualTo(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN));
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {

					long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.aBigInteger = $v RETURN COUNT(n) AS cnt",
						Collections.singletonMap("v", DEFAULT_CONVERSION_SERVICE
							.convert(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN),
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
					double[] r = DEFAULT_CONVERSION_SERVICE.convert(v, double[].class);
					assertThat(r).containsExactly(1.1, 2.2, 3.3);
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {

					long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.doubleArray = $v RETURN COUNT(n) AS cnt",
						Collections.singletonMap("v", DEFAULT_CONVERSION_SERVICE
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

					float r = DEFAULT_CONVERSION_SERVICE.convert(v, float.class);
					assertThat(r).isEqualTo(23.42F);

					Float rM = DEFAULT_CONVERSION_SERVICE.convert(v, Float.class);
					assertThat(rM).isEqualTo(Float.valueOf(23.42F));
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("v", DEFAULT_CONVERSION_SERVICE.convert(23.42F, TYPE_DESCRIPTOR_OF_VALUE));
					parameters
						.put("vM", DEFAULT_CONVERSION_SERVICE.convert(Float.valueOf(23.42F), TYPE_DESCRIPTOR_OF_VALUE));

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
					float[] r = DEFAULT_CONVERSION_SERVICE.convert(v, float[].class);
					assertThat(r).containsExactly(4.4F, 5.5F);
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {

					long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.floatArray = $v RETURN COUNT(n) AS cnt",
						Collections.singletonMap("v", DEFAULT_CONVERSION_SERVICE
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

					int r = DEFAULT_CONVERSION_SERVICE.convert(v, int.class);
					assertThat(r).isEqualTo(42);

					Integer rM = DEFAULT_CONVERSION_SERVICE.convert(v, Integer.class);
					assertThat(rM).isEqualTo(Integer.valueOf(42));
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("v", DEFAULT_CONVERSION_SERVICE.convert(42, TYPE_DESCRIPTOR_OF_VALUE));
					parameters
						.put("vM", DEFAULT_CONVERSION_SERVICE.convert(Integer.valueOf(42), TYPE_DESCRIPTOR_OF_VALUE));

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
					int[] r = DEFAULT_CONVERSION_SERVICE.convert(v, int[].class);
					assertThat(r).containsExactly(21, 9);
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {

					long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.intArray = $v RETURN COUNT(n) AS cnt",
						Collections.singletonMap("v", DEFAULT_CONVERSION_SERVICE
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
					Locale r = DEFAULT_CONVERSION_SERVICE.convert(v, Locale.class);
					assertThat(r).isEqualTo(Locale.GERMANY);
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {

					long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.aLocale = $v RETURN COUNT(n) AS cnt",
						Collections.singletonMap("v", DEFAULT_CONVERSION_SERVICE
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
					long[] r = DEFAULT_CONVERSION_SERVICE.convert(v, long[].class);
					assertThat(r).containsExactly(Long.MIN_VALUE, Long.MAX_VALUE);
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {

					long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.longArray = $v RETURN COUNT(n) AS cnt",
						Collections.singletonMap("v", DEFAULT_CONVERSION_SERVICE
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

					short r = DEFAULT_CONVERSION_SERVICE.convert(v, short.class);
					assertThat(r).isEqualTo((short) 127);

					Short rM = DEFAULT_CONVERSION_SERVICE.convert(v, Short.class);
					assertThat(rM).isEqualTo(Short.valueOf((short) 127));
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("v", DEFAULT_CONVERSION_SERVICE.convert((short) 127, TYPE_DESCRIPTOR_OF_VALUE));
					parameters
						.put("vM",
							DEFAULT_CONVERSION_SERVICE.convert(Short.valueOf((short) 127), TYPE_DESCRIPTOR_OF_VALUE));

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
					short[] r = DEFAULT_CONVERSION_SERVICE.convert(v, short[].class);
					assertThat(r).containsExactly((short) -10, (short) 10);
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {

					long cnt = session.run("MATCH (n:AdditionalTypes) WHERE n.shortArray = $v RETURN COUNT(n) AS cnt",
						Collections.singletonMap("v", DEFAULT_CONVERSION_SERVICE
							.convert(new short[] { -10, 10 }, TYPE_DESCRIPTOR_OF_VALUE))).single()
						.get("cnt").asLong();
					assertThat(cnt).isEqualTo(1L);
				}
			}
		}
	}

	@Nested
	class SpatialTypes {

		@Nested
		class SpringPoint {

			@Test
			void read() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Value v = session.run("MATCH (n:SpatialTypes) RETURN n.sdnPoint as r").single().get("r");

					org.springframework.data.geo.Point r = (org.springframework.data.geo.Point) DEFAULT_CONVERSION_SERVICE
						.convert(v, TypeDescriptor.valueOf(org.springframework.data.geo.Point.class));
					assertThat(r).isEqualTo(NEO_HQ.asSpringPoint());
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {

					long cnt = session.run("MATCH (n:SpatialTypes) WHERE n.sdnPoint = $v RETURN COUNT(n) AS cnt",
						Collections.singletonMap("v", DEFAULT_CONVERSION_SERVICE
							.convert(NEO_HQ.asSpringPoint(), TYPE_DESCRIPTOR_OF_VALUE))).single()
						.get("cnt").asLong();
					assertThat(cnt).isEqualTo(1L);
				}
			}
		}

		@Nested
		class Neo4jPoints {
			@Test
			void read() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Value n = session.run("MATCH (n:SpatialTypes) RETURN n").single().get("n");

					GeographicPoint2d geo2d = DEFAULT_CONVERSION_SERVICE
						.convert(n.get("geo2d"), GeographicPoint2d.class);
					assertThat(geo2d).isEqualTo(MINC.asGeo2d());

					GeographicPoint3d geo3d = DEFAULT_CONVERSION_SERVICE
						.convert(n.get("geo3d"), GeographicPoint3d.class);
					assertThat(geo3d).isEqualTo(CLARION.asGeo3d(27.0));

					CartesianPoint2d car2d = DEFAULT_CONVERSION_SERVICE.convert(n.get("car2d"), CartesianPoint2d.class);
					assertThat(car2d).isEqualTo(new CartesianPoint2d(10, 20));

					CartesianPoint3d car3d = DEFAULT_CONVERSION_SERVICE.convert(n.get("car3d"), CartesianPoint3d.class);
					assertThat(car3d).isEqualTo(new CartesianPoint3d(30, 40, 50));
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {

					Map<String, Object> parameters = new HashMap<>();
					parameters
						.put("geo2d", DEFAULT_CONVERSION_SERVICE.convert(MINC.asGeo2d(), TYPE_DESCRIPTOR_OF_VALUE));
					parameters.put("geo3d",
						DEFAULT_CONVERSION_SERVICE.convert(CLARION.asGeo3d(27.0), TYPE_DESCRIPTOR_OF_VALUE));
					parameters.put("car2d",
						DEFAULT_CONVERSION_SERVICE.convert(new CartesianPoint2d(10, 20), TYPE_DESCRIPTOR_OF_VALUE));
					parameters.put("car3d",
						DEFAULT_CONVERSION_SERVICE.convert(new CartesianPoint3d(30, 40, 50), TYPE_DESCRIPTOR_OF_VALUE));

					long cnt = session.run(
						"MATCH (n:SpatialTypes) WHERE n.geo2d = $geo2d AND n.geo3d = $geo3d AND n.car2d = $car2d AND n.car3d = $car3d RETURN COUNT(n) AS cnt",
						parameters
					).single()
						.get("cnt").asLong();
					assertThat(cnt).isEqualTo(1L);
				}
			}
		}
	}
}
