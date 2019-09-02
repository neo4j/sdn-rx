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

import org.neo4j.springframework.data.core.mapping.Neo4jPersistentProperty;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.data.mapping.model.ParameterValueProvider;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * @author Michael J. Simons
 * @soundtrack The Kleptones - A Night At The Hip-Hopera
 * @since 1.0
 */
public class DefaultNeo4jConverter implements Neo4jConverter {

	private final CustomConversions customConversions;
	private final ConfigurableConversionService conversionService;

	public DefaultNeo4jConverter(CustomConversions customConversions, ConfigurableConversionService conversionService) {

		Assert.notNull(customConversions, "CustomConversions must not be null!");
		// conversionService will be asserted by customConversions.registerConvertersIn

		this.customConversions = customConversions;
		this.conversionService = conversionService;

		customConversions.registerConvertersIn(this.conversionService);
	}

	@Override
	@Nullable
	public Object readValue(@Nullable Object value, TypeInformation<?> type) {

		if (null == value) {
			return null;
		}

		if (customConversions.hasCustomReadTarget(value.getClass(), type.getType())) {
			return conversionService.convert(value, type.getType());
		}

		return getPotentiallyConvertedSimpleRead(value, type.getType());
	}

	@Override
	public <T> PersistentPropertyAccessor<T> decoratePropertyAccessor(
		PersistentPropertyAccessor<T> targetPropertyAccessor) {

		return new ConvertingPropertyAccessor<>(targetPropertyAccessor, conversionService);
	}

	@Override
	public <T> ParameterValueProvider<Neo4jPersistentProperty> decorateParameterValueProvider(
		ParameterValueProvider<Neo4jPersistentProperty> targetParameterValueProvider) {

		return new ParameterValueProvider<Neo4jPersistentProperty>() {
			@Override
			public Object getParameterValue(PreferredConstructor.Parameter parameter) {

				Object originalValue = targetParameterValueProvider.getParameterValue(parameter);
				return readValue(originalValue, parameter.getType());
			}
		};
	}

	/**
	 * Checks whether we have a custom conversion for the given simple object. Converts the given value if so, applies
	 * {@link Enum} handling or returns the value as is.
	 *
	 * @param value  to be converted. May be {@code null}..
	 * @param target may be {@code null}..
	 * @return the converted value if a conversion applies or the original value. Might return {@code null}.
	 */
	@Nullable
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object getPotentiallyConvertedSimpleRead(@Nullable Object value, @Nullable Class<?> target) {

		if (value == null || target == null || ClassUtils.isAssignableValue(target, value)) {
			return value;
		}

		if (Enum.class.isAssignableFrom(target)) {
			return Enum.valueOf((Class<Enum>) target, value.toString());
		}

		return conversionService.convert(value, target);
	}
}
