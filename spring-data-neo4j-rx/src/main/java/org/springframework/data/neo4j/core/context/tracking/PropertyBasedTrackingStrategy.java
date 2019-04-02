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

import org.springframework.data.neo4j.core.schema.NodeDescription;

/**
 * @author Gerrit Meier
 */
public class PropertyBasedTrackingStrategy implements EntityTrackingStrategy {

	@Override
	public void track(NodeDescription nodeDescription, Object entity) {
		// React to events propagated from the enhanced class and store them
	}

	@Override
	public Collection<EntityChangeEvent> getAggregatedDelta(Object entity) {
		// Aggregate collected events and return it
		return null;
	}
}
