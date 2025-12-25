package com.github.wnameless.json.jsonschemadatagenerator;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ObjectMapperFactoryTest {

  @AfterEach
  void resetFactory() {
    // Always reset after each test to avoid side effects
    ObjectMapperFactory.reset();
  }

  @Nested
  class GetObjectMapperTests {

    @Test
    void returnsNonNull() {
      ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();

      assertNotNull(mapper);
    }

    @Test
    void returnsSameInstance() {
      ObjectMapper first = ObjectMapperFactory.getObjectMapper();
      ObjectMapper second = ObjectMapperFactory.getObjectMapper();

      assertSame(first, second, "Should return the same instance");
    }
  }

  @Nested
  class SetObjectMapperTests {

    @Test
    void setsCustomMapper() {
      ObjectMapper customMapper = new ObjectMapper();
      ObjectMapperFactory.setObjectMapper(customMapper);

      assertSame(customMapper, ObjectMapperFactory.getObjectMapper());
    }

    @Test
    void nullMapper_throwsException() {
      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> ObjectMapperFactory.setObjectMapper(null)
      );

      assertEquals("ObjectMapper cannot be null", exception.getMessage());
    }

    @Test
    void multipleCustomMappers_lastOneWins() {
      ObjectMapper first = new ObjectMapper();
      ObjectMapper second = new ObjectMapper();

      ObjectMapperFactory.setObjectMapper(first);
      ObjectMapperFactory.setObjectMapper(second);

      assertSame(second, ObjectMapperFactory.getObjectMapper());
      assertNotSame(first, ObjectMapperFactory.getObjectMapper());
    }
  }

  @Nested
  class ResetTests {

    @Test
    void resetsToNewInstance() {
      ObjectMapper original = ObjectMapperFactory.getObjectMapper();

      ObjectMapperFactory.reset();
      ObjectMapper afterReset = ObjectMapperFactory.getObjectMapper();

      assertNotSame(original, afterReset, "Should create a new instance after reset");
    }

    @Test
    void resetAfterCustomMapper_createsNewDefault() {
      ObjectMapper customMapper = new ObjectMapper();
      ObjectMapperFactory.setObjectMapper(customMapper);

      ObjectMapperFactory.reset();
      ObjectMapper afterReset = ObjectMapperFactory.getObjectMapper();

      assertNotSame(customMapper, afterReset, "Should not be the custom mapper after reset");
    }

    @Test
    void multipleResets_eachCreatesNewInstance() {
      ObjectMapper first = ObjectMapperFactory.getObjectMapper();

      ObjectMapperFactory.reset();
      ObjectMapper second = ObjectMapperFactory.getObjectMapper();

      ObjectMapperFactory.reset();
      ObjectMapper third = ObjectMapperFactory.getObjectMapper();

      assertNotSame(first, second);
      assertNotSame(second, third);
      assertNotSame(first, third);
    }
  }

  @Nested
  class IntegrationTests {

    @Test
    void customMapperUsedByGenerator() throws Exception {
      // This test verifies that the factory is actually used by the generator
      ObjectMapper customMapper = new ObjectMapper();
      ObjectMapperFactory.setObjectMapper(customMapper);

      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string", "default": "test" }
            }
          }
          """;

      // Should not throw - generator uses the factory's mapper
      var generator = JsonSchemaDataGenerator.builder().build();
      var result = generator.generate(schema);

      assertNotNull(result);
      assertEquals("test", result.get("name").asString());
    }

    @Test
    void customMapperUsedByNavigator() throws Exception {
      ObjectMapper customMapper = new ObjectMapper();
      ObjectMapperFactory.setObjectMapper(customMapper);

      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string" }
            }
          }
          """;

      // Should not throw - navigator uses the factory's mapper
      var navigator = JsonSchemaPathNavigator.of(schema);
      var result = navigator.getSchema("$.name");

      assertNotNull(result);
      assertEquals("string", result.get("type").asString());
    }
  }
}
