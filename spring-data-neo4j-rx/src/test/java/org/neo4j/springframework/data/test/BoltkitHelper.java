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
package org.neo4j.springframework.data.test;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

/**
 * This helper is for internal use only.
 *
 * @author Michael J. Simons
 * @soundtrack Deichkind - Noch fünf Minuten Mutti
 * @since 1.0
 */
public final class BoltkitHelper {

	public static Driver getClusterConnection() {
		// TODO Actually call boltkit ;)
		return GraphDatabase.driver("neo4j://localhost:7687", AuthTokens.basic("neo4j", "secret"));
	}

	private BoltkitHelper() {
	}
}
