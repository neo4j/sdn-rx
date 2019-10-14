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

import org.neo4j.springframework.data.core.mapping.Neo4jPersistentEntity;
import org.springframework.data.repository.core.support.PersistentEntityInformation;

/**
 * Default implementation of Neo4j specific entity information.
 *
 * @author Michael J. Simons
 * @soundtrack Bear McCreary - Battlestar Galactica Season 1
 * @since 1.0
 */
final class DefaultNeo4jEntityInformation<T, ID> extends PersistentEntityInformation<T, ID>
	implements Neo4jEntityInformation<T, ID> {

	private final Neo4jPersistentEntity<T> entityMetaData;

	DefaultNeo4jEntityInformation(Neo4jPersistentEntity<T> entityMetaData) {
		super(entityMetaData);
		this.entityMetaData = entityMetaData;
	}

	/*
	 * (non-Javadoc)
	 * @see Neo4jEntityInformation#getEntityMetaData()
	 */
	@Override
	public Neo4jPersistentEntity<T> getEntityMetaData() {
		return this.entityMetaData;
	}
}
