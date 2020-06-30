/*
 * Copyright (c) 2019-2020 "Neo4j,"
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
package org.neo4j.springframework.data.repository.config;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.neo4j.springframework.data.repository.config.StartupLogger.Mode;

/**
 * @author Michael J. Simons
 * @soundtrack Helge & Hardcore - Jazz
 */
class StartupLoggerTest {

	@Test
	void startingMessageShouldFit() {

		String message = new StartupLogger(Mode.IMPERATIVE).getStartingMessage();
		assertThat(message).matches(
			"Bootstrapping imperative Neo4j repositories based on an unknown version of SDN\\/RX with Spring Data Commons v2\\.\\d+\\.\\d+.RELEASE and Neo4j Driver v4\\.\\d+\\.\\d+(?:-.*)\\.");
	}
}
