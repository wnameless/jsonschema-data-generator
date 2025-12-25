package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls how the generator handles the {@code contains} keyword in JSON Schema arrays.
 *
 * <p>
 * The {@code contains} keyword specifies that at least one item in the array must match a given
 * schema. This option determines the placement of the matching item(s) within the generated array.
 *
 * @see JsonSchemaDataGenerator#withContainsOption(ContainsOption)
 * @author Wei-Ming Wu
 */
public enum ContainsOption {

  /**
   * Ignore the {@code contains} constraint. No special item will be generated.
   */
  NONE,

  /**
   * Place the item(s) matching the {@code contains} schema at the beginning of the array.
   */
  FIRST,

  /**
   * Place the item(s) matching the {@code contains} schema at the end of the array.
   */
  LAST,

  /**
   * Randomly place the item(s) matching the {@code contains} schema within the array.
   */
  RANDOM,

  /**
   * Distribute the item(s) matching the {@code contains} schema throughout the array by shuffling
   * all items.
   */
  DISTRIBUTE;

}
