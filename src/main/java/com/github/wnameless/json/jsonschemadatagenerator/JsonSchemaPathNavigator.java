package com.github.wnameless.json.jsonschemadatagenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * A navigator for querying partial schemas from a flattened JSON Schema using JsonPath-style
 * expressions.
 *
 * <p>
 * Supports the following path syntax:
 * <ul>
 * <li>{@code $} - Root schema</li>
 * <li>{@code $.property} - Object property</li>
 * <li>{@code $.a.b.c} - Nested properties</li>
 * <li>{@code $.array[*]} - Array items schema</li>
 * <li>{@code $.array[0]} - Specific tuple position</li>
 * </ul>
 */
public final class JsonSchemaPathNavigator {

  private static final Pattern PATH_PATTERN =
      Pattern.compile("\\$|\\.(\\w+)|\\[(\\d+|\\*)]");

  private final JsonNode schemaNode;

  private static ObjectMapper mapper() {
    return ObjectMapperFactory.getObjectMapper();
  }

  private JsonSchemaPathNavigator(JsonNode schemaNode) {
    this.schemaNode = schemaNode;
  }

  /**
   * Creates a navigator from a JSON Schema string.
   *
   * @param jsonSchema the JSON Schema as a string
   * @return a new JsonSchemaPathNavigator instance
   * @throws IOException if schema parsing fails
   */
  public static JsonSchemaPathNavigator of(String jsonSchema) throws IOException {
    JsonNode node = mapper().valueToTree(JsonSchemaFlattener.flattenJsonSchema(jsonSchema));
    return new JsonSchemaPathNavigator(node);
  }

  /**
   * Creates a navigator from a JSON Schema file.
   *
   * @param jsonSchemaFile the JSON Schema file
   * @return a new JsonSchemaPathNavigator instance
   * @throws IOException if schema parsing fails
   */
  public static JsonSchemaPathNavigator of(File jsonSchemaFile) throws IOException {
    JsonNode node = mapper().valueToTree(JsonSchemaFlattener.flattenJsonSchema(jsonSchemaFile));
    return new JsonSchemaPathNavigator(node);
  }

  /**
   * Creates a navigator from an existing JsonNode schema.
   *
   * @param schemaNode the JSON Schema as a JsonNode
   * @return a new JsonSchemaPathNavigator instance
   */
  public static JsonSchemaPathNavigator of(JsonNode schemaNode) {
    return new JsonSchemaPathNavigator(schemaNode);
  }

  /**
   * Gets the schema at the specified JSON path.
   *
   * @param jsonPath the JSON path expression (e.g., "$.user.name", "$.items[*]")
   * @return the schema at the specified path
   * @throws InvalidJsonPathException if the path is invalid or not found
   */
  public JsonNode getSchema(String jsonPath) throws InvalidJsonPathException {
    if (jsonPath == null || jsonPath.isEmpty()) {
      throw new InvalidJsonPathException("Path cannot be null or empty");
    }

    List<PathSegment> segments = parsePath(jsonPath);
    return navigateToSchema(segments);
  }

  /**
   * Finds the schema at the specified JSON path, returning an Optional.
   *
   * @param jsonPath the JSON path expression
   * @return an Optional containing the schema if found, or empty if not found
   */
  public Optional<JsonNode> findSchema(String jsonPath) {
    try {
      return Optional.of(getSchema(jsonPath));
    } catch (InvalidJsonPathException e) {
      return Optional.empty();
    }
  }

  /**
   * Returns the root schema.
   *
   * @return a deep copy of the root schema
   */
  public JsonNode getRootSchema() {
    return schemaNode.deepCopy();
  }

  /**
   * Checks if a path exists in the schema.
   *
   * @param jsonPath the JSON path expression
   * @return true if the path exists, false otherwise
   */
  public boolean hasPath(String jsonPath) {
    return findSchema(jsonPath).isPresent();
  }

  // Path segment types
  private sealed interface PathSegment
      permits PropertySegment, ArrayIndexSegment, WildcardSegment {
  }

  private record PropertySegment(String propertyName) implements PathSegment {
  }

  private record ArrayIndexSegment(int index) implements PathSegment {
  }

  private record WildcardSegment() implements PathSegment {
  }

  private List<PathSegment> parsePath(String jsonPath) throws InvalidJsonPathException {
    List<PathSegment> segments = new ArrayList<>();

    if (!jsonPath.startsWith("$")) {
      throw new InvalidJsonPathException("Path must start with '$': " + jsonPath);
    }

    Matcher matcher = PATH_PATTERN.matcher(jsonPath);
    int lastEnd = 0;

    while (matcher.find()) {
      if (matcher.start() != lastEnd) {
        throw new InvalidJsonPathException(
            "Invalid path syntax at position " + lastEnd + ": " + jsonPath);
      }
      lastEnd = matcher.end();

      if (matcher.group().equals("$")) {
        continue; // Root marker, no segment needed
      } else if (matcher.group(1) != null) {
        // Property: .propertyName
        segments.add(new PropertySegment(matcher.group(1)));
      } else if (matcher.group(2) != null) {
        // Array access: [0] or [*]
        String indexStr = matcher.group(2);
        if (indexStr.equals("*")) {
          segments.add(new WildcardSegment());
        } else {
          segments.add(new ArrayIndexSegment(Integer.parseInt(indexStr)));
        }
      }
    }

    if (lastEnd != jsonPath.length()) {
      throw new InvalidJsonPathException("Unexpected characters at end of path: " + jsonPath);
    }

    return segments;
  }

  private JsonNode navigateToSchema(List<PathSegment> segments) throws InvalidJsonPathException {
    JsonNode current = schemaNode;

    for (PathSegment segment : segments) {
      JsonNode next = null;

      if (segment instanceof PropertySegment prop) {
        next = navigateToProperty(current, prop.propertyName());
      } else if (segment instanceof ArrayIndexSegment idx) {
        next = navigateToArrayIndex(current, idx.index());
      } else if (segment instanceof WildcardSegment) {
        next = navigateToArrayItems(current);
      }

      if (next == null) {
        throw new InvalidJsonPathException("Path not found at segment: " + segment);
      }

      current = next;
    }

    return current.deepCopy();
  }

  private JsonNode navigateToProperty(JsonNode schema, String propertyName) {
    // Check "properties" object
    if (schema.has("properties")) {
      JsonNode properties = schema.get("properties");
      if (properties.has(propertyName)) {
        return properties.get(propertyName);
      }
    }
    return null;
  }

  private JsonNode navigateToArrayIndex(JsonNode schema, int index) {
    // 1. Check prefixItems (JSON Schema 2020-12)
    if (schema.has("prefixItems")) {
      JsonNode prefixItems = schema.get("prefixItems");
      if (prefixItems.isArray() && index < prefixItems.size()) {
        return prefixItems.get(index);
      }
      // If index >= prefixItems.size(), check items for additional items
      if (schema.has("items") && !schema.get("items").isArray()) {
        return schema.get("items");
      }
    }

    // 2. Check items array (legacy tuple format: draft-04 to draft-07)
    if (schema.has("items") && schema.get("items").isArray()) {
      JsonNode itemsArray = schema.get("items");
      if (index < itemsArray.size()) {
        return itemsArray.get(index);
      }
      // Check additionalItems for positions beyond tuple
      if (schema.has("additionalItems") && !schema.get("additionalItems").isBoolean()) {
        return schema.get("additionalItems");
      }
    }

    // 3. Check items as single schema (homogeneous array)
    if (schema.has("items") && !schema.get("items").isArray()) {
      return schema.get("items");
    }

    return null;
  }

  private JsonNode navigateToArrayItems(JsonNode schema) {
    // [*] returns the schema for array items

    // 1. For homogeneous arrays: items is a single schema
    if (schema.has("items") && !schema.get("items").isArray()) {
      return schema.get("items");
    }

    // 2. For prefixItems with additional items schema (2020-12)
    if (schema.has("prefixItems") && schema.has("items") && !schema.get("items").isArray()) {
      return schema.get("items");
    }

    // 3. For legacy tuple format with additionalItems
    if (schema.has("items") && schema.get("items").isArray()) {
      if (schema.has("additionalItems") && !schema.get("additionalItems").isBoolean()) {
        return schema.get("additionalItems");
      }
    }

    return null;
  }

}
