package org.springframework.data.neo4j.core.cypher2.renderer;

import java.util.Locale;

public final class RenderUtils {

	public static CharSequence escape(String unescapedString) {
		return String.format(Locale.ENGLISH, "`%s`", unescapedString);
	}

	private RenderUtils() {
	}
}
