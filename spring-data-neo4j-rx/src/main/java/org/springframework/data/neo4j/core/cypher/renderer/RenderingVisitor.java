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
import org.springframework.data.neo4j.core.cypher.Match;
import org.springframework.data.neo4j.core.cypher.Node;
import org.springframework.data.neo4j.core.cypher.Property;
import org.springframework.data.neo4j.core.cypher.Return;
import org.springframework.data.neo4j.core.cypher.StringLiteral;
import org.springframework.data.neo4j.core.cypher.SymbolicName;
import org.springframework.data.neo4j.core.cypher.Where;
import org.springframework.data.neo4j.core.cypher.support.ReflectiveVisitor;
import org.springframework.data.neo4j.core.cypher.support.TypedSubtree;
import org.springframework.data.neo4j.core.cypher.support.Visitable;

/**
 * This is a simple (some would call it naive) implementation of a visitor to the Cypher AST created by the Cypher builder
 * based on the {@link ReflectiveVisitor reflective visitor}.
 * <p/>
 * It takes care of separating elements of sub trees containing the element type with a separator and provides pairs of
 * {@code enter} / {@code leave} for the structuring elements of the Cypher AST as needed.
 * <p/>
 * This rendering visitor is not meant to be used outside framework code and we don't give any guarantees on the format
 * being output apart from that it works within the constraints of SDN-RX.
 *
 * @author Michael J. Simons
 */
public class RenderingVisitor extends ReflectiveVisitor {

	StringBuilder builder = new StringBuilder();

	String separator = null;

	boolean needsSeparator = false;

	void enableSeparator(boolean on) {
		this.needsSeparator = on;
		this.separator = null;
	}

	@Override
	protected void preEnter(Visitable visitable) {

		if (visitable instanceof TypedSubtree) {
			enableSeparator(true);
		}

		if (needsSeparator && separator != null) {
			builder.append(separator);
			separator = null;
		}
	}

	@Override
	protected void postLeave(Visitable visitable) {

		if (needsSeparator) {
			separator = ", ";
		}

		if (visitable instanceof TypedSubtree) {
			enableSeparator(false);
		}
	}

	void enter(Match match) {
		builder.append("MATCH ");
	}

	void leave(Match match) {
		builder.append(" ");
	}

	void enter(Where where) {
		builder.append(" WHERE ");
	}

	void enter(Return returning) {
		builder.append("RETURN ");
	}

	void enter(Property property) {
		builder
			.append(property.getParentAlias())
			.append(".")
			.append(property.getName());
	}

	void enter(Comparison comparison) {
		builder
			.append(" ")
			.append(comparison.getComparator())
			.append(" ");
	}

	void enter(StringLiteral expression) {
		builder.append(expression.toString());
	}

	void enter(Node node) {
		builder.append("(")
			.append(node.getSymbolicName().map(SymbolicName::getName).orElse(""))
			.append(":")
			.append(node.getLabels().stream().map(RenderUtils::escapeName).collect(joining(":")))
			.append(")");
	}

	void enter(SymbolicName symbolicName) {
		builder.append(symbolicName.getName());
	}

	public String getRenderedContent() {
		return this.builder.toString();
	}
}

