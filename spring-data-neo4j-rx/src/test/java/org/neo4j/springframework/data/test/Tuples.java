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
package org.neo4j.springframework.data.test;

import lombok.NonNull;
import lombok.Value;

/**
 * See <a href="https://twitter.com/rotnroll666/status/928983026813558784">rotnrolls law</a>.
 *
 * @author Michael J. Simons
 */
public final class Tuples {

	/**
	 * A tuple of 2 values.
	 *
	 * @param <V1> Type of value 1
	 * @param <V2> Type of value 2
	 */
	@Value
	public static final class Tuple2<V1, V2> {

		private @NonNull final V1 v1;

		private @NonNull final V2 v2;
	}

	public static <V1, V2> Tuple2<V1, V2> of(V1 v1, V2 v2) {
		return new Tuple2<>(v1, v2);
	}
}
