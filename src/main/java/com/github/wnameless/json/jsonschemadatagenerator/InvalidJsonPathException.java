package com.github.wnameless.json.jsonschemadatagenerator;

/**
 * Exception thrown when a JSON path expression is invalid or cannot be resolved.
 *
 * <p>
 * This exception is thrown by {@link JsonSchemaPathNavigator} when:
 * <ul>
 *   <li>The path syntax is malformed (e.g., doesn't start with "$")</li>
 *   <li>The path contains invalid characters or segments</li>
 *   <li>The path references a property or index that doesn't exist in the schema</li>
 * </ul>
 *
 * @see JsonSchemaPathNavigator#getSchema(String)
 * @author Wei-Ming Wu
 */
public class InvalidJsonPathException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message describing the invalid path
   */
  public InvalidJsonPathException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message describing the invalid path
   * @param cause the underlying cause of the exception
   */
  public InvalidJsonPathException(String message, Throwable cause) {
    super(message, cause);
  }

}
