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

import org.apiguardian.api.API;
import org.springframework.data.neo4j.core.cypher.support.Visitable;
import org.springframework.data.neo4j.core.cypher.support.Visitor;
import org.springframework.lang.Nullable;

/**
 * See <a href="https://s3.amazonaws.com/artifacts.opencypher.org/M14/railroad/With.html">With</a>.
 *
 * @author Michael J. Simons
 * @soundtrack Ferris MC - Ferris MC's Audiobiographie
 * @since 1.0
 */
@API(status = API.Status.INTERNAL, since = "1.0")
public final class With implements Visitable {

	@Nullable
	private final ReadingClause readingClause;

	private final UpdatingClause updatingClause;

	private final boolean distinct;

	private final ReturnBody body;

	@Nullable
	private final Where where;

	static With createReadingWith(ReadingClause readingClause, boolean distinct, ExpressionList returnItems,
		Order order, Skip skip, Limit limit, @Nullable Where where) {

		return new With(readingClause, null, distinct, returnItems, order, skip, limit, where);
	}

	private With(@Nullable ReadingClause readingClause, @Nullable UpdatingClause updatingClause, boolean distinct,
		ExpressionList returnItems, Order order, Skip skip, Limit limit, @Nullable Where where) {
		this.readingClause = readingClause;
		this.updatingClause = updatingClause;
		this.distinct = distinct;
		this.body = new ReturnBody(returnItems, order, skip, limit);
		this.where = where;
	}

	public boolean isDistinct() {
		return distinct;
	}

	@Override
	public void accept(Visitor visitor) {
		Visitable.visitIfNotNull(readingClause, visitor);
		visitor.enter(this);
		this.body.accept(visitor);
		Visitable.visitIfNotNull(where, visitor);
		visitor.leave(this);
	}
}
