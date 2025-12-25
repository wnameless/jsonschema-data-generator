package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls whether the allOf keyword is merged during schema flattening.
 *
 * <p>
 * Some libraries (e.g., react-jsonschema-form) use allOf for conditional logic rather than schema
 * combination. In these cases, calling mergeAllOf() can cause errors.
 *
 * @author Wei-Ming Wu
 */
public enum AllOfOption {

  /**
   * Merge allOf schemas into a single combined schema (default behavior). Use this when allOf
   * represents schema composition/inheritance.
   */
  MERGE,

  /**
   * Skip merging allOf schemas, leaving them as-is. Use this when allOf is used for conditional
   * logic (e.g., if/then/else patterns).
   */
  SKIP;

}
