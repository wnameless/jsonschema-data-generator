package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls how the generator determines the number of items in arrays containing primitive types.
 *
 * <p>
 * This option applies to arrays where the {@code items} schema defines a primitive type (string,
 * number, integer, boolean, or null). For arrays of objects, use {@link ArrayOption} instead.
 *
 * <p>
 * The generator respects {@code minItems} and {@code maxItems} constraints when applicable.
 *
 * @see ArrayOption
 * @see JsonSchemaDataGenerator#withPrimitiveArrayOption(PrimitiveArrayOption)
 * @author Wei-Ming Wu
 */
public enum PrimitiveArrayOption {

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
