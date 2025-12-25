package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls how the generator selects a value from the {@code enum} keyword in JSON Schema.
 *
 * <p>
 * When a schema property defines an {@code enum} array of allowed values, this option determines
 * which value is selected for generation.
 *
 * @see JsonSchemaDataGenerator#withEnumOption(EnumOption)
 * @author Wei-Ming Wu
 */
public enum EnumOption {

  /**
   * Return null instead of selecting an enum value.
   */
  NULL,

  /**
   * Select the first value in the {@code enum} array. This provides deterministic output.
   */
  FIRST,

  /**
   * Select the last value in the {@code enum} array.
   */
  LAST,

  /**
   * Randomly select a value from the {@code enum} array. Each generation may produce different
   * results.
   */
  RANDOM;

}
