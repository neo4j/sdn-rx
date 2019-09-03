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

import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.TypeSystem;
import org.neo4j.springframework.data.core.convert.Neo4jConverter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.data.mapping.model.ParameterValueProvider;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * @author Michael J. Simons
 * @soundtrack The Kleptones - A Night At The Hip-Hopera
 * @since 1.0
 */
final class DefaultNeo4jConverter implements Neo4jConverter {

	private final CustomConversions customConversions;
	private final ConfigurableConversionService conversionService;

	DefaultNeo4jConverter(CustomConversions customConversions, ConfigurableConversionService conversionService) {

		Assert.notNull(customConversions, "CustomConversions must not be null!");
		// conversionService will be asserted by customConversions.registerConvertersIn

		this.customConversions = customConversions;
		this.conversionService = conversionService;

		customConversions.registerConvertersIn(this.conversionService);
	}

	@Override
	@Nullable
	public Object readValue(@Nullable Object value, TypeSystem typeSystem, TypeInformation<?> type) {

		if (value == null || value == Values.NULL) {
			return null;
		}

		TypeInformation<?> relevantType = type.getActualType() == null ? type : type.getActualType();

		if (value instanceof Value && typeSystem.LIST().isTypeOf((Value) value)) {
			return ((Value) value).asList(v -> readValue(v, typeSystem, relevantType));
		}

		return conversionService.convert(value, relevantType.getType());
	}

	@Override
	public <T> PersistentPropertyAccessor<T> decoratePropertyAccessor(TypeSystem typeSystem,
		PersistentPropertyAccessor<T> targetPropertyAccessor) {

		return new ConvertingPropertyAccessor<>(targetPropertyAccessor, conversionService);
	}

	@Override
	public <T> ParameterValueProvider<Neo4jPersistentProperty> decorateParameterValueProvider(
		TypeSystem typeSystem,
		ParameterValueProvider<Neo4jPersistentProperty> targetParameterValueProvider) {

		return new ParameterValueProvider<Neo4jPersistentProperty>() {
			@Override
			public Object getParameterValue(PreferredConstructor.Parameter parameter) {

				Object originalValue = targetParameterValueProvider.getParameterValue(parameter);
				return readValue(originalValue, typeSystem, parameter.getType());
			}
		};
	}


}
