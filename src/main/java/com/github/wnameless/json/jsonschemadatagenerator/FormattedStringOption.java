package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Controls how the generator produces strings with a {@code format} or {@code pattern} keyword.
 *
 * <p>
 * This option applies to string properties that have additional constraints like {@code "format":
 * "email"} or {@code "pattern": "^[A-Z]+$"}.
 *
 * @see JsonSchemaDataGenerator#withFormattedStringOption(FormattedStringOption)
 * @author Wei-Ming Wu
 */
public enum FormattedStringOption {

  /**
   * Return null instead of generating a formatted string.
   */
  NULL,

  /**
   * Use the DataFaker library to generate realistic values for known formats (email, uri, uuid,
   * date, date-time, time, hostname, ipv4, ipv6). For {@code pattern} constraints, uses regex
   * generation.
   */
  DATAFAKER,

  /**
   * Generate random strings that satisfy the format or pattern constraints. For formats, generates
   * valid but random-looking values. For patterns, uses regex-based generation.
   */
  RANDOM;

}
