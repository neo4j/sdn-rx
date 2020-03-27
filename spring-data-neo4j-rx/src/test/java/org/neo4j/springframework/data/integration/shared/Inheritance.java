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
package org.neo4j.springframework.data.integration.shared;

import java.util.List;
import java.util.Objects;

import org.neo4j.springframework.data.core.schema.GeneratedValue;
import org.neo4j.springframework.data.core.schema.Id;
import org.neo4j.springframework.data.core.schema.Node;
import org.neo4j.springframework.data.core.schema.Relationship;

/**
 * @author Gerrit Meier
 */
public class Inheritance {


	/**
	 * super base class
	 */
	@Node
	public static abstract class SuperBaseClass {
		@Id @GeneratedValue private Long id;

		public Long getId() {
			return id;
		}
	}

	/**
	 * base class
	 */
	@Node
	public static abstract class BaseClass extends SuperBaseClass {

		private final String name;

		protected BaseClass(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * first concrete implementation
	 */
	@Node
	public static class ConcreteClassA extends BaseClass {

		private final String concreteSomething;

		public ConcreteClassA(String name, String concreteSomething) {
			super(name);
			this.concreteSomething = concreteSomething;
		}

		public String getConcreteSomething() {
			return concreteSomething;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			ConcreteClassA that = (ConcreteClassA) o;
			return concreteSomething.equals(that.concreteSomething);
		}

		@Override
		public int hashCode() {
			return Objects.hash(concreteSomething);
		}
	}

	/**
	 * second concrete implementation
	 */
	@Node
	public static class ConcreteClassB extends BaseClass {

		private final Integer age;

		public ConcreteClassB(String name, Integer age) {
			super(name);
			this.age = age;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			ConcreteClassB that = (ConcreteClassB) o;
			return age.equals(that.age);
		}

		@Override
		public int hashCode() {
			return Objects.hash(age);
		}
	}

	/**
	 * Base class with explicit primary and additional labels.
	 */
	@Node({"LabeledBaseClass", "And_another_one"})
	public static abstract class BaseClassWithLabels {

		@Id @GeneratedValue
		private Long id;
	}

	/**
	 * Class that also has explicit labels
	 */
	@Node({"ExtendingClassA", "And_yet_more_labels"})
	public static class ExtendingClassWithLabelsA extends BaseClassWithLabels {

		private final String name;

		public ExtendingClassWithLabelsA(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			ExtendingClassWithLabelsA that = (ExtendingClassWithLabelsA) o;
			return name.equals(that.name);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name);
		}
	}

	/**
	 * Another class that also has explicit labels
	 */
	@Node({"ExtendingClassB", "And_other_labels"})
	public static class ExtendingClassWithLabelsB extends BaseClassWithLabels {

		private final String somethingElse;

		public ExtendingClassWithLabelsB(String somethingElse) {
			this.somethingElse = somethingElse;
		}

		public String getSomethingElse() {
			return somethingElse;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			ExtendingClassWithLabelsB that = (ExtendingClassWithLabelsB) o;
			return somethingElse.equals(that.somethingElse);
		}

		@Override
		public int hashCode() {
			return Objects.hash(somethingElse);
		}
	}

	/**
	 * Class that has generic relationships
	 */
	@Node
	public static class RelationshipToAbstractClass {

		@Id @GeneratedValue
		private Long id;

		@Relationship("HAS")
		private List<SuperBaseClass> things;

		public void setThings(List<SuperBaseClass> things) {
			this.things = things;
		}

		public List<SuperBaseClass> getThings() {
			return things;
		}
	}
}
