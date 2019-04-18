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
import org.springframework.data.neo4j.core.cypher.renderer.RenderingVisitor;

/**
 * @author Michael J. Simons
 */
public class CypherTest {

	@Nested
	class SingleQuerySinglePart {

		@Test
		void readingAndReturn() {

			Node bikeNode = Cypher.node("b", "Bike");
			Node userNode = Cypher.node("u", "User");
			Node tripNode = Cypher.node("t", "Trip");

			/*

			String cypher = "MATCH (o:User {name: $name}) - [:OWNS] -> (b:Bike) - [:USED_ON] -> (t:Trip) " +
			"WHERE t.takenOn > $aDate " +
			"  AND b.name =~ $bikeName " +
			"  AND t.location = $location " +  // TODO Nice place to add coordinates
			"RETURN b";

			 */

			Statement statement;

			statement = Cypher.match(bikeNode, userNode, Cypher.node("o", "U"))
				.where(userNode.property("name").matches(".*aName"))
				.returning(bikeNode, userNode)
				.build();

			RenderingVisitor x;

			x = new RenderingVisitor();
			statement.accept(x);
			System.out.println(x.getRenderedContent());

			statement = Cypher
				.match(userNode
					.outgoingRelationShipTo(bikeNode).withType("OWNS").create()
				)
				.where(userNode.property("name").matches(".*aName"))
				.returning(bikeNode, userNode)
				.build();

			x = new RenderingVisitor();
			statement.accept(x);
			System.out.println(x.getRenderedContent());
		}
	}

}
