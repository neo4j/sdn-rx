/*
 * Copyright (c) 2019-2020 "Neo4j,"
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
package org.neo4j.doc.springframework.data.docs.repositories.conversion;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.driver.Value;
import org.neo4j.springframework.data.core.convert.Neo4jConversions;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

/**
 * @author Gerrit Meier
 */
// tag::custom-converter.implementation[]
public class MyCustomTypeConverter implements GenericConverter {

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		Set<ConvertiblePair> convertiblePairs = new HashSet<>();
		convertiblePairs.add(new ConvertiblePair(MyCustomType.class, Value.class));
		convertiblePairs.add(new ConvertiblePair(Value.class, MyCustomType.class));
		return convertiblePairs;
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (MyCustomType.class.isAssignableFrom(sourceType.getType())) {
			// convert to Neo4j Driver Value
			return convertToNeo4jValue(source);
		} else {
			// convert to MyCustomType
			return convertToMyCustomType(source);
		}
	}
	// end::custom-converter.implementation[]
	// tag::custom-converter.neo4jConversions[]
	@Bean
	public Neo4jConversions neo4jConversions() {
		Set<GenericConverter> additionalConverters = Collections.singleton(new MyCustomTypeConverter());
		return new Neo4jConversions(additionalConverters);
	}
	// end::custom-converter.neo4jConversions[]

	private static class MyCustomType {
	}

	private Object convertToNeo4jValue(Object source) {
		return null;
	}

	private Object convertToMyCustomType(Object source) {
		return null;
	}
// tag::custom-converter.implementation[]
}
// end::custom-converter.implementation[]


