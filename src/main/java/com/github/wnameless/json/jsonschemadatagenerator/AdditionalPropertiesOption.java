package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls how the generator handles the {@code additionalProperties} keyword in JSON Schema.
 *
 * <p>
 * When a schema defines {@code additionalProperties}, this option determines whether and how many
 * additional properties are generated beyond those explicitly defined in {@code properties}.
 *
 * @see JsonSchemaDataGenerator#withAdditionalPropertiesOption(AdditionalPropertiesOption)
 * @author Wei-Ming Wu
 */
public enum AdditionalPropertiesOption {

  /**
   * Do not generate any additional properties. The generated object will only contain properties
   * explicitly defined in the schema's {@code properties} keyword.
   */
  NONE,

  /**
   * Generate exactly one additional property. Useful for testing that additional properties are
   * handled correctly without generating excessive data.
   */
  GENERATE_ONE,

  /**
   * Generate 2-3 additional properties. Provides a more realistic representation of objects with
   * dynamic properties.
   */
  GENERATE_FEW;

}
