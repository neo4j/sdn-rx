package org.springframework.data.neo4j.core.cypher;

import java.util.List;

public class PatternElementChain implements PatternElement {

	private List<Relationship> relationships;

	PatternElementChain(List<Relationship> relationships) {
		this.relationships = relationships;
	}
}
