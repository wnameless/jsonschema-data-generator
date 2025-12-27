package com.github.wnameless.json.jsonschemadatagenerator;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import io.zenwave360.jsonrefparser.$RefParser;
import io.zenwave360.jsonrefparser.$Refs;

/**
 * Utility class for flattening JSON schemas by resolving $ref references and optionally merging
 * allOf constructs.
 *
 * <p>
 * Uses {@link RefParserFactory} to obtain $ref parsing options. The {@link AllOfOption} parameter
 * controls whether allOf schemas are merged during flattening.
 *
 * @author Wei-Ming Wu
 */
public final class JsonSchemaFlattener {

  private JsonSchemaFlattener() {}

  /**
   * Flattens a JSON schema string by resolving $ref references.
   *
   * @param jsonSchema the JSON schema as a string
   * @param allOfOption controls whether allOf schemas are merged
   * @return a flattened schema map
   * @throws IOException if parsing fails
   */
  public static Map<String, Object> flattenJsonSchema(String jsonSchema, AllOfOption allOfOption)
      throws IOException {
    $RefParser parser = new $RefParser(jsonSchema)
        .withOptions(RefParserFactory.getOptions());
    return parseAndFlatten(parser, allOfOption);
  }

  /**
   * Flattens a JSON schema file by resolving $ref references.
   *
   * @param jsonSchemaFile the JSON schema file
   * @param allOfOption controls whether allOf schemas are merged
   * @return a flattened schema map
   * @throws IOException if parsing fails
   */
  public static Map<String, Object> flattenJsonSchema(File jsonSchemaFile, AllOfOption allOfOption)
      throws IOException {
    $RefParser parser = new $RefParser(jsonSchemaFile)
        .withOptions(RefParserFactory.getOptions());
    return parseAndFlatten(parser, allOfOption);
  }

  /**
   * Flattens a JSON schema using a user-provided $RefParser instance.
   *
   * @param parser the pre-configured $RefParser instance
   * @param allOfOption controls whether allOf schemas are merged
   * @return a flattened schema map
   * @throws IOException if parsing fails
   */
  public static Map<String, Object> flattenJsonSchema($RefParser parser, AllOfOption allOfOption)
      throws IOException {
    return parseAndFlatten(parser, allOfOption);
  }

  private static Map<String, Object> parseAndFlatten($RefParser parser, AllOfOption allOfOption)
      throws IOException {
    $RefParser parsed = parser.parse().dereference();

    if (allOfOption == AllOfOption.MERGE) {
      parsed = parsed.mergeAllOf();
    }

    $Refs refs = parsed.getRefs();
    return refs.schema();
  }

}
