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

import static java.util.stream.Collectors.*;
import static org.neo4j.springframework.data.core.schema.NodeDescription.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.logging.LogFactory;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.neo4j.driver.types.TypeSystem;
import org.neo4j.springframework.data.core.convert.Neo4jConverter;
import org.neo4j.springframework.data.core.schema.RelationshipDescription;
import org.neo4j.springframework.data.core.schema.SchemaUtils;
import org.springframework.core.log.LogAccessor;
import org.springframework.data.convert.EntityInstantiators;
import org.springframework.data.mapping.AssociationHandler;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mapping.model.ParameterValueProvider;
import org.springframework.lang.Nullable;

/**
 * The central logic of mapping Neo4j's {@link org.neo4j.driver.Record records} to entities based on the Spring
 * implementation of the {@link org.neo4j.springframework.data.core.schema.Schema},
 * represented by the {@link Neo4jMappingContext}.
 *
 * @author Gerrit Meier
 * @author Michael J. Simons
 * @since 1.0
 */
final class DefaultNeo4jMappingFunction<T> implements BiFunction<TypeSystem, Record, T> {

	private static final LogAccessor log = new LogAccessor(LogFactory.getLog(DefaultNeo4jMappingFunction.class));

	/**
	 * The shared entity instantiators of this context. Those should not be recreated for each entity or even not for
	 * each query, as otherwise the cache of Spring's org.springframework.data.convert.ClassGeneratingEntityInstantiator
	 * won't apply
	 */
	private static final EntityInstantiators INSTANTIATORS = new EntityInstantiators();

	/**
	 * The description of the possible root node from where the mapping should start.
	 */
	private final Neo4jPersistentEntity<T> rootNodeDescription;

	private final Neo4jMappingContext mappingContext;

	private final Neo4jConverter converter;

	private static final Predicate<Map.Entry<String, Object>> IS_LIST = entry -> entry.getValue() instanceof List;

	DefaultNeo4jMappingFunction(Neo4jPersistentEntity<T> rootNodeDescription, Neo4jMappingContext neo4jMappingContext, Neo4jConverter converter) {

		this.rootNodeDescription = rootNodeDescription;
		this.mappingContext = neo4jMappingContext;
		this.converter = converter;
	}

	@Override
	public T apply(TypeSystem typeSystem, Record record) {
		Map<Object, Object> knownObjects = new ConcurrentHashMap<>();

		// That would be the place to call a custom converter for the whole object, if any such thing would be
		// available (Converter<Record, DomainObject>
		try {
			Predicate<Value> isNode = v -> v.hasType(typeSystem.NODE());
			Predicate<Value> isMap = value -> value.hasType(typeSystem.MAP());

			List<Value> recordValues = record.values();

			Supplier<T> generatedStatementHandler = () ->
				recordValues.stream()
					.filter(isMap)
					.map(value -> map(typeSystem, value.asMap(Function.identity()), rootNodeDescription, knownObjects))
					.findFirst()
					.orElseGet(() -> {
						log.warn(() -> String
							.format("Could not find mappable nodes or relationships inside %s for %s", record,
								rootNodeDescription));
						return null;
					});

			String nodeLabel = rootNodeDescription.getPrimaryLabel();
			return recordValues.stream()
				.filter(isNode)
				.map(Value::asNode)
				.filter(node -> node.hasLabel(nodeLabel))
				.map(node -> mergeIntoMap(node, record))
				.map(mergedAttributes -> map(typeSystem, mergedAttributes, rootNodeDescription, knownObjects))
				.findFirst()
				.orElseGet(generatedStatementHandler);
		} catch (Exception e) {
			throw new MappingException("Error mapping " + record.toString(), e);
		}
	}

	/**
	 * Merges the root node of a query and the remaining record into one map, adding the internal ID of the node, too.
	 *
	 * @param node   Node whose attributes are about to be merged
	 * @param record Optional record that should be merged
	 * @return
	 */
	private static Map<String, Value> mergeIntoMap(Node node, @Nullable Record record) {
		Map<String, Value> mergedAttributes = new HashMap<>(node.asMap(Function.identity()));
		mergedAttributes.put(NAME_OF_INTERNAL_ID, Values.value(node.id()));
		if (record != null) {
			mergedAttributes.putAll(record.asMap(Function.identity()));
		}
		return mergedAttributes;
	}

	/**
	 * @param queryResult     The original query result
	 * @param nodeDescription The node description of the current entity to be mapped from the result
	 * @param knownObjects    The current list of known objects
	 * @param <ET>            As in entity type
	 * @return
	 */
	private <ET> ET map(TypeSystem typeSystem, Map<String, Value> queryResult,
		Neo4jPersistentEntity<ET> nodeDescription,
		Map<Object, Object> knownObjects) {

		ET instance = instantiate(typeSystem, nodeDescription, queryResult);

		PersistentPropertyAccessor<ET> propertyAccessor = converter
			.decoratePropertyAccessor(typeSystem, nodeDescription.getPropertyAccessor(instance));
		if (nodeDescription.requiresPropertyPopulation()) {

			// Fill simple properties
			Predicate<Neo4jPersistentProperty> isConstructorParameter = nodeDescription
				.getPersistenceConstructor()::isConstructorParameter;
			nodeDescription.doWithProperties(populateFrom(queryResult, propertyAccessor, isConstructorParameter));

			// Fill associations
			Collection<RelationshipDescription> relationships = mappingContext
				.getRelationshipsOf(nodeDescription.getPrimaryLabel());
			Function<String, Neo4jPersistentEntity<?>> relatedNodeDescriptionLookup =
				relatedLabel -> (Neo4jPersistentEntity<?>) mappingContext.getNodeDescription(relatedLabel);
			nodeDescription.doWithAssociations(
				populateFrom(typeSystem, queryResult, propertyAccessor, relationships, relatedNodeDescriptionLookup,
					knownObjects));
		}
		return instance;
	}

	private <ET> ET instantiate(TypeSystem typeSystem, Neo4jPersistentEntity<ET> anotherNodeDescription,
		Map<String, Value> values) {

		ParameterValueProvider<Neo4jPersistentProperty> parameterValueProvider = new ParameterValueProvider<Neo4jPersistentProperty>() {
			@Override
			public Object getParameterValue(PreferredConstructor.Parameter parameter) {

				Neo4jPersistentProperty matchingProperty = anotherNodeDescription
					.getRequiredPersistentProperty(parameter.getName());
				return extractValueOf(matchingProperty, values);
			}
		};
		parameterValueProvider = converter.decorateParameterValueProvider(typeSystem, parameterValueProvider);
		return INSTANTIATORS.getInstantiatorFor(anotherNodeDescription)
			.createInstance(anotherNodeDescription, parameterValueProvider);
	}

	private static PropertyHandler<Neo4jPersistentProperty> populateFrom(
		Map<String, Value> queryResult,
		PersistentPropertyAccessor<?> propertyAccessor,
		Predicate<Neo4jPersistentProperty> isConstructorParameter
	) {
		return property -> {
			if (isConstructorParameter.test(property)) {
				return;
			}

			Object value = extractValueOf(property, queryResult);
			propertyAccessor.setProperty(property, value);
		};
	}

	private AssociationHandler<Neo4jPersistentProperty> populateFrom(
		TypeSystem typeSystem,
		Map<String, Value> queryResult,
		PersistentPropertyAccessor<?> propertyAccessor,
		Collection<RelationshipDescription> relationships,
		Function<String, Neo4jPersistentEntity<?>> relatedNodeDescriptionLookup,
		Map<Object, Object> knownObjects
	) {
		return association -> {
			Neo4jPersistentProperty inverse = association.getInverse();

			RelationshipDescription relationship = relationships.stream()
				.filter(r -> r.getPropertyName().equals(inverse.getName()))
				.findFirst().get();

			String relationshipType = relationship.getType();
			String targetLabel = relationship.getTarget();

			Neo4jPersistentEntity<?> targetNodeDescription = relatedNodeDescriptionLookup.apply(targetLabel);

			List<Object> value = new ArrayList<>();
			Value list = (Value) queryResult.get(SchemaUtils.generateRelatedNodesCollectionName(relationship));

			// if the list is null the mapping is based on a custom query
			if (list == null) {

				Predicate<Map.Entry<String, Value>> isList = entry -> entry.getValue() instanceof Value && typeSystem
					.LIST().isTypeOf(
						(Value) entry.getValue());

				Predicate<Map.Entry<String, Value>> containsOnlyRelationships = entry -> entry.getValue()
					.asList(Function.identity())
					.stream()
					.allMatch(listEntry -> typeSystem.RELATIONSHIP().isTypeOf(listEntry));

				Predicate<Map.Entry<String, Value>> containsOnlyNodes = entry -> entry.getValue()
					.asList(Function.identity())
					.stream()
					.allMatch(listEntry -> typeSystem.NODE().isTypeOf(listEntry));

				// find relationships in the result
				List<Relationship> allMatchingTypeRelationshipsInResult = queryResult.entrySet().stream()
					.filter(isList.and(containsOnlyRelationships))
					.flatMap(entry -> entry.getValue().asList(Value::asRelationship).stream())
					.filter(r -> r.type().equals(relationshipType))
					.collect(toList());

				List<Node> allNodesWithMatchingLabelInResult = queryResult.entrySet().stream()
					.filter(isList.and(containsOnlyNodes))
					.flatMap(entry -> entry.getValue().asList(Value::asNode).stream())
					.filter(n -> n.hasLabel(targetLabel))
					.collect(toList());

				if (allNodesWithMatchingLabelInResult.isEmpty() && allMatchingTypeRelationshipsInResult.isEmpty()) {
					return;
				}

				for (Node possibleValueNode : allNodesWithMatchingLabelInResult) {
						long nodeId = possibleValueNode.id();

					for (Relationship possibleRelationship : allMatchingTypeRelationshipsInResult) {
								if (possibleRelationship.endNodeId() == nodeId) {
									Map<String, Value> newPropertyMap = mergeIntoMap(possibleValueNode, null);
									value.add(map(typeSystem, newPropertyMap, targetNodeDescription, knownObjects));
									break;
						}
					}
				}
			} else {
				for (Value relatedEntity : list.asList(Function.identity())) {
					Neo4jPersistentProperty idProperty = targetNodeDescription.getRequiredIdProperty();

					// internal (generated) id or external set
					Object idValue = idProperty.isInternalIdProperty()
						? relatedEntity.get(NAME_OF_INTERNAL_ID)
						: relatedEntity.get(idProperty.getName());

					Object valueEntry = knownObjects.computeIfAbsent(idValue,
						(id) -> map(typeSystem, relatedEntity.asMap(Function.identity()), targetNodeDescription,
							knownObjects));

					value.add(valueEntry);
				}
			}

			if (inverse.getTypeInformation().isCollectionLike()) {
				if (inverse.getType().equals(Set.class)) {
					propertyAccessor.setProperty(inverse, new HashSet(value));
				} else {
					propertyAccessor.setProperty(inverse, value);
				}
			} else {
				propertyAccessor.setProperty(inverse, value.isEmpty() ? null : value.get(0));
			}
		};
	}

	private static Object extractValueOf(Neo4jPersistentProperty property, Map<String, Value> propertyContainer) {
		if (property.isInternalIdProperty()) {
			return propertyContainer.get(NAME_OF_INTERNAL_ID);
		} else {
			String graphPropertyName = property.getPropertyName();
			return propertyContainer.get(graphPropertyName);
		}
	}
}
