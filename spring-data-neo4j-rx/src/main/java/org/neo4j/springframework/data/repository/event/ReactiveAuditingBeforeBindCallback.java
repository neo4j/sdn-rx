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
package org.neo4j.springframework.data.repository.event;

import reactor.core.publisher.Mono;

import org.apiguardian.api.API;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.Ordered;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.data.mapping.callback.EntityCallback;
import org.springframework.util.Assert;

/**
 * Reactive {@link EntityCallback} to populate auditing related fields on an entity about to be bound to a record.
 *
 * @author Michael J. Simons
 * @soundtrack Iron Maiden - The Number Of The Beast
 * @see AuditingBeforeBindCallback
 * @since 1.0
 */
@API(status = API.Status.INTERNAL, since = "1.0")
public final class ReactiveAuditingBeforeBindCallback implements ReactiveBeforeBindCallback<Object>, Ordered {

	public static final int NEO4J_REACTIVE_AUDITING_ORDER = 100;

	private final ObjectFactory<IsNewAwareAuditingHandler> auditingHandlerFactory;

	/**
	 * Creates a new {@link ReactiveAuditingBeforeBindCallback} using the {@link AuditingHandler} provided by the
	 * given {@link ObjectFactory}.
	 *
	 * @param auditingHandlerFactory must not be {@literal null}.
	 */
	public ReactiveAuditingBeforeBindCallback(ObjectFactory<IsNewAwareAuditingHandler> auditingHandlerFactory) {

		Assert.notNull(auditingHandlerFactory, "IsNewAwareAuditingHandler must not be null!");
		this.auditingHandlerFactory = auditingHandlerFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see org.neo4j.springframework.data.repository.event.ReactiveBeforeBindCallback#onBeforeBind(java.lang.Object)
	 */
	@Override
	public Publisher<Object> onBeforeBind(Object entity) {

		return Mono.fromSupplier(() -> auditingHandlerFactory.getObject().markAudited(entity));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	@Override
	public int getOrder() {
		return NEO4J_REACTIVE_AUDITING_ORDER;
	}
}
