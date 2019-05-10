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
package org.springframework.data.neo4j.repository.query;

import static org.springframework.data.neo4j.core.cypher.Cypher.*;

import java.util.Iterator;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.neo4j.core.cypher.Condition;
import org.springframework.data.neo4j.core.cypher.Cypher;
import org.springframework.data.neo4j.core.cypher.Statement;
import org.springframework.data.neo4j.core.cypher.renderer.CypherRenderer;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.data.neo4j.core.mapping.Neo4jPersistentProperty;
import org.springframework.data.neo4j.core.schema.NodeDescription;
import org.springframework.data.neo4j.repository.query.Neo4jQueryMethod.Neo4jParameter;
import org.springframework.data.neo4j.repository.query.Neo4jQueryMethod.Neo4jParameters;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

/**
 * A Cypher-DSL based implementation of the {@link AbstractQueryCreator} that eventually creates Cypher queries as strings
 * to be used by a Neo4j client or driver as statement template.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
final class CypherQueryCreator extends AbstractQueryCreator<String, Condition> {

	private final Neo4jMappingContext mappingContext;
	private final Class<?> domainType;
	private final NodeDescription<?> nodeDescription;

	private final Parameters<Neo4jParameters, Neo4jParameter> formalParameters;
	private Iterator<Neo4jParameter> it;

	CypherQueryCreator(Neo4jMappingContext mappingContext, Class<?> domainType,
		Parameters<Neo4jParameters, Neo4jParameter>  formalParameters, PartTree tree
	) {
		super(tree);

		this.mappingContext = mappingContext;
		this.domainType = domainType;
		this.nodeDescription = this.mappingContext.getRequiredNodeDescription(this.domainType);
		this.formalParameters = formalParameters;
	}

	@Override
	protected Condition create(Part part, Iterator<Object> iterator) {

		this.it = formalParameters.iterator();
		return createImpl(part);
	}

	@Override
	protected Condition and(Part part, Condition base, Iterator<Object> iterator) {

		if (base == null) {
			return create(part, iterator);
		}

		return base.and(createImpl(part));
	}

	@Override
	protected Condition or(Condition base, Condition condition) {
		return base.or(condition);
	}

	@Override
	protected String complete(Condition condition, Sort sort) {

		Statement statement = mappingContext
			.prepareMatchOf(nodeDescription, Optional.of(condition))
			.returning(Cypher.symbolicName("n"))
			.build();

		return CypherRenderer.create().render(statement);
	}

	private Condition createImpl(Part part) {
		PersistentPropertyPath<Neo4jPersistentProperty> path = mappingContext
			.getPersistentPropertyPath(part.getProperty());
		Neo4jPersistentProperty persistentProperty = path.getLeafProperty();

		Neo4jParameter formalParameter = it.next();
		switch (part.getType()) {

			case SIMPLE_PROPERTY:
				return property("n", persistentProperty.getPropertyName())
					.isEqualTo(parameter(formalParameter.getNameOrIndex()));
			default:
				throw new IllegalArgumentException("Unsupported part type: " + part.getType());
		}
	}
}
