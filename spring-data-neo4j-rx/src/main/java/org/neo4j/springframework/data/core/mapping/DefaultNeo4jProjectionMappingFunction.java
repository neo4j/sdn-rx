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
package org.neo4j.springframework.data.core.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.TypeSystem;
import org.neo4j.driver.util.Pair;
import org.neo4j.springframework.data.core.convert.Neo4jConverter;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.util.ClassTypeInformation;

/**
 * @author Gerrit Meier
 */
final class DefaultNeo4jProjectionMappingFunction implements BiFunction<TypeSystem, Record, Map<String, Object>> {


	private final Class domainType;
	private final Neo4jConverter converter;

	DefaultNeo4jProjectionMappingFunction(Class domainType, Neo4jConverter converter) {
		this.domainType = domainType;
		this.converter = converter;
	}

	@Override
	public Map<String, Object> apply(TypeSystem typeSystem, Record record) {
		SpelAwareProxyProjectionFactory spelAwareProxyProjectionFactory = new SpelAwareProxyProjectionFactory();

		Map<String, Value> recordValues = new HashMap<>();

		List<Pair<String, Value>> fields = record.fields();
		for (Pair<String, Value> field : fields) {

			// either it is the result of
			if (field.value().hasType(typeSystem.NODE()) || field.value().hasType(typeSystem.MAP())) { // a node as in `RETURN n` or  a map like `RETURN n{ .name}`
				recordValues.putAll(field.value().asMap(Function.identity()));
			} else { // or the whole result is a map like `RETURN n.name as name, x.y as z`
				recordValues.put(field.key(), field.value());
			}
		}

		Map<String, Object> sourceValues = new HashMap<>();
		spelAwareProxyProjectionFactory.getProjectionInformation(domainType).getInputProperties().forEach(property -> {

			String sourceValueKey = property.getName();
			Value value = recordValues.get(sourceValueKey);
			ClassTypeInformation<?> targetSourceValueType = ClassTypeInformation.from(property.getPropertyType());
			Object sourceValue = this.converter.readValue(value, targetSourceValueType);

			sourceValues.put(sourceValueKey, sourceValue);
		});

		return sourceValues;

		//return spelAwareProxyProjectionFactory.createProjection(domainType, sourceValues);
	}
}
