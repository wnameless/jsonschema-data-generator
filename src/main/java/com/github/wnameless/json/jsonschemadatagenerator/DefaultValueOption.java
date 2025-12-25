package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls how the generator handles the {@code default} keyword in JSON Schema.
 *
 * <p>
 * When a schema property has a {@code default} value defined, this option determines whether that
 * value is used or ignored during generation.
 *
 * @see JsonSchemaDataGenerator#withDefaultValueOption(DefaultValueOption)
 * @author Wei-Ming Wu
 */
public enum DefaultValueOption {

  /**
   * Ignore the {@code default} keyword and generate a value based on the schema type and other
   * constraints.
   */
  IGNORE,

  /**
   * Use the {@code default} value from the schema when present. This takes precedence over other
   * generation options.
   */
  USE;

}
