package org.springframework.data.neo4j.repository.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Getter
public class ExecutableQuery {

	private final Class<?> resultType;
	private final boolean collectionQuery;
	private final Supplier<String> querySupplier;
	private final Map<String, Object> parameters;

}
