package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls the base behavior for generating string values.
 *
 * <p>
 * This option provides a simple override for all string generation. For more fine-grained control
 * over formatted strings (those with format or pattern), use {@link FormattedStringOption}.
 *
 * @see FormattedStringOption
 * @see JsonSchemaDataGenerator#withStringOption(StringOption)
 * @author Wei-Ming Wu
 */
public enum StringOption {

  /**
   * Return null for all string properties.
   */
  NULL,

  /**
   * Return an empty string ({@code ""}) for all string properties.
   */
  EMPTY,

  /**
   * Generate values based on schema constraints. This is the default behavior that respects
   * minLength, maxLength, pattern, and format constraints.
   */
  GENERATED;

}
