package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls how the generator selects a schema from the {@code anyOf} keyword in JSON Schema.
 *
 * <p>
 * When a schema uses {@code anyOf} to specify multiple valid schemas, this option determines which
 * schema branch is used for data generation.
 *
 * @see JsonSchemaDataGenerator#withAnyOfOption(AnyOfOption)
 * @author Wei-Ming Wu
 */
public enum AnyOfOption {

  /**
   * Return null instead of generating data from any of the schemas.
   */
  NULL,

  /**
   * Use the first schema in the {@code anyOf} array. This provides deterministic output.
   */
  FIRST,

  /**
   * Use the last schema in the {@code anyOf} array.
   */
  LAST,

  /**
   * Randomly select a schema from the {@code anyOf} array. Each generation may produce different
   * results.
   */
  RANDOM;

}
