package org.springframework.data.neo4j.core.cypher2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.neo4j.core.cypher2.StatementBuilder.OngoingMatch;
import org.springframework.data.neo4j.core.cypher2.StatementBuilder.OngoingMatchAndReturn;

class DefaultStatementBuilder
	implements StatementBuilder, OngoingMatch, OngoingMatchAndReturn {

	private List<PatternPart> matchList = new ArrayList<>();
	private List<ReturnItem> returnList = new ArrayList<>();
	private Condition where;

	@Override
	public OngoingMatch match(PatternPart... pattern) {

		this.matchList.addAll(Arrays.asList(pattern));
		return this;
	}

	@Override
	public OngoingMatchAndReturn returning(Expression... expressions) {

		this.returnList.addAll(Arrays.asList(expressions));
		return this;
	}

	@Override
	public OngoingMatchAndReturn returning(Node... nodes) {

		this.returnList.addAll(Arrays.asList(nodes).stream()
			.map(node -> node.getSymbolicName().map(Expression.class::cast).orElse(node))
			.collect(Collectors.toList()));
		return this;
	}

	@Override
	public OngoingMatch where(Condition condition) {

		this.where = condition;
		return this;
	}

	@Override
	public Statement build() {

		Pattern pattern = new Pattern(this.matchList);
		Match match = new Match(pattern, this.where == null ? null : new Where(this.where));
		return new SinglePartQuery(match, new Return(returnList));
	}
}
