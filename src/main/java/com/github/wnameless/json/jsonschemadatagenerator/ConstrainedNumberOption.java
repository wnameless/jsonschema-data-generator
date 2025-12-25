package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls how the generator produces numbers when constraints like {@code minimum}, {@code
 * maximum}, or {@code multipleOf} are specified.
 *
 * <p>
 * This option applies to properties with {@code "type": "number"} or {@code "type": "integer"} that
 * have numeric constraints defined.
 *
 * @see JsonSchemaDataGenerator#withConstrainedNumberOption(ConstrainedNumberOption)
 * @author Wei-Ming Wu
 */
public enum ConstrainedNumberOption {

  /**
   * Return null instead of generating a number.
   */
  NULL,

  /**
   * Generate the minimum allowed value (respecting {@code exclusiveMinimum} if present).
   */
  MINIMUM,

  /**
   * Generate the maximum allowed value (respecting {@code exclusiveMaximum} if present).
   */
  MAXIMUM,

  /**
   * Generate the midpoint between minimum and maximum values.
   */
  MIDPOINT,

  /**
   * Use the DataFaker library to generate a random value within the constraints. This produces
   * realistic-looking random numbers.
   */
  DATAFAKER,

  /**
   * Generate a completely random number (may not respect constraints). Use with caution.
   */
  RANDOM;

}
