/*
 * Copyright (c) 2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.neo4j.core.context.tracking;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apiguardian.api.API;

/**
 * Compares two entities of the same instance and creates a delta {@link EntityChangeEvent}.
 *
 * @author Gerrit Meier
 */
@API(status = API.Status.INTERNAL, since = "1.0")
public class EntityState {

	private final Map<String, Object> oldState;
	private final Field[] objectFields;
	private final Object identifier;

	public EntityState(Object oldEntity) {
		objectFields = oldEntity.getClass().getDeclaredFields();
		this.identifier = computeIdentifier(oldEntity);
		this.oldState = retrieveStateFrom(oldEntity);
	}

	public Set<EntityChangeEvent> computeDelta(Object newObject) {
		if (!sameObject(newObject)) {
			throw new IllegalArgumentException("The objects to compare are not the same.");
		}
		Set<EntityChangeEvent> changes = new HashSet<>();

		for (Field field : objectFields) {
			Object newValue = getFieldValueOrHashCode(field, newObject);
			if (!oldState.get(field.getName()).equals(newValue)) {
				changes.add(new EntityChangeEvent(field.getName(), newValue));
			}
		}

		return changes;
	}

	private boolean sameObject(Object objectToCompare) {
		return this.identifier.equals(computeIdentifier(objectToCompare));
	}

	private Object computeIdentifier(Object object) {
		return System.identityHashCode(object);
	}

	private Map<String, Object> retrieveStateFrom(Object entity) {
		Map<String, Object> state = new HashMap<>();

		for (Field field : objectFields) {
			state.put(field.getName(), getFieldValueOrHashCode(field, entity));
		}

		return Collections.unmodifiableMap(state);
	}

	private Object getFieldValueOrHashCode(Field field, Object entity) {
		try {
			field.setAccessible(true);
			Object value = field.get(entity);
			field.setAccessible(false);
			if (!Collection.class.isAssignableFrom(value.getClass())) {
				return value;
			} else {
				return value.hashCode();
			}
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Cannot determine field value", e);
		}
	}
}
