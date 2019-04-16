package org.springframework.data.neo4j.core.cypher2;

public interface StatementBuilder {

	OngoingMatch match(PatternPart... pattern);

	interface OngoingMatchAndReturn extends BuildableMatch {

	}

	interface OngoingMatch {

		OngoingMatchAndReturn returning(Expression... expressions);

		OngoingMatchAndReturn returning(Node... nodes);

		OngoingMatch where(Condition name);
	}

	interface BuildableMatch {

		Statement build();
	}
}
