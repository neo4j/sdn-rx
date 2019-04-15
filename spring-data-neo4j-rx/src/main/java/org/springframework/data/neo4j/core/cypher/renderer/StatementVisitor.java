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
package org.springframework.data.neo4j.core.cypher.renderer;

import static java.util.stream.Collectors.*;

import org.springframework.data.neo4j.core.cypher.Comparison;
import org.springframework.data.neo4j.core.cypher.Literal;
import org.springframework.data.neo4j.core.cypher.Match;
import org.springframework.data.neo4j.core.cypher.Node;
import org.springframework.data.neo4j.core.cypher.Property;
import org.springframework.data.neo4j.core.cypher.Return;
import org.springframework.data.neo4j.core.cypher.Visitable;
import org.springframework.data.neo4j.core.cypher.Where;

/**
 * @author Michael J. Simons
 */
public class StatementVisitor implements PartRenderer {

	private final RenderContext renderContext;

	private final StringBuilder builder = new StringBuilder();

	private int childCounter = 0;

	private boolean inMatch = false;

	private boolean inComparision = false;

	public StatementVisitor(RenderContext renderContext) {
		this.renderContext = renderContext;
	}

	@Override
	public void enter(Visitable segment) {

		if (segment instanceof Match) {
			builder.append("MATCH ");
			childCounter = 0;
			inMatch = true;
		}

		if (segment instanceof Where) {
			builder.append("WHERE ");
		}

		if (segment instanceof Comparison) {
			builder.append(((Comparison) segment).getComparator()).append(" ");
		}

		if (segment instanceof Return) {
			builder.append("RETURN ");
			childCounter = 0;
		}

		if (segment instanceof Property) {
			builder
				.append(((Property) segment).getParentAlias()).append(".")
				.append(((Property) segment).getName()).append(" ");
		}

		if (segment instanceof Literal) {
			builder.append(segment.toString()).append(" ");
		}

		if (segment instanceof Node) {
			Node node = (Node) segment;
			if (childCounter > 0) {
				builder.append(", ");
			}

			if (inMatch) {
				builder.append("(")
					.append(node.getAlias()).append(":").append(node.getLabels().stream().collect(joining(":", "`", "`")))
					.append(")");
			} else {
				builder.append(node.getAlias());
			}
			childCounter++;
		}
	}

	@Override
	public void leave(Visitable segment) {
		if (segment instanceof Match) {
			builder.append(" ");
			inMatch = false;
		}
	}

	@Override
	public CharSequence getRenderedPart() {
		return builder;
	}
}
