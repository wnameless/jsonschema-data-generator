package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls how the generator selects a schema from the {@code oneOf} keyword in JSON Schema.
 *
 * <p>
 * When a schema uses {@code oneOf} to specify that exactly one of multiple schemas must be valid,
 * this option determines which schema branch is used for data generation.
 *
 * @see JsonSchemaDataGenerator#withOneOfOption(OneOfOption)
 * @author Wei-Ming Wu
 */
public enum OneOfOption {

  /**
   * Return null instead of generating data from any of the schemas.
   */
  NULL,

  /**
   * Use the first schema in the {@code oneOf} array. This provides deterministic output.
   */
  FIRST,

  /**
   * Use the last schema in the {@code oneOf} array.
   */
  LAST,

  /**
   * Randomly select a schema from the {@code oneOf} array. Each generation may produce different
   * results.
   */
  RANDOM;

}
