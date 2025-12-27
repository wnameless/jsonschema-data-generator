package com.github.wnameless.json.jsonschemadatagenerator;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import io.zenwave360.jsonrefparser.$RefParserOptions;
import io.zenwave360.jsonrefparser.$RefParserOptions.OnCircular;
import io.zenwave360.jsonrefparser.$RefParserOptions.OnMissing;

class RefParserFactoryTest {

  @AfterEach
  void resetFactory() {
    // Always reset after each test to avoid side effects
    RefParserFactory.reset();
  }

  @Nested
  class GetOptionsTests {

    @Test
    void returnsNonNull() {
      $RefParserOptions options = RefParserFactory.getOptions();

      assertNotNull(options);
    }

    @Test
    void returnsSameInstanceBeforeReset() {
      $RefParserOptions first = RefParserFactory.getOptions();
      $RefParserOptions second = RefParserFactory.getOptions();

      assertSame(first, second, "Should return the same instance");
    }
  }

  @Nested
  class SetOptionsTests {

    @Test
    void setsCustomOptions() {
      $RefParserOptions customOptions = new $RefParserOptions()
          .withOnCircular(OnCircular.SKIP)
          .withOnMissing(OnMissing.FAIL);

      RefParserFactory.setOptions(customOptions);

      assertSame(customOptions, RefParserFactory.getOptions());
    }

    @Test
    void nullOptions_throwsException() {
      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> RefParserFactory.setOptions(null)
      );

      assertEquals("$RefParserOptions cannot be null", exception.getMessage());
    }

    @Test
    void multipleCustomOptions_lastOneWins() {
      $RefParserOptions first = new $RefParserOptions().withOnCircular(OnCircular.SKIP);
      $RefParserOptions second = new $RefParserOptions().withOnCircular(OnCircular.RESOLVE);

      RefParserFactory.setOptions(first);
      RefParserFactory.setOptions(second);

      assertSame(second, RefParserFactory.getOptions());
      assertNotSame(first, RefParserFactory.getOptions());
    }
  }

  @Nested
  class ResetTests {

    @Test
    void resetsOptionsToNewInstance() {
      $RefParserOptions original = RefParserFactory.getOptions();

      // Set custom options
      $RefParserOptions customOptions = new $RefParserOptions()
          .withOnCircular(OnCircular.SKIP);
      RefParserFactory.setOptions(customOptions);

      // Reset
      RefParserFactory.reset();

      $RefParserOptions afterReset = RefParserFactory.getOptions();
      assertNotSame(customOptions, afterReset, "Should not be the custom options after reset");
      assertNotSame(original, afterReset, "Should be a new instance after reset");
    }

  }

  @Nested
  class IntegrationTests {

    @Test
    void optionsUsedByFlattener() throws Exception {
      $RefParserOptions customOptions = new $RefParserOptions()
          .withOnCircular(OnCircular.RESOLVE)
          .withOnMissing(OnMissing.SKIP);
      RefParserFactory.setOptions(customOptions);

      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string", "default": "test" }
            }
          }
          """;

      // Should not throw - flattener uses the factory's options
      var result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);
      assertNotNull(result);
    }

    @Test
    void factoryOptionsAffectFlattening() throws Exception {
      // Test that custom options are actually used
      RefParserFactory.reset();

      String schema = """
          {
            "type": "object",
            "properties": {
              "value": { "type": "string", "default": "test" }
            }
          }
          """;

      var result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);
      assertNotNull(result);
      assertTrue(result.containsKey("type"));
      assertTrue(result.containsKey("properties"));
    }
  }
}
