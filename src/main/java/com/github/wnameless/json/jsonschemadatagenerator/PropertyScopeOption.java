package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls which properties are included when generating object values.
 *
 * <p>
 * This option determines whether the generator produces all defined properties or only those marked
 * as required in the schema's {@code required} array.
 *
 * @see JsonSchemaDataGenerator#withPropertyScopeOption(PropertyScopeOption)
 * @author Wei-Ming Wu
 */
public enum PropertyScopeOption {

  /**
   * Generate all properties defined in the schema's {@code properties} keyword, regardless of
   * whether they are required.
   */
  ALL,

  /**
   * Generate only properties listed in the schema's {@code required} array. Optional properties
   * will be omitted from the generated object.
   */
  REQUIRED_ONLY;

}
