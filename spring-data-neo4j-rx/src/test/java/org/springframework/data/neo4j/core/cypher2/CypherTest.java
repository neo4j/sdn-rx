package org.springframework.data.neo4j.core.cypher2;

import static java.util.stream.Collectors.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.neo4j.core.cypher2.renderer.RenderUtils;
import org.springframework.data.neo4j.core.cypher2.support.ReflectiveVisitor;
import org.springframework.data.neo4j.core.cypher2.support.TypedSubtree;
import org.springframework.data.neo4j.core.cypher2.support.Visitable;

public class CypherTest {

	@Nested
	class SingleQuerySinglePart {

		@Test
		void readingAndReturn() {

			Node bikeNode = Cypher.node("n", "Bike");
			Node userNode = Cypher.node("u", "User");

			Statement statement = Cypher.match(bikeNode, userNode, Cypher.node("o", "U"))
				.where(userNode.property("name").matches(".*aName"))
				.returning(bikeNode, userNode)
				.build();

			X x = new X();
			statement.accept(x);
			System.out.println(x.builder);
		}
	}

	public static class X extends ReflectiveVisitor {

		StringBuilder builder = new StringBuilder();

		String separator = null;

		boolean needsSeparator = false;

		void enableSeparator(boolean on) {
			this.needsSeparator = on;
			this.separator = null;
		}

		@Override
		public void preEnter(Visitable visitable) {

			if (visitable instanceof TypedSubtree) {
				enableSeparator(true);
			}

			if (needsSeparator && separator != null) {
				builder.append(separator);
				separator = null;
			}
		}

		@Override
		public void postLeave(Visitable visitable) {

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
				.append(node.getLabels().stream().map(RenderUtils::escape).collect(joining(":")))
				.append(")");
		}

		void enter(SymbolicName symbolicName) {
			builder.append(symbolicName.getName());
		}
	}
}
