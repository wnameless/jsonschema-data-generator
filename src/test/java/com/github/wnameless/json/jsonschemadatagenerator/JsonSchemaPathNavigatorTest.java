package com.github.wnameless.json.jsonschemadatagenerator;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

class JsonSchemaPathNavigatorTest {

  @Nested
  class RootPathTests {

    @Test
    void dollarSign_returnsEntireSchema() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string" }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);
      JsonNode result = navigator.getSchema("$");

      assertEquals("object", result.get("type").asString());
      assertTrue(result.has("properties"));
    }

    @Test
    void getRootSchema_returnsEntireSchema() throws Exception {
      String schema = """
          {
            "type": "object",
            "title": "Test Schema"
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);
      JsonNode result = navigator.getRootSchema();

      assertEquals("object", result.get("type").asString());
      assertEquals("Test Schema", result.get("title").asString());
    }
  }

  @Nested
  class PropertyPathTests {

    @Test
    void simpleProperty_returnsPropertySchema() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string", "title": "Name Field" }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);
      JsonNode result = navigator.getSchema("$.name");

      assertEquals("string", result.get("type").asString());
      assertEquals("Name Field", result.get("title").asString());
    }

    @Test
    void nestedProperties_returnsNestedSchema() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "user": {
                "type": "object",
                "properties": {
                  "address": {
                    "type": "object",
                    "properties": {
                      "street": { "type": "string", "maxLength": 100 }
                    }
                  }
                }
              }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);
      JsonNode result = navigator.getSchema("$.user.address.street");

      assertEquals("string", result.get("type").asString());
      assertEquals(100, result.get("maxLength").asInt());
    }

    @Test
    void propertyWithEnum_returnsEnumValues() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "status": {
                "type": "string",
                "enum": ["active", "inactive", "pending"]
              }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);
      JsonNode result = navigator.getSchema("$.status");

      assertEquals("string", result.get("type").asString());
      assertTrue(result.has("enum"));
      assertEquals(3, result.get("enum").size());
      assertEquals("active", result.get("enum").get(0).asString());
    }
  }

  @Nested
  class ArrayPathTests {

    @Test
    void arrayWildcard_returnsItemsSchema() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "tags": {
                "type": "array",
                "items": { "type": "string", "minLength": 1 }
              }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);
      JsonNode result = navigator.getSchema("$.tags[*]");

      assertEquals("string", result.get("type").asString());
      assertEquals(1, result.get("minLength").asInt());
    }

    @Test
    void arrayIndex_forHomogeneousArray_returnsItemsSchema() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "items": {
                "type": "array",
                "items": { "type": "integer", "minimum": 0 }
              }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);
      JsonNode result = navigator.getSchema("$.items[0]");

      assertEquals("integer", result.get("type").asString());
      assertEquals(0, result.get("minimum").asInt());
    }

    @Test
    void arrayIndex_forPrefixItems_returnsCorrectPositionSchema() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "tuple": {
                "type": "array",
                "prefixItems": [
                  { "type": "string", "title": "First" },
                  { "type": "number", "title": "Second" },
                  { "type": "boolean", "title": "Third" }
                ]
              }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);

      JsonNode first = navigator.getSchema("$.tuple[0]");
      assertEquals("string", first.get("type").asString());
      assertEquals("First", first.get("title").asString());

      JsonNode second = navigator.getSchema("$.tuple[1]");
      assertEquals("number", second.get("type").asString());
      assertEquals("Second", second.get("title").asString());

      JsonNode third = navigator.getSchema("$.tuple[2]");
      assertEquals("boolean", third.get("type").asString());
      assertEquals("Third", third.get("title").asString());
    }

    @Test
    void arrayIndex_beyondPrefixItems_returnsItemsSchema() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "data": {
                "type": "array",
                "prefixItems": [
                  { "type": "string" }
                ],
                "items": { "type": "integer", "title": "Additional" }
              }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);

      JsonNode first = navigator.getSchema("$.data[0]");
      assertEquals("string", first.get("type").asString());

      JsonNode additional = navigator.getSchema("$.data[1]");
      assertEquals("integer", additional.get("type").asString());
      assertEquals("Additional", additional.get("title").asString());
    }

    @Test
    void arrayWildcard_forPrefixItemsWithItems_returnsItemsSchema() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "mixed": {
                "type": "array",
                "prefixItems": [
                  { "type": "string" }
                ],
                "items": { "type": "number", "title": "Rest" }
              }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);
      JsonNode result = navigator.getSchema("$.mixed[*]");

      assertEquals("number", result.get("type").asString());
      assertEquals("Rest", result.get("title").asString());
    }

    @Test
    void legacyTupleArray_returnsCorrectSchemas() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "legacy": {
                "type": "array",
                "items": [
                  { "type": "string", "title": "First" },
                  { "type": "number", "title": "Second" }
                ],
                "additionalItems": { "type": "boolean", "title": "Extra" }
              }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);

      JsonNode first = navigator.getSchema("$.legacy[0]");
      assertEquals("string", first.get("type").asString());

      JsonNode second = navigator.getSchema("$.legacy[1]");
      assertEquals("number", second.get("type").asString());

      // Beyond tuple, uses additionalItems
      JsonNode extra = navigator.getSchema("$.legacy[2]");
      assertEquals("boolean", extra.get("type").asString());
      assertEquals("Extra", extra.get("title").asString());

      // Wildcard returns additionalItems
      JsonNode wildcard = navigator.getSchema("$.legacy[*]");
      assertEquals("boolean", wildcard.get("type").asString());
    }

    @Test
    void nestedArrayAccess_works() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "matrix": {
                "type": "array",
                "items": {
                  "type": "array",
                  "items": { "type": "number", "title": "Cell" }
                }
              }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);

      JsonNode inner = navigator.getSchema("$.matrix[*][*]");
      assertEquals("number", inner.get("type").asString());
      assertEquals("Cell", inner.get("title").asString());
    }
  }

  @Nested
  class ErrorHandlingTests {

    @Test
    void nullPath_throwsException() throws Exception {
      String schema = """
          { "type": "object" }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);

      assertThrows(InvalidJsonPathException.class, () -> navigator.getSchema(null));
    }

    @Test
    void emptyPath_throwsException() throws Exception {
      String schema = """
          { "type": "object" }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);

      assertThrows(InvalidJsonPathException.class, () -> navigator.getSchema(""));
    }

    @Test
    void pathWithoutDollar_throwsException() throws Exception {
      String schema = """
          { "type": "object" }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);

      assertThrows(InvalidJsonPathException.class, () -> navigator.getSchema("name"));
    }

    @Test
    void nonexistentProperty_throwsException() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string" }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);

      assertThrows(InvalidJsonPathException.class, () -> navigator.getSchema("$.nonexistent"));
    }

    @Test
    void invalidPathSyntax_throwsException() throws Exception {
      String schema = """
          { "type": "object" }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);

      assertThrows(InvalidJsonPathException.class, () -> navigator.getSchema("$..name"));
      assertThrows(InvalidJsonPathException.class, () -> navigator.getSchema("$["));
    }
  }

  @Nested
  class FindSchemaTests {

    @Test
    void findSchema_returnsOptionalWithValue_forValidPath() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string" }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);
      var result = navigator.findSchema("$.name");

      assertTrue(result.isPresent());
      assertEquals("string", result.get().get("type").asString());
    }

    @Test
    void findSchema_returnsEmpty_forInvalidPath() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string" }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);
      var result = navigator.findSchema("$.nonexistent");

      assertTrue(result.isEmpty());
    }
  }

  @Nested
  class HasPathTests {

    @Test
    void hasPath_returnsTrue_forExistingPath() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string" }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);

      assertTrue(navigator.hasPath("$"));
      assertTrue(navigator.hasPath("$.name"));
    }

    @Test
    void hasPath_returnsFalse_forNonexistentPath() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string" }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);

      assertFalse(navigator.hasPath("$.nonexistent"));
      assertFalse(navigator.hasPath("$.name.child"));
    }
  }

  @Nested
  class SchemaMetadataTests {

    @Test
    void canAccessTitle() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "email": {
                "type": "string",
                "title": "Email Address",
                "format": "email"
              }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);
      JsonNode result = navigator.getSchema("$.email");

      assertEquals("Email Address", result.get("title").asString());
      assertEquals("email", result.get("format").asString());
    }

    @Test
    void canAccessConstraints() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "age": {
                "type": "integer",
                "minimum": 0,
                "maximum": 150,
                "description": "Person's age"
              }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);
      JsonNode result = navigator.getSchema("$.age");

      assertEquals(0, result.get("minimum").asInt());
      assertEquals(150, result.get("maximum").asInt());
      assertEquals("Person's age", result.get("description").asString());
    }

    @Test
    void canAccessDefault() throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "status": {
                "type": "string",
                "default": "pending"
              }
            }
          }
          """;
      var navigator = JsonSchemaPathNavigator.of(schema);
      JsonNode result = navigator.getSchema("$.status");

      assertEquals("pending", result.get("default").asString());
    }
  }

}
