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
package org.neo4j.springframework.data.integration.shared;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.internal.value.StringValue;
import org.neo4j.springframework.data.core.schema.GeneratedValue;
import org.neo4j.springframework.data.core.schema.Id;
import org.neo4j.springframework.data.core.schema.Node;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

/**
 * @author Gerrit Meier
 */
@Node("CustomTypes")
public class ThingWithCustomTypes {

	@Id @GeneratedValue private final Long id;

	private CustomType customType;

	public ThingWithCustomTypes(Long id, CustomType customType) {
		this.id = id;
		this.customType = customType;
	}

	public ThingWithCustomTypes withId(Long newId) {
		return new ThingWithCustomTypes(newId, this.customType);
	}

	public CustomType getCustomType() {
		return customType;
	}

	/**
	 * Custom type to convert
	 */
	public static class CustomType {

		private final String value;

		public static CustomType of(String value) {
			return new CustomType(value);
		}

		public String getValue() {
			return value;
		}

		private CustomType(String value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			CustomType that = (CustomType) o;
			return value.equals(that.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}
	}

	/**
	 * Converter that converts the custom type.
	 */
	public static class CustomTypeConverter implements GenericConverter {

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			Set<ConvertiblePair> convertiblePairs = new HashSet<>();
			convertiblePairs.add(new ConvertiblePair(Value.class, CustomType.class));
			convertiblePairs.add(new ConvertiblePair(CustomType.class, Value.class));
			return convertiblePairs;
		}

		@Override
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {

			if (StringValue.class.isAssignableFrom(sourceType.getType())) {
				return CustomType.of(((StringValue) source).asString());
			} else {
				return Values.value(((CustomType) source).getValue());
			}
		}
	}
}
