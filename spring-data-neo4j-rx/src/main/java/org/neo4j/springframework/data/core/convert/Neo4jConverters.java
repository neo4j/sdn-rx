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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.neo4j.driver.Values;
import org.springframework.core.convert.converter.Converter;

/**
 * Built-in converters for Neo4j.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
abstract class Neo4jConverters {

	/**
	 * @return the converters to be registered.
	 */
	static Collection<Object> getConvertersToRegister() {

		List<Object> converters = new ArrayList<>();


		converters.add(DoubleToFloatConverter.INSTANCE);
		// converters.add(reading(String.class, URI.class, URI::create).andWriting(URI::toString));

		return converters;
	}

	enum DoubleToFloatConverter implements Converter<Double, Float> {
		INSTANCE;

		@Override
		public Float convert(Double source) {

			if (source == null) {
				return null;
			}

			// Apply the same conversion
			return Values.value(source).asFloat();
		}
	}

	private Neo4jConverters() {
	}
}
