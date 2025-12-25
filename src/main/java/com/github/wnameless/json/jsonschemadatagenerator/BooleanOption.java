package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls how the generator produces boolean values.
 *
 * <p>
 * This option determines the value generated for properties with {@code "type": "boolean"}.
 *
 * @see JsonSchemaDataGenerator#withBooleanOption(BooleanOption)
 * @author Wei-Ming Wu
 */
public enum BooleanOption {

  /**
   * Return null instead of a boolean value.
   */
  NULL,

  /**
   * Always generate {@code true}.
   */
  TRUE,

  /**
   * Always generate {@code false}.
   */
  FALSE,

  /**
   * Randomly generate either {@code true} or {@code false}.
   */
  RANDOM;

}
