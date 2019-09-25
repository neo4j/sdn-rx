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

/**
 * @author Michael J. Simons
 */
class Neo4jClientExtensionsTest {

	@Test
	fun `RunnableSpec#inDatabase(targetDatabase) extension should call its Java counterpart`() {

		val runnableSpec = mockk<Neo4jClient.RunnableSpec>(relaxed = true)

		runnableSpec.inDatabase("foobar");

		verify(exactly = 1) { runnableSpec.`in`("foobar") }
	}

	@Test
	fun `OngoingDelegation#inDatabase(targetDatabase) extension should call its Java counterpart`() {

		val ongoingDelegation = mockk<Neo4jClient.OngoingDelegation<Any>>(relaxed = true)

		ongoingDelegation.inDatabase("foobar");

		verify(exactly = 1) { ongoingDelegation.`in`("foobar") }
	}

	@Test
	fun `RunnableSpecTightToDatabase#fetchAs() extension should call its Java counterpart`() {

		val runnableSpec = mockk<Neo4jClient.RunnableSpecTightToDatabase>(relaxed = true)

		val mappingSpec: Neo4jClient.RecordFetchSpec<String?, Collection<String>, String> =
				runnableSpec.mappedBy { _, record -> "Foo" };

		verify(exactly = 1) { runnableSpec.fetchAs(String::class.java) }
	}
}
