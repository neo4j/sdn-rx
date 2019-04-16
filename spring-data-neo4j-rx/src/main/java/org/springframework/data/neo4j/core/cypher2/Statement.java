package org.springframework.data.neo4j.core.cypher2;

import org.springframework.data.neo4j.core.cypher2.support.AstNode;

/**
 * Shall be the common interfaces for queries that we support.
 * For reference see: <a href="https://s3.amazonaws.com/artifacts.opencypher.org/M14/railroad/Cypher.html">Cypher</a>.
 * We have skipped the intermediate "Query" structure so a statement in the context of this generator is either a
 * {@link RegularQuery} or a {@code StandaloneCall}.
 *
 * @author Michael J. Simons
 */

public interface Statement extends AstNode {
	static StatementBuilder builder() {

		return new DefaultStatementBuilder();
	}

	interface RegularQuery extends Statement {
	}

	interface SingleQuery extends RegularQuery {
	}
}
