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
package org.neo4j.springframework.data.test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import reactor.core.publisher.Mono;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.reactive.RxSession;
import org.neo4j.driver.reactive.RxTransaction;

/**
 * Some preconfigured driver mocks, mainly used to for Spring Integration tests where the behaviour of configuration
 * and integration with Spring is tested and not with the database.
 *
 * @author Michael J. Simons
 * @soundtrack Elton John - Greatest Hits 1970-2002
 * @since 1.0
 */
public final class DriverMocks {

	/**
	 * @return An instance usable in a test where an open session with an ongoing transaction is required.
	 */
	public static Driver withOpenSessionAndTransaction() {

		Transaction transaction = mock(Transaction.class);
		when(transaction.isOpen()).thenReturn(true);

		Session session = mock(Session.class);
		when(session.isOpen()).thenReturn(true);
		when(session.beginTransaction(any(TransactionConfig.class))).thenReturn(transaction);

		Driver driver = mock(Driver.class);
		when(driver.session(any(SessionConfig.class))).thenReturn(session);
		return driver;
	}

	public static Driver withOpenReactiveSessionAndTransaction() {

		RxTransaction transaction = mock(RxTransaction.class);

		RxSession session = mock(RxSession.class);
		when(session.beginTransaction(any(TransactionConfig.class))).thenReturn(Mono.just(transaction));

		Driver driver = mock(Driver.class);
		when(driver.rxSession(any(SessionConfig.class))).thenReturn(session);
		return driver;
	}

	private DriverMocks() {
	}
}
