![Maven Central Version](https://img.shields.io/maven-central/v/com.github.wnameless.json/jsonschema-data-generator)

jsonschema-data-generator
=============
A Java library that generates data from JSON Schema definitions. It flattens JSON schemas (resolving `$ref` references, `allOf`, etc.) and generates corresponding JSON data structures with configurable generation strategies.

## Key Features

- **Modern JSON Schema Support** — Supports JSON Schema 2020-12 keywords like `prefixItems`, `contains`, `minContains`/`maxContains` alongside draft-04/06/07 keywords
- **Fine-Grained Control** — 20+ configuration options for precise control over generated data (enums, arrays, strings, numbers, composition keywords, and more)
- **Preset Modes** — Four ready-to-use configurations (`minimal`, `normal`, `verbose`, `skeleton`) for common use cases
- **Deterministic Output** — Options like `FIRST`, `MINIMUM`, `MIDPOINT` enable reproducible, predictable output for testing
- **Circular Schema Handling** — Built-in recursion depth control for self-referencing schemas
- **Union Type Support** — Intelligent handling of union types like `["string", "null"]` with configurable strategies
- **Realistic Fake Data** — Integrated with DataFaker for generating realistic emails, dates, UUIDs, and more
- **Regex Pattern Generation** — Uses RgxGen to generate strings matching `pattern` constraints

## Purpose
Converts a JSON Schema
```json
{
  "type": "object",
  "properties": {
    "name": { "type": "string", "minLength": 1 },
    "age": { "type": "integer", "minimum": 0 },
    "email": { "type": "string", "format": "email" }
  },
  "required": ["name", "age"]
}
```
into generated JSON data
```json
{
  "name": "abc",
  "age": 0,
  "email": "john.doe@example.com"
}
```

# Maven Repo
```xml
<dependency>
  <groupId>com.github.wnameless.json</groupId>
  <artifactId>jsonschema-data-generator</artifactId>
  <version>${newestVersion}</version>
  <!-- Newest version shows in the maven-central badge above -->
</dependency>
```

# Quick Start
```java
String jsonSchema = """
    {
      "type": "object",
      "properties": {
        "name": { "type": "string" },
        "age": { "type": "integer", "minimum": 0, "maximum": 100 },
        "email": { "type": "string", "format": "email" }
      },
      "required": ["name", "age"]
    }
    """;

// Generate with default settings
JsonNode data = JsonSchemaDataGenerator.normal().generate(jsonSchema);
System.out.println(data.toPrettyString());
// {
//   "name": "",
//   "age": 50,
//   "email": "john.doe@example.com"
// }
```

# Preset Configurations

The library provides four preset configurations for common use cases:

```java
// Minimal - generates absolute minimum valid data
// Only required fields, empty arrays, minimum valid values for constrained fields
JsonNode minimal = JsonSchemaDataGenerator.minimal().generate(jsonSchema);

// Normal - balanced defaults for useful data generation (default)
JsonNode normal = JsonSchemaDataGenerator.normal().generate(jsonSchema);

// Verbose - comprehensive data with random variations
// All properties, additional/pattern properties, random selections
JsonNode verbose = JsonSchemaDataGenerator.verbose().generate(jsonSchema);

// Skeleton - shows JSON structure with null values
// Uses defaults when present, otherwise null for all primitives
JsonNode skeleton = JsonSchemaDataGenerator.skeleton().generate(jsonSchema);
```

# Custom Configuration

Use the builder pattern to customize generation behavior:

```java
JsonSchemaDataGenerator generator = JsonSchemaDataGenerator.builder()
    .enumOption(EnumOption.RANDOM)
    .arrayOption(ArrayOption.RANDOM)
    .booleanOption(BooleanOption.RANDOM)
    .formattedStringOption(FormattedStringOption.DATAFAKER)
    .constrainedNumberOption(ConstrainedNumberOption.DATAFAKER)
    .propertyScopeOption(PropertyScopeOption.REQUIRED_ONLY)
    .build();

JsonNode data = generator.generate(jsonSchema);
```

Or use the fluent API:

```java
JsonNode data = JsonSchemaDataGenerator.normal()
    .withEnumOption(EnumOption.RANDOM)
    .withArrayOption(ArrayOption.ONE)
    .withBooleanOption(BooleanOption.TRUE)
    .generate(jsonSchema);
```

# Generate from File

```java
File schemaFile = new File("path/to/schema.json");
JsonNode data = JsonSchemaDataGenerator.normal().generate(schemaFile);
```

# Option Reference

| Option | Values | Purpose |
|--------|--------|---------|
| StringOption | NULL, EMPTY, GENERATED | Base string value generation |
| NumberOption | NULL, ZERO, GENERATED | Base number value generation |
| BooleanOption | NULL, TRUE, FALSE, RANDOM | Boolean value generation |
| EnumOption | NULL, FIRST, LAST, RANDOM | Enum value selection |
| FormattedStringOption | NULL, DATAFAKER, RANDOM | String format/pattern handling |
| ConstrainedNumberOption | NULL, MINIMUM, MAXIMUM, MIDPOINT, DATAFAKER, RANDOM | Number range handling |
| ArrayOption | NULL, EMPTY, ONE, RANDOM | Object array item count |
| PrimitiveArrayOption | NULL, EMPTY, ONE, RANDOM | Primitive array item count |
| PropertyScopeOption | ALL, REQUIRED_ONLY | Property inclusion scope |
| DefaultValueOption | USE, IGNORE | Default value handling |
| ExamplesOption | NONE, FIRST, LAST, RANDOM | Examples keyword handling |
| ContainsOption | NONE, FIRST, LAST, RANDOM, DISTRIBUTE | Contains constraint handling |
| OneOfOption | NULL, FIRST, LAST, RANDOM | oneOf branch selection |
| AnyOfOption | NULL, FIRST, LAST, RANDOM | anyOf branch selection |
| UniqueItemsOption | ENFORCE, IGNORE | uniqueItems constraint |
| AdditionalPropertiesOption | NONE, GENERATE_ONE, GENERATE_FEW | Additional properties generation |
| PatternPropertiesOption | NONE, GENERATE_ONE, GENERATE_FEW | Pattern properties generation |
| UnionTypeOption | FIRST_NON_NULL, FIRST, LAST, RANDOM, NULL_FIRST | Union type array handling (e.g., `["string", "null"]`) |
| RecursionDepthOption | NONE, SHALLOW, MODERATE, DEEP, VERY_DEEP | Recursion depth for circular schemas |
| AllOfOption | MERGE, SKIP | AllOf keyword handling during flattening |

**Naming Convention**:
- `NULL` = produce null value (for value-defining keywords)
- `NONE` = skip this feature (for hints/constraints)
- `DATAFAKER` = use DataFaker library for realistic random values

# Supported JSON Schema Keywords

## Type Keywords
- `type` (string, number, integer, boolean, null, object, array)
- Union types: `["string", "null"]`

## String Keywords
- `minLength`, `maxLength`
- `pattern` (regex)
- `format` (email, uri, uuid, date, date-time, time, hostname, ipv4, ipv6)

## Number/Integer Keywords
- `minimum`, `maximum`
- `exclusiveMinimum`, `exclusiveMaximum`
- `multipleOf`

## Object Keywords
- `properties`, `required`
- `additionalProperties`
- `patternProperties`
- `propertyNames`
- `minProperties`, `maxProperties`

## Array Keywords
- `items` (single schema or tuple)
- `prefixItems` (JSON Schema 2020-12)
- `additionalItems`
- `contains`, `minContains`, `maxContains`
- `minItems`, `maxItems`
- `uniqueItems`

## Composition Keywords
- `allOf` (merged during flattening)
- `anyOf`, `oneOf`

## Other Keywords
- `$ref` (resolved during flattening)
- `const`, `enum`
- `default`, `examples`

# Additional Components

## JsonSchemaFlattener
Flattens JSON schemas by resolving `$ref` references and optionally merging `allOf` constructs:

```java
// From string
Map<String, Object> flattenedMap = JsonSchemaFlattener.flattenJsonSchema(jsonSchema);

// From file
Map<String, Object> flattenedMap = JsonSchemaFlattener.flattenJsonSchema(schemaFile);

// With custom RefParser
$RefParser refParser = new $RefParser(schemaFile)
    .withOptions(new $RefParserOptions().withMaxDepth(20));
Map<String, Object> flattenedMap = JsonSchemaFlattener.flattenJsonSchema(refParser);
```

## JsonSchemaPathNavigator
Navigates flattened schemas using JsonPath-style expressions:

```java
Map<String, Object> flattenedMap = JsonSchemaFlattener.flattenJsonSchema(jsonSchema);
JsonSchemaPathNavigator navigator = new JsonSchemaPathNavigator(flattenedMap);

// Navigate to root
JsonNode rootSchema = navigator.navigate("$");

// Navigate to a property
JsonNode nameSchema = navigator.navigate("$.name");

// Navigate into arrays
JsonNode itemSchema = navigator.navigate("$.items[*]");
JsonNode firstItemSchema = navigator.navigate("$.items[0]");
```

## ObjectMapperFactory
Configure a custom Jackson ObjectMapper:

```java
ObjectMapper customMapper = JsonMapper.builder()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .build();
ObjectMapperFactory.setObjectMapper(customMapper);
```

## RefParserFactory
Configure schema parsing options:

```java
// Set custom RefParser options
$RefParserOptions options = new $RefParserOptions().withMaxDepth(50);
RefParserFactory.setOptions(options);

// Control allOf merging
RefParserFactory.setAllOfOption(AllOfOption.SKIP);
```

# Requirements

- Java 17 or higher
- Java 9 Module System supported (module name: `com.github.wnameless.json.jsonschemadatagenerator`)

# Dependencies

- Jackson 3.x (`tools.jackson`) for JSON processing
- `json-schema-ref-parser-jvm` for $ref resolution
- `rgxgen` for regex-based string generation
- `datafaker` for realistic fake data generation

# License

Apache License 2.0
