package org.springframework.data.neo4j.core.cypher2;

import lombok.RequiredArgsConstructor;

import org.springframework.data.neo4j.core.cypher2.Statement.SingleQuery;
import org.springframework.data.neo4j.core.cypher2.support.Visitor;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
public class SinglePartQuery implements SingleQuery {

	private @Nullable final ReadingClause readingClause;

	private final Return aReturn;

	@Override
	public void accept(Visitor visitor) {

		if (readingClause != null) {
			readingClause.accept(visitor);
		}

		aReturn.accept(visitor);
	}
}
