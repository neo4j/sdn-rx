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
package org.neo4j.springframework.data.integration.imperative;

import static org.assertj.core.api.Assertions.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Value;
import org.neo4j.springframework.data.config.AbstractNeo4jConfig;
import org.neo4j.springframework.data.core.Neo4jTemplate;
import org.neo4j.springframework.data.integration.shared.DynamicLabels.DynamicLabelsWithMultipleNodeLabels;
import org.neo4j.springframework.data.integration.shared.DynamicLabels.DynamicLabelsWithNodeLabel;
import org.neo4j.springframework.data.integration.shared.DynamicLabels.ExtendedBaseClass1;
import org.neo4j.springframework.data.integration.shared.DynamicLabels.SimpleDynamicLabels;
import org.neo4j.springframework.data.integration.shared.DynamicLabels.SimpleDynamicLabelsCtor;
import org.neo4j.springframework.data.integration.shared.DynamicLabels.SimpleDynamicLabelsWithBusinessId;
import org.neo4j.springframework.data.integration.shared.DynamicLabels.SimpleDynamicLabelsWithBusinessIdAndVersion;
import org.neo4j.springframework.data.integration.shared.DynamicLabels.SimpleDynamicLabelsWithVersion;
import org.neo4j.springframework.data.integration.shared.DynamicLabels.SuperNode;
import org.neo4j.springframework.data.test.Neo4jExtension;
import org.neo4j.springframework.data.test.Neo4jIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Michael J. Simons
 * @soundtrack Samy Deluxe - Samy Deluxe
 */
@Neo4jIntegrationTest
public class DynamicLabelsIT {

	protected static Neo4jExtension.Neo4jConnectionSupport neo4jConnectionSupport;

	protected final Driver driver;

	protected long idOfSimpleDynamicLabelsEntity;

	protected long idOfSimpleDynamicLabelsEntityVersioned;

	protected long idOfSimpleDynamicLabelsCtorEntity;

	protected long idOfExtendedBaseClass1;

	protected DynamicLabelsIT(@Autowired Driver driver) {
		this.driver = driver;
	}

	@BeforeEach
	protected void setupData() {
		try (Session session = driver.session(); Transaction transaction = session.beginTransaction()) {
			transaction.run("MATCH (n) detach delete n");
			idOfSimpleDynamicLabelsEntity = transaction.run(""
				+ "CREATE (t:SimpleDynamicLabels:Foo:Bar:Baz:Foobar) "
				+ "RETURN id(t) as id").single().get("id").asLong();
			idOfSimpleDynamicLabelsEntityVersioned = transaction.run(""
				+ "CREATE (t:SimpleDynamicLabelsWithVersion:Foo:Bar:Baz:Foobar {myVersion: 0}) "
				+ "RETURN id(t) as id").single().get("id").asLong();
			transaction.run(""
				+ "CREATE (t:SimpleDynamicLabelsWithBusinessId:Foo:Bar:Baz:Foobar {id: 'E1'}) "
				+ "RETURN id(t) as id").single().get("id").asLong();
			transaction.run(""
				+ "CREATE (t:SimpleDynamicLabelsWithBusinessIdAndVersion:Foo:Bar:Baz:Foobar {id: 'E2', myVersion: 0}) "
				+ "RETURN id(t) as id").single().get("id").asLong();
			idOfSimpleDynamicLabelsCtorEntity = transaction.run(""
				+ "CREATE (t:SimpleDynamicLabelsCtor:Foo:Bar:Baz:Foobar) "
				+ "RETURN id(t) as id").single().get("id").asLong();
			idOfExtendedBaseClass1 = transaction.run(""
				+ "CREATE (t:DynamicLabelsBaseClass:ExtendedBaseClass1:D1:D2:D3) "
				+ "RETURN id(t) as id").single().get("id").asLong();
			transaction.commit();
		}
	}

	@Test
	void shouldReadDynamicLabels(@Autowired Neo4jTemplate template) {

		Optional<SimpleDynamicLabels> optionalEntity = template
			.findById(idOfSimpleDynamicLabelsEntity, SimpleDynamicLabels.class);
		assertThat(optionalEntity).hasValueSatisfying(entity ->
			assertThat(entity.moreLabels).containsExactlyInAnyOrder("Foo", "Bar", "Baz", "Foobar")
		);
	}

	@Test
	void shouldWriteDynamicLabels(@Autowired Neo4jTemplate template) {

		SimpleDynamicLabels entity = template.findById(idOfSimpleDynamicLabelsEntity, SimpleDynamicLabels.class).get();
		entity.moreLabels.remove("Foo");
		entity.moreLabels.add("Fizz");
		template.save(entity);
		try (Session session = driver.session()) {
			List<String> labels = session
				.readTransaction(tx -> tx.run("MATCH (n) WHERE id(n) = $id AND NOT EXISTS(n.moreLabels) RETURN labels(n) AS labels",
					Collections.singletonMap("id", idOfSimpleDynamicLabelsEntity))
					.single().get("labels")
					.asList(Value::asString)
				);
			assertThat(labels).containsExactlyInAnyOrder("SimpleDynamicLabels", "Fizz", "Bar", "Baz", "Foobar");
		}
	}

	@Test
	void shouldWriteDynamicLabels2(@Autowired Neo4jTemplate template) {

		SimpleDynamicLabelsWithBusinessId entity = template.findById("E1", SimpleDynamicLabelsWithBusinessId.class).get();
		entity.moreLabels.remove("Foo");
		entity.moreLabels.add("Fizz");
		template.save(entity);
		try (Session session = driver.session()) {
			List<String> labels = session
				.readTransaction(tx -> tx.run("MATCH (n) WHERE n.id = $id AND NOT EXISTS(n.moreLabels) RETURN labels(n) AS labels",
					Collections.singletonMap("id", entity.id))
					.single().get("labels")
					.asList(Value::asString)
				);
			assertThat(labels).containsExactlyInAnyOrder("SimpleDynamicLabelsWithBusinessId", "Fizz", "Bar", "Baz", "Foobar");
		}
	}

	@Test
	void shouldWriteNewDynamicLabels(@Autowired Neo4jTemplate template) {

		SimpleDynamicLabels entity = new SimpleDynamicLabels();
		entity.moreLabels = new HashSet<>();
		entity.moreLabels.add("A");
		entity.moreLabels.add("B");
		entity.moreLabels.add("C");
		long id = template.save(entity).id;
		try (Session session = driver.session()) {
			List<String> labels = session
				.readTransaction(tx -> tx.run("MATCH (n) WHERE id(n) = $id AND NOT EXISTS(n.moreLabels) RETURN labels(n) AS labels",
					Collections.singletonMap("id", id))
					.single().get("labels")
					.asList(Value::asString)
				);
			assertThat(labels).containsExactlyInAnyOrder("SimpleDynamicLabels", "A", "B", "C");
		}
	}

	@Test
	void shouldWriteNewDynamicLabels2(@Autowired Neo4jTemplate template) {

		SimpleDynamicLabelsWithBusinessId entity = new SimpleDynamicLabelsWithBusinessId();
		entity.id = UUID.randomUUID().toString();
		entity.moreLabels = new HashSet<>();
		entity.moreLabels.add("A");
		entity.moreLabels.add("B");
		entity.moreLabels.add("C");
		template.save(entity);
		try (Session session = driver.session()) {
			List<String> labels = session
				.readTransaction(
					tx -> tx.run("MATCH (n) WHERE n.id = $id AND NOT EXISTS(n.moreLabels) RETURN labels(n) AS labels",
						Collections.singletonMap("id", entity.id))
						.single().get("labels")
						.asList(Value::asString)
				);
			assertThat(labels).containsExactlyInAnyOrder("SimpleDynamicLabelsWithBusinessId", "A", "B", "C");
		}
	}

	@Test
	void shouldWriteNewDynamicLabels3(@Autowired Neo4jTemplate template) {

		SimpleDynamicLabels entity = new SimpleDynamicLabels();
		entity.moreLabels = new HashSet<>();
		entity.moreLabels.add("A");
		entity.moreLabels.add("B");
		entity.moreLabels.add("C");
		SuperNode superNode = new SuperNode();
		superNode.relatedTo = entity;
		long id = template.save(superNode).relatedTo.id;
		try (Session session = driver.session()) {
			List<String> labels = session
				.readTransaction(
					tx -> tx.run("MATCH (n) WHERE id(n) = $id AND NOT EXISTS(n.moreLabels) RETURN labels(n) AS labels",
						Collections.singletonMap("id", id))
						.single().get("labels")
						.asList(Value::asString)
				);
			assertThat(labels).containsExactlyInAnyOrder("SimpleDynamicLabels", "A", "B", "C");
		}
	}

	@Test
	void shouldWriteDynamicLabelsWithVersions(@Autowired Neo4jTemplate template) {

		SimpleDynamicLabelsWithVersion entity = template
			.findById(idOfSimpleDynamicLabelsEntityVersioned, SimpleDynamicLabelsWithVersion.class).get();
		entity.moreLabels.remove("Foo");
		entity.moreLabels.add("Fizz");
		entity = template.save(entity);
		assertThat(entity.myVersion).isNotNull().isEqualTo(1);
		try (Session session = driver.session()) {
			List<String> labels = session
				.readTransaction(tx -> tx.run("MATCH (n) WHERE id(n) = $id AND NOT EXISTS(n.moreLabels) RETURN labels(n) AS labels",
					Collections.singletonMap("id", idOfSimpleDynamicLabelsEntityVersioned))
					.single().get("labels")
					.asList(Value::asString)
				);
			assertThat(labels).containsExactlyInAnyOrder("SimpleDynamicLabelsWithVersion", "Fizz", "Bar", "Baz", "Foobar");
		}
	}

	@Test
	void shouldWriteDynamicLabelsWithVersions2(@Autowired Neo4jTemplate template) {

		SimpleDynamicLabelsWithBusinessIdAndVersion entity = template.findById("E2", SimpleDynamicLabelsWithBusinessIdAndVersion.class).get();
		entity.moreLabels.remove("Foo");
		entity.moreLabels.add("Fizz");
		entity = template.save(entity);
		assertThat(entity.myVersion).isNotNull().isEqualTo(1);
		try (Session session = driver.session()) {
			List<String> labels = session
				.readTransaction(tx -> tx.run("MATCH (n) WHERE n.id = $id AND NOT EXISTS(n.moreLabels) RETURN labels(n) AS labels",
					Collections.singletonMap("id", "E2"))
					.single().get("labels")
					.asList(Value::asString)
				);
			assertThat(labels).containsExactlyInAnyOrder("SimpleDynamicLabelsWithBusinessIdAndVersion", "Fizz", "Bar", "Baz", "Foobar");
		}
	}

	@Test
	void shouldWriteNewDynamicLabelsWithVersions(@Autowired Neo4jTemplate template) {

		SimpleDynamicLabelsWithVersion entity = new SimpleDynamicLabelsWithVersion();
		entity.moreLabels = new HashSet<>();
		entity.moreLabels.add("A");
		entity.moreLabels.add("B");
		entity.moreLabels.add("C");
		entity = template.save(entity);
		assertThat(entity.myVersion).isNotNull().isEqualTo(0);
		long id = entity.id;
		try (Session session = driver.session()) {
			List<String> labels = session
				.readTransaction(tx -> tx.run("MATCH (n) WHERE id(n) = $id AND NOT EXISTS(n.moreLabels) RETURN labels(n) AS labels",
					Collections.singletonMap("id", id))
					.single().get("labels")
					.asList(Value::asString)
				);
			assertThat(labels).containsExactlyInAnyOrder("SimpleDynamicLabelsWithVersion", "A", "B", "C");
		}
	}

	@Test
	void shouldWriteNewDynamicLabelsWithVersions2(@Autowired Neo4jTemplate template) {

		SimpleDynamicLabelsWithBusinessIdAndVersion entity = new SimpleDynamicLabelsWithBusinessIdAndVersion();
		String id = UUID.randomUUID().toString();
		entity.id = id;
		entity.moreLabels = new HashSet<>();
		entity.moreLabels.add("A");
		entity.moreLabels.add("B");
		entity.moreLabels.add("C");
		entity = template.save(entity);
		assertThat(entity.myVersion).isNotNull().isEqualTo(0);
		try (Session session = driver.session()) {
			List<String> labels = session
				.readTransaction(tx -> tx.run("MATCH (n) WHERE n.id = $id AND NOT EXISTS(n.moreLabels) RETURN labels(n) AS labels",
					Collections.singletonMap("id", id))
					.single().get("labels")
					.asList(Value::asString)
				);
			assertThat(labels).containsExactlyInAnyOrder("SimpleDynamicLabelsWithBusinessIdAndVersion", "A", "B", "C");
		}
	}

	@Test
	void shouldReadDynamicLabelsCtor(@Autowired Neo4jTemplate template) {

		Optional<SimpleDynamicLabelsCtor> optionalEntity = template
			.findById(idOfSimpleDynamicLabelsCtorEntity, SimpleDynamicLabelsCtor.class);
		assertThat(optionalEntity).hasValueSatisfying(entity ->
			assertThat(entity.moreLabels).containsExactlyInAnyOrder("Foo", "Bar", "Baz", "Foobar")
		);
	}

	@Test
	void shouldReadDynamicLabelsWithNodeLabels(@Autowired Neo4jTemplate template) {

		Optional<DynamicLabelsWithNodeLabel> optionalEntity = template
			.findById(idOfSimpleDynamicLabelsEntity, DynamicLabelsWithNodeLabel.class);
		assertThat(optionalEntity).hasValueSatisfying(entity ->
			assertThat(entity.moreLabels).containsExactlyInAnyOrder("SimpleDynamicLabels", "Foo", "Bar", "Foobar")
		);
	}

	@Test
	void shouldReadDynamicLabelsWithMultipleNodeLabels(@Autowired Neo4jTemplate template) {

		Optional<DynamicLabelsWithMultipleNodeLabels> optionalEntity = template
			.findById(idOfSimpleDynamicLabelsEntity, DynamicLabelsWithMultipleNodeLabels.class);
		assertThat(optionalEntity).hasValueSatisfying(entity ->
			assertThat(entity.moreLabels).containsExactlyInAnyOrder("SimpleDynamicLabels", "Baz", "Foobar")
		);
	}

	@Test
	void shouldReadDynamicLabelsInInheritance(@Autowired Neo4jTemplate template) {

		Optional<ExtendedBaseClass1> optionalEntity = template
			.findById(idOfExtendedBaseClass1, ExtendedBaseClass1.class);
		assertThat(optionalEntity).hasValueSatisfying(entity ->
			assertThat(entity.moreLabels).containsExactlyInAnyOrder("D1", "D2", "D3")
		);
	}

	@Configuration
	@EnableTransactionManagement
	static class Config extends AbstractNeo4jConfig {

		@Bean
		public Driver driver() {
			return neo4jConnectionSupport.getDriver();
		}

		@Override
		protected Collection<String> getMappingBasePackages() {
			return Collections.singletonList("org.neo4j.springframework.data.integration.shared.f");
		}
	}
}
