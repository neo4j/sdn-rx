package org.springframework.data.neo4j.core.cypher2;

import org.springframework.data.neo4j.core.cypher2.support.AstNode;
import org.springframework.data.neo4j.core.cypher2.support.Visitor;

public class Where implements AstNode {

	private final Condition condition;

	public Where(Condition condition) {
		this.condition = condition;
	}

	@Override
	public void accept(Visitor visitor) {

		visitor.enter(this);

		this.condition.accept(visitor);

		visitor.leave(this);
	}
}
