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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Gerrit Meier
 */
public class EntityComparisonStrategy implements EntityTrackingStrategy {

	private final Map<Integer, EntityState> statesOfEntities = new HashMap<>();

	@Override
	public void registerEntity(Object entity) {
		statesOfEntities.put(getObjectIdentifier(entity), new EntityState(entity));
	}

	@Override
	public Collection<EntityChangeEvent> getAggregatedDelta(Object entity) {
		return statesOfEntities.get(getObjectIdentifier(entity)).computeDelta(entity);
	}

}
