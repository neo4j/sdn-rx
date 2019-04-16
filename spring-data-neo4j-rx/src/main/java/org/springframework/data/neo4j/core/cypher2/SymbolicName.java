package org.springframework.data.neo4j.core.cypher2;

public class SymbolicName implements Expression {

	private final String name;

	public SymbolicName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
