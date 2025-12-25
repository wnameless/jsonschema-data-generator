package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls how the generator handles the {@code uniqueItems} keyword in JSON Schema arrays.
 *
 * <p>
 * When {@code uniqueItems} is set to {@code true} in a schema, all items in the array must be
 * unique. This option determines whether the generator enforces this constraint.
 *
 * @see JsonSchemaDataGenerator#withUniqueItemsOption(UniqueItemsOption)
 * @author Wei-Ming Wu
 */
public enum UniqueItemsOption {

  /**
   * Ignore the {@code uniqueItems} constraint. Generated arrays may contain duplicate values.
   */
  IGNORE,

  /**
   * Enforce the {@code uniqueItems} constraint. The generator will attempt to produce unique values
   * for each array item, retrying generation if duplicates are detected.
   */
  ENFORCE;

}
