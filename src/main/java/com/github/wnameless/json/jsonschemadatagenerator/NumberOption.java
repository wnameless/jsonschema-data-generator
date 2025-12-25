package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls the base behavior for generating number and integer values.
 *
 * <p>
 * This option provides a simple override for all numeric generation. For more fine-grained control
 * over constrained numbers (those with minimum, maximum, or multipleOf), use
 * {@link ConstrainedNumberOption}.
 *
 * @see ConstrainedNumberOption
 * @see JsonSchemaDataGenerator#withNumberOption(NumberOption)
 * @author Wei-Ming Wu
 */
public enum NumberOption {

  /**
   * Return null for all number/integer properties.
   */
  NULL,

  /**
   * Return 0 for all number/integer properties.
   */
  ZERO,

  /**
   * Generate values based on schema constraints. This is the default behavior that respects
   * minimum, maximum, multipleOf, and other numeric constraints.
   */
  GENERATED;

}
