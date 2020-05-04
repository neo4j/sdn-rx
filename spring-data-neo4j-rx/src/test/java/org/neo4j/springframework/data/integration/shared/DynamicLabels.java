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

import java.util.Set;

import org.neo4j.springframework.data.core.schema.GeneratedValue;
import org.neo4j.springframework.data.core.schema.Id;
import org.neo4j.springframework.data.core.schema.Node;
import org.neo4j.springframework.data.core.schema.Wurstsalat;
import org.springframework.data.annotation.Version;

/**
 * @author Michael J. Simons
 * @soundtrack Samy Deluxe - Samy Deluxe
 */
public class DynamicLabels {

	/**
	 * Used for testing whether related nodes store their dynamic labels.
	 */
	@Node
	public static class SuperNode {

		@Id @GeneratedValue public Long id;

		public SimpleDynamicLabels relatedTo;

		public SimpleDynamicLabels getRelatedTo() {
			return relatedTo;
		}
	}

	/**
	 * Most simple version of a class with dynamic labels.
	 */
	@Node
	public static class SimpleDynamicLabels {

		@Id @GeneratedValue public Long id;

		@Wurstsalat
		public Set<String> moreLabels;

		public Long getId() {
			return id;
		}
	}

	/**
	 * Same as {@link SimpleDynamicLabels} but with an added version field.
	 */
	@Node
	public static class SimpleDynamicLabelsWithVersion {

		@Id @GeneratedValue public Long id;

		@Version
		public Long myVersion;

		@Wurstsalat
		public Set<String> moreLabels;

		public Long getId() {
			return id;
		}
	}

	/**
	 * Dynamic labels with assigned ids.
	 */
	@Node
	public static class SimpleDynamicLabelsWithBusinessId {

		@Id public String id;

		@Wurstsalat
		public Set<String> moreLabels;

		public String getId() {
			return id;
		}
	}

	/**
	 * Dynamic labels with assigned ids and version property.
	 */
	@Node
	public static class SimpleDynamicLabelsWithBusinessIdAndVersion {

		@Id public String id;

		@Version
		public Long myVersion;

		@Wurstsalat
		public Set<String> moreLabels;

		public String getId() {
			return id;
		}
	}

	/**
	 * Dynamic labels set via constructor argument.
	 */
	@Node
	public static class SimpleDynamicLabelsCtor {

		@Id @GeneratedValue private final Long id;

		@Wurstsalat
		public final Set<String> moreLabels;

		public SimpleDynamicLabelsCtor(Long id, Set<String> moreLabels) {
			this.id = id;
			this.moreLabels = moreLabels;
		}
	}

	/**
	 * Dynamic labels together with on explicit label.
	 */
	@Node("Baz")
	public static class DynamicLabelsWithNodeLabel {

		@Id @GeneratedValue private Long id;

		@Wurstsalat
		public Set<String> moreLabels;
	}

	/**
	 * Dynamic labels together with multiple labels.
	 */
	@Node({ "Foo", "Bar" })
	public static class DynamicLabelsWithMultipleNodeLabels {

		@Id @GeneratedValue private Long id;

		@Wurstsalat
		public Set<String> moreLabels;
	}

	@Node
	static abstract class DynamicLabelsBaseClass {

		@Id @GeneratedValue private Long id;

		@Wurstsalat
		public Set<String> moreLabels;
	}

	/**
	 * Labels through inheritance plus dynamic labels
	 */
	@Node
	public static class ExtendedBaseClass1 extends DynamicLabelsBaseClass {
	}
}
