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
package org.neo4j.springframework.data.core.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apiguardian.api.API;
import org.neo4j.springframework.data.core.cypher.Expression;
import org.springframework.lang.Nullable;

/**
 * Describes how a class is mapped to a node inside the database. It provides navigable links to relationships and
 * access to the nodes properties.
 *
 * @param <T> The type of the underlying class
 * @author Michael J. Simons
 * @since 1.0
 */
@API(status = API.Status.INTERNAL, since = "1.0")
public interface NodeDescription<T> {

	/**
	 * @return The primary label of this entity inside Neo4j.
	 */
	String getPrimaryLabel();

	/**
	 * @return the list of all additional labels (All labels except the {@link NodeDescription#getPrimaryLabel()}.
	 */
	List<String> getAdditionalLabels();

	/**
	 * @return The list of all static labels, that is the union of {@link #getPrimaryLabel()} + {@link #getAdditionalLabels()}.
	 * Order is guaranteed to be the primary first, than the others.
	 * @since 1.1
	 */
	default List<String> getStaticLabels() {
		List<String> staticLabels = new ArrayList<>();
		staticLabels.add(this.getPrimaryLabel());
		staticLabels.addAll(this.getAdditionalLabels());
		return staticLabels;
	}

	/**
	 * @return The concrete class to which a node with the given {@link #getPrimaryLabel()} is mapped to
	 */
	Class<T> getUnderlyingClass();

	/**
	 * @return A description how to determine primary ids for nodes fitting this description
	 */
	@Nullable
	IdDescription getIdDescription();

	/**
	 * @return A collection of persistent properties that are mapped to graph properties and not to relationships
	 */
	Collection<GraphPropertyDescription> getGraphProperties();

	/**
	 * @return All graph properties including all properties from the extending classes if this entity is a parent entity.
	 */
	Collection<GraphPropertyDescription> getGraphPropertiesInHierarchy();


	/**
	 * Retrieves a {@link GraphPropertyDescription} by its field name.
	 *
	 * @param fieldName The field name for which the graph property description should be retrieved
	 * @return An empty optional if there is no property known for the given field.
	 */
	Optional<GraphPropertyDescription> getGraphProperty(String fieldName);

	/**
	 * @return True if entities for this node use Neo4j internal ids.
	 */
	default boolean isUsingInternalIds() {
		return this.getIdDescription().isInternallyGeneratedId();
	}

	/**
	 * This returns the outgoing relationships this node has to other nodes.
	 *
	 * @return The relationships defined by instances of this node.
	 */
	Collection<RelationshipDescription> getRelationships();

	/**
	 * Register a direct child node description for this entity.
	 *
	 * @param child - {@link NodeDescription} that defines an extending class.
	 */
	void addChildNodeDescription(NodeDescription<?> child);

	/**
	 * Retrieve all direct child node descriptions which extend this entity.
	 *
	 * @return all direct child node description.
	 */
	Collection<NodeDescription<?>> getChildNodeDescriptionsInHierarchy();

	/**
	 * Register the direct parent node description.
	 *
	 * @param parent - {@link NodeDescription} that describes the parent entity.
	 */
	void setParentNodeDescription(NodeDescription<?> parent);

	/**
	 * @return An expression that represents the right identifier type.
	 */
	default Expression getIdExpression() {

		return this.getIdDescription().asIdExpression();
	}
}
