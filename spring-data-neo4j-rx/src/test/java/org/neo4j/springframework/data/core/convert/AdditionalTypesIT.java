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

import lombok.Builder;

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
import org.neo4j.springframework.data.types.CartesianPoint2d;
import org.neo4j.springframework.data.types.CartesianPoint3d;
import org.neo4j.springframework.data.types.GeographicPoint2d;
import org.neo4j.springframework.data.types.GeographicPoint3d;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.geo.Point;

/**
 * @author Michael J. Simons
 */
@ExtendWith(Neo4jExtension.class)
class AdditionalTypesIT {

	public static final TypeDescriptor TYPE_DESCRIPTOR_OF_VALUE = TypeDescriptor.valueOf(Value.class);
	private static Neo4jConnectionSupport neo4jConnectionSupport;
	private static DefaultConversionService conversionService;

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
	static void createConversionService() {

		conversionService = new DefaultConversionService();
		new Neo4jConversions().registerConvertersIn(conversionService);

		try (Session session = neo4jConnectionSupport.getDriver().session()) {
			session.writeTransaction(w -> {
				Map<String, Object> parameters;

				w.run("MATCH (n) detach delete n");

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
					+ " n.shortArray = [-10, 10]"
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
	class BooleanArray {

		@Test
		void read() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Value v = session.run("MATCH (n:AdditionalTypes) RETURN n.booleanArray as r").single().get("r");
				boolean[] r = conversionService.convert(v, boolean[].class);
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

				byte r = conversionService.convert(v, byte.class);
				assertThat(r).isEqualTo((byte) 6);

				Byte rM = conversionService.convert(v, Byte.class);
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

				char r = conversionService.convert(v, char.class);
				assertThat(r).isEqualTo('x');

				Character rM = conversionService.convert(v, Character.class);
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
				char[] r = conversionService.convert(v, char[].class);
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
				Date r = conversionService.convert(v, Date.class);
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
				double[] r = conversionService.convert(v, double[].class);
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

				float r = conversionService.convert(v, float.class);
				assertThat(r).isEqualTo(23.42F);

				Float rM = conversionService.convert(v, Float.class);
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
				float[] r = conversionService.convert(v, float[].class);
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

				int r = conversionService.convert(v, int.class);
				assertThat(r).isEqualTo(42);

				Integer rM = conversionService.convert(v, Integer.class);
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
				int[] r = conversionService.convert(v, int[].class);
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
				Locale r = conversionService.convert(v, Locale.class);
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
				long[] r = conversionService.convert(v, long[].class);
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

				short r = conversionService.convert(v, short.class);
				assertThat(r).isEqualTo((short) 127);

				Short rM = conversionService.convert(v, Short.class);
				assertThat(rM).isEqualTo(Short.valueOf((short) 127));
			}
		}

		@Test
		void write() {
			try (Session session = neo4jConnectionSupport.getDriver().session()) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put("v", conversionService.convert((short) 127, TYPE_DESCRIPTOR_OF_VALUE));
				parameters.put("vM", conversionService.convert(Short.valueOf((short) 127), TYPE_DESCRIPTOR_OF_VALUE));

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
				short[] r = conversionService.convert(v, short[].class);
				assertThat(r).containsExactly((short) -10, (short) 10);
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

	@Nested
	class SpatialTypes {

		@Nested
		class SpringPoint {

			@Test
			void read() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {
					Value v = session.run("MATCH (n:SpatialTypes) RETURN n.sdnPoint as r").single().get("r");

					org.springframework.data.geo.Point r = (org.springframework.data.geo.Point) conversionService
						.convert(v, TypeDescriptor.valueOf(org.springframework.data.geo.Point.class));
					assertThat(r).isEqualTo(NEO_HQ.asSpringPoint());
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {

					long cnt = session.run("MATCH (n:SpatialTypes) WHERE n.sdnPoint = $v RETURN COUNT(n) AS cnt",
						Collections.singletonMap("v", conversionService
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

					GeographicPoint2d geo2d = conversionService.convert(n.get("geo2d"), GeographicPoint2d.class);
					assertThat(geo2d).isEqualTo(MINC.asGeo2d());

					GeographicPoint3d geo3d = conversionService.convert(n.get("geo3d"), GeographicPoint3d.class);
					assertThat(geo3d).isEqualTo(CLARION.asGeo3d(27.0));

					CartesianPoint2d car2d = conversionService.convert(n.get("car2d"), CartesianPoint2d.class);
					assertThat(car2d).isEqualTo(new CartesianPoint2d(10, 20));

					CartesianPoint3d car3d = conversionService.convert(n.get("car3d"), CartesianPoint3d.class);
					assertThat(car3d).isEqualTo(new CartesianPoint3d(30, 40, 50));
				}
			}

			@Test
			void write() {
				try (Session session = neo4jConnectionSupport.getDriver().session()) {

					Map<String, Object> parameters = new HashMap<>();
					parameters.put("geo2d", conversionService.convert(MINC.asGeo2d(), TYPE_DESCRIPTOR_OF_VALUE));
					parameters.put("geo3d", conversionService.convert(CLARION.asGeo3d(27.0), TYPE_DESCRIPTOR_OF_VALUE));
					parameters.put("car2d",
						conversionService.convert(new CartesianPoint2d(10, 20), TYPE_DESCRIPTOR_OF_VALUE));
					parameters.put("car3d",
						conversionService.convert(new CartesianPoint3d(30, 40, 50), TYPE_DESCRIPTOR_OF_VALUE));

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
