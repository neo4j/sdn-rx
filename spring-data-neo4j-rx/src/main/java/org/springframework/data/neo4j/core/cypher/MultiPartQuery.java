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
import java.util.List;

import org.apiguardian.api.API;
import org.springframework.data.neo4j.core.cypher.Statement.SingleQuery;
import org.springframework.data.neo4j.core.cypher.support.Visitor;

/**
 * See <a href="https://s3.amazonaws.com/artifacts.opencypher.org/M14/railroad/MultiPartQuery.html">MultiPartQuery</a>.
 *
 * @author Michael J. Simons
 * @soundtrack Ferris MC - Ferris MC's Audiobiographie
 * @since 1.0
 */
@API(status = API.Status.INTERNAL, since = "1.0")
public class MultiPartQuery implements SingleQuery {

	private final List<With> withs;

	private final SinglePartQuery remainder;

	public MultiPartQuery(List<With> withs, SinglePartQuery remainder) {
		this.withs = new ArrayList<>(withs);
		this.remainder = remainder;
	}

	@Override
	public void accept(Visitor visitor) {

		withs.forEach(w -> w.accept(visitor));
		remainder.accept(visitor);
	}
}
