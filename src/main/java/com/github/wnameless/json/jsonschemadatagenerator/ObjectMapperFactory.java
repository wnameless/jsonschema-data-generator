package com.github.wnameless.json.jsonschemadatagenerator;

import tools.jackson.databind.ObjectMapper;

/**
 * Factory for providing a shared {@link ObjectMapper} instance used by
 * {@link JsonSchemaDataGenerator} and {@link JsonSchemaPathNavigator}.
 *
 * <p>
 * Users can configure a custom ObjectMapper to meet their needs:
 *
 * <pre>
 * ObjectMapper customMapper = new ObjectMapper();
 * customMapper.configure(...);
 * ObjectMapperFactory.setObjectMapper(customMapper);
 * </pre>
 *
 * @author Wei-Ming Wu
 */
public final class ObjectMapperFactory {

  private static ObjectMapper objectMapper = new ObjectMapper();

  private ObjectMapperFactory() {}

  /**
   * Returns the current ObjectMapper instance.
   *
   * @return the ObjectMapper
   */
  public static ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  /**
   * Sets a custom ObjectMapper to be used by all generators and navigators.
   *
   * @param mapper the custom ObjectMapper
   * @throws IllegalArgumentException if mapper is null
   */
  public static void setObjectMapper(ObjectMapper mapper) {
    if (mapper == null) {
      throw new IllegalArgumentException("ObjectMapper cannot be null");
    }
    objectMapper = mapper;
  }

  /**
   * Resets the ObjectMapper to a new default instance.
   */
  public static void reset() {
    objectMapper = new ObjectMapper();
  }

}
