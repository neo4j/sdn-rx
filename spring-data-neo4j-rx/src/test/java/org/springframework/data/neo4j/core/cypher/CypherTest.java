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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.neo4j.core.cypher.renderer.CypherRenderer;
import org.springframework.data.neo4j.core.cypher.renderer.Renderer;

/**
 * @author Michael J. Simons
 */
public class CypherTest {

	@Nested
	class SingleQuerySinglePart {

		@Test
		void readingAndReturn() {

			Node bikeNode = Cypher.node("Bike").as("b");
			Node userNode = Cypher.node("User").as("u");
			Node tripNode = Cypher.node("Trip").as("u");

			/*

			String cypher = "MATCH (o:User {name: $name}) - [:OWNS] -> (b:Bike) - [:USED_ON] -> (t:Trip) " +
			"WHERE t.takenOn > $aDate " +
			"  AND b.name =~ $bikeName " +
			"  AND t.location = $location " +  // TODO Nice place to add coordinates
			"RETURN b";

			 */

			Statement statement;

			statement = Cypher.match(bikeNode, userNode, Cypher.node("U").as("o"))
				.where(userNode.property("name").matches(".*aName"))
				.returning(bikeNode, userNode)
				.build();

			Renderer cypherRenderer = CypherRenderer.create();
			System.out.println(cypherRenderer.render(statement));

			statement = Cypher
				.match(userNode
					.outgoingRelationShipTo(bikeNode).withType("OWNS").create()
				)
				.where(userNode.property("name").matches(".*aName"))
				.returning(bikeNode, userNode)
				.build();

			System.out.println(cypherRenderer.render(statement));
			statement = Cypher
				.match(userNode
					.outgoingRelationShipTo(bikeNode).withType("OWNS").as("r1")
					.outgoingRelationShipTo(tripNode).withType("USED_ON").as("r2")
					.create()
				)
				.where(userNode.property("name").matches(".*aName"))
				.returning(bikeNode, userNode)
				.build();

			System.out.println(cypherRenderer.render(statement));
		}
	}

}
