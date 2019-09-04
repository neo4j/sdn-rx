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

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.TypeSystem;
import org.neo4j.driver.util.Pair;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

/**
 * @author Gerrit Meier
 */
final class DefaultNeo4jProjectionMappingFunction<T> implements BiFunction<TypeSystem, Record, T> {

	private final Class<T> domainType;

	DefaultNeo4jProjectionMappingFunction(Class<T> domainType) {
		this.domainType = domainType;
	}

	@Override
	public T apply(TypeSystem typeSystem, Record record) {
		SpelAwareProxyProjectionFactory spelAwareProxyProjectionFactory = new SpelAwareProxyProjectionFactory();

		Map sourceValues = new HashMap<>();

		List<Pair<String, Value>> fields = record.fields();
		for (Pair<String, Value> field : fields) {
			// either it is the result of
			if (field.value().hasType(typeSystem.NODE())) { // a node as in `RETURN n`
				sourceValues = field.value().asNode().asMap();
			} else if (field.value().hasType(typeSystem.MAP())) { // a map like `RETURN n{ .name}`
				sourceValues = field.value().asMap();
			} else {
				sourceValues = record.asMap(); // or the whole result is a map like `RETURN n.name as name, x.y as z`
			}
		}

		return spelAwareProxyProjectionFactory.createProjection(domainType, sourceValues);
	}
}
