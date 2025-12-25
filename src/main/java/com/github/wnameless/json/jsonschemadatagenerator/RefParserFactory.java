package com.github.wnameless.json.jsonschemadatagenerator;

import io.zenwave360.jsonrefparser.$RefParserOptions;
import io.zenwave360.jsonrefparser.$RefParserOptions.OnCircular;
import io.zenwave360.jsonrefparser.$RefParserOptions.OnMissing;

/**
 * Factory for providing {@link $RefParserOptions} and {@link AllOfOption} used by
 * {@link JsonSchemaFlattener} when parsing and dereferencing JSON schemas.
 *
 * <p>
 * By default, circular references are resolved and missing references are skipped. The generator
 * uses {@link RecursionDepthOption} to control how deeply recursive schemas are expanded during
 * data generation.
 *
 * <p>
 * Users can configure custom options for advanced use cases:
 *
 * <pre>
 * // Skip circular references instead of resolving them
 * $RefParserOptions options = new $RefParserOptions()
 *     .withOnCircular(OnCircular.SKIP)
 *     .withOnMissing(OnMissing.FAIL);
 * RefParserFactory.setOptions(options);
 * </pre>
 *
 * <p>
 * Users can also control whether allOf schemas are merged:
 *
 * <pre>
 * // Skip merging allOf for conditional logic patterns
 * RefParserFactory.setAllOfOption(AllOfOption.SKIP);
 * </pre>
 */
public final class RefParserFactory {

  private static $RefParserOptions options = createDefaultOptions();
  private static AllOfOption allOfOption = AllOfOption.MERGE;

  private RefParserFactory() {}

  private static $RefParserOptions createDefaultOptions() {
    return new $RefParserOptions()
        .withOnCircular(OnCircular.RESOLVE)
        .withOnMissing(OnMissing.SKIP);
  }

  /**
   * Returns the current $RefParserOptions.
   *
   * @return the $RefParserOptions
   */
  public static $RefParserOptions getOptions() {
    return options;
  }

  /**
   * Sets custom $RefParserOptions to be used by all schema flattening operations.
   *
   * @param newOptions the custom options
   * @throws IllegalArgumentException if options is null
   */
  public static void setOptions($RefParserOptions newOptions) {
    if (newOptions == null) {
      throw new IllegalArgumentException("$RefParserOptions cannot be null");
    }
    options = newOptions;
  }

  /**
   * Returns the current AllOfOption.
   *
   * @return the AllOfOption
   */
  public static AllOfOption getAllOfOption() {
    return allOfOption;
  }

  /**
   * Sets the AllOfOption to control whether allOf schemas are merged during flattening.
   *
   * @param option the AllOfOption
   * @throws IllegalArgumentException if option is null
   */
  public static void setAllOfOption(AllOfOption option) {
    if (option == null) {
      throw new IllegalArgumentException("AllOfOption cannot be null");
    }
    allOfOption = option;
  }

  /**
   * Resets all options to default values (OnCircular.RESOLVE, OnMissing.SKIP, AllOfOption.MERGE).
   */
  public static void reset() {
    options = createDefaultOptions();
    allOfOption = AllOfOption.MERGE;
  }

}
