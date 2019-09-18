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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.TypeSystem;
import org.neo4j.springframework.data.core.convert.Neo4jConverter;
import org.neo4j.springframework.data.core.convert.Neo4jConversions;
import org.neo4j.springframework.data.core.convert.Neo4jSimpleTypes;
import org.neo4j.springframework.data.core.schema.IdDescription;
import org.neo4j.springframework.data.core.schema.IdGenerator;
import org.neo4j.springframework.data.core.schema.NodeDescription;
import org.neo4j.springframework.data.core.schema.Relationship;
import org.neo4j.springframework.data.core.schema.RelationshipDescription;
import org.neo4j.springframework.data.core.schema.Schema;
import org.neo4j.springframework.data.core.schema.UnknownEntityException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;

/**
 * An implementation of both a {@link Schema} as well as a Neo4j version of Spring Datas
 * {@link org.springframework.data.mapping.context.MappingContext}. It is recommended to provide
 * the initial set of classes through {@link #setInitialEntitySet(Set)}.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
@API(status = API.Status.INTERNAL, since = "1.0")
public final class Neo4jMappingContext
	extends AbstractMappingContext<Neo4jPersistentEntity<?>, Neo4jPersistentProperty> implements Schema,
	BeanDefinitionRegistryPostProcessor {

	/**
	 * A lookup of entities based on their primary label. We depend on the locking mechanism provided by the
	 * {@link AbstractMappingContext}, so this lookup is not synchronized further.
	 */
	private final Map<String, NodeDescription<?>> nodeDescriptionsByPrimaryLabel = new HashMap<>();

	private final ConcurrentMap<String, Collection<RelationshipDescription>> relationshipsByPrimaryLabel = new ConcurrentHashMap<>();

	/**
	 * A map of fallback id generators, that have not been added to the application context
	 */
	private final Map<Class<? extends IdGenerator<?>>, IdGenerator<?>> fallbackIdGenerators = new ConcurrentHashMap<>();

	/**
	 * The converter used in this mapping context.
	 */
	private final Neo4jConverter converter;

	private @Nullable ListableBeanFactory beanFactory;

	public Neo4jMappingContext() {

		this(new Neo4jConversions());
	}

	public Neo4jMappingContext(Neo4jConversions neo4jConversions) {

		super.setSimpleTypeHolder(Neo4jSimpleTypes.HOLDER);

		final ConfigurableConversionService conversionService = new DefaultConversionService();
		neo4jConversions.registerConvertersIn(conversionService);
		this.converter = new DefaultNeo4jConverter(conversionService);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.mapping.context.AbstractMappingContext#createPersistentEntity(org.springframework.data.util.TypeInformation)
	 */
	@Override
	protected <T> Neo4jPersistentEntity<?> createPersistentEntity(TypeInformation<T> typeInformation) {

		final DefaultNeo4jPersistentEntity<T> newEntity = new DefaultNeo4jPersistentEntity<>(typeInformation);
		String primaryLabel = newEntity.getPrimaryLabel();

		if (this.nodeDescriptionsByPrimaryLabel.containsKey(primaryLabel)) {
			// @formatter:off
			throw new MappingException(
				String.format(Locale.ENGLISH, "The schema already contains a node description under the primary label %s",
						primaryLabel));
			// @formatter:on
		}

		if (this.nodeDescriptionsByPrimaryLabel.containsValue(newEntity)) {
			Optional<String> label = this.nodeDescriptionsByPrimaryLabel.entrySet().stream()
				.filter(e -> e.getValue().equals(newEntity)).map(
					Map.Entry::getKey).findFirst();

			throw new MappingException(
				String.format(Locale.ENGLISH, "The schema already contains description %s under the primary label %s",
					newEntity, label.orElse("n/a")));
		}

		NodeDescription<?> existingDescription = this.getNodeDescription(newEntity.getUnderlyingClass());
		if (existingDescription != null) {

			throw new MappingException(String.format(Locale.ENGLISH,
				"The schema already contains description with the underlying class %s under the primary label %s",
				newEntity.getUnderlyingClass().getName(), existingDescription.getPrimaryLabel()));
		}

		this.nodeDescriptionsByPrimaryLabel.put(primaryLabel, newEntity);

		return newEntity;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.mapping.context.AbstractMappingContext#createPersistentProperty(org.springframework.data.mapping.model.Property, org.springframework.data.mapping.model.MutablePersistentEntity, org.springframework.data.mapping.model.SimpleTypeHolder)
	 */
	@Override
	protected Neo4jPersistentProperty createPersistentProperty(Property property,
		Neo4jPersistentEntity<?> neo4jPersistentProperties, SimpleTypeHolder simpleTypeHolder) {

		return new DefaultNeo4jPersistentProperty(property, neo4jPersistentProperties, simpleTypeHolder);
	}

	@Override
	@Nullable
	public NodeDescription<?> getNodeDescription(String primaryLabel) {
		return this.nodeDescriptionsByPrimaryLabel.get(primaryLabel);
	}

	NodeDescription<?> getRequiredNodeDescription(String primaryLabel) {

		NodeDescription<?> nodeDescription = this.getNodeDescription(primaryLabel);
		if (nodeDescription == null) {
			throw new MappingException(
				String.format("Required node description not found with primary label '%s'", primaryLabel));
		}
		return nodeDescription;
	}

	@Override
	public NodeDescription<?> getNodeDescription(Class<?> underlyingClass) {

		for (NodeDescription<?> nodeDescription : this.nodeDescriptionsByPrimaryLabel.values()) {
			if (nodeDescription.getUnderlyingClass().equals(underlyingClass)) {
				return nodeDescription;
			}
		}
		return null;
	}

	@Override
	public Collection<RelationshipDescription> getRelationshipsOf(String primaryLabel) {

		return this.relationshipsByPrimaryLabel.computeIfAbsent(primaryLabel, this::computeRelationshipsOf);
	}

	public Converter getConverterFor(ReturnedType returnedType) {
		Class<?> targetType = returnedType.getReturnedType();
		boolean projecting = returnedType.isProjecting() && !this.hasPersistentEntityFor(targetType);

		return projecting ? new ProjectingConverter(targetType, this.converter) :
			new MappingConverter(this.getPersistentEntity(targetType), this, this.converter);
	}

	@Override
	public <T> Function<T, Map<String, Object>> getRequiredBinderFunctionFor(Class<T> sourceClass) {

		if (!this.hasPersistentEntityFor(sourceClass)) {
			throw new UnknownEntityException(sourceClass);
		}

		Neo4jPersistentEntity neo4jPersistentEntity = this.getPersistentEntity(sourceClass);
		return new DefaultNeo4jBinderFunction<T>(neo4jPersistentEntity, converter);
	}

	private Collection<RelationshipDescription> computeRelationshipsOf(String primaryLabel) {

		NodeDescription<?> nodeDescription = getRequiredNodeDescription(primaryLabel);

		final List<RelationshipDescription> relationships = new ArrayList<>();

		Neo4jPersistentEntity<?> entity = this.getPersistentEntity(nodeDescription.getUnderlyingClass());
		entity.doWithAssociations((Association<Neo4jPersistentProperty> association) -> {

			Neo4jPersistentProperty inverse = association.getInverse();
			Neo4jPersistentEntity<?> obverseOwner = this
				.getPersistentEntity(inverse.getAssociationTargetType());

			Relationship outgoingRelationship = inverse.findAnnotation(Relationship.class);


			String type;
			if (outgoingRelationship != null && outgoingRelationship.type() != null) {
				type = outgoingRelationship.type();
			} else {
				type = inverse.getName();
			}

			Relationship.Direction direction = Relationship.Direction.OUTGOING;
			if (outgoingRelationship != null) {
				direction = outgoingRelationship.direction();
			}

			relationships
				.add(new DefaultRelationshipDescription(type, primaryLabel, obverseOwner.getPrimaryLabel(),
					inverse.getName(), direction));
		});

		return Collections.unmodifiableCollection(relationships);
	}

	@Override
	public <T extends IdGenerator<?>> T getOrCreateIdGeneratorOfType(Class<T> idGeneratorType) {
		Supplier<T> fallbackSupplier = () -> (T) this.fallbackIdGenerators
			.computeIfAbsent(idGeneratorType, BeanUtils::instantiateClass);

		if (this.beanFactory == null) {
			return fallbackSupplier.get();
		} else {
			return this.beanFactory
				.getBeanProvider(idGeneratorType)
				.getIfUnique(fallbackSupplier);
		}
	}

	@Override
	public <T extends IdGenerator<?>> Optional<T> getIdGenerator(String reference) {
		try {
			return Optional.of((T) this.beanFactory.getBean(reference));
		} catch (NoSuchBeanDefinitionException e) {
			return Optional.empty();
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		super.setApplicationContext(applicationContext);

		this.beanFactory = applicationContext;
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

		// Register all the known id generators
		this.getPersistentEntities().stream()
			.map(Neo4jPersistentEntity::getIdDescription)
			.filter(IdDescription::isExternallyGeneratedId).map(IdDescription::getIdGeneratorClass)
			.filter(Optional::isPresent).map(Optional::get)
			.distinct()
			.map(generatorClass -> {
				RootBeanDefinition definition = new RootBeanDefinition(generatorClass);
				definition.setRole(RootBeanDefinition.ROLE_INFRASTRUCTURE);
				return definition;
			})
			.forEach(definition -> {
				String beanName = BeanDefinitionReaderUtils.generateBeanName(definition, registry);
				registry.registerBeanDefinition(beanName, definition);
			});
	}

	@Override
	public void postProcessBeanFactory(@SuppressWarnings({ "HiddenField" }) ConfigurableListableBeanFactory beanFactory) throws BeansException {
	}
}
