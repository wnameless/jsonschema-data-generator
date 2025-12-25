package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls the maximum recursion depth during data generation for circular/recursive schemas.
 *
 * <p>
 * When using {@code OnCircular.RESOLVE} in {@link RefParserFactory}, circular references are
 * resolved in the schema. This option controls how deeply the generator will recurse into
 * self-referencing structures before returning null.
 */
public enum RecursionDepthOption {

  /**
   * No recursion allowed. Self-referencing fields return null immediately. Equivalent to depth
   * limit of 0.
   */
  NONE(0),

  /**
   * Minimal recursion - stops after 1 level of self-reference. Useful for seeing the structure
   * once without deep nesting.
   */
  SHALLOW(1),

  /**
   * Moderate recursion - allows up to 3 levels of self-reference. Good balance between data
   * richness and termination. This is the default.
   */
  MODERATE(3),

  /**
   * Deep recursion - allows up to 5 levels of self-reference. Use when you need richer recursive
   * data structures.
   */
  DEEP(5),

  /**
   * Very deep recursion - allows up to 10 levels. Use with caution as this can generate very large
   * JSON structures.
   */
  VERY_DEEP(10);

  private final int maxDepth;

  RecursionDepthOption(int maxDepth) {
    this.maxDepth = maxDepth;
  }

  /**
   * Returns the maximum recursion depth for this option.
   *
   * @return the maximum depth
   */
  public int getMaxDepth() {
    return maxDepth;
  }

}
