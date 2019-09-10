package org.neo4j.springframework.data.core.convert;

import static org.springframework.data.convert.ConverterBuilder.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.IsoDuration;
import org.neo4j.driver.types.Point;

/**
 * Conversions for all known Cypher types, diretly supported by the driver.
 * See <a href="https://neo4j.com/docs/driver-manual/current/cypher-values/">Working with Cypher values</a>.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
final class CypherTypes {

	static final List<?> CONVERTERS;

	static {

		List<ConverterAware> hlp = new ArrayList<>();
		hlp.add(reading(Value.class, Void.class, v -> null).andWriting(v -> Values.NULL));
		hlp.add(reading(Value.class, void.class, v -> null).andWriting(v -> Values.NULL));
		// List will be unwrapped by {@code Neo4jConverter}
		hlp.add(reading(Value.class, Map.class, Value::asMap).andWriting(Values::value));
		hlp.add(reading(Value.class, Boolean.class, Value::asBoolean).andWriting(Values::value));
		hlp.add(reading(Value.class, boolean.class, Value::asBoolean).andWriting(Values::value));
		hlp.add(reading(Value.class, Long.class, Value::asLong).andWriting(Values::value));
		hlp.add(reading(Value.class, long.class, Value::asLong).andWriting(Values::value));
		hlp.add(reading(Value.class, Double.class, Value::asDouble).andWriting(Values::value));
		hlp.add(reading(Value.class, double.class, Value::asDouble).andWriting(Values::value));
		hlp.add(reading(Value.class, String.class, Value::asString).andWriting(Values::value));
		hlp.add(reading(Value.class, byte[].class, Value::asByteArray).andWriting(Values::value));
		hlp.add(reading(Value.class, LocalDate.class, Value::asLocalDate).andWriting(Values::value));
		hlp.add(reading(Value.class, OffsetTime.class, Value::asOffsetTime).andWriting(Values::value));
		hlp.add(reading(Value.class, LocalTime.class, Value::asLocalTime).andWriting(Values::value));
		hlp.add(reading(Value.class, ZonedDateTime.class, Value::asZonedDateTime).andWriting(Values::value));
		hlp.add(reading(Value.class, LocalDateTime.class, Value::asLocalDateTime).andWriting(Values::value));
		hlp.add(reading(Value.class, IsoDuration.class, Value::asIsoDuration).andWriting(Values::value));
		hlp.add(reading(Value.class, Point.class, Value::asPoint).andWriting(Values::value));

		CONVERTERS = Collections.unmodifiableList(hlp);
	}

	private CypherTypes() {
	}
}
