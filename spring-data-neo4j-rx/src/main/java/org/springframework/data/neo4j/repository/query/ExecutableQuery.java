package org.springframework.data.neo4j.repository.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
@Getter
public class ExecutableQuery {

	private final Class<?> resultType;
	private final boolean collectionQuery;
	private final String cypher;
	private final Map<String, Object> parameters;

}
