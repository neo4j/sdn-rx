package org.springframework.data.neo4j.core.cypher2;

import org.springframework.data.neo4j.core.cypher2.StatementBuilder.OngoingMatch;

public class Cypher {

	public static Node node(String alias, String primaryLabel, String... additionalLabels) {

		return Node.create(alias, primaryLabel, additionalLabels);
	}

	public static OngoingMatch match(PatternPart... pattern) {

		return Statement.builder().match(pattern);
	}





}
