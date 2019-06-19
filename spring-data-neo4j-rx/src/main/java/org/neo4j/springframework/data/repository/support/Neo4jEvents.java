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
package org.neo4j.springframework.data.repository.support;

import org.neo4j.springframework.data.repository.event.BeforeBindCallback;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.lang.Nullable;

/**
 * Utility class that orchestrates both an {@link ApplicationEventPublisher} and {@link EntityCallbacks}.
 * All the methods provided here check for their availability and do nothing when an event cannot be published.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
final class Neo4jEvents {

	private final @Nullable ApplicationEventPublisher eventPublisher;
	private final @Nullable EntityCallbacks entityCallbacks;

	Neo4jEvents(@Nullable ApplicationEventPublisher eventPublisher,
		@Nullable EntityCallbacks entityCallbacks) {
		this.eventPublisher = eventPublisher;
		this.entityCallbacks = entityCallbacks;
	}

	protected <T> T maybeCallBeforeBind(T object) {
		if (entityCallbacks != null) {
			return entityCallbacks.callback(BeforeBindCallback.class, object);
		}

		return object;
	}
}
