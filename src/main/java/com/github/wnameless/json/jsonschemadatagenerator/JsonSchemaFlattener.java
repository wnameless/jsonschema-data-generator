package com.github.wnameless.json.jsonschemadatagenerator;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import io.zenwave360.jsonrefparser.$RefParser;
import io.zenwave360.jsonrefparser.$Refs;

/**
 * Utility class for flattening JSON schemas by resolving $ref references and merging allOf
 * constructs.
 *
 * <p>
 * Uses {@link RefParserFactory} to obtain configuration options. Users can customize parsing
 * behavior by calling {@link RefParserFactory#setOptions} and
 * {@link RefParserFactory#setAllOfOption} before invoking flatten methods.
 */
public final class JsonSchemaFlattener {

  private JsonSchemaFlattener() {}

  /**
   * Flattens a JSON schema string by resolving $ref references.
   *
   * @param jsonSchema the JSON schema as a string
   * @return a flattened schema map
   * @throws IOException if parsing fails
   */
  public static Map<String, Object> flattenJsonSchema(String jsonSchema) throws IOException {
    $RefParser parser = new $RefParser(jsonSchema)
        .withOptions(RefParserFactory.getOptions());
    return parseAndFlatten(parser);
  }

  /**
   * Flattens a JSON schema file by resolving $ref references.
   *
   * @param jsonSchemaFile the JSON schema file
   * @return a flattened schema map
   * @throws IOException if parsing fails
   */
  public static Map<String, Object> flattenJsonSchema(File jsonSchemaFile) throws IOException {
    $RefParser parser = new $RefParser(jsonSchemaFile)
        .withOptions(RefParserFactory.getOptions());
    return parseAndFlatten(parser);
  }

  /**
   * Flattens a JSON schema using a user-provided $RefParser instance.
   *
   * <p>
   * Note: This method still respects {@link RefParserFactory#getAllOfOption} for controlling
   * whether allOf schemas are merged.
   *
   * @param parser the pre-configured $RefParser instance
   * @return a flattened schema map
   * @throws IOException if parsing fails
   */
  public static Map<String, Object> flattenJsonSchema($RefParser parser) throws IOException {
    return parseAndFlatten(parser);
  }

  private static Map<String, Object> parseAndFlatten($RefParser parser) throws IOException {
    $RefParser parsed = parser.parse().dereference();

    // Conditionally call mergeAllOf based on factory setting
    if (RefParserFactory.getAllOfOption() == AllOfOption.MERGE) {
      parsed = parsed.mergeAllOf();
    }

    $Refs refs = parsed.getRefs();
    return refs.schema();
  }

}
