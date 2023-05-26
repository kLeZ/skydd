/*
 * Copyright (C) 2023 Alessandro 'kLeZ' Accardo
 *
 * This file is part of skydd.
 *
 * skydd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * skydd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with skydd.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package me.klez.skydd;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import jakarta.annotation.Nonnull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.TimeZone;

/**
 * This is a Utility class that could be useful to handle json structures in the form of {@link JsonNode}s.
 */
public final class JsonUtils {
	public static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<>() {
	};

	private JsonUtils() {
		throw new RuntimeException("Cannot instantiate a utility class!");
	}

	/**
	 * This method will convert a given arbitrary (serializable) object to a {@link Map}&lt;{@link String}, {@link Object}&gt;.
	 *
	 * @param object an arbitrary serializable object.
	 * @return a {@link Map}&lt;{@link String}, {@link Object}&gt; representing the given object.
	 */
	public static Map<String, Object> convertToMap(final Object object) {
		return convertToMap(convertToJsonNode(object));
	}

	/**
	 * This method will convert a given arbitrary (serializable) object to a {@link JsonNode}.
	 *
	 * @param object an arbitrary serializable object.
	 * @return a {@link JsonNode} representing the given object.
	 */
	public static JsonNode convertToJsonNode(final Object object) {
		return createMapper().convertValue(object, JsonNode.class);
	}

	/**
	 * This method will convert a given json node to a {@link Map}&lt;{@link String}, {@link Object}&gt;.
	 *
	 * @param node a json node.
	 * @return a {@link Map}&lt;{@link String}, {@link Object}&gt; representing the given node.
	 */
	public static Map<String, Object> convertToMap(final JsonNode node) {
		return createMapper().convertValue(node, MAP_TYPE_REFERENCE);
	}

	/**
	 * This method creates a standard {@link ObjectMapper} used for serialization and deserialization of JSON data structures.
	 *
	 * @return an instance of {@link ObjectMapper}, configured with a reasonable set of features and configurations passed by a {@link JsonFactory}
	 * object.
	 */
	public static ObjectMapper createMapper() {
		return new ObjectMapper(createFactory()).findAndRegisterModules()
				.enable(SerializationFeature.INDENT_OUTPUT)
				.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
				.setTimeZone(TimeZone.getDefault())
				.setPropertyNamingStrategy(PropertyNamingStrategies.SnakeCaseStrategy.INSTANCE);
	}

	private static JsonFactory createFactory() {
		return JsonFactory.builder().build();
	}

	/**
	 * Converts a given epoch number to a {@link ZonedDateTime}.
	 *
	 * @param epochSecond    number of seconds since 1970-01-01T00:0
	 * @param nanoAdjustment the number of nanoseconds to add to the epoch time.
	 * @return a {@link ZonedDateTime} representing the given epoch time.
	 */
	@Nonnull
	public static ZonedDateTime getZonedDateTime(int epochSecond, int nanoAdjustment) {
		return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond, nanoAdjustment), ZoneId.systemDefault());
	}

}
