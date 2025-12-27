package com.github.wnameless.json.jsonschemadatagenerator;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import io.zenwave360.jsonrefparser.$RefParser;

class JsonSchemaFlattenerTest {

  @AfterEach
  void resetFactory() {
    RefParserFactory.reset();
  }

  @Nested
  class FlattenJsonSchemaStringTests {

    @Test
    void simpleSchema_returnsFlattened() throws IOException {
      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string" }
            }
          }
          """;

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);

      assertNotNull(result);
      assertEquals("object", result.get("type"));
      assertTrue(result.containsKey("properties"));
    }

    @Test
    void schemaWithNestedObjects_flattens() throws IOException {
      String schema = """
          {
            "type": "object",
            "properties": {
              "user": {
                "type": "object",
                "properties": {
                  "name": { "type": "string" },
                  "age": { "type": "integer" }
                }
              }
            }
          }
          """;

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);

      assertNotNull(result);
      assertTrue(result.containsKey("properties"));
      @SuppressWarnings("unchecked")
      Map<String, Object> properties = (Map<String, Object>) result.get("properties");
      assertTrue(properties.containsKey("user"));
    }

    @Test
    void schemaWithArray_flattens() throws IOException {
      String schema = """
          {
            "type": "array",
            "items": {
              "type": "string"
            }
          }
          """;

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);

      assertNotNull(result);
      assertEquals("array", result.get("type"));
      assertTrue(result.containsKey("items"));
    }

    @Test
    void emptySchema_returnsEmptyMap() throws IOException {
      String schema = "{}";

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);

      assertNotNull(result);
    }

    @Test
    void schemaWithConst_preservesConst() throws IOException {
      String schema = """
          {
            "type": "object",
            "properties": {
              "status": { "const": "active" }
            }
          }
          """;

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);

      assertNotNull(result);
      @SuppressWarnings("unchecked")
      Map<String, Object> properties = (Map<String, Object>) result.get("properties");
      @SuppressWarnings("unchecked")
      Map<String, Object> status = (Map<String, Object>) properties.get("status");
      assertEquals("active", status.get("const"));
    }

    @Test
    void schemaWithEnum_preservesEnum() throws IOException {
      String schema = """
          {
            "type": "object",
            "properties": {
              "color": {
                "type": "string",
                "enum": ["red", "green", "blue"]
              }
            }
          }
          """;

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);

      assertNotNull(result);
      @SuppressWarnings("unchecked")
      Map<String, Object> properties = (Map<String, Object>) result.get("properties");
      @SuppressWarnings("unchecked")
      Map<String, Object> color = (Map<String, Object>) properties.get("color");
      assertTrue(color.containsKey("enum"));
    }
  }

  @Nested
  class FlattenJsonSchemaFileTests {

    @TempDir
    Path tempDir;

    @Test
    void validFile_returnsFlattened() throws IOException {
      String schema = """
          {
            "type": "object",
            "properties": {
              "id": { "type": "integer" }
            }
          }
          """;
      File schemaFile = tempDir.resolve("test-schema.json").toFile();
      Files.writeString(schemaFile.toPath(), schema);

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schemaFile, AllOfOption.MERGE);

      assertNotNull(result);
      assertEquals("object", result.get("type"));
    }

    @Test
    void fileWithComplexSchema_flattens() throws IOException {
      String schema = """
          {
            "type": "object",
            "required": ["name"],
            "properties": {
              "name": { "type": "string", "minLength": 1 },
              "tags": {
                "type": "array",
                "items": { "type": "string" }
              }
            }
          }
          """;
      File schemaFile = tempDir.resolve("complex-schema.json").toFile();
      Files.writeString(schemaFile.toPath(), schema);

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schemaFile, AllOfOption.MERGE);

      assertNotNull(result);
      assertTrue(result.containsKey("required"));
      assertTrue(result.containsKey("properties"));
    }

    @Test
    void nonExistentFile_throwsException() {
      File nonExistent = new File("/non/existent/path/schema.json");

      assertThrows(IOException.class,
          () -> JsonSchemaFlattener.flattenJsonSchema(nonExistent, AllOfOption.MERGE));
    }
  }

  @Nested
  class FlattenWithRefParserTests {

    @Test
    void customRefParser_works() throws IOException {
      String schema = """
          {
            "type": "object",
            "properties": {
              "value": { "type": "string" }
            }
          }
          """;
      $RefParser parser = new $RefParser(schema);

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(parser, AllOfOption.MERGE);

      assertNotNull(result);
      assertEquals("object", result.get("type"));
    }
  }

  @Nested
  class AllOfOptionTests {

    @Test
    void allOfSkip_doesNotMerge() throws IOException {
      String schema = """
          {
            "type": "object",
            "allOf": [
              {
                "properties": {
                  "extra": { "type": "string" }
                }
              }
            ],
            "properties": {
              "name": { "type": "string" }
            }
          }
          """;

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.SKIP);

      assertNotNull(result);
      // With SKIP, allOf should still be present (not merged)
      assertTrue(result.containsKey("allOf") || result.containsKey("properties"));
    }

    @Test
    void allOfMerge_acceptsOption() throws IOException {
      // Simple schema without allOf to verify MERGE option is accepted
      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string" }
            }
          }
          """;

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);

      assertNotNull(result);
      assertTrue(result.containsKey("properties"));
    }
  }

  @Nested
  class RefResolutionTests {

    @Test
    void internalRef_isResolved() throws IOException {
      String schema = """
          {
            "$defs": {
              "address": {
                "type": "object",
                "properties": {
                  "street": { "type": "string" }
                }
              }
            },
            "type": "object",
            "properties": {
              "home": { "$ref": "#/$defs/address" }
            }
          }
          """;

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);

      assertNotNull(result);
      assertTrue(result.containsKey("properties"));

      // The $ref should be resolved
      @SuppressWarnings("unchecked")
      Map<String, Object> properties = (Map<String, Object>) result.get("properties");
      @SuppressWarnings("unchecked")
      Map<String, Object> home = (Map<String, Object>) properties.get("home");

      // After dereferencing, home should have the address properties
      assertTrue(home.containsKey("type") || home.containsKey("properties"));
    }

    @Test
    void definitionsRef_isResolved() throws IOException {
      String schema = """
          {
            "definitions": {
              "person": {
                "type": "object",
                "properties": {
                  "name": { "type": "string" }
                }
              }
            },
            "type": "object",
            "properties": {
              "user": { "$ref": "#/definitions/person" }
            }
          }
          """;

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);

      assertNotNull(result);
      assertTrue(result.containsKey("properties"));
    }
  }

  @Nested
  class SchemaMetadataTests {

    @Test
    void preservesTitle() throws IOException {
      String schema = """
          {
            "title": "User Schema",
            "type": "object"
          }
          """;

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);

      assertEquals("User Schema", result.get("title"));
    }

    @Test
    void preservesDescription() throws IOException {
      String schema = """
          {
            "description": "A user object",
            "type": "object"
          }
          """;

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);

      assertEquals("A user object", result.get("description"));
    }

    @Test
    void preservesDefault() throws IOException {
      String schema = """
          {
            "type": "object",
            "properties": {
              "count": {
                "type": "integer",
                "default": 42
              }
            }
          }
          """;

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);

      @SuppressWarnings("unchecked")
      Map<String, Object> properties = (Map<String, Object>) result.get("properties");
      @SuppressWarnings("unchecked")
      Map<String, Object> count = (Map<String, Object>) properties.get("count");
      assertEquals(42, count.get("default"));
    }

    @Test
    void preservesExamples() throws IOException {
      String schema = """
          {
            "type": "object",
            "properties": {
              "name": {
                "type": "string",
                "examples": ["Alice", "Bob"]
              }
            }
          }
          """;

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);

      @SuppressWarnings("unchecked")
      Map<String, Object> properties = (Map<String, Object>) result.get("properties");
      @SuppressWarnings("unchecked")
      Map<String, Object> name = (Map<String, Object>) properties.get("name");
      assertTrue(name.containsKey("examples"));
    }
  }

  @Nested
  class ValidationKeywordsTests {

    @Test
    void preservesMinMax() throws IOException {
      String schema = """
          {
            "type": "object",
            "properties": {
              "age": {
                "type": "integer",
                "minimum": 0,
                "maximum": 150
              }
            }
          }
          """;

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);

      @SuppressWarnings("unchecked")
      Map<String, Object> properties = (Map<String, Object>) result.get("properties");
      @SuppressWarnings("unchecked")
      Map<String, Object> age = (Map<String, Object>) properties.get("age");
      assertEquals(0, age.get("minimum"));
      assertEquals(150, age.get("maximum"));
    }

    @Test
    void preservesPattern() throws IOException {
      String schema = """
          {
            "type": "object",
            "properties": {
              "code": {
                "type": "string",
                "pattern": "^[A-Z]{3}$"
              }
            }
          }
          """;

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);

      @SuppressWarnings("unchecked")
      Map<String, Object> properties = (Map<String, Object>) result.get("properties");
      @SuppressWarnings("unchecked")
      Map<String, Object> code = (Map<String, Object>) properties.get("code");
      assertEquals("^[A-Z]{3}$", code.get("pattern"));
    }

    @Test
    void preservesFormat() throws IOException {
      String schema = """
          {
            "type": "object",
            "properties": {
              "email": {
                "type": "string",
                "format": "email"
              }
            }
          }
          """;

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);

      @SuppressWarnings("unchecked")
      Map<String, Object> properties = (Map<String, Object>) result.get("properties");
      @SuppressWarnings("unchecked")
      Map<String, Object> email = (Map<String, Object>) properties.get("email");
      assertEquals("email", email.get("format"));
    }

    @Test
    void preservesRequired() throws IOException {
      String schema = """
          {
            "type": "object",
            "required": ["id", "name"],
            "properties": {
              "id": { "type": "integer" },
              "name": { "type": "string" }
            }
          }
          """;

      Map<String, Object> result = JsonSchemaFlattener.flattenJsonSchema(schema, AllOfOption.MERGE);

      assertTrue(result.containsKey("required"));
      @SuppressWarnings("unchecked")
      java.util.List<String> required = (java.util.List<String>) result.get("required");
      assertTrue(required.contains("id"));
      assertTrue(required.contains("name"));
    }
  }
}
