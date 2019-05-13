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
package org.springframework.data.neo4j.core.cypher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.neo4j.core.cypher.ReadingClause.MatchList;
import org.springframework.data.neo4j.core.cypher.StatementBuilder.OngoingMatch;
import org.springframework.data.neo4j.core.cypher.StatementBuilder.OngoingMatchWithWhere;
import org.springframework.data.neo4j.core.cypher.StatementBuilder.OngoingMatchWithoutWhere;
import org.springframework.util.Assert;

/**
 * @author Michael J. Simons
 * @author Gerrit Meier
 * @since 1.0
 */
class DefaultStatementBuilder
	implements StatementBuilder,
	OngoingMatch,
	OngoingMatchWithWhere,
	OngoingMatchWithoutWhere {

	/**
	 * Current list of matches to be generated.
	 */
	private final List<DefaultMatchBuilder> matchesList = new ArrayList<>();

	/**
	 * The latest ongoing match,
	 */
	private DefaultMatchBuilder currentOngoingMatch;

	/**
	 * A list of already build withs.
	 */
	private final List<With> withList = new ArrayList<>();

	@Override
	public OngoingMatchWithoutWhere optionalMatch(PatternElement... pattern) {

		return this.match(true, pattern);
	}

	@Override
	public OngoingMatchWithoutWhere match(PatternElement... pattern) {

		return this.match(false, pattern);
	}

	private OngoingMatchWithoutWhere match(boolean optional, PatternElement... pattern) {

		Assert.notNull(pattern, "Patterns to match are required.");
		Assert.notEmpty(pattern, "At least one pattern to match is required.");

		if (this.currentOngoingMatch != null) {
			this.matchesList.add(this.currentOngoingMatch);
		}
		this.currentOngoingMatch = new DefaultMatchBuilder(optional);
		this.currentOngoingMatch.matchList.addAll(Arrays.asList(pattern));
		return this;
	}

	@Override
	public OngoingMatchAndReturn returning(Expression... expressions) {

		DefaultStatementWithReturnBuilder ongoingMatchAndReturn = new DefaultStatementWithReturnBuilder(false);
		ongoingMatchAndReturn.addExpressions(expressions);
		return ongoingMatchAndReturn;
	}

	@Override
	public OngoingMatchAndReturn returningDistinct(Expression... expressions) {

		DefaultStatementWithReturnBuilder ongoingMatchAndReturn = new DefaultStatementWithReturnBuilder(true);
		ongoingMatchAndReturn.addExpressions(expressions);
		return ongoingMatchAndReturn;
	}

	@Override
	public OngoingMatchAndWithWithoutWhere with(Expression... expressions) {

		return with(false, expressions);
	}

	@Override
	public OngoingMatchAndWithWithoutWhere withDistinct(Expression... expressions) {

		return with(true, expressions);
	}

	private OngoingMatchAndWithWithoutWhere with(boolean distinct, Expression... expressions) {

		DefaultStatementWithWithBuilder ongoingMatchAndWith = new DefaultStatementWithWithBuilder(distinct,
			buildMatchList().orElse(null), null);
		ongoingMatchAndWith.addExpressions(expressions);
		return ongoingMatchAndWith;
	}


	@Override
	public OngoingMatchAndDelete delete(Expression... expressions) {

		return new DefaultStatementWithDeleteBuilder(false, expressions);
	}

	@Override
	public OngoingMatchAndDelete detachDelete(Expression... expressions) {

		return new DefaultStatementWithDeleteBuilder(true, expressions);
	}

	@Override
	public OngoingMatchWithWhere where(Condition newCondition) {

		this.currentOngoingMatch.conditionBuilder.where(newCondition);
		return this;
	}

	@Override
	public OngoingMatchWithWhere and(Condition additionalCondition) {

		this.currentOngoingMatch.conditionBuilder.and(additionalCondition);
		return this;
	}

	@Override
	public OngoingMatchWithWhere or(Condition additionalCondition) {

		this.currentOngoingMatch.conditionBuilder.or(additionalCondition);
		return this;
	}

	protected Optional<MatchList> buildMatchList() {
		List<Match> completeMatchesList = Stream.concat(this.matchesList.stream(),
			this.currentOngoingMatch == null ? Stream.empty() : Stream.of(this.currentOngoingMatch))
			.map(DefaultMatchBuilder::buildMatch)
			.collect(Collectors.toList());

		this.currentOngoingMatch = null;
		this.matchesList.clear();
		return completeMatchesList.isEmpty() ? Optional.empty() : Optional.of(new MatchList(completeMatchesList));
	}

	DefaultStatementBuilder addFinishedWith(With with) {

		withList.add(with);
		return this;
	}


	class DefaultStatementWithReturnBuilder
		implements OngoingMatchAndReturn, OngoingOrderDefinition, OngoingMatchAndReturnWithOrder {

		protected final List<Expression> returnList = new ArrayList<>();
		protected final List<SortItem> sortItemList = new ArrayList<>();
		protected boolean distinct;
		protected SortItem lastSortItem;
		protected Skip skip;
		protected Limit limit;

		DefaultStatementWithReturnBuilder(boolean distinct) {
			this.distinct = distinct;
		}

		protected final DefaultStatementWithReturnBuilder addExpressions(Expression... expressions) {

			Assert.notNull(expressions, "Expressions to return are required.");
			Assert.notEmpty(expressions, "At least one expressions to return is required.");

			this.returnList.addAll(Arrays.asList(expressions));
			return this;
		}

		@Override
		public final OngoingMatchAndReturn orderBy(SortItem... sortItem) {
			Arrays.stream(sortItem).forEach(this.sortItemList::add);
			return this;
		}

		@Override
		public final OngoingOrderDefinition orderBy(Expression expression) {
			this.lastSortItem = Cypher.sort(expression);
			return this;
		}

		@Override
		public final OngoingOrderDefinition and(Expression expression) {
			return orderBy(expression);
		}

		@Override
		public final OngoingMatchAndReturn descending() {
			this.sortItemList.add(this.lastSortItem.descending());
			this.lastSortItem = null;
			return this;
		}

		@Override
		public final OngoingMatchAndReturn ascending() {
			this.sortItemList.add(this.lastSortItem.ascending());
			this.lastSortItem = null;
			return this;
		}

		@Override
		public final OngoingMatchAndReturn skip(Number number) {
			skip = Skip.of(number);
			return this;
		}

		@Override
		public final OngoingMatchAndReturn limit(Number number) {
			limit = Limit.of(number);
			return this;
		}

		protected final Optional<Return> buildReturn() {

			if (returnList.isEmpty()) {
				return Optional.empty();
			}

			ExpressionList returnItems = new ExpressionList(this.returnList);

			if (lastSortItem != null) {
				sortItemList.add(lastSortItem);
			}
			Order order = sortItemList.size() > 0 ? new Order(sortItemList) : null;
			return Optional.of(new Return(distinct, returnItems, order, skip, limit));
		}

		@Override
		public Statement build() {

			// This must be filled at this stage
			Return aReturn = buildReturn().get();
			// The match list can be null (only return or the remainder of a with)
			Optional<MatchList> optionalMatchList = buildMatchList();

			SinglePartQuery singlePartQuery = SinglePartQuery
				.createReturningQuery(aReturn, optionalMatchList.orElse(null));

			if (withList.isEmpty()) {
				return singlePartQuery;
			} else {
				return new MultiPartQuery(withList, singlePartQuery);
			}
		}
	}

	class DefaultStatementWithWithBuilder extends DefaultStatementWithReturnBuilder
		implements OngoingMatchAndWithWithoutWhere, OngoingMatchAndWithWithWhere {

		private final DefaultConditionBuilder conditionBuilder = new DefaultConditionBuilder();

		private final ReadingClause readingClause;

		private final UpdatingClause updatingClause;

		DefaultStatementWithWithBuilder(boolean distinct, ReadingClause readingClause, UpdatingClause updatingClause) {
			super(distinct);
			this.readingClause = readingClause;
			this.updatingClause = updatingClause;
		}

		protected final With buildWith() {

			ExpressionList returnItems = new ExpressionList(super.returnList);

			if (lastSortItem != null) {
				sortItemList.add(lastSortItem);
			}
			Order order = sortItemList.size() > 0 ? new Order(sortItemList) : null;
			Where where = conditionBuilder.buildCondition().map(Where::new).orElse(null);

			if (readingClause != null) {
				return With.createReadingWith(readingClause, distinct, returnItems, order, skip, limit, where);
			}
			throw new UnsupportedOperationException();
		}

		@Override
		public OngoingMatchAndReturn returning(Expression... expressions) {

			return DefaultStatementBuilder.this
				.addFinishedWith(buildWith())
				.returning(expressions);
		}

		@Override
		public OngoingMatchAndReturn returningDistinct(Expression... expressions) {

			return DefaultStatementBuilder.this
				.addFinishedWith(buildWith())
				.returning(expressions);
		}

		@Override
		public OngoingMatchAndDelete delete(Expression... expressions) {

			return DefaultStatementBuilder.this
				.addFinishedWith(buildWith())
				.delete(expressions);
		}

		@Override
		public OngoingMatchAndDelete detachDelete(Expression... expressions) {

			return DefaultStatementBuilder.this
				.addFinishedWith(buildWith())
				.detachDelete(expressions);
		}

		public OngoingMatchAndWithWithoutWhere with(Expression... expressions) {

			return DefaultStatementBuilder.this
				.addFinishedWith(buildWith())
				.with(expressions);
		}

		@Override
		public OngoingMatchAndWithWithoutWhere withDistinct(Expression... expressions) {

			return DefaultStatementBuilder.this
				.addFinishedWith(buildWith())
				.withDistinct(expressions);
		}

		@Override
		public OngoingMatchAndWithWithWhere where(Condition newCondition) {

			this.conditionBuilder.where(newCondition);
			return this;
		}

		@Override
		public OngoingMatchAndWithWithWhere and(Condition additionalCondition) {

			this.conditionBuilder.and(additionalCondition);
			return this;
		}

		@Override
		public OngoingMatchAndWithWithWhere or(Condition additionalCondition) {

			this.conditionBuilder.or(additionalCondition);
			return this;
		}

		@Override
		public OngoingMatchWithoutWhere match(PatternElement... pattern) {

			return DefaultStatementBuilder.this
				.addFinishedWith(buildWith())
				.match(pattern);
		}

		@Override
		public OngoingMatchWithoutWhere optionalMatch(PatternElement... pattern) {

			return DefaultStatementBuilder.this
				.addFinishedWith(buildWith())
				.optionalMatch(pattern);
		}
	}

	class DefaultStatementWithDeleteBuilder extends DefaultStatementWithReturnBuilder
		implements OngoingMatchAndDelete {

		private final List<Expression> deleteList;
		private final boolean detach;

		DefaultStatementWithDeleteBuilder(boolean detach, Expression... expressions) {
			super(false);
			this.detach = detach;

			Assert.notNull(expressions, "Expressions to delete are required.");
			Assert.notEmpty(expressions, "At least one expressions to delete is required.");

			this.deleteList = Arrays.asList(expressions);
		}

		@Override
		public OngoingMatchAndReturn returning(Expression... expressions) {

			Assert.notNull(expressions, "Expressions to return are required.");
			Assert.notEmpty(expressions, "At least one expressions to return is required.");

			super.returnList.addAll(Arrays.asList(expressions));
			return this;
		}

		@Override
		public OngoingMatchAndReturn returningDistinct(Expression... expressions) {

			returning(expressions);
			super.distinct = true;
			return this;
		}

		protected final Delete buildDelete() {

			ExpressionList deleteItems = new ExpressionList(this.deleteList);
			return new Delete(deleteItems, this.detach);
		}

		@Override
		public Statement build() {

			MatchList matchList = buildMatchList().get();
			Delete delete = buildDelete();
			Optional<Return> optionalReturn = buildReturn();

			return SinglePartQuery.createUpdatingQuery(matchList, delete, optionalReturn.orElse(null));
		}

		@Override
		public OngoingMatchAndWithWithoutWhere with(Expression... expressions) {
			return null;
		}

		@Override
		public OngoingMatchAndWithWithoutWhere withDistinct(Expression... expressions) {
			return null;
		}
	}

	static class DefaultMatchBuilder {

		private final List<PatternElement> matchList = new ArrayList<>();

		private final DefaultConditionBuilder conditionBuilder = new DefaultConditionBuilder();

		private final boolean optional;

		DefaultMatchBuilder(boolean optional) {
			this.optional = optional;
		}

		Match buildMatch() {
			Pattern pattern = new Pattern(this.matchList);
			return new Match(optional, pattern, conditionBuilder.buildCondition().map(Where::new).orElse(null));
		}
	}

	static final class DefaultConditionBuilder {
		protected Condition condition;

		void where(Condition newCondition) {

			this.condition = newCondition;
		}

		void and(Condition additionalCondition) {

			this.condition = this.condition.and(additionalCondition);
		}

		void or(Condition additionalCondition) {

			this.condition = this.condition.or(additionalCondition);
		}

		private boolean hasCondition() {
			return !(this.condition == null || this.condition == CompoundCondition.EMPTY_CONDITION);
		}

		Optional<Condition> buildCondition() {
			return hasCondition() ? Optional.of(this.condition) : Optional.empty();
		}
	}

}
