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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Point;
import org.springframework.data.convert.ConverterBuilder;
import org.springframework.util.Assert;

/**
 * Mapping of spatial types.
 *
 * @author Michael J. Simons
 */
final class SpatialTypes {

	static final List<?> CONVERTERS;

	static {

		List<ConverterBuilder.ConverterAware> hlp = new ArrayList<>();
		// List will be unwrapped by {@code Neo4jConverter}
		hlp.add(reading(Value.class, org.springframework.data.geo.Point.class, SpatialTypes::asPoint)
			.andWriting(SpatialTypes::value));
		hlp.add(reading(Value.class, org.springframework.data.geo.Point[].class, SpatialTypes::asPointArray)
			.andWriting(SpatialTypes::value));

		CONVERTERS = Collections.unmodifiableList(hlp);
	}

	private static org.springframework.data.geo.Point asPoint(Value value) {
		Point point = value.asPoint();
		Assert.isTrue(point.srid() == 4326, "srid must be 4326");
		return new org.springframework.data.geo.Point(point.x(), point.y());
	}

	private static Value value(org.springframework.data.geo.Point point) {
		return Values.point(4326, point.getX(), point.getY());
	}

	private static org.springframework.data.geo.Point[] asPointArray(Value value) {
		org.springframework.data.geo.Point[] array = new org.springframework.data.geo.Point[value.size()];
		int i = 0;
		for (org.springframework.data.geo.Point v : value.values(SpatialTypes::asPoint)) {
			array[i++] = v;
		}
		return array;
	}

	private static Value value(org.springframework.data.geo.Point[] aPointArray) {
		if (aPointArray == null) {
			return Values.NULL;
		}

		Value[] values = new Value[aPointArray.length];
		int i = 0;
		for (org.springframework.data.geo.Point v : aPointArray) {
			values[i++] = value(v);
		}

		return Values.value(values);
	}

	private SpatialTypes() {
	}
}
