package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls how the generator handles the {@code patternProperties} keyword in JSON Schema.
 *
 * <p>
 * When a schema defines {@code patternProperties}, this option determines whether and how many
 * properties matching each pattern are generated.
 *
 * @see JsonSchemaDataGenerator#withPatternPropertiesOption(PatternPropertiesOption)
 * @author Wei-Ming Wu
 */
public enum PatternPropertiesOption {

  /**
   * Do not generate any pattern properties. The generated object will only contain properties
   * explicitly defined in the schema's {@code properties} keyword.
   */
  NONE,

  /**
   * Generate exactly one property per pattern. The property name is generated using regex-based
   * string generation from the pattern.
   */
  GENERATE_ONE,

  /**
   * Generate 2-3 properties per pattern. Provides a more realistic representation of objects with
   * pattern-based properties.
   */
  GENERATE_FEW;

}
