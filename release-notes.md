# Release Notes

## v0.2.0

### Breaking Changes

- **AllOfOption moved from RefParserFactory to JsonSchemaDataGenerator**
  - `RefParserFactory.getAllOfOption()` and `RefParserFactory.setAllOfOption()` have been removed
  - `JsonSchemaFlattener.flattenJsonSchema()` methods now require an `AllOfOption` parameter
  - Configure AllOfOption per generator instance instead of globally:
    ```java
    // Old (no longer works)
    RefParserFactory.setAllOfOption(AllOfOption.SKIP);

    // New
    JsonSchemaDataGenerator generator = JsonSchemaDataGenerator.builder()
        .allOfOption(AllOfOption.SKIP)
        .build();
    ```

### Improvements

- **Options reorganized into logical groups** for better discoverability:
  - Schema Composition: AllOfOption, OneOfOption, AnyOfOption
  - Schema Processing: RecursionDepthOption
  - Type Handling: UnionTypeOption
  - Primitive Values: StringOption, NumberOption, BooleanOption, EnumOption
  - Primitive Constraints: FormattedStringOption, ConstrainedNumberOption
  - Object Properties: PropertyScopeOption, AdditionalPropertiesOption, PatternPropertiesOption
  - Array Handling: ArrayOption, PrimitiveArrayOption, ContainsOption, UniqueItemsOption
  - Schema Keywords: DefaultValueOption, ExamplesOption

- **JsonSchemaPathNavigator** now supports optional `AllOfOption` parameter in `of()` methods

### Bug Fixes

- Fixed incorrect API documentation in README

---

## v0.1.0

Initial release with support for:
- JSON Schema data generation with configurable options
- Multiple preset configurations: minimal(), normal(), verbose(), skeleton()
- $ref resolution and allOf merging via json-schema-ref-parser-jvm
- Regex-based string generation via rgxgen
- Realistic fake data generation via datafaker
- JsonPath-style schema navigation via JsonSchemaPathNavigator
