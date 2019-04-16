package org.springframework.data.neo4j.core.cypher2;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.data.neo4j.core.cypher2.support.Visitable;
import org.springframework.data.neo4j.core.cypher2.support.Visitor;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
public class Match implements ReadingClause {

	private final Pattern pattern;

	private @Nullable final Where optionalWhere;

	@Override
	public void accept(Visitor visitor) {

		visitor.enter(this);
		this.pattern.accept(visitor);
		visitIfNotNull(optionalWhere, visitor);
		visitor.leave(this);
	}

	private static void visitIfNotNull(@Nullable Visitable visitable, Visitor visitor) {

		if (visitable != null) {
			visitable.accept(visitor);
		}
	}
}
