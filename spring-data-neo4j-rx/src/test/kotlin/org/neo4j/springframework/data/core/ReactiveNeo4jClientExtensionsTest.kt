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
package org.neo4j.springframework.data.core

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * @author Michael J. Simons
 */
class ReactiveNeo4jClientExtensionsTest {

	@Test
	fun `RunnableSpec#inDatabase(targetDatabase) extension should call its Java counterpart`() {

		val runnableSpec = mockk<ReactiveNeo4jClient.ReactiveRunnableSpec>(relaxed = true)

		runnableSpec.inDatabase("foobar");

		verify(exactly = 1) { runnableSpec.`in`("foobar") }
	}

	@Test
	fun `OngoingDelegation#inDatabase(targetDatabase) extension should call its Java counterpart`() {

		val ongoingDelegation = mockk<ReactiveNeo4jClient.OngoingReactiveDelegation<Any>>(relaxed = true)

		ongoingDelegation.inDatabase("foobar");

		verify(exactly = 1) { ongoingDelegation.`in`("foobar") }
	}

	@Test
	fun `ReactiveRunnableDelegation#fetchAs() extension should call its Java counterpart`() {

		val runnableSpec = mockk<ReactiveNeo4jClient.ReactiveRunnableSpecTightToDatabase>(relaxed = true)

		val mappingSpec : Neo4jClient.MappingSpec<Mono<String>, Flux<String>, String> =
				runnableSpec.fetchAs();

		verify(exactly = 1) { runnableSpec.fetchAs(String::class.java) }
	}
}
