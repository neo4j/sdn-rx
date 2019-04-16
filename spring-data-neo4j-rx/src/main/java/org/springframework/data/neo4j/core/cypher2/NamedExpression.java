package org.springframework.data.neo4j.core.cypher2;

import org.springframework.data.neo4j.core.cypher2.support.Visitor;

public class NamedExpression implements Named, Expression {

	private final Expression expression;

	private final String symbolicName;

	public NamedExpression(Expression expression, String symbolicName) {

		this.expression = expression;
		this.symbolicName = symbolicName;
	}

	@Override
	public String getSymbolicName() {
		return symbolicName;
	}
}
