package org.springframework.data.neo4j.core.cypher;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.data.neo4j.core.cypher.support.Visitor;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * See <a href="https://s3.amazonaws.com/artifacts.opencypher.org/M14/railroad/RelationshipPattern.html">RelationshipPattern</a>.
 *
 * @author Michael J. Simons
 */
public class Relationship implements PatternElement {

	public enum Direction {
		/**
		 * Left to right
		 */
		LTR("-", "->"),
		/**
		 * Right to left
		 */
		RTR("<-", "-"),
		/**
		 * None
		 */
		UNI("-", "-");

		Direction(String symbolLeft, String symbolRight) {
			this.symbolLeft = symbolLeft;
			this.symbolRight = symbolRight;
		}

		private final String symbolLeft;

		private final String symbolRight;

		public String getSymbolLeft() {
			return symbolLeft;
		}

		public String getSymbolRight() {
			return symbolRight;
		}
	}

	static Relationship create(Node left,
		@Nullable Direction direction, Node right, @Nullable String symbolicName, String... types) {

		Assert.notNull(left, "Left node is required.");
		Assert.notNull(right, "Right node is required.");

		RelationshipDetail details = new RelationshipDetail(
			Optional.ofNullable(direction).orElse(Direction.UNI),
			Optional.ofNullable(symbolicName).map(SymbolicName::new).orElse(null), Arrays.asList(types));
		return new Relationship(left, details, right);
	}

	private final Node left;

	private final Node right;

	private final RelationshipDetail details;

	Relationship(Node left, RelationshipDetail details, Node right) {
		this.left = left;
		this.right = right;
		this.details = details;
	}

	@Override
	public void accept(Visitor visitor) {

		visitor.enter(this);

		left.accept(visitor);
		details.accept(visitor);
		right.accept(visitor);

		visitor.leave(this);
	}
}
