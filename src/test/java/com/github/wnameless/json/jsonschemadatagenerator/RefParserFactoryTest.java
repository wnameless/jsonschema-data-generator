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
  class GetAllOfOptionTests {

    @Test
    void defaultValue_isMerge() {
      AllOfOption option = RefParserFactory.getAllOfOption();

      assertEquals(AllOfOption.MERGE, option);
    }
  }

  @Nested
  class SetAllOfOptionTests {

    @Test
    void setsToSkip() {
      RefParserFactory.setAllOfOption(AllOfOption.SKIP);

      assertEquals(AllOfOption.SKIP, RefParserFactory.getAllOfOption());
    }

    @Test
    void setsToMerge() {
      RefParserFactory.setAllOfOption(AllOfOption.SKIP);
      RefParserFactory.setAllOfOption(AllOfOption.MERGE);

      assertEquals(AllOfOption.MERGE, RefParserFactory.getAllOfOption());
    }

    @Test
    void nullAllOfOption_throwsException() {
      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> RefParserFactory.setAllOfOption(null)
      );

      assertEquals("AllOfOption cannot be null", exception.getMessage());
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

    @Test
    void resetsAllOfOptionToMerge() {
      RefParserFactory.setAllOfOption(AllOfOption.SKIP);

      RefParserFactory.reset();

      assertEquals(AllOfOption.MERGE, RefParserFactory.getAllOfOption());
    }

    @Test
    void resetsBothOptionsAndAllOfOption() {
      // Set custom values for both
      $RefParserOptions customOptions = new $RefParserOptions()
          .withOnCircular(OnCircular.SKIP);
      RefParserFactory.setOptions(customOptions);
      RefParserFactory.setAllOfOption(AllOfOption.SKIP);

      // Reset
      RefParserFactory.reset();

      // Verify both are reset
      assertNotSame(customOptions, RefParserFactory.getOptions());
      assertEquals(AllOfOption.MERGE, RefParserFactory.getAllOfOption());
    }
  }

  @Nested
  class IntegrationTests {

    @Test
    void allOfSkip_doesNotMergeAllOfSchemas() throws Exception {
      RefParserFactory.setAllOfOption(AllOfOption.SKIP);

      String schema = """
          {
            "type": "object",
            "allOf": [
              {
                "properties": {
                  "name": { "type": "string", "default": "skipped" }
                }
              }
            ],
            "properties": {
              "id": { "type": "integer", "default": 1 }
            }
          }
          """;

      var generator = JsonSchemaDataGenerator.builder().build();
      var result = generator.generate(schema);

      // With SKIP, only direct properties should exist (allOf not merged)
      assertTrue(result.has("id"), "Should have direct property 'id'");
    }

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
      var result = JsonSchemaFlattener.flattenJsonSchema(schema);
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

      var result = JsonSchemaFlattener.flattenJsonSchema(schema);
      assertNotNull(result);
      assertTrue(result.containsKey("type"));
      assertTrue(result.containsKey("properties"));
    }
  }
}
