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
package org.neo4j.springframework.data.examples.references_and_aggregates.books;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.springframework.data.core.schema.GeneratedValue;
import org.springframework.data.annotation.Id;

/**
 * @author Michael J. Simons
 */
public class PurchaseOrder {
	@Id @GeneratedValue
	private Long id;
	private final String shippingAddress;
	private final Set<OrderItem> items = new HashSet<>();

	public PurchaseOrder(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	PurchaseOrder addItem(String product, int quantity) {

		items.add(createOrderItem(product, quantity));
		return this;
	}

	private static OrderItem createOrderItem(String product, int quantity) {

		OrderItem item = new OrderItem(product, quantity);
		return item;
	}
}
