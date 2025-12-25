package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls how the generator handles the {@code examples} keyword in JSON Schema.
 *
 * <p>
 * The {@code examples} keyword provides an array of sample values for a schema. This option
 * determines whether and how these example values are used during generation.
 *
 * @see JsonSchemaDataGenerator#withExamplesOption(ExamplesOption)
 * @author Wei-Ming Wu
 */
public enum ExamplesOption {

  /**
   * Ignore the {@code examples} keyword and generate values based on the schema type and
   * constraints.
   */
  NONE,

  /**
   * Use the first example from the {@code examples} array. This provides deterministic output.
   */
  FIRST,

  /**
   * Use the last example from the {@code examples} array.
   */
  LAST,

  /**
   * Randomly select an example from the {@code examples} array. Each generation may produce
   * different results.
   */
  RANDOM;

}
