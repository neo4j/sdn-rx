package org.springframework.data.neo4j.core.cypher2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * See <a href="https://s3.amazonaws.com/artifacts.opencypher.org/M14/railroad/NodePattern.html">NodePattern</a>.
 *
 * @author Michael J. Simons
 */
public class Node implements PatternPart, Expression {

	static Node create(@Nullable String symbolicName, String primaryLabel, String... additionalLabels) {

		Assert.hasText(primaryLabel, "A primary label is required.");

		List<String> labels = new ArrayList<>();
		labels.add(primaryLabel);
		labels.addAll(Arrays.asList(additionalLabels));

		return new Node(Optional.ofNullable(symbolicName).map(SymbolicName::new).orElse(null), labels);
	}

	private @Nullable final SymbolicName symbolicName;

	private final List<String> labels;

	Node(SymbolicName symbolicName, List<String> labels) {

		this.symbolicName = symbolicName;
		this.labels = labels;
	}

	public Optional<SymbolicName> getSymbolicName() {
		return Optional.ofNullable(symbolicName);
	}

	public List<String> getLabels() {
		return Collections.unmodifiableList(labels);
	}

	/**
	 * Creates a new {@link Property} associated with this property container..
	 * <p/>
	 * Note: The property container does not track property creation and there is no possibility to enumerate all
	 * properties that have been created for this node.
	 *
	 * @param name property name, must not be {@literal null} or empty.
	 * @return a new {@link Property} associated with this {@link Node}.
	 */
	public Property property(String name) {

		return Property.create(this, name);
	}
}
