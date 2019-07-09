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
package org.neo4j.springframework.data.core.schema;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * @author Michael J. Simons
 */
class IdDescriptionTest {

	@Test
	void isAssignedShouldWork() {

		assertThat(IdDescription.forAssignedIds("foobar").isAssignedId()).isTrue();
		assertThat(IdDescription.forAssignedIds("foobar").isExternallyGeneratedId()).isFalse();
		assertThat(IdDescription.forAssignedIds("foobar").isInternallyGeneratedId()).isFalse();
	}

	@Test
	void idIsGeneratedInternallyShouldWork() {

		assertThat(IdDescription.forInternallyGeneratedIds().isAssignedId()).isFalse();
		assertThat(IdDescription.forInternallyGeneratedIds().isExternallyGeneratedId()).isFalse();
		assertThat(IdDescription.forInternallyGeneratedIds().isInternallyGeneratedId()).isTrue();
	}

	@Test
	void idIsGeneratedExternally() {

		assertThat(IdDescription.forExternallyGeneratedIds(DummyIdGenerator.class, null, "foobar").isAssignedId()).isFalse();
		assertThat(IdDescription.forExternallyGeneratedIds(DummyIdGenerator.class, null, "foobar").isExternallyGeneratedId())
			.isTrue();
		assertThat(IdDescription.forExternallyGeneratedIds(DummyIdGenerator.class, null, "foobar").isInternallyGeneratedId())
			.isFalse();

		assertThat(IdDescription.forExternallyGeneratedIds(null, "someId", "foobar").isAssignedId()).isFalse();
		assertThat(IdDescription.forExternallyGeneratedIds(null, "someId", "foobar").isExternallyGeneratedId())
			.isTrue();
		assertThat(IdDescription.forExternallyGeneratedIds(null, "someId", "foobar").isInternallyGeneratedId())
			.isFalse();
	}

	private static class DummyIdGenerator implements IdGenerator<Void> {

		@Override
		public Void generateId(String primaryLabel, Object entity) {
			return null;
		}
	}
}
