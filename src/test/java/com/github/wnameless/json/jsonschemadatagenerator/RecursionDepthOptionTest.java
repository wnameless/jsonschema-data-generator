package com.github.wnameless.json.jsonschemadatagenerator;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

class RecursionDepthOptionTest {

  @Nested
  class GetMaxDepthTests {

    @Test
    void none_returnsZero() {
      assertEquals(0, RecursionDepthOption.NONE.getMaxDepth());
    }

    @Test
    void shallow_returnsOne() {
      assertEquals(1, RecursionDepthOption.SHALLOW.getMaxDepth());
    }

    @Test
    void moderate_returnsThree() {
      assertEquals(3, RecursionDepthOption.MODERATE.getMaxDepth());
    }

    @Test
    void deep_returnsFive() {
      assertEquals(5, RecursionDepthOption.DEEP.getMaxDepth());
    }

    @Test
    void veryDeep_returnsTen() {
      assertEquals(10, RecursionDepthOption.VERY_DEEP.getMaxDepth());
    }
  }

  @Nested
  class EnumValuesTests {

    @Test
    void allValuesExist() {
      RecursionDepthOption[] values = RecursionDepthOption.values();

      assertEquals(5, values.length);
      assertNotNull(RecursionDepthOption.valueOf("NONE"));
      assertNotNull(RecursionDepthOption.valueOf("SHALLOW"));
      assertNotNull(RecursionDepthOption.valueOf("MODERATE"));
      assertNotNull(RecursionDepthOption.valueOf("DEEP"));
      assertNotNull(RecursionDepthOption.valueOf("VERY_DEEP"));
    }

    @Test
    void maxDepthsAreOrdered() {
      assertTrue(RecursionDepthOption.NONE.getMaxDepth() < RecursionDepthOption.SHALLOW.getMaxDepth());
      assertTrue(RecursionDepthOption.SHALLOW.getMaxDepth() < RecursionDepthOption.MODERATE.getMaxDepth());
      assertTrue(RecursionDepthOption.MODERATE.getMaxDepth() < RecursionDepthOption.DEEP.getMaxDepth());
      assertTrue(RecursionDepthOption.DEEP.getMaxDepth() < RecursionDepthOption.VERY_DEEP.getMaxDepth());
    }
  }

  @Nested
  class GeneratorIntegrationTests {

    // A self-referencing schema (like a tree structure)
    private static final String RECURSIVE_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "name": { "type": "string", "default": "node" },
            "children": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "name": { "type": "string", "default": "child" },
                  "children": {
                    "type": "array",
                    "items": {
                      "type": "object",
                      "properties": {
                        "name": { "type": "string", "default": "grandchild" }
                      }
                    }
                  }
                }
              }
            }
          }
        }
        """;

    @Test
    void none_limitsRecursion() throws Exception {
      var gen = JsonSchemaDataGenerator.builder()
          .recursionDepthOption(RecursionDepthOption.NONE)
          .arrayOption(ArrayOption.ONE)
          .build();

      JsonNode result = gen.generate(RECURSIVE_SCHEMA);

      assertNotNull(result);
      assertTrue(result.has("name"));
    }

    @Test
    void shallow_allowsOneLevel() throws Exception {
      var gen = JsonSchemaDataGenerator.builder()
          .recursionDepthOption(RecursionDepthOption.SHALLOW)
          .arrayOption(ArrayOption.ONE)
          .build();

      JsonNode result = gen.generate(RECURSIVE_SCHEMA);

      assertNotNull(result);
      assertTrue(result.has("name"));
      assertTrue(result.has("children"));
    }

    @Test
    void moderate_isDefault() throws Exception {
      var gen = JsonSchemaDataGenerator.builder()
          .arrayOption(ArrayOption.ONE)
          .build();

      // Default should be MODERATE
      JsonNode result = gen.generate(RECURSIVE_SCHEMA);

      assertNotNull(result);
      assertTrue(result.has("name"));
    }

    @Test
    void withRecursionDepthOption_fluentApi() {
      var original = JsonSchemaDataGenerator.builder().build();
      var modified = original.withRecursionDepthOption(RecursionDepthOption.DEEP);

      assertNotSame(original, modified);
    }

    @Test
    void deep_generatesMoreLevels() throws Exception {
      var gen = JsonSchemaDataGenerator.builder()
          .recursionDepthOption(RecursionDepthOption.DEEP)
          .arrayOption(ArrayOption.ONE)
          .build();

      JsonNode result = gen.generate(RECURSIVE_SCHEMA);

      assertNotNull(result);
      assertTrue(result.has("name"));
      assertTrue(result.has("children"));
    }
  }
}
