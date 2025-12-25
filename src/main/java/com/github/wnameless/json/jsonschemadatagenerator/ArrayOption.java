package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls how the generator determines the number of items in arrays containing objects.
 *
 * <p>
 * This option applies to arrays where the {@code items} schema defines an object type. For arrays
 * of primitive types (strings, numbers, booleans), use {@link PrimitiveArrayOption} instead.
 *
 * <p>
 * The generator respects {@code minItems} and {@code maxItems} constraints when applicable.
 *
 * @see PrimitiveArrayOption
 * @see JsonSchemaDataGenerator#withArrayOption(ArrayOption)
 * @author Wei-Ming Wu
 */
public enum ArrayOption {

  /**
   * Generate an empty array (or respect {@code minItems} if specified).
   */
  NULL,

  /**
   * Generate an empty array, respecting {@code minItems} constraint if specified.
   */
  EMPTY,

  /**
   * Generate an array with exactly one item (or respect {@code minItems} if greater than 1).
   */
  ONE,

  /**
   * Generate an array with a random number of items within the schema's constraints.
   */
  RANDOM;

}
