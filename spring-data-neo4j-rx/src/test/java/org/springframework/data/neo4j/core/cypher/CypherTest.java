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
package org.springframework.data.neo4j.core.cypher;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.data.neo4j.core.cypher.renderer.CypherRenderer;
import org.springframework.data.neo4j.core.cypher.renderer.Renderer;

/**
 * @author Michael J. Simons
 */
class CypherTest {

	@Test
	void shouldCreateMatchStatement() {

		Node bikeNode = Cypher.node("n", "Bike");
		Node userNode = Cypher.node("u", "User");

		Statement matchAndReturnAllBikes = Cypher
			.match(bikeNode, userNode)
			.returning(bikeNode)
			.build();

		Renderer renderer = CypherRenderer.create();
		String cypher = renderer.render(matchAndReturnAllBikes);
		assertThat(cypher)
			.isNotEmpty()
			.isEqualTo("MATCH (n:`Bike`), (u:`User`) RETURN n");
	}
}
