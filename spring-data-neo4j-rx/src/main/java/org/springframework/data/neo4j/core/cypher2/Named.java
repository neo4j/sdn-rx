package org.springframework.data.neo4j.core.cypher2;

/**
 * Named element exposing a {@link #getSymbolicName() symbolic name}.
 *
 * @author Michael J. Simons
 */
public interface Named {

	String getSymbolicName();
}
