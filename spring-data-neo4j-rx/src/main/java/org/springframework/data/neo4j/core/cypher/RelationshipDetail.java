package org.springframework.data.neo4j.core.cypher;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.core.cypher.Relationship.Direction;
import org.springframework.data.neo4j.core.cypher.support.Visitable;
import org.springframework.lang.Nullable;

/**
 * See <a href="">RelationshipDetail</a>
 *
 * @author Michael J. Simons
 */
public class RelationshipDetail implements Visitable {

	private final Direction direction;

	private @Nullable final SymbolicName symbolicName;

	private final List<String> types;

	public RelationshipDetail(Direction direction,
		@Nullable SymbolicName symbolicName, List<String> types) {
		this.direction = direction;
		this.symbolicName = symbolicName;
		this.types = types;
	}

	public Direction getDirection() {
		return direction;
	}

	public Optional<SymbolicName> getSymbolicName() {
		return Optional.ofNullable(symbolicName);
	}

	public List<String> getTypes() {
		return Collections.unmodifiableList(types);
	}

	public boolean isTyped() {
		return !this.types.isEmpty();
	}
}
