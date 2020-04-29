package org.neo4j.springframework.data.core.mapping;

import java.util.Collection;
import java.util.List;

import org.neo4j.springframework.data.core.schema.NodeDescription;

/**
 * Wraps a resolved node description together with the complete list of labels returned from the database and the list
 * of labels not statically defined in the resolved node hierarchy.
 *
 * @author Michael J. Simons
 * @since 1.1
 * @soundtrack The Rolling Stones - Living In A Ghost Town
 */
final class NodeDescriptionAndLabels {

	private final NodeDescription<?> nodeDescription;

	private final Collection<String> dynamicLabels;

	public NodeDescriptionAndLabels(NodeDescription<?> nodeDescription,
		Collection<String> dynamicLabels) {
		this.nodeDescription = nodeDescription;
		this.dynamicLabels = dynamicLabels;
	}

	public NodeDescription<?> getNodeDescription() {
		return nodeDescription;
	}

	public Collection<String> getDynamicLabels() {
		return dynamicLabels;
	}
}
