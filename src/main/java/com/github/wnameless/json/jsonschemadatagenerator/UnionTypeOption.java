package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls how the generator handles union type arrays like {@code ["string", "null"]}.
 *
 * @author Wei-Ming Wu
 */
public enum UnionTypeOption {

  /**
   * Pick the first non-null type in the array. Falls back to "null" if all types are null.
   * This is the default behavior.
   */
  FIRST_NON_NULL,

  /**
   * Pick the first type in the array regardless of whether it's null.
   */
  FIRST,

  /**
   * Pick the last type in the array.
   */
  LAST,

  /**
   * Randomly select a type from the array.
   */
  RANDOM,

  /**
   * Prefer null if present in the array, otherwise use the first type.
   */
  NULL_FIRST;

}
