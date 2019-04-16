package org.springframework.data.neo4j.core.cypher2.support;

public interface AstNode extends Visitable {

	@Override
	default void accept(Visitor visitor) {

		visitor.enter(this);
		visitor.leave(this);
	}

}
