package org.springframework.data.neo4j.core.cypher2;

import java.util.List;

import org.springframework.data.neo4j.core.cypher2.support.TypedSubtree;

/**
 * See <a href="https://s3.amazonaws.com/artifacts.opencypher.org/M14/railroad/Pattern.html">Pattern</a>.
 */
public class Pattern extends TypedSubtree<PatternPart> {

	public Pattern(List<PatternPart> patternParts) {
		super(patternParts);
	}
}
