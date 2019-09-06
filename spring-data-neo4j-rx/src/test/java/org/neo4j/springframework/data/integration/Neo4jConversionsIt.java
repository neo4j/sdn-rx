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
import static org.junit.jupiter.api.DynamicTest.*;

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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.springframework.data.core.convert.Neo4jConversions;
import org.neo4j.springframework.data.test.Neo4jExtension;
import org.neo4j.springframework.data.test.Neo4jExtension.Neo4jConnectionSupport;
import org.neo4j.springframework.data.test.Tuples;
import org.neo4j.springframework.data.test.Tuples.Tuple2;
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

	private static final List<Tuple2<String, Object>> CYPHER_TYPES = Arrays.asList(
		Tuples.of("aBoolean", true),
		Tuples.of("aLong", Long.MAX_VALUE),
		Tuples.of("aDouble", 1.7976931348),
		Tuples.of("aString", "Hallo, Cypher"),
		Tuples.of("aByteArray", "A thing".getBytes()),
		Tuples.of("aPoint", Values.point(7203, 47, 11).asPoint()),
		Tuples.of("aLocalDate", LocalDate.of(2015, 7, 21)),
		Tuples.of("anOffsetTime", OffsetTime.of(12, 31, 0, 0, ZoneOffset.ofHours(1))),
		Tuples.of("aLocalTime", LocalTime.of(12, 31, 14)),
		Tuples.of("aZoneDateTime", ZonedDateTime
			.of(2015, 7, 21, 21, 40, 32, 0, TimeZone.getTimeZone("America/New_York").toZoneId())),
		Tuples.of("aLocalDateTime", LocalDateTime.of(2015, 7, 21, 21, 0)),
		Tuples.of("anIsoDuration", Values.isoDuration(0, 14, 58320, 0).asObject())
	);

	private static final List<Tuple2<String, Object>> ADDITIONAL_TYPES = Arrays.asList(
		Tuples.of("booleanArray", new boolean[] { true, true, false }),
		Tuples.of("aByte", (byte) 6),
		Tuples.of("aChar", 'x'),
		Tuples.of("charArray", new char[] { 'x', 'y', 'z' }),
		Tuples.of("aDate", Date.from(LocalDateTime.of(2019, 9, 21, 0, 0, 0).toInstant(ZoneOffset.UTC))),
		Tuples.of("aBigDecimal", BigDecimal.valueOf(Double.MAX_VALUE).multiply(BigDecimal.TEN)),
		Tuples.of("aBigInteger", BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN)),
		Tuples.of("doubleArray", new double[] { 1.1, 2.2, 3.3 }),
		Tuples.of("aFloat", 23.42F),
		Tuples.of("floatArray", new float[] { 4.4F, 5.5F }),
		Tuples.of("anInt", 42),
		Tuples.of("intArray", new int[] { 21, 9 }),
		Tuples.of("aLocale", Locale.GERMANY),
		Tuples.of("longArray", new long[] { Long.MIN_VALUE, Long.MAX_VALUE }),
		Tuples.of("aShort", (short) 127),
		Tuples.of("shortArray", new short[] { -10, 10 }),
		Tuples.of("aPeriod", Period.of(23, 4, 7)),
		Tuples.of("aDuration", Duration.ofHours(25).plusMinutes(63).plusSeconds(65))
	);

	private static final List<Tuple2<String, Object>> SPATIAL_TYPES = Arrays.asList(
		Tuples.of("sdnPoint", NEO_HQ.asSpringPoint()),
		Tuples.of("geo2d", MINC.asGeo2d()),
		Tuples.of("car2d", new CartesianPoint2d(10, 20)),
		Tuples.of("car3d", new CartesianPoint3d(30, 40, 50))
	);

	@TestFactory
	@DisplayName("Objects")
	Stream<DynamicNode> objects() {
		Map<String, List<Tuple2<String, Object>>> parameterSources = new HashMap<>();
		parameterSources.put("CypherTypes", CYPHER_TYPES);
		parameterSources.put("AdditionalTypes", ADDITIONAL_TYPES);
		parameterSources.put("SpatialTypes", SPATIAL_TYPES);

		return parameterSources.entrySet().stream()
			.map(entry -> {

				DynamicContainer reads = DynamicContainer.dynamicContainer("read", entry.getValue().stream()
					.map(a -> dynamicTest(a.getV1(),
						() -> Neo4jConversionsIt.read(entry.getKey(), a.getV1(), a.getV2()))));

				DynamicContainer writes = DynamicContainer.dynamicContainer("write", entry.getValue().stream()
					.map(a -> dynamicTest(a.getV1(),
						() -> Neo4jConversionsIt.write(entry.getKey(), a.getV1(), a.getV2()))));

				return DynamicContainer.dynamicContainer(entry.getKey(), Arrays.asList(reads, writes));

			});
	}

	@TestFactory
	@DisplayName("Custom conversions")
	Stream<DynamicTest> customConversions() {
		final DefaultConversionService customConversionService = new DefaultConversionService();

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

		return Stream.of(
			dynamicTest("read",
				() -> assertThat(customConversionService.convert(Values.value("gestern"), LocalDate.class))
					.isEqualTo(LocalDate.now().minusDays(1))),
			dynamicTest("write",
				() -> assertThat(customConversionService.convert(LocalDate.now().plusDays(1), TYPE_DESCRIPTOR_OF_VALUE))
					.isEqualTo(Values.value("morgen")))
		);
	}

	@Nested
	class Primitives {

		@Test
		void cypherTypes() {
			boolean b = DEFAULT_CONVERSION_SERVICE.convert(Values.value(true), boolean.class);
			assertThat(b).isEqualTo(true);

			long l = DEFAULT_CONVERSION_SERVICE.convert(Values.value(Long.MAX_VALUE), long.class);
			assertThat(l).isEqualTo(Long.MAX_VALUE);

			double d = DEFAULT_CONVERSION_SERVICE.convert(Values.value(1.7976931348), double.class);
			assertThat(d).isEqualTo(1.7976931348);
		}

		@Test
		void additionalTypes() {

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
	}

	static void read(String label, String attribute, Object t) {
		try (Session session = neo4jConnectionSupport.getDriver().session()) {
			Value v = session.run("MATCH (n) WHERE labels(n) = [$label] RETURN n[$attribute] as r",
				Values.parameters("label", label, "attribute", attribute)).single().get("r");

			Object converted = DEFAULT_CONVERSION_SERVICE.convert(v, t.getClass());
			assertThat(converted).isEqualTo(t);
		}
	}

	static void write(String label, String attribute, Object t) {
		try (Session session = neo4jConnectionSupport.getDriver().session()) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("label", label);
			parameters.put("attribute", attribute);
			parameters.put("v", DEFAULT_CONVERSION_SERVICE.convert(t, TYPE_DESCRIPTOR_OF_VALUE));

			long cnt = session
				.run("MATCH (n) WHERE labels(n) = [$label]  AND n[$attribute] = $v RETURN COUNT(n) AS cnt",
					parameters)
				.single().get("cnt").asLong();
			assertThat(cnt).isEqualTo(1L);
		}
	}
}
