package com.github.wnameless.json.jsonschemadatagenerator;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import tools.jackson.databind.JsonNode;

class JsonSchemaDataGeneratorTest {

  // Set to true to write test outputs to target/test-output/
  private static final boolean WRITE_OUTPUT = true;

  private static final String SCHEMAS_PATH = "src/test/resources/json-schemas/";
  private static final Path OUTPUT_PATH = Path.of("target/test-output");

  private JsonSchemaDataGenerator generator;

  @BeforeAll
  static void setUpOutput() throws IOException {
    if (WRITE_OUTPUT) {
      Files.createDirectories(OUTPUT_PATH);
    }
  }

  @BeforeEach
  void setUp() {
    generator = JsonSchemaDataGenerator.builder().build();
  }

  private static void writeOutput(TestInfo testInfo, JsonNode result) {
    if (!WRITE_OUTPUT) {
      return;
    }
    String className = testInfo.getTestClass().map(Class::getSimpleName).orElse("Unknown");
    String methodName = testInfo.getTestMethod().map(m -> m.getName()).orElse("unknown");
    String fileName = className + "_" + methodName + ".json";
    try {
      Files.writeString(OUTPUT_PATH.resolve(fileName), result.toPrettyString());
    } catch (IOException e) {
      System.err.println("Failed to write output: " + e.getMessage());
    }
  }

  @Nested
  class EnumOptionTests {

    private static final String ENUM_SCHEMA = """
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

    @Test
    void enumOption_NULL_returnsNull(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().enumOption(EnumOption.NULL).build();
      JsonNode result = gen.generate(ENUM_SCHEMA);
      writeOutput(testInfo, result);
      assertTrue(result.get("color").isNull());
    }

    @Test
    void enumOption_FIRST_returnsFirstValue(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().enumOption(EnumOption.FIRST).build();
      JsonNode result = gen.generate(ENUM_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals("red", result.get("color").asString());
    }

    @Test
    void enumOption_LAST_returnsLastValue(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().enumOption(EnumOption.LAST).build();
      JsonNode result = gen.generate(ENUM_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals("blue", result.get("color").asString());
    }

    @Test
    void enumOption_RANDOM_returnsValidValue(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().enumOption(EnumOption.RANDOM).build();
      JsonNode result = gen.generate(ENUM_SCHEMA);
      writeOutput(testInfo, result);
      String value = result.get("color").asString();
      assertTrue(value.equals("red") || value.equals("green") || value.equals("blue"));
    }
  }

  @Nested
  class AnyOfOptionTests {

    private static final String ANYOF_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "value": {
              "anyOf": [
                { "type": "string", "enum": ["first"] },
                { "type": "string", "enum": ["second"] },
                { "type": "string", "enum": ["third"] }
              ]
            }
          }
        }
        """;

    @Test
    void anyOfOption_NULL_returnsNull(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().anyOfOption(AnyOfOption.NULL).build();
      JsonNode result = gen.generate(ANYOF_SCHEMA);
      writeOutput(testInfo, result);
      assertTrue(result.get("value").isNull());
    }

    @Test
    void anyOfOption_FIRST_returnsFirstBranch(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().anyOfOption(AnyOfOption.FIRST)
          .enumOption(EnumOption.FIRST).build();
      JsonNode result = gen.generate(ANYOF_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals("first", result.get("value").asString());
    }

    @Test
    void anyOfOption_LAST_returnsLastBranch(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().anyOfOption(AnyOfOption.LAST)
          .enumOption(EnumOption.FIRST).build();
      JsonNode result = gen.generate(ANYOF_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals("third", result.get("value").asString());
    }
  }

  @Nested
  class OneOfOptionTests {

    private static final String ONEOF_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "mode": {
              "oneOf": [
                { "const": "alpha" },
                { "const": "beta" },
                { "const": "gamma" }
              ]
            }
          }
        }
        """;

    @Test
    void oneOfOption_NULL_returnsNull(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().oneOfOption(OneOfOption.NULL).build();
      JsonNode result = gen.generate(ONEOF_SCHEMA);
      writeOutput(testInfo, result);
      assertTrue(result.get("mode").isNull());
    }

    @Test
    void oneOfOption_FIRST_returnsFirstBranch(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().oneOfOption(OneOfOption.FIRST).build();
      JsonNode result = gen.generate(ONEOF_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals("alpha", result.get("mode").asString());
    }

    @Test
    void oneOfOption_LAST_returnsLastBranch(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().oneOfOption(OneOfOption.LAST).build();
      JsonNode result = gen.generate(ONEOF_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals("gamma", result.get("mode").asString());
    }
  }

  @Nested
  class ArrayOptionTests {

    private static final String ARRAY_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "items": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "name": { "type": "string" }
                }
              }
            }
          }
        }
        """;

    @Test
    void arrayOption_NULL_returnsEmptyArray(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().arrayOption(ArrayOption.NULL).build();
      JsonNode result = gen.generate(ARRAY_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals(0, result.get("items").size());
    }

    @Test
    void arrayOption_EMPTY_returnsEmptyArray(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().arrayOption(ArrayOption.EMPTY).build();
      JsonNode result = gen.generate(ARRAY_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals(0, result.get("items").size());
    }

    @Test
    void arrayOption_ONE_returnsSingleItem(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().arrayOption(ArrayOption.ONE).build();
      JsonNode result = gen.generate(ARRAY_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals(1, result.get("items").size());
    }

    @Test
    void arrayOption_RANDOM_returnsMultipleItems(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().arrayOption(ArrayOption.RANDOM).build();
      JsonNode result = gen.generate(ARRAY_SCHEMA);
      writeOutput(testInfo, result);
      assertTrue(result.get("items").size() >= 0);
    }

    @Test
    void arrayOption_respectsMinItems(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "items": {
                "type": "array",
                "minItems": 3,
                "items": { "type": "object" }
              }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().arrayOption(ArrayOption.ONE).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);
      assertTrue(result.get("items").size() >= 3);
    }
  }

  @Nested
  class PrimitiveArrayOptionTests {

    private static final String PRIMITIVE_ARRAY_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "tags": {
              "type": "array",
              "items": { "type": "string" }
            }
          }
        }
        """;

    @Test
    void primitiveArrayOption_ONE_returnsSingleItem(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().primitiveArrayOption(PrimitiveArrayOption.ONE)
          .formattedStringOption(FormattedStringOption.DATAFAKER).build();
      JsonNode result = gen.generate(PRIMITIVE_ARRAY_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals(1, result.get("tags").size());
    }

    @Test
    void primitiveArrayOption_EMPTY_returnsEmptyArray(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().primitiveArrayOption(PrimitiveArrayOption.EMPTY)
          .build();
      JsonNode result = gen.generate(PRIMITIVE_ARRAY_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals(0, result.get("tags").size());
    }
  }

  @Nested
  class BooleanOptionTests {

    private static final String BOOLEAN_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "active": { "type": "boolean" }
          }
        }
        """;

    @Test
    void booleanOption_NULL_returnsNull(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().booleanOption(BooleanOption.NULL).build();
      JsonNode result = gen.generate(BOOLEAN_SCHEMA);
      writeOutput(testInfo, result);
      assertTrue(result.get("active").isNull());
    }

    @Test
    void booleanOption_TRUE_returnsTrue(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().booleanOption(BooleanOption.TRUE).build();
      JsonNode result = gen.generate(BOOLEAN_SCHEMA);
      writeOutput(testInfo, result);
      assertTrue(result.get("active").asBoolean());
    }

    @Test
    void booleanOption_FALSE_returnsFalse(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().booleanOption(BooleanOption.FALSE).build();
      JsonNode result = gen.generate(BOOLEAN_SCHEMA);
      writeOutput(testInfo, result);
      assertFalse(result.get("active").asBoolean());
    }

    @Test
    void booleanOption_RANDOM_returnsBoolean(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().booleanOption(BooleanOption.RANDOM).build();
      JsonNode result = gen.generate(BOOLEAN_SCHEMA);
      writeOutput(testInfo, result);
      assertTrue(result.get("active").isBoolean());
    }
  }

  @Nested
  class FormattedStringOptionTests {

    @Test
    void formattedStringOption_NULL_returnsNull(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string" }
            }
          }
          """;
      var gen =
          JsonSchemaDataGenerator.builder().formattedStringOption(FormattedStringOption.NULL).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);
      assertTrue(result.get("name").isNull());
    }

    @Test
    void formattedStringOption_FAKE_generatesString(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string" }
            }
          }
          """;
      var gen =
          JsonSchemaDataGenerator.builder().formattedStringOption(FormattedStringOption.DATAFAKER).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);
      assertTrue(result.get("name").isString());
    }

    @Test
    void stringFormat_email_generatesValidEmail(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "email": { "type": "string", "format": "email" }
            }
          }
          """;
      var gen =
          JsonSchemaDataGenerator.builder().formattedStringOption(FormattedStringOption.DATAFAKER).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);
      String email = result.get("email").asString();
      assertTrue(email.contains("@"));
    }

    @Test
    void stringFormat_uuid_generatesValidUUID(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "id": { "type": "string", "format": "uuid" }
            }
          }
          """;
      var gen =
          JsonSchemaDataGenerator.builder().formattedStringOption(FormattedStringOption.DATAFAKER).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);
      String uuid = result.get("id").asString();
      assertEquals(36, uuid.length());
      assertTrue(uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    void stringPattern_generatesMatchingString(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "code": { "type": "string", "pattern": "[A-Z]{3}[0-9]{3}" }
            }
          }
          """;
      var gen =
          JsonSchemaDataGenerator.builder().formattedStringOption(FormattedStringOption.DATAFAKER).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);
      String code = result.get("code").asString();
      assertTrue(code.matches("[A-Z]{3}[0-9]{3}"));
    }

    @Test
    void stringMinLength_respectsConstraint(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string", "minLength": 10 }
            }
          }
          """;
      var gen =
          JsonSchemaDataGenerator.builder().formattedStringOption(FormattedStringOption.DATAFAKER).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);
      String name = result.get("name").asString();
      assertTrue(name.length() >= 10);
    }
  }

  @Nested
  class ConstrainedNumberOptionTests {

    private static final String NUMBER_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "age": { "type": "integer", "minimum": 18, "maximum": 65 }
          }
        }
        """;

    @Test
    void constrainedNumberOption_NULL_returnsNull(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().constrainedNumberOption(ConstrainedNumberOption.NULL).build();
      JsonNode result = gen.generate(NUMBER_SCHEMA);
      writeOutput(testInfo, result);
      assertTrue(result.get("age").isNull());
    }

    @Test
    void constrainedNumberOption_MINIMUM_returnsMinimum(TestInfo testInfo) throws Exception {
      var gen =
          JsonSchemaDataGenerator.builder().constrainedNumberOption(ConstrainedNumberOption.MINIMUM).build();
      JsonNode result = gen.generate(NUMBER_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals(18, result.get("age").asInt());
    }

    @Test
    void constrainedNumberOption_MAXIMUM_returnsMaximum(TestInfo testInfo) throws Exception {
      var gen =
          JsonSchemaDataGenerator.builder().constrainedNumberOption(ConstrainedNumberOption.MAXIMUM).build();
      JsonNode result = gen.generate(NUMBER_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals(65, result.get("age").asInt());
    }

    @Test
    void constrainedNumberOption_MIDPOINT_returnsMidpoint(TestInfo testInfo) throws Exception {
      var gen =
          JsonSchemaDataGenerator.builder().constrainedNumberOption(ConstrainedNumberOption.MIDPOINT).build();
      JsonNode result = gen.generate(NUMBER_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals(41, result.get("age").asInt()); // (18+65)/2 = 41
    }

    @Test
    void constrainedNumberOption_FAKE_returnsValueInRange(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().constrainedNumberOption(ConstrainedNumberOption.DATAFAKER).build();
      JsonNode result = gen.generate(NUMBER_SCHEMA);
      writeOutput(testInfo, result);
      int age = result.get("age").asInt();
      assertTrue(age >= 18 && age <= 65);
    }

    @Test
    void constrainedNumberOption_RANDOM_returnsAnyValue(TestInfo testInfo) throws Exception {
      var gen =
          JsonSchemaDataGenerator.builder().constrainedNumberOption(ConstrainedNumberOption.RANDOM).build();
      JsonNode result = gen.generate(NUMBER_SCHEMA);
      writeOutput(testInfo, result);
      assertTrue(result.get("age").isIntegralNumber());
    }

    @Test
    void numberMultipleOf_respectsConstraint(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "value": { "type": "integer", "minimum": 0, "maximum": 100, "multipleOf": 5 }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().constrainedNumberOption(ConstrainedNumberOption.DATAFAKER).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);
      int value = result.get("value").asInt();
      assertEquals(0, value % 5);
    }
  }

  @Nested
  class DefaultValueOptionTests {

    private static final String DEFAULT_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "name": { "type": "string", "default": "John Doe" }
          }
        }
        """;

    @Test
    void defaultValueOption_USE_returnsDefaultValue(TestInfo testInfo) throws Exception {
      var gen =
          JsonSchemaDataGenerator.builder().defaultValueOption(DefaultValueOption.USE).build();
      JsonNode result = gen.generate(DEFAULT_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals("John Doe", result.get("name").asString());
    }

    @Test
    void defaultValueOption_IGNORE_ignoresDefaultValue(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().defaultValueOption(DefaultValueOption.IGNORE)
          .formattedStringOption(FormattedStringOption.DATAFAKER).build();
      JsonNode result = gen.generate(DEFAULT_SCHEMA);
      writeOutput(testInfo, result);
      assertNotEquals("John Doe", result.get("name").asString());
    }
  }

  @Nested
  class PropertyScopeOptionTests {

    private static final String REQUIRED_SCHEMA = """
        {
          "type": "object",
          "required": ["id"],
          "properties": {
            "id": { "type": "integer" },
            "name": { "type": "string" },
            "email": { "type": "string" }
          }
        }
        """;

    @Test
    void propertyScopeOption_ALL_includesAllProperties(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().propertyScopeOption(PropertyScopeOption.ALL)
          .constrainedNumberOption(ConstrainedNumberOption.DATAFAKER).build();
      JsonNode result = gen.generate(REQUIRED_SCHEMA);
      writeOutput(testInfo, result);
      assertTrue(result.has("id"));
      assertTrue(result.has("name"));
      assertTrue(result.has("email"));
    }

    @Test
    void propertyScopeOption_REQUIRED_ONLY_includesOnlyRequired(TestInfo testInfo) throws Exception {
      var gen =
          JsonSchemaDataGenerator.builder().propertyScopeOption(PropertyScopeOption.REQUIRED_ONLY)
              .constrainedNumberOption(ConstrainedNumberOption.DATAFAKER).build();
      JsonNode result = gen.generate(REQUIRED_SCHEMA);
      writeOutput(testInfo, result);
      assertTrue(result.has("id"));
      assertFalse(result.has("name"));
      assertFalse(result.has("email"));
    }
  }

  @Nested
  class ConstTests {

    @Test
    void const_returnsConstValue(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "type": { "const": "user" }
            }
          }
          """;
      JsonNode result = generator.generate(schema);
      writeOutput(testInfo, result);
      assertEquals("user", result.get("type").asString());
    }
  }

  @Nested
  class FluentApiTests {

    @Test
    void withEnumOption_createsNewInstance() {
      var original = JsonSchemaDataGenerator.builder().build();
      var modified = original.withEnumOption(EnumOption.RANDOM);
      assertNotSame(original, modified);
    }

    @Test
    void fluent_chainedCalls(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "color": { "type": "string", "enum": ["red", "green"] },
              "active": { "type": "boolean" }
            }
          }
          """;
      JsonNode result = JsonSchemaDataGenerator.builder().build().withEnumOption(EnumOption.LAST)
          .withBooleanOption(BooleanOption.TRUE).generate(schema);
      writeOutput(testInfo, result);

      assertEquals("green", result.get("color").asString());
      assertTrue(result.get("active").asBoolean());
    }
  }

  @Nested
  class FileBasedTests {

    @Test
    void generate_alternativesJson(TestInfo testInfo) throws Exception {
      File file = new File(SCHEMAS_PATH + "alternatives.json");
      var gen = JsonSchemaDataGenerator.builder().enumOption(EnumOption.FIRST)
          .anyOfOption(AnyOfOption.FIRST).oneOfOption(OneOfOption.FIRST).build();
      JsonNode result = gen.generate(file);
      writeOutput(testInfo, result);

      assertTrue(result.has("currentColor"));
      assertTrue(result.has("colorMask"));
      assertTrue(result.has("blendMode"));
    }

    @Test
    void generate_validationJson(TestInfo testInfo) throws Exception {
      File file = new File(SCHEMAS_PATH + "validation.json");
      var gen = JsonSchemaDataGenerator.builder().formattedStringOption(FormattedStringOption.DATAFAKER)
          .constrainedNumberOption(ConstrainedNumberOption.DATAFAKER).build();
      JsonNode result = gen.generate(file);
      writeOutput(testInfo, result);

      assertTrue(result.has("firstName"));
      assertTrue(result.has("pass1"));
      assertTrue(result.has("age"));
      assertTrue(result.get("age").asInt() >= 18);
    }

    @Test
    void generate_referencesJson(TestInfo testInfo) throws Exception {
      File file = new File(SCHEMAS_PATH + "references.json");
      var gen =
          JsonSchemaDataGenerator.builder().formattedStringOption(FormattedStringOption.DATAFAKER).build();
      JsonNode result = gen.generate(file);
      writeOutput(testInfo, result);

      assertTrue(result.has("billing_address"));
      assertTrue(result.has("shipping_address"));
      assertTrue(result.get("billing_address").has("street_address"));
    }

    @Test
    void generate_largeJson_withRandomEnum(TestInfo testInfo) throws Exception {
      File file = new File(SCHEMAS_PATH + "large.json");
      var gen = JsonSchemaDataGenerator.builder().enumOption(EnumOption.RANDOM)
          .formattedStringOption(FormattedStringOption.DATAFAKER).build();
      JsonNode result = gen.generate(file);
      writeOutput(testInfo, result);

      assertTrue(result.has("choice1"));
      String choice = result.get("choice1").asString();
      assertTrue(choice.startsWith("option #"));
    }

    @Test
    void generate_errorsJson_withConstraints(TestInfo testInfo) throws Exception {
      File file = new File(SCHEMAS_PATH + "errors.json");
      var gen = JsonSchemaDataGenerator.builder().formattedStringOption(FormattedStringOption.DATAFAKER)
          .booleanOption(BooleanOption.RANDOM).primitiveArrayOption(PrimitiveArrayOption.ONE)
          .enumOption(EnumOption.RANDOM).build();
      JsonNode result = gen.generate(file);
      writeOutput(testInfo, result);

      assertTrue(result.has("firstName"));
      assertTrue(result.has("active"));
      assertTrue(result.has("skills"));
      assertTrue(result.has("multipleChoicesList"));
    }

    @Test
    void generate_comprehensiveJson_withAllFeatures(TestInfo testInfo) throws Exception {
      File file = new File(SCHEMAS_PATH + "comprehensive.json");
      var gen = JsonSchemaDataGenerator.builder()
          .formattedStringOption(FormattedStringOption.DATAFAKER)
          .constrainedNumberOption(ConstrainedNumberOption.DATAFAKER)
          .enumOption(EnumOption.RANDOM)
          .booleanOption(BooleanOption.RANDOM)
          .arrayOption(ArrayOption.ONE)
          .primitiveArrayOption(PrimitiveArrayOption.ONE)
          .oneOfOption(OneOfOption.FIRST)
          .anyOfOption(AnyOfOption.FIRST)
          .containsOption(ContainsOption.FIRST)
          .examplesOption(ExamplesOption.RANDOM)
          .additionalPropertiesOption(AdditionalPropertiesOption.GENERATE_ONE)
          .patternPropertiesOption(PatternPropertiesOption.GENERATE_ONE)
          .propertyScopeOption(PropertyScopeOption.ALL)
          .build();
      JsonNode result = gen.generate(file);
      writeOutput(testInfo, result);

      // Required fields
      assertTrue(result.has("id"));
      assertTrue(result.has("name"));
      assertTrue(result.has("status"));
      assertTrue(result.has("profile"));
      assertTrue(result.has("tags"));
      assertTrue(result.has("metadata"));

      // Const field
      assertEquals("FIXED_VALUE", result.get("constField").asString());

      // Default value for score
      assertTrue(result.has("score"));

      // Nested profile with $ref
      JsonNode profile = result.get("profile");
      assertTrue(profile.has("firstName"));
      assertTrue(profile.has("lastName"));

      // Deep nested object
      assertTrue(result.has("deepNested"));
      JsonNode deepNested = result.get("deepNested");
      assertTrue(deepNested.has("level1"));

      // Tuple arrays
      assertTrue(result.has("coordinates"));
      assertTrue(result.has("mixedTuple"));

      // Matrix (3D nested array)
      assertTrue(result.has("matrix"));
      assertTrue(result.get("matrix").isArray());

      // oneOf/anyOf
      assertTrue(result.has("paymentMethod"));
      assertTrue(result.has("contactInfo"));

      // Organization with recursive $ref
      assertTrue(result.has("organization"));
      JsonNode org = result.get("organization");
      assertTrue(org.has("name"));
      assertTrue(org.has("type"));

      // Formatted strings
      assertTrue(result.has("formattedStrings"));
      JsonNode formats = result.get("formattedStrings");
      assertTrue(formats.has("email"));
      assertTrue(formats.has("uuid"));
      assertTrue(formats.has("dateTime"));
    }
  }

  @Nested
  class UniqueItemsOptionTests {

    private static final String UNIQUE_ITEMS_ENUM_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "colors": {
              "type": "array",
              "uniqueItems": true,
              "items": {
                "type": "string",
                "enum": ["red", "green", "blue", "yellow", "purple"]
              }
            }
          }
        }
        """;

    private static final String UNIQUE_ITEMS_BOOLEAN_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "flags": {
              "type": "array",
              "uniqueItems": true,
              "minItems": 3,
              "items": {
                "type": "boolean"
              }
            }
          }
        }
        """;

    @Test
    void uniqueItemsOption_ENFORCE_generatesUniqueValues(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().uniqueItemsOption(UniqueItemsOption.ENFORCE)
          .enumOption(EnumOption.RANDOM).primitiveArrayOption(PrimitiveArrayOption.RANDOM).build();
      JsonNode result = gen.generate(UNIQUE_ITEMS_ENUM_SCHEMA);
      writeOutput(testInfo, result);

      JsonNode colors = result.get("colors");
      java.util.Set<String> seen = new java.util.HashSet<>();
      for (JsonNode color : colors) {
        String value = color.asString();
        assertFalse(seen.contains(value), "Duplicate value found: " + value);
        seen.add(value);
      }
    }

    @Test
    void uniqueItemsOption_IGNORE_allowsDuplicates(TestInfo testInfo) throws Exception {
      // With IGNORE and FIRST enum option, all items will be the same
      var gen = JsonSchemaDataGenerator.builder().uniqueItemsOption(UniqueItemsOption.IGNORE)
          .enumOption(EnumOption.FIRST).primitiveArrayOption(PrimitiveArrayOption.RANDOM).build();
      JsonNode result = gen.generate(UNIQUE_ITEMS_ENUM_SCHEMA);
      writeOutput(testInfo, result);

      JsonNode colors = result.get("colors");
      if (colors.size() > 1) {
        // All items should be "red" (first enum value)
        for (JsonNode color : colors) {
          assertEquals("red", color.asString());
        }
      }
    }

    @Test
    void uniqueItemsOption_ENFORCE_stopsWhenExhausted(TestInfo testInfo) throws Exception {
      // Boolean array with minItems=3 but only 2 unique values possible
      var gen = JsonSchemaDataGenerator.builder().uniqueItemsOption(UniqueItemsOption.ENFORCE)
          .booleanOption(BooleanOption.RANDOM).primitiveArrayOption(PrimitiveArrayOption.ONE)
          .build();

      JsonNode result = gen.generate(UNIQUE_ITEMS_BOOLEAN_SCHEMA);
      writeOutput(testInfo, result);

      JsonNode flags = result.get("flags");
      // Should have at most 2 unique boolean values (true and false)
      assertTrue(flags.size() <= 2, "Should stop when unique values exhausted");
    }

    @Test
    void uniqueItemsOption_defaultIsENFORCE(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().enumOption(EnumOption.RANDOM)
          .primitiveArrayOption(PrimitiveArrayOption.RANDOM).build();
      JsonNode result = gen.generate(UNIQUE_ITEMS_ENUM_SCHEMA);
      writeOutput(testInfo, result);

      JsonNode colors = result.get("colors");
      java.util.Set<String> seen = new java.util.HashSet<>();
      for (JsonNode color : colors) {
        String value = color.asString();
        assertFalse(seen.contains(value), "Default should enforce uniqueness");
        seen.add(value);
      }
    }

    @Test
    void withUniqueItemsOption_fluentApi(TestInfo testInfo) throws Exception {
      var original = JsonSchemaDataGenerator.builder().build();
      var modified = original.withUniqueItemsOption(UniqueItemsOption.IGNORE);
      assertNotSame(original, modified);
    }

    @Test
    void uniqueItemsOption_ENFORCE_withObjectArray(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "users": {
                "type": "array",
                "uniqueItems": true,
                "items": {
                  "type": "object",
                  "properties": {
                    "role": {
                      "type": "string",
                      "enum": ["admin", "user", "guest"]
                    }
                  }
                }
              }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().uniqueItemsOption(UniqueItemsOption.ENFORCE)
          .enumOption(EnumOption.RANDOM).arrayOption(ArrayOption.RANDOM).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      JsonNode users = result.get("users");
      java.util.Set<String> seen = new java.util.HashSet<>();
      for (JsonNode user : users) {
        String jsonStr = user.toString();
        assertFalse(seen.contains(jsonStr), "Duplicate object found: " + jsonStr);
        seen.add(jsonStr);
      }
    }

    @Test
    void uniqueItemsOption_ENFORCE_withComplexObjectArray(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "items": {
                "type": "array",
                "uniqueItems": true,
                "items": {
                  "type": "object",
                  "properties": {
                    "id": { "type": "integer", "minimum": 1, "maximum": 5 },
                    "active": { "type": "boolean" }
                  }
                }
              }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().uniqueItemsOption(UniqueItemsOption.ENFORCE)
          .constrainedNumberOption(ConstrainedNumberOption.RANDOM).booleanOption(BooleanOption.RANDOM)
          .arrayOption(ArrayOption.RANDOM).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      JsonNode items = result.get("items");
      java.util.Set<String> seen = new java.util.HashSet<>();
      for (JsonNode item : items) {
        String jsonStr = item.toString();
        assertFalse(seen.contains(jsonStr), "Duplicate complex object found: " + jsonStr);
        seen.add(jsonStr);
      }
    }
  }

  @Nested
  class ExclusiveMinMaxTests {

    @Test
    void exclusiveMinimum_integer_excludesBoundary(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "value": { "type": "integer", "exclusiveMinimum": 10 }
            }
          }
          """;
      var gen =
          JsonSchemaDataGenerator.builder().constrainedNumberOption(ConstrainedNumberOption.MINIMUM).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      int value = result.get("value").asInt();
      assertTrue(value > 10, "Value should be greater than exclusiveMinimum");
    }

    @Test
    void exclusiveMaximum_integer_excludesBoundary(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "value": { "type": "integer", "minimum": 0, "exclusiveMaximum": 100 }
            }
          }
          """;
      var gen =
          JsonSchemaDataGenerator.builder().constrainedNumberOption(ConstrainedNumberOption.MAXIMUM).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      int value = result.get("value").asInt();
      assertTrue(value < 100, "Value should be less than exclusiveMaximum");
    }

    @Test
    void exclusiveBounds_integer_withMinimumOption(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "value": { "type": "integer", "exclusiveMinimum": 5, "exclusiveMaximum": 10 }
            }
          }
          """;
      var gen =
          JsonSchemaDataGenerator.builder().constrainedNumberOption(ConstrainedNumberOption.MINIMUM).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      int value = result.get("value").asInt();
      assertEquals(6, value, "Minimum should be exclusiveMinimum + 1");
    }

    @Test
    void exclusiveBounds_integer_withMaximumOption(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "value": { "type": "integer", "exclusiveMinimum": 5, "exclusiveMaximum": 10 }
            }
          }
          """;
      var gen =
          JsonSchemaDataGenerator.builder().constrainedNumberOption(ConstrainedNumberOption.MAXIMUM).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      int value = result.get("value").asInt();
      assertEquals(9, value, "Maximum should be exclusiveMaximum - 1");
    }

    @Test
    void exclusiveMinimum_number_excludesBoundary(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "value": { "type": "number", "exclusiveMinimum": 0.0 }
            }
          }
          """;
      var gen =
          JsonSchemaDataGenerator.builder().constrainedNumberOption(ConstrainedNumberOption.MINIMUM).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      double value = result.get("value").asDouble();
      assertTrue(value > 0.0, "Value should be greater than exclusiveMinimum");
    }

    @Test
    void exclusiveMaximum_number_excludesBoundary(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "value": { "type": "number", "minimum": 0, "exclusiveMaximum": 1.0 }
            }
          }
          """;
      var gen =
          JsonSchemaDataGenerator.builder().constrainedNumberOption(ConstrainedNumberOption.MAXIMUM).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      double value = result.get("value").asDouble();
      assertTrue(value < 1.0, "Value should be less than exclusiveMaximum");
    }

    @Test
    void exclusiveBounds_number_withFakeOption(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "value": { "type": "number", "exclusiveMinimum": 0, "exclusiveMaximum": 100 }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().constrainedNumberOption(ConstrainedNumberOption.DATAFAKER).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      double value = result.get("value").asDouble();
      assertTrue(value > 0 && value < 100, "Value should be within exclusive bounds");
    }

    @Test
    void mixedBounds_minimumWithExclusiveMaximum(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "value": { "type": "integer", "minimum": 0, "exclusiveMaximum": 10 }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().constrainedNumberOption(ConstrainedNumberOption.DATAFAKER).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      int value = result.get("value").asInt();
      assertTrue(value >= 0 && value < 10, "Value should respect minimum and exclusiveMaximum");
    }
  }

  @Nested
  class EdgeCaseTests {

    @Test
    void emptyObject_returnsEmptyObject(TestInfo testInfo) throws Exception {
      String schema = """
          { "type": "object" }
          """;
      JsonNode result = generator.generate(schema);
      writeOutput(testInfo, result);
      assertTrue(result.isObject());
      assertEquals(0, result.size());
    }

    @Test
    void arrayWithoutItems_returnsEmptyArray(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "list": { "type": "array" }
            }
          }
          """;
      JsonNode result = generator.generate(schema);
      writeOutput(testInfo, result);
      assertTrue(result.get("list").isArray());
      assertEquals(0, result.get("list").size());
    }

    @Test
    void nullType_returnsNull(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "nothing": { "type": "null" }
            }
          }
          """;
      JsonNode result = generator.generate(schema);
      writeOutput(testInfo, result);
      assertTrue(result.get("nothing").isNull());
    }

    @Test
    void emptyEnum_returnsNull(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "empty": { "type": "string", "enum": [] }
            }
          }
          """;
      JsonNode result = generator.generate(schema);
      writeOutput(testInfo, result);
      assertTrue(result.get("empty").isNull());
    }

    @Test
    void nestedObjects_generatesRecursively(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "level1": {
                "type": "object",
                "properties": {
                  "level2": {
                    "type": "object",
                    "properties": {
                      "value": { "type": "string", "default": "deep" }
                    }
                  }
                }
              }
            }
          }
          """;
      JsonNode result = generator.generate(schema);
      writeOutput(testInfo, result);
      assertEquals("deep", result.get("level1").get("level2").get("value").asString());
    }
  }

  @Nested
  class ExamplesOptionTests {

    private static final String EXAMPLES_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "name": {
              "type": "string",
              "examples": ["Alice", "Bob", "Charlie"]
            }
          }
        }
        """;

    @Test
    void examplesOption_FIRST_returnsFirstExample(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().examplesOption(ExamplesOption.FIRST).build();
      JsonNode result = gen.generate(EXAMPLES_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals("Alice", result.get("name").asString());
    }

    @Test
    void examplesOption_LAST_returnsLastExample(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().examplesOption(ExamplesOption.LAST).build();
      JsonNode result = gen.generate(EXAMPLES_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals("Charlie", result.get("name").asString());
    }

    @Test
    void examplesOption_RANDOM_returnsValidExample(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().examplesOption(ExamplesOption.RANDOM).build();
      JsonNode result = gen.generate(EXAMPLES_SCHEMA);
      writeOutput(testInfo, result);
      String value = result.get("name").asString();
      assertTrue(value.equals("Alice") || value.equals("Bob") || value.equals("Charlie"));
    }

    @Test
    void examplesOption_NULL_ignoresExamples(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().examplesOption(ExamplesOption.NONE)
          .formattedStringOption(FormattedStringOption.DATAFAKER).build();
      JsonNode result = gen.generate(EXAMPLES_SCHEMA);
      writeOutput(testInfo, result);
      // Should generate a string, not from examples
      assertTrue(result.get("name").isString());
    }

    @Test
    void examples_enumTakesPrecedence(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "status": {
                "type": "string",
                "enum": ["active", "inactive"],
                "examples": ["pending", "archived"]
              }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().enumOption(EnumOption.FIRST)
          .examplesOption(ExamplesOption.FIRST).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);
      assertEquals("active", result.get("status").asString());
    }

    @Test
    void examples_withObjectType(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "config": {
                "type": "object",
                "examples": [{"key": "value1"}, {"key": "value2"}]
              }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().examplesOption(ExamplesOption.FIRST).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);
      assertEquals("value1", result.get("config").get("key").asString());
    }
  }

  @Nested
  class PrefixItemsTests {

    @Test
    void prefixItems_generatesTupleArray(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "tuple": {
                "type": "array",
                "prefixItems": [
                  { "type": "string", "const": "first" },
                  { "type": "integer", "const": 42 },
                  { "type": "boolean", "const": true }
                ]
              }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      JsonNode tuple = result.get("tuple");
      assertEquals(3, tuple.size());
      assertEquals("first", tuple.get(0).asString());
      assertEquals(42, tuple.get(1).asInt());
      assertTrue(tuple.get(2).asBoolean());
    }

    @Test
    void prefixItems_withMinItemsGeneratesExtras(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "data": {
                "type": "array",
                "prefixItems": [
                  { "type": "string", "const": "header" }
                ],
                "items": { "type": "integer", "const": 999 },
                "minItems": 3
              }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      JsonNode data = result.get("data");
      assertEquals(3, data.size());
      assertEquals("header", data.get(0).asString());
      assertEquals(999, data.get(1).asInt());
      assertEquals(999, data.get(2).asInt());
    }

    @Test
    void prefixItems_mixedTypes(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "record": {
                "type": "array",
                "prefixItems": [
                  { "type": "string", "default": "name" },
                  { "type": "number", "default": 3.14 },
                  { "type": "null" }
                ]
              }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      JsonNode record = result.get("record");
      assertEquals(3, record.size());
      assertEquals("name", record.get(0).asString());
      assertEquals(3.14, record.get(1).asDouble(), 0.001);
      assertTrue(record.get(2).isNull());
    }
  }

  @Nested
  class MinMaxPropertiesTests {

    @Test
    void maxProperties_limitsPropertyCount(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "maxProperties": 2,
            "properties": {
              "a": { "type": "string", "default": "A" },
              "b": { "type": "string", "default": "B" },
              "c": { "type": "string", "default": "C" }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);
      assertTrue(result.size() <= 2);
    }

    @Test
    void minProperties_ensuresMinimumCount(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "minProperties": 3,
            "properties": {
              "a": { "type": "string", "default": "A" },
              "b": { "type": "string", "default": "B" },
              "c": { "type": "string", "default": "C" },
              "d": { "type": "string", "default": "D" }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);
      assertTrue(result.size() >= 3);
    }

    @Test
    void requiredAlwaysIncluded_evenWithMaxProperties(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "required": ["id", "name"],
            "maxProperties": 2,
            "properties": {
              "id": { "type": "integer", "default": 1 },
              "name": { "type": "string", "default": "Test" },
              "optional": { "type": "string", "default": "Extra" }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      assertTrue(result.has("id"));
      assertTrue(result.has("name"));
      assertEquals(2, result.size());
    }

    @Test
    void propertyScopeOption_withMaxProperties(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "required": ["id"],
            "maxProperties": 5,
            "properties": {
              "id": { "type": "integer", "default": 1 },
              "name": { "type": "string", "default": "Test" },
              "email": { "type": "string", "default": "test@test.com" }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder()
          .propertyScopeOption(PropertyScopeOption.REQUIRED_ONLY).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      assertTrue(result.has("id"));
      assertEquals(1, result.size());
    }
  }

  @Nested
  class ContainsOptionTests {

    @Test
    void containsOption_FIRST_placesContainsItemFirst(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "items": {
                "type": "array",
                "contains": { "type": "string", "const": "SPECIAL" },
                "items": { "type": "string", "const": "normal" },
                "minItems": 3
              }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().containsOption(ContainsOption.FIRST)
          .primitiveArrayOption(PrimitiveArrayOption.ONE).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      JsonNode items = result.get("items");
      assertTrue(items.size() >= 1);
      assertEquals("SPECIAL", items.get(0).asString());
    }

    @Test
    void containsOption_LAST_placesContainsItemLast(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "items": {
                "type": "array",
                "contains": { "type": "string", "const": "SPECIAL" },
                "items": { "type": "string", "const": "normal" },
                "minItems": 3
              }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().containsOption(ContainsOption.LAST)
          .primitiveArrayOption(PrimitiveArrayOption.ONE).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      JsonNode items = result.get("items");
      assertTrue(items.size() >= 1);
      assertEquals("SPECIAL", items.get(items.size() - 1).asString());
    }

    @Test
    void containsOption_NULL_ignoresContains(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "items": {
                "type": "array",
                "contains": { "type": "string", "const": "SPECIAL" },
                "items": { "type": "string", "const": "normal" }
              }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().containsOption(ContainsOption.NONE)
          .primitiveArrayOption(PrimitiveArrayOption.ONE).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      JsonNode items = result.get("items");
      // Should use items schema only, no contains
      for (JsonNode item : items) {
        assertEquals("normal", item.asString());
      }
    }

    @Test
    void minContains_generatesMultipleMatchingItems(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "data": {
                "type": "array",
                "contains": { "type": "integer", "const": 42 },
                "minContains": 3,
                "items": { "type": "integer", "const": 0 },
                "minItems": 5
              }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().containsOption(ContainsOption.FIRST)
          .primitiveArrayOption(PrimitiveArrayOption.ONE).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      JsonNode data = result.get("data");
      int count42 = 0;
      for (JsonNode item : data) {
        if (item.asInt() == 42) count42++;
      }
      assertTrue(count42 >= 3, "Should have at least 3 items matching contains schema");
    }

    @Test
    void containsWithoutItems_onlyGeneratesContainsItems(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "list": {
                "type": "array",
                "contains": { "type": "string", "const": "only" },
                "minContains": 2
              }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().containsOption(ContainsOption.FIRST).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      JsonNode list = result.get("list");
      assertTrue(list.size() >= 2);
      for (JsonNode item : list) {
        assertEquals("only", item.asString());
      }
    }
  }

  @Nested
  class AdditionalItemsTests {

    @Test
    void legacyTuple_itemsAsArray_generatesCorrectTuple(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "array",
            "items": [
              { "type": "string", "const": "first" },
              { "type": "integer", "const": 42 },
              { "type": "boolean", "const": true }
            ]
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      assertEquals(3, result.size());
      assertEquals("first", result.get(0).asString());
      assertEquals(42, result.get(1).asInt());
      assertTrue(result.get(2).asBoolean());
    }

    @Test
    void legacyTuple_additionalItemsFalse_stopsAfterTuple(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "array",
            "items": [
              { "type": "string", "const": "a" },
              { "type": "string", "const": "b" }
            ],
            "additionalItems": false,
            "minItems": 5
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      // Should only have 2 items (additionalItems: false stops further generation)
      assertEquals(2, result.size());
    }

    @Test
    void legacyTuple_additionalItemsSchema_generatesExtras(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "array",
            "items": [
              { "type": "string", "const": "first" }
            ],
            "additionalItems": { "type": "integer", "const": 99 },
            "minItems": 4
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      assertEquals(4, result.size());
      assertEquals("first", result.get(0).asString());
      assertEquals(99, result.get(1).asInt());
      assertEquals(99, result.get(2).asInt());
      assertEquals(99, result.get(3).asInt());
    }

    @Test
    void prefixItems_withAdditionalItems_generatesExtras(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "array",
            "prefixItems": [
              { "type": "string", "const": "prefix" }
            ],
            "additionalItems": { "type": "integer", "const": 77 },
            "minItems": 3
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      assertEquals(3, result.size());
      assertEquals("prefix", result.get(0).asString());
      assertEquals(77, result.get(1).asInt());
      assertEquals(77, result.get(2).asInt());
    }

    @Test
    void prefixItems_itemsTakesPrecedenceOverAdditionalItems(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "array",
            "prefixItems": [
              { "type": "string", "const": "prefix" }
            ],
            "items": { "type": "string", "const": "items_schema" },
            "additionalItems": { "type": "integer", "const": 88 },
            "minItems": 3
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      assertEquals(3, result.size());
      assertEquals("prefix", result.get(0).asString());
      // items takes precedence over additionalItems in 2020-12 style
      assertEquals("items_schema", result.get(1).asString());
      assertEquals("items_schema", result.get(2).asString());
    }
  }

  @Nested
  class AdditionalPropertiesTests {

    @Test
    void additionalProperties_NULL_doesNotGenerate(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string", "const": "test" }
            },
            "additionalProperties": { "type": "integer" }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder()
          .additionalPropertiesOption(AdditionalPropertiesOption.NONE).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      assertEquals(1, result.size());
      assertTrue(result.has("name"));
    }

    @Test
    void additionalProperties_GENERATE_ONE_generatesOneExtra(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string", "const": "test" }
            },
            "additionalProperties": { "type": "integer", "const": 42 }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder()
          .additionalPropertiesOption(AdditionalPropertiesOption.GENERATE_ONE)
          .constrainedNumberOption(ConstrainedNumberOption.MINIMUM).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      assertEquals(2, result.size());
      assertTrue(result.has("name"));
      assertTrue(result.has("additional_0"));
      assertEquals(42, result.get("additional_0").asInt());
    }

    @Test
    void additionalProperties_GENERATE_FEW_generatesMultiple(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string", "const": "test" }
            },
            "additionalProperties": { "type": "string", "const": "extra" }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder()
          .additionalPropertiesOption(AdditionalPropertiesOption.GENERATE_FEW).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      // 1 regular + 2-4 additional
      assertTrue(result.size() >= 3 && result.size() <= 5);
      assertTrue(result.has("name"));
    }

    @Test
    void additionalProperties_false_doesNotGenerate(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string", "const": "test" }
            },
            "additionalProperties": false
          }
          """;
      var gen = JsonSchemaDataGenerator.builder()
          .additionalPropertiesOption(AdditionalPropertiesOption.GENERATE_ONE).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      assertEquals(1, result.size());
      assertTrue(result.has("name"));
    }

    @Test
    void additionalProperties_withoutProperties_stillGenerates(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "additionalProperties": { "type": "string", "const": "value" }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder()
          .additionalPropertiesOption(AdditionalPropertiesOption.GENERATE_ONE).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      assertEquals(1, result.size());
      assertTrue(result.has("additional_0"));
      assertEquals("value", result.get("additional_0").asString());
    }
  }

  @Nested
  class PatternPropertiesTests {

    @Test
    void patternProperties_NULL_doesNotGenerate(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "patternProperties": {
              "^x-": { "type": "string", "const": "extension" }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder()
          .patternPropertiesOption(PatternPropertiesOption.NONE).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      assertEquals(0, result.size());
    }

    @Test
    void patternProperties_GENERATE_ONE_generatesOnePerPattern(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "patternProperties": {
              "^x-": { "type": "string", "const": "extension" }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder()
          .patternPropertiesOption(PatternPropertiesOption.GENERATE_ONE).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      assertEquals(1, result.size());
      // Property name should match the pattern ^x-
      String propName = result.propertyNames().iterator().next();
      assertTrue(propName.startsWith("x-"), "Property name should start with 'x-': " + propName);
      assertEquals("extension", result.get(propName).asString());
    }

    @Test
    void patternProperties_GENERATE_FEW_generatesMultiplePerPattern(TestInfo testInfo)
        throws Exception {
      // Use a pattern that generates varied output (includes random characters)
      String schema = """
          {
            "type": "object",
            "patternProperties": {
              "^test_[a-z]{3}$": { "type": "integer", "const": 123 }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder()
          .patternPropertiesOption(PatternPropertiesOption.GENERATE_FEW).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      // Should generate 2-4 properties (may be fewer if duplicates are generated)
      assertTrue(result.size() >= 1 && result.size() <= 4);
      for (String name : result.propertyNames()) {
        assertTrue(name.matches("^test_[a-z]{3}$"), "Property should match pattern: " + name);
      }
    }

    @Test
    void patternProperties_multiplePatterns_generatesForEach(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "patternProperties": {
              "^a_": { "type": "string", "const": "a_value" },
              "^b_": { "type": "string", "const": "b_value" }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder()
          .patternPropertiesOption(PatternPropertiesOption.GENERATE_ONE).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      assertEquals(2, result.size());
      boolean hasA = false, hasB = false;
      for (String name : result.propertyNames()) {
        if (name.startsWith("a_")) hasA = true;
        if (name.startsWith("b_")) hasB = true;
      }
      assertTrue(hasA && hasB, "Should have properties matching both patterns");
    }
  }

  @Nested
  class PropertyNamesTests {

    @Test
    void propertyNames_pattern_usedForAdditionalProperties(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "propertyNames": { "pattern": "^x-[a-z]+$" },
            "additionalProperties": { "type": "string", "const": "value" }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder()
          .additionalPropertiesOption(AdditionalPropertiesOption.GENERATE_ONE).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      assertEquals(1, result.size());
      String propName = result.propertyNames().iterator().next();
      assertTrue(propName.matches("^x-[a-z]+$"),
          "Property name should match pattern ^x-[a-z]+$: " + propName);
    }

    @Test
    void propertyNames_fallbackOnNoPattern(TestInfo testInfo) throws Exception {
      String schema = """
          {
            "type": "object",
            "propertyNames": { "minLength": 1 },
            "additionalProperties": { "type": "string", "const": "value" }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder()
          .additionalPropertiesOption(AdditionalPropertiesOption.GENERATE_ONE).build();
      JsonNode result = gen.generate(schema);
      writeOutput(testInfo, result);

      assertEquals(1, result.size());
      // Should use fallback name
      assertTrue(result.has("additional_0"));
    }
  }

  @Nested
  class GeneratorModeTests {

    private static final String TEST_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "required_field": { "type": "string", "format": "email" },
            "optional_field": { "type": "integer", "minimum": 0, "maximum": 100 },
            "items": {
              "type": "array",
              "items": { "type": "string" }
            }
          },
          "required": ["required_field"],
          "additionalProperties": { "type": "string" },
          "patternProperties": {
            "^x-": { "type": "string" }
          }
        }
        """;

    @Test
    void minimal_generatesOnlyRequiredFields(TestInfo testInfo) throws Exception {
      JsonNode result = JsonSchemaDataGenerator.minimal().generate(TEST_SCHEMA);
      writeOutput(testInfo, result);

      // Only required fields
      assertTrue(result.has("required_field"));
      assertFalse(result.has("optional_field"));
      assertFalse(result.has("items"));

      // No additional or pattern properties
      assertEquals(1, result.size());

      // Format should be null (formattedStringOption = NULL)
      assertTrue(result.get("required_field").isNull());
    }

    @Test
    void normal_generatesAllDefinedProperties(TestInfo testInfo) throws Exception {
      JsonNode result = JsonSchemaDataGenerator.normal().generate(TEST_SCHEMA);
      writeOutput(testInfo, result);

      // All defined properties (propertyScopeOption = ALL)
      assertTrue(result.has("required_field"));
      assertTrue(result.has("optional_field"));
      assertTrue(result.has("items"));

      // Format should be fake email (formattedStringOption = FAKE)
      assertFalse(result.get("required_field").isNull());

      // No additional or pattern properties (default is NULL)
      assertEquals(3, result.size());
    }

    @Test
    void verbose_generatesComprehensiveData(TestInfo testInfo) throws Exception {
      JsonNode result = JsonSchemaDataGenerator.verbose().generate(TEST_SCHEMA);
      writeOutput(testInfo, result);

      // All defined properties
      assertTrue(result.has("required_field"));
      assertTrue(result.has("optional_field"));
      assertTrue(result.has("items"));

      // Should have additional properties (additionalPropertiesOption = GENERATE_FEW)
      // and pattern properties (patternPropertiesOption = GENERATE_FEW)
      assertTrue(result.size() > 3, "Should have additional/pattern properties");
    }

    @Test
    void minimal_canBeCustomizedWithFluentApi(TestInfo testInfo) throws Exception {
      JsonNode result = JsonSchemaDataGenerator.minimal()
          .withFormattedStringOption(FormattedStringOption.DATAFAKER).generate(TEST_SCHEMA);
      writeOutput(testInfo, result);

      // Still only required fields
      assertEquals(1, result.size());
      assertTrue(result.has("required_field"));

      // But now with fake email format
      assertFalse(result.get("required_field").isNull());
      assertTrue(result.get("required_field").asString().contains("@"));
    }

    @Test
    void normal_matchesDefaultBuilder(TestInfo testInfo) throws Exception {
      // normal() should produce equivalent results to builder().build()
      // We can't compare directly due to randomness in some fields,
      // but we can verify the structure is the same
      JsonNode normalResult = JsonSchemaDataGenerator.normal().generate(TEST_SCHEMA);
      JsonNode builderResult = JsonSchemaDataGenerator.builder().build().generate(TEST_SCHEMA);
      writeOutput(testInfo, normalResult);

      assertEquals(normalResult.size(), builderResult.size());
      assertEquals(normalResult.has("required_field"), builderResult.has("required_field"));
      assertEquals(normalResult.has("optional_field"), builderResult.has("optional_field"));
      assertEquals(normalResult.has("items"), builderResult.has("items"));
    }

    @Test
    void skeleton_generatesNullValuesWithStructure(TestInfo testInfo) throws Exception {
      JsonNode result = JsonSchemaDataGenerator.skeleton().generate(TEST_SCHEMA);
      writeOutput(testInfo, result);

      // All properties should be present (propertyScopeOption = ALL)
      assertTrue(result.has("required_field"));
      assertTrue(result.has("optional_field"));
      assertTrue(result.has("items"));

      // All values should be null
      assertTrue(result.get("required_field").isNull());
      assertTrue(result.get("optional_field").isNull());

      // Array should have one element with null value
      assertTrue(result.get("items").isArray());
      assertEquals(1, result.get("items").size());
      assertTrue(result.get("items").get(0).isNull());

      // No additional or pattern properties
      assertEquals(3, result.size());
    }

    @Test
    void skeleton_usesDefaultValuesWhenPresent(TestInfo testInfo) throws Exception {
      String schemaWithDefault = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string" },
              "status": { "type": "string", "default": "pending" },
              "count": { "type": "integer", "default": 42 }
            }
          }
          """;
      JsonNode result = JsonSchemaDataGenerator.skeleton().generate(schemaWithDefault);
      writeOutput(testInfo, result);

      // Name should be null (no default)
      assertTrue(result.get("name").isNull());

      // Status and count should use default values
      assertEquals("pending", result.get("status").asString());
      assertEquals(42, result.get("count").asInt());
    }
  }

  @Nested
  class StringOptionTests {

    private static final String STRING_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "name": { "type": "string" },
            "email": { "type": "string", "format": "email" }
          }
        }
        """;

    @Test
    void stringOption_NULL_returnsNull(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().stringOption(StringOption.NULL).build();
      JsonNode result = gen.generate(STRING_SCHEMA);
      writeOutput(testInfo, result);
      assertTrue(result.get("name").isNull());
      assertTrue(result.get("email").isNull());
    }

    @Test
    void stringOption_EMPTY_returnsEmptyString(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().stringOption(StringOption.EMPTY).build();
      JsonNode result = gen.generate(STRING_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals("", result.get("name").asString());
      assertEquals("", result.get("email").asString());
    }

    @Test
    void stringOption_GENERATED_usesExistingLogic(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder()
          .stringOption(StringOption.GENERATED)
          .formattedStringOption(FormattedStringOption.DATAFAKER)
          .build();
      JsonNode result = gen.generate(STRING_SCHEMA);
      writeOutput(testInfo, result);
      // Name should be generated (non-empty)
      assertFalse(result.get("name").asString().isEmpty());
      // Email should have @ (fake format)
      assertTrue(result.get("email").asString().contains("@"));
    }
  }

  @Nested
  class NumberOptionTests {

    private static final String NUMBER_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "count": { "type": "integer" },
            "price": { "type": "number" }
          }
        }
        """;

    @Test
    void numberOption_NULL_returnsNull(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().numberOption(NumberOption.NULL).build();
      JsonNode result = gen.generate(NUMBER_SCHEMA);
      writeOutput(testInfo, result);
      assertTrue(result.get("count").isNull());
      assertTrue(result.get("price").isNull());
    }

    @Test
    void numberOption_ZERO_returnsZero(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().numberOption(NumberOption.ZERO).build();
      JsonNode result = gen.generate(NUMBER_SCHEMA);
      writeOutput(testInfo, result);
      assertEquals(0, result.get("count").asInt());
      assertEquals(0.0, result.get("price").asDouble(), 0.001);
    }

    @Test
    void numberOption_GENERATED_usesExistingLogic(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder()
          .numberOption(NumberOption.GENERATED)
          .constrainedNumberOption(ConstrainedNumberOption.MINIMUM)
          .build();
      String schemaWithRange = """
          {
            "type": "object",
            "properties": {
              "count": { "type": "integer", "minimum": 10, "maximum": 100 }
            }
          }
          """;
      JsonNode result = gen.generate(schemaWithRange);
      writeOutput(testInfo, result);
      assertEquals(10, result.get("count").asInt());
    }
  }

  @Nested
  class BooleanOptionNullTests {

    private static final String BOOLEAN_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "active": { "type": "boolean" }
          }
        }
        """;

    @Test
    void booleanOption_NULL_returnsNull(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder().booleanOption(BooleanOption.NULL).build();
      JsonNode result = gen.generate(BOOLEAN_SCHEMA);
      writeOutput(testInfo, result);
      assertTrue(result.get("active").isNull());
    }
  }

  @Nested
  class UnionTypeOptionTests {

    private static final String UNION_TYPE_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "nullable": {
              "type": ["string", "null"]
            }
          }
        }
        """;

    private static final String NULL_FIRST_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "value": {
              "type": ["null", "integer"]
            }
          }
        }
        """;

    private static final String MULTI_TYPE_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "value": {
              "type": ["string", "integer", "boolean", "null"]
            }
          }
        }
        """;

    @Test
    void unionTypeOption_FIRST_NON_NULL_picksFirstNonNull(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder()
          .unionTypeOption(UnionTypeOption.FIRST_NON_NULL)
          .formattedStringOption(FormattedStringOption.DATAFAKER)
          .build();
      JsonNode result = gen.generate(UNION_TYPE_SCHEMA);
      writeOutput(testInfo, result);
      // ["string", "null"] -> should pick "string"
      assertTrue(result.get("nullable").isString());
    }

    @Test
    void unionTypeOption_FIRST_NON_NULL_fallsBackToNull(TestInfo testInfo) throws Exception {
      String allNullSchema = """
          {
            "type": "object",
            "properties": {
              "value": { "type": ["null", "null"] }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder()
          .unionTypeOption(UnionTypeOption.FIRST_NON_NULL)
          .build();
      JsonNode result = gen.generate(allNullSchema);
      writeOutput(testInfo, result);
      // All types are null, should fall back to null
      assertTrue(result.get("value").isNull());
    }

    @Test
    void unionTypeOption_FIRST_picksFirstType(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder()
          .unionTypeOption(UnionTypeOption.FIRST)
          .formattedStringOption(FormattedStringOption.DATAFAKER)
          .build();
      JsonNode result = gen.generate(UNION_TYPE_SCHEMA);
      writeOutput(testInfo, result);
      // ["string", "null"] -> should pick "string" (first)
      assertTrue(result.get("nullable").isString());
    }

    @Test
    void unionTypeOption_FIRST_picksNullIfFirst(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder()
          .unionTypeOption(UnionTypeOption.FIRST)
          .build();
      JsonNode result = gen.generate(NULL_FIRST_SCHEMA);
      writeOutput(testInfo, result);
      // ["null", "integer"] -> should pick "null" (first)
      assertTrue(result.get("value").isNull());
    }

    @Test
    void unionTypeOption_LAST_picksLastType(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder()
          .unionTypeOption(UnionTypeOption.LAST)
          .build();
      JsonNode result = gen.generate(UNION_TYPE_SCHEMA);
      writeOutput(testInfo, result);
      // ["string", "null"] -> should pick "null" (last)
      assertTrue(result.get("nullable").isNull());
    }

    @Test
    void unionTypeOption_LAST_picksIntegerIfLast(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder()
          .unionTypeOption(UnionTypeOption.LAST)
          .constrainedNumberOption(ConstrainedNumberOption.DATAFAKER)
          .build();
      JsonNode result = gen.generate(NULL_FIRST_SCHEMA);
      writeOutput(testInfo, result);
      // ["null", "integer"] -> should pick "integer" (last)
      assertTrue(result.get("value").isIntegralNumber());
    }

    @Test
    void unionTypeOption_RANDOM_picksValidType(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder()
          .unionTypeOption(UnionTypeOption.RANDOM)
          .formattedStringOption(FormattedStringOption.DATAFAKER)
          .constrainedNumberOption(ConstrainedNumberOption.DATAFAKER)
          .booleanOption(BooleanOption.TRUE)
          .build();
      JsonNode result = gen.generate(MULTI_TYPE_SCHEMA);
      writeOutput(testInfo, result);
      // ["string", "integer", "boolean", "null"] -> should pick one of these
      JsonNode value = result.get("value");
      assertTrue(value.isString() || value.isIntegralNumber() || value.isBoolean() || value.isNull(),
          "Value should be one of: string, integer, boolean, or null");
    }

    @Test
    void unionTypeOption_NULL_FIRST_prefersNullIfPresent(TestInfo testInfo) throws Exception {
      var gen = JsonSchemaDataGenerator.builder()
          .unionTypeOption(UnionTypeOption.NULL_FIRST)
          .build();
      JsonNode result = gen.generate(UNION_TYPE_SCHEMA);
      writeOutput(testInfo, result);
      // ["string", "null"] -> should pick "null" (null is present)
      assertTrue(result.get("nullable").isNull());
    }

    @Test
    void unionTypeOption_NULL_FIRST_fallsBackToFirstIfNoNull(TestInfo testInfo) throws Exception {
      String noNullSchema = """
          {
            "type": "object",
            "properties": {
              "value": { "type": ["string", "integer"] }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder()
          .unionTypeOption(UnionTypeOption.NULL_FIRST)
          .formattedStringOption(FormattedStringOption.DATAFAKER)
          .build();
      JsonNode result = gen.generate(noNullSchema);
      writeOutput(testInfo, result);
      // ["string", "integer"] -> should pick "string" (first, no null present)
      assertTrue(result.get("value").isString());
    }

    @Test
    void unionTypeOption_emptyTypeArray_defaultsToObject(TestInfo testInfo) throws Exception {
      String emptyTypeSchema = """
          {
            "type": "object",
            "properties": {
              "value": { "type": [] }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder().build();
      JsonNode result = gen.generate(emptyTypeSchema);
      writeOutput(testInfo, result);
      // Empty type array should default to object
      assertTrue(result.get("value").isObject());
    }

    @Test
    void withUnionTypeOption_fluentApi(TestInfo testInfo) throws Exception {
      var original = JsonSchemaDataGenerator.builder().build();
      var modified = original.withUnionTypeOption(UnionTypeOption.NULL_FIRST);
      assertNotSame(original, modified);
    }

    @Test
    void unionTypeOption_defaultIsFIRST_NON_NULL(TestInfo testInfo) throws Exception {
      // Default behavior should be FIRST_NON_NULL
      var gen = JsonSchemaDataGenerator.builder()
          .formattedStringOption(FormattedStringOption.DATAFAKER)
          .build();
      JsonNode result = gen.generate(UNION_TYPE_SCHEMA);
      writeOutput(testInfo, result);
      // ["string", "null"] -> default (FIRST_NON_NULL) picks "string"
      assertTrue(result.get("nullable").isString());
    }

    @Test
    void unionTypeOption_singleType_worksNormally(TestInfo testInfo) throws Exception {
      String singleTypeSchema = """
          {
            "type": "object",
            "properties": {
              "name": { "type": "string" }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder()
          .unionTypeOption(UnionTypeOption.LAST)
          .formattedStringOption(FormattedStringOption.DATAFAKER)
          .build();
      JsonNode result = gen.generate(singleTypeSchema);
      writeOutput(testInfo, result);
      // Single type (not array) should still work
      assertTrue(result.get("name").isString());
    }

    @Test
    void unionTypeOption_defaultValueTakesPrecedence(TestInfo testInfo) throws Exception {
      String schemaWithDefault = """
          {
            "type": "object",
            "properties": {
              "value": {
                "type": ["integer", "null"],
                "default": "not-an-integer"
              }
            }
          }
          """;
      var gen = JsonSchemaDataGenerator.builder()
          .unionTypeOption(UnionTypeOption.FIRST)
          .defaultValueOption(DefaultValueOption.USE)
          .build();
      JsonNode result = gen.generate(schemaWithDefault);
      writeOutput(testInfo, result);
      // Default value should take precedence over type selection
      assertEquals("not-an-integer", result.get("value").asString());
    }
  }
}
