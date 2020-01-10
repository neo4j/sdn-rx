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
package org.neo4j.springframework.data.core.mapping;

import static org.neo4j.springframework.data.core.schema.Relationship.Direction.*;

import java.util.Objects;

import org.neo4j.springframework.data.core.schema.NodeDescription;
import org.neo4j.springframework.data.core.schema.Relationship;
import org.neo4j.springframework.data.core.schema.RelationshipDescription;
import org.springframework.data.mapping.Association;
import org.springframework.lang.Nullable;

/**
 * @author Michael J. Simons
 * @author Gerrit Meier
 * @since 1.0
 */
class DefaultRelationshipDescription extends Association<Neo4jPersistentProperty> implements RelationshipDescription {

	private final String type;

	private final boolean dynamic;

	private final NodeDescription<?> source;

	private final NodeDescription<?> target;

	private final String fieldName;

	private final Relationship.Direction direction;

	private Class<?> relationshipPropertiesClass;

	DefaultRelationshipDescription(Neo4jPersistentProperty inverse,
		Neo4jPersistentProperty obverse,
		String type, boolean dynamic, NodeDescription<?> source, String fieldName, NodeDescription<?> target,
		Relationship.Direction direction, @Nullable Class<?> relationshipPropertiesClass) {

		super(inverse, obverse);

		this.type = type;
		this.dynamic = dynamic;
		this.source = source;
		this.fieldName = fieldName;
		this.target = target;
		this.direction = direction;
		this.relationshipPropertiesClass = relationshipPropertiesClass;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public boolean isDynamic() {
		return dynamic;
	}

	@Override
	public NodeDescription<?>  getTarget() {
		return target;
	}

	@Override
	public NodeDescription<?>  getSource() {
		return source;
	}

	@Override
	public String getFieldName() {
		return fieldName;
	}

	@Override
	public Relationship.Direction getDirection() {
		return direction;
	}

	@Override
	public Class<?> getRelationshipPropertiesClass() {
		return relationshipPropertiesClass;
	}

	@Override
	public boolean hasRelationshipProperties() {
		return getRelationshipPropertiesClass() != null;
	}

	@Override
	public String toString() {
		return "DefaultRelationshipDescription{" +
			"type='" + type + '\'' +
			", source='" + source + '\'' +
			", direction='" + direction + '\'' +
			", target='" + target +
			'}';
	}

	public RelationshipDescription asInverse() {
		return new InvertedRelationshipDescription(type, target, source, direction);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof DefaultRelationshipDescription)) {
			return false;
		}
		DefaultRelationshipDescription that = (DefaultRelationshipDescription) o;
		return getType().equals(that.getType()) && getTarget().equals(that.getTarget())
			&& getSource().equals(that.getSource()) && getDirection().equals(that.getDirection());
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, target, source, direction);
	}

	/**
	 * This class should get only created by a {@link RelationshipDescription} to create an inverted version of
	 * the relationship description for comparison reasons.
	 * Only the fields for equality comparison with a {@link DefaultRelationshipDescription} are implemented and
	 * most of the getters are stubs and will result in an {@link UnsupportedOperationException}.
	 */
	static class InvertedRelationshipDescription extends DefaultRelationshipDescription {

		private static final String ACCESS_EXCEPTION = "Do not try to access stubbed fields";

		private final String type;
		private final NodeDescription<?> source;
		private final NodeDescription<?> target;
		private final Relationship.Direction direction;

		public InvertedRelationshipDescription(String type, NodeDescription<?> source, NodeDescription<?> target,
				Relationship.Direction direction) {

			// To keep the equals / hashCodes just in the DefaultRelationshipDescription we need to extend it.
			// don't judge me
			super(null, null, null, false, null, null, null, null, null);

			this.type = type;
			this.source = source;
			this.target = target;
			this.direction = direction.equals(INCOMING) ? OUTGOING : INCOMING;
		}

		@Override
		public String getType() {
			return type;
		}

		@Override
		public NodeDescription<?> getSource() {
			return source;
		}

		@Override
		public NodeDescription<?> getTarget() {
			return target;
		}

		@Override
		public Relationship.Direction getDirection() {
			return direction;
		}

		// below are unused fields that do not make any sense for the inverse comparison
		@Override
		public boolean isDynamic() {
			throw new UnsupportedOperationException(ACCESS_EXCEPTION);
		}

		@Override
		public String getFieldName() {
			throw new UnsupportedOperationException(ACCESS_EXCEPTION);
		}

		@Override
		public Class<?> getRelationshipPropertiesClass() {
			throw new UnsupportedOperationException(ACCESS_EXCEPTION);
		}

		@Override
		public boolean hasRelationshipProperties() {
			throw new UnsupportedOperationException(ACCESS_EXCEPTION);
		}

		@Override
		public RelationshipDescription asInverse() {
			throw new UnsupportedOperationException(ACCESS_EXCEPTION);
		}
	}
}
