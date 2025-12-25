package com.github.wnameless.json.jsonschemadatagenerator;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import com.github.curiousoddman.rgxgen.RgxGen;
import lombok.Builder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.BigIntegerNode;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.DecimalNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

/**
 * Generates JSON data from JSON Schema definitions with configurable generation strategies.
 *
 * <p>
 * This class provides the main entry point for generating sample JSON data that conforms to a given
 * JSON Schema. It supports a wide range of JSON Schema keywords including types, constraints,
 * composition (anyOf, oneOf, allOf), and validation keywords.
 *
 * <h2>Quick Start</h2>
 * <pre>{@code
 * // Using preset configurations
 * JsonNode data = JsonSchemaDataGenerator.normal().generate(jsonSchema);
 *
 * // Using custom configuration
 * JsonNode data = JsonSchemaDataGenerator.builder()
 *     .enumOption(EnumOption.RANDOM)
 *     .arrayOption(ArrayOption.ONE)
 *     .build()
 *     .generate(jsonSchema);
 * }</pre>
 *
 * <h2>Preset Configurations</h2>
 * <ul>
 *   <li>{@link #minimal()} - Generates minimum valid data (required fields only, empty arrays)</li>
 *   <li>{@link #normal()} - Balanced defaults for useful data generation</li>
 *   <li>{@link #verbose()} - Comprehensive data with random variations</li>
 *   <li>{@link #skeleton()} - Shows JSON structure with null values</li>
 * </ul>
 *
 * @see JsonSchemaFlattener
 * @see JsonSchemaPathNavigator
 * @author Wei-Ming Wu
 */
@Builder(toBuilder = true)
public final class JsonSchemaDataGenerator {

  // Static factory methods for preset configurations

  /**
   * Creates a generator with minimal settings - generates the absolute minimum valid data.
   * Only required fields, empty arrays, null for formatted strings/numbers.
   */
  public static JsonSchemaDataGenerator minimal() {
    return builder()
        .enumOption(EnumOption.FIRST)
        .anyOfOption(AnyOfOption.FIRST)
        .oneOfOption(OneOfOption.FIRST)
        .arrayOption(ArrayOption.EMPTY)
        .primitiveArrayOption(PrimitiveArrayOption.EMPTY)
        .booleanOption(BooleanOption.FALSE)
        .formattedStringOption(FormattedStringOption.NULL)
        .constrainedNumberOption(ConstrainedNumberOption.NULL)
        .defaultValueOption(DefaultValueOption.USE)
        .propertyScopeOption(PropertyScopeOption.REQUIRED_ONLY)
        .uniqueItemsOption(UniqueItemsOption.ENFORCE)
        .examplesOption(ExamplesOption.NONE)
        .containsOption(ContainsOption.FIRST)
        .additionalPropertiesOption(AdditionalPropertiesOption.NONE)
        .patternPropertiesOption(PatternPropertiesOption.NONE)
        .recursionDepthOption(RecursionDepthOption.NONE)
        .build();
  }

  /**
   * Creates a generator with normal settings - balanced defaults for useful data generation.
   * This is equivalent to using builder().build().
   */
  public static JsonSchemaDataGenerator normal() {
    return builder().build();
  }

  /**
   * Creates a generator with verbose settings - comprehensive data with random variations.
   * Generates all properties, additional/pattern properties, and uses random selections.
   */
  public static JsonSchemaDataGenerator verbose() {
    return builder()
        .enumOption(EnumOption.RANDOM)
        .anyOfOption(AnyOfOption.RANDOM)
        .oneOfOption(OneOfOption.RANDOM)
        .arrayOption(ArrayOption.RANDOM)
        .primitiveArrayOption(PrimitiveArrayOption.RANDOM)
        .booleanOption(BooleanOption.RANDOM)
        .formattedStringOption(FormattedStringOption.DATAFAKER)
        .constrainedNumberOption(ConstrainedNumberOption.RANDOM)
        .defaultValueOption(DefaultValueOption.USE)
        .propertyScopeOption(PropertyScopeOption.ALL)
        .uniqueItemsOption(UniqueItemsOption.ENFORCE)
        .examplesOption(ExamplesOption.RANDOM)
        .containsOption(ContainsOption.RANDOM)
        .additionalPropertiesOption(AdditionalPropertiesOption.GENERATE_FEW)
        .patternPropertiesOption(PatternPropertiesOption.GENERATE_FEW)
        .recursionDepthOption(RecursionDepthOption.DEEP)
        .build();
  }

  /**
   * Creates a generator with skeleton settings - shows JSON structure with null values.
   * Uses default values when present, otherwise returns null for all primitives.
   */
  public static JsonSchemaDataGenerator skeleton() {
    return builder()
        .stringOption(StringOption.NULL)
        .numberOption(NumberOption.NULL)
        .booleanOption(BooleanOption.NULL)
        .enumOption(EnumOption.NULL)
        .arrayOption(ArrayOption.ONE)
        .primitiveArrayOption(PrimitiveArrayOption.ONE)
        .defaultValueOption(DefaultValueOption.USE)
        .propertyScopeOption(PropertyScopeOption.ALL)
        .additionalPropertiesOption(AdditionalPropertiesOption.NONE)
        .patternPropertiesOption(PatternPropertiesOption.NONE)
        .recursionDepthOption(RecursionDepthOption.SHALLOW)
        .build();
  }

  // Existing options
  @Builder.Default
  private final EnumOption enumOption = EnumOption.FIRST;
  @Builder.Default
  private final AnyOfOption anyOfOption = AnyOfOption.FIRST;
  @Builder.Default
  private final OneOfOption oneOfOption = OneOfOption.FIRST;
  @Builder.Default
  private final ArrayOption arrayOption = ArrayOption.ONE;
  @Builder.Default
  private final PrimitiveArrayOption primitiveArrayOption = PrimitiveArrayOption.ONE;

  // New options
  @Builder.Default
  private final BooleanOption booleanOption = BooleanOption.FALSE;
  @Builder.Default
  private final FormattedStringOption formattedStringOption = FormattedStringOption.DATAFAKER;
  @Builder.Default
  private final ConstrainedNumberOption constrainedNumberOption = ConstrainedNumberOption.DATAFAKER;
  @Builder.Default
  private final DefaultValueOption defaultValueOption = DefaultValueOption.USE;
  @Builder.Default
  private final PropertyScopeOption propertyScopeOption = PropertyScopeOption.ALL;
  @Builder.Default
  private final UniqueItemsOption uniqueItemsOption = UniqueItemsOption.ENFORCE;
  @Builder.Default
  private final ExamplesOption examplesOption = ExamplesOption.FIRST;
  @Builder.Default
  private final ContainsOption containsOption = ContainsOption.FIRST;
  @Builder.Default
  private final AdditionalPropertiesOption additionalPropertiesOption =
      AdditionalPropertiesOption.NONE;
  @Builder.Default
  private final PatternPropertiesOption patternPropertiesOption = PatternPropertiesOption.NONE;
  @Builder.Default
  private final StringOption stringOption = StringOption.GENERATED;
  @Builder.Default
  private final NumberOption numberOption = NumberOption.GENERATED;
  @Builder.Default
  private final UnionTypeOption unionTypeOption = UnionTypeOption.FIRST_NON_NULL;
  @Builder.Default
  private final RecursionDepthOption recursionDepthOption = RecursionDepthOption.MODERATE;

  private final Random random = new Random();

  private ObjectMapper mapper() {
    return ObjectMapperFactory.getObjectMapper();
  }

  // Fluent API methods

  /**
   * Returns a new generator with the specified enum option.
   *
   * @param option the enum selection strategy
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withEnumOption(EnumOption option) {
    return this.toBuilder().enumOption(option).build();
  }

  /**
   * Returns a new generator with the specified anyOf option.
   *
   * @param option the anyOf schema selection strategy
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withAnyOfOption(AnyOfOption option) {
    return this.toBuilder().anyOfOption(option).build();
  }

  /**
   * Returns a new generator with the specified oneOf option.
   *
   * @param option the oneOf schema selection strategy
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withOneOfOption(OneOfOption option) {
    return this.toBuilder().oneOfOption(option).build();
  }

  /**
   * Returns a new generator with the specified array option for object arrays.
   *
   * @param option the array item count strategy for object arrays
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withArrayOption(ArrayOption option) {
    return this.toBuilder().arrayOption(option).build();
  }

  /**
   * Returns a new generator with the specified array option for primitive arrays.
   *
   * @param option the array item count strategy for primitive arrays
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withPrimitiveArrayOption(PrimitiveArrayOption option) {
    return this.toBuilder().primitiveArrayOption(option).build();
  }

  /**
   * Returns a new generator with the specified boolean option.
   *
   * @param option the boolean value generation strategy
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withBooleanOption(BooleanOption option) {
    return this.toBuilder().booleanOption(option).build();
  }

  /**
   * Returns a new generator with the specified formatted string option.
   *
   * @param option the strategy for generating strings with format or pattern constraints
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withFormattedStringOption(FormattedStringOption option) {
    return this.toBuilder().formattedStringOption(option).build();
  }

  /**
   * Returns a new generator with the specified constrained number option.
   *
   * @param option the strategy for generating numbers with min/max constraints
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withConstrainedNumberOption(ConstrainedNumberOption option) {
    return this.toBuilder().constrainedNumberOption(option).build();
  }

  /**
   * Returns a new generator with the specified default value option.
   *
   * @param option whether to use schema default values
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withDefaultValueOption(DefaultValueOption option) {
    return this.toBuilder().defaultValueOption(option).build();
  }

  /**
   * Returns a new generator with the specified property scope option.
   *
   * @param option whether to generate all properties or required only
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withPropertyScopeOption(PropertyScopeOption option) {
    return this.toBuilder().propertyScopeOption(option).build();
  }

  /**
   * Returns a new generator with the specified unique items option.
   *
   * @param option whether to enforce uniqueItems constraint
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withUniqueItemsOption(UniqueItemsOption option) {
    return this.toBuilder().uniqueItemsOption(option).build();
  }

  /**
   * Returns a new generator with the specified examples option.
   *
   * @param option how to use schema examples
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withExamplesOption(ExamplesOption option) {
    return this.toBuilder().examplesOption(option).build();
  }

  /**
   * Returns a new generator with the specified contains option.
   *
   * @param option how to handle array contains constraints
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withContainsOption(ContainsOption option) {
    return this.toBuilder().containsOption(option).build();
  }

  /**
   * Returns a new generator with the specified additional properties option.
   *
   * @param option how many additional properties to generate
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withAdditionalPropertiesOption(AdditionalPropertiesOption option) {
    return this.toBuilder().additionalPropertiesOption(option).build();
  }

  /**
   * Returns a new generator with the specified pattern properties option.
   *
   * @param option how many pattern properties to generate
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withPatternPropertiesOption(PatternPropertiesOption option) {
    return this.toBuilder().patternPropertiesOption(option).build();
  }

  /**
   * Returns a new generator with the specified string option.
   *
   * @param option the base string value generation strategy
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withStringOption(StringOption option) {
    return this.toBuilder().stringOption(option).build();
  }

  /**
   * Returns a new generator with the specified number option.
   *
   * @param option the base number value generation strategy
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withNumberOption(NumberOption option) {
    return this.toBuilder().numberOption(option).build();
  }

  /**
   * Returns a new generator with the specified union type option.
   *
   * @param option how to handle union type arrays like ["string", "null"]
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withUnionTypeOption(UnionTypeOption option) {
    return this.toBuilder().unionTypeOption(option).build();
  }

  /**
   * Returns a new generator with the specified recursion depth option.
   *
   * @param option the maximum recursion depth for circular schemas
   * @return a new generator instance with the updated option
   */
  public JsonSchemaDataGenerator withRecursionDepthOption(RecursionDepthOption option) {
    return this.toBuilder().recursionDepthOption(option).build();
  }

  // Main public API

  /**
   * Generates JSON data from a JSON Schema string.
   *
   * <p>
   * The schema is first flattened (resolving $ref references and optionally merging allOf) before
   * data generation. The generated data conforms to the schema's type, constraints, and validation
   * keywords.
   *
   * @param jsonSchema the JSON Schema as a string
   * @return a JsonNode containing the generated data
   * @throws Exception if schema parsing or generation fails
   */
  public JsonNode generate(String jsonSchema) throws Exception {
    Map<String, Object> flattenedMap = JsonSchemaFlattener.flattenJsonSchema(jsonSchema);
    JsonNode schemaNode = mapToJsonNode(flattenedMap);
    return generateValue(schemaNode, new HashMap<>());
  }

  /**
   * Generates JSON data from a JSON Schema file.
   *
   * <p>
   * The schema is first flattened (resolving $ref references and optionally merging allOf) before
   * data generation. The generated data conforms to the schema's type, constraints, and validation
   * keywords.
   *
   * @param jsonSchemaFile the JSON Schema file
   * @return a JsonNode containing the generated data
   * @throws Exception if schema parsing or generation fails
   */
  public JsonNode generate(File jsonSchemaFile) throws Exception {
    Map<String, Object> flattenedMap = JsonSchemaFlattener.flattenJsonSchema(jsonSchemaFile);
    JsonNode schemaNode = mapToJsonNode(flattenedMap);
    return generateValue(schemaNode, new HashMap<>());
  }

  /**
   * Converts a Map to JsonNode while handling circular references.
   * Uses identity-based tracking to detect and break circular references.
   */
  private JsonNode mapToJsonNode(Map<String, Object> map) {
    // Track objects currently in the call stack (ancestors) to detect circular refs
    Set<Object> inProgress = Collections.newSetFromMap(new IdentityHashMap<>());
    return convertToJsonNode(map, inProgress);
  }

  private JsonNode convertToJsonNode(Object value, Set<Object> inProgress) {
    if (value == null) {
      return NullNode.instance;
    }

    // Handle primitive types directly
    if (value instanceof String s) {
      return new StringNode(s);
    }
    if (value instanceof Boolean b) {
      return BooleanNode.valueOf(b);
    }
    if (value instanceof Integer i) {
      return new BigIntegerNode(BigInteger.valueOf(i));
    }
    if (value instanceof Long l) {
      return new BigIntegerNode(BigInteger.valueOf(l));
    }
    if (value instanceof BigInteger bi) {
      return new BigIntegerNode(bi);
    }
    if (value instanceof Double d) {
      return new DecimalNode(BigDecimal.valueOf(d));
    }
    if (value instanceof Float f) {
      return new DecimalNode(BigDecimal.valueOf(f));
    }
    if (value instanceof BigDecimal bd) {
      return new DecimalNode(bd);
    }
    if (value instanceof Number n) {
      return new DecimalNode(new BigDecimal(n.toString()));
    }

    // For complex objects (Map/Collection), check for circular references
    if (value instanceof Map || value instanceof Collection) {
      // If this object is already in progress (ancestor in call stack), it's circular
      if (inProgress.contains(value)) {
        // Return null to break the cycle - circular reference detected
        return NullNode.instance;
      }

      // Mark as in progress before recursing
      inProgress.add(value);

      try {
        if (value instanceof Map<?, ?> m) {
          ObjectNode objectNode = mapper().createObjectNode();
          for (Map.Entry<?, ?> entry : m.entrySet()) {
            String key = String.valueOf(entry.getKey());
            objectNode.set(key, convertToJsonNode(entry.getValue(), inProgress));
          }
          return objectNode;
        }

        if (value instanceof Collection<?> c) {
          ArrayNode arrayNode = mapper().createArrayNode();
          for (Object item : c) {
            arrayNode.add(convertToJsonNode(item, inProgress));
          }
          return arrayNode;
        }
      } finally {
        // Remove from in-progress after this subtree is complete
        inProgress.remove(value);
      }
    }

    // Fallback: try to use Jackson for unknown types
    return mapper().valueToTree(value);
  }

  private JsonNode generateValue(JsonNode schema, Map<String, Integer> recursionTracker) {
    // Generate fingerprint for this schema to track recursion
    String fingerprint = generateSchemaFingerprint(schema);

    // Check recursion depth - only limit when we've EXCEEDED maxDepth
    // (allows initial processing even with NONE, but prevents re-processing same schema)
    int currentDepth = recursionTracker.getOrDefault(fingerprint, 0);
    if (currentDepth > recursionDepthOption.getMaxDepth()) {
      return NullNode.instance;
    }

    // Increment depth before recursing
    recursionTracker.put(fingerprint, currentDepth + 1);

    try {
      return generateValueInternal(schema, recursionTracker);
    } finally {
      // Decrement depth when returning
      recursionTracker.put(fingerprint, currentDepth);
    }
  }

  private String generateSchemaFingerprint(JsonNode schema) {
    StringBuilder sb = new StringBuilder();
    if (schema.has("type")) {
      JsonNode typeNode = schema.get("type");
      if (typeNode.isArray()) {
        sb.append("type:[");
        for (JsonNode t : typeNode) {
          sb.append(t.asString()).append(",");
        }
        sb.append("]");
      } else {
        sb.append("type:").append(typeNode.asString());
      }
    }
    if (schema.has("properties")) {
      sb.append("|props:");
      schema.get("properties").propertyNames().forEach(sb::append);
    }
    if (schema.has("title")) {
      sb.append("|title:").append(schema.get("title").asString());
    }
    if (schema.has("$id")) {
      sb.append("|$id:").append(schema.get("$id").asString());
    }
    return sb.toString();
  }

  private JsonNode generateValueInternal(JsonNode schema, Map<String, Integer> recursionTracker) {
    // 1. Handle const (always returns the const value)
    if (schema.has("const")) {
      return schema.get("const").deepCopy();
    }

    // 2. Handle default value based on option
    if (defaultValueOption == DefaultValueOption.USE && schema.has("default")) {
      return schema.get("default").deepCopy();
    }

    // 3. Handle enum
    if (schema.has("enum")) {
      return generateEnumValue(schema.get("enum"));
    }

    // 4. Handle examples
    if (examplesOption != ExamplesOption.NONE && schema.has("examples")) {
      return generateExamplesValue(schema.get("examples"));
    }

    // 5. Handle anyOf
    if (schema.has("anyOf")) {
      return generateAnyOfValue(schema.get("anyOf"), recursionTracker);
    }

    // 6. Handle oneOf
    if (schema.has("oneOf")) {
      return generateOneOfValue(schema.get("oneOf"), recursionTracker);
    }

    // 7. Type-based generation
    String type = getType(schema);

    return switch (type) {
      case "object" -> generateObjectValue(schema, recursionTracker);
      case "array" -> generateArrayValue(schema, recursionTracker);
      case "string" -> generateStringValue(schema);
      case "integer" -> generateIntegerValue(schema);
      case "number" -> generateNumberValue(schema);
      case "boolean" -> generateBooleanValue();
      case "null" -> NullNode.instance;
      default -> NullNode.instance;
    };
  }

  private String getType(JsonNode schema) {
    if (!schema.has("type")) {
      return "object";
    }

    JsonNode typeNode = schema.get("type");

    // Handle type as array (e.g., ["string", "null"])
    if (typeNode.isArray()) {
      // Guard: empty array defaults to object
      if (typeNode.isEmpty()) {
        return "object";
      }

      List<String> types = new ArrayList<>();
      for (JsonNode t : typeNode) {
        types.add(t.asString());
      }

      return switch (unionTypeOption) {
        case FIRST -> types.get(0);
        case LAST -> types.get(types.size() - 1);
        case RANDOM -> types.get(random.nextInt(types.size()));
        case NULL_FIRST -> types.contains("null") ? "null" : types.get(0);
        case FIRST_NON_NULL -> types.stream()
            .filter(t -> !"null".equals(t))
            .findFirst()
            .orElse("null");
      };
    }

    // Handle type as string
    return typeNode.asString();
  }

  private JsonNode generateEnumValue(JsonNode enumArray) {
    if (enumArray == null || enumArray.isEmpty()) {
      return NullNode.instance;
    }

    int index = switch (enumOption) {
      case NULL -> -1;
      case FIRST -> 0;
      case LAST -> enumArray.size() - 1;
      case RANDOM -> random.nextInt(enumArray.size());
    };

    return index < 0 ? NullNode.instance : enumArray.get(index).deepCopy();
  }

  private JsonNode generateExamplesValue(JsonNode examplesArray) {
    if (examplesArray == null || examplesArray.isEmpty()) {
      return NullNode.instance;
    }

    int index = switch (examplesOption) {
      case NONE -> -1;
      case FIRST -> 0;
      case LAST -> examplesArray.size() - 1;
      case RANDOM -> random.nextInt(examplesArray.size());
    };

    return index < 0 ? NullNode.instance : examplesArray.get(index).deepCopy();
  }

  private JsonNode generateAnyOfValue(JsonNode anyOfArray, Map<String, Integer> recursionTracker) {
    if (anyOfArray == null || anyOfArray.isEmpty()) {
      return NullNode.instance;
    }

    int index = switch (anyOfOption) {
      case NULL -> -1;
      case FIRST -> 0;
      case LAST -> anyOfArray.size() - 1;
      case RANDOM -> random.nextInt(anyOfArray.size());
    };

    return index < 0 ? NullNode.instance : generateValue(anyOfArray.get(index), recursionTracker);
  }

  private JsonNode generateOneOfValue(JsonNode oneOfArray, Map<String, Integer> recursionTracker) {
    if (oneOfArray == null || oneOfArray.isEmpty()) {
      return NullNode.instance;
    }

    int index = switch (oneOfOption) {
      case NULL -> -1;
      case FIRST -> 0;
      case LAST -> oneOfArray.size() - 1;
      case RANDOM -> random.nextInt(oneOfArray.size());
    };

    return index < 0 ? NullNode.instance : generateValue(oneOfArray.get(index), recursionTracker);
  }

  private JsonNode generateObjectValue(JsonNode schema, Map<String, Integer> recursionTracker) {
    ObjectNode objectNode = mapper().createObjectNode();

    // Handle regular properties if they exist
    if (schema.has("properties")) {
      JsonNode properties = schema.get("properties");
      Set<String> requiredFields = new HashSet<>();

      if (schema.has("required")) {
        schema.get("required").forEach(node -> requiredFields.add(node.asString()));
      }

      // Get minProperties/maxProperties constraints
      int minProps = schema.has("minProperties") ? schema.get("minProperties").asInt() : 0;
      int maxProps =
          schema.has("maxProperties") ? schema.get("maxProperties").asInt() : Integer.MAX_VALUE;

      // Collect all property names
      List<String> requiredList = new ArrayList<>();
      List<String> optionalList = new ArrayList<>();

      Iterator<Map.Entry<String, JsonNode>> fields = properties.properties().iterator();
      while (fields.hasNext()) {
        String fieldName = fields.next().getKey();
        if (requiredFields.contains(fieldName)) {
          requiredList.add(fieldName);
        } else {
          optionalList.add(fieldName);
        }
      }

      // Determine target property count
      int targetCount = determinePropertyCount(minProps, maxProps, requiredList.size(),
          requiredList.size() + optionalList.size());

      // Select properties to generate
      List<String> propertiesToGenerate = selectProperties(requiredList, optionalList, targetCount);

      // Generate selected properties
      for (String fieldName : propertiesToGenerate) {
        JsonNode fieldSchema = properties.get(fieldName);
        objectNode.set(fieldName, generateValue(fieldSchema, recursionTracker));
      }
    }

    // Handle patternProperties
    if (patternPropertiesOption != PatternPropertiesOption.NONE
        && schema.has("patternProperties")) {
      JsonNode patternProps = schema.get("patternProperties");

      Iterator<Map.Entry<String, JsonNode>> patterns = patternProps.properties().iterator();
      while (patterns.hasNext()) {
        Map.Entry<String, JsonNode> entry = patterns.next();
        String pattern = entry.getKey();
        JsonNode patternSchema = entry.getValue();

        int count = (patternPropertiesOption == PatternPropertiesOption.GENERATE_ONE) ? 1
            : 2 + random.nextInt(2);

        for (int i = 0; i < count; i++) {
          String propName = generatePatternPropertyName(pattern);
          objectNode.set(propName, generateValue(patternSchema, recursionTracker));
        }
      }
    }

    // Handle additionalProperties
    if (additionalPropertiesOption != AdditionalPropertiesOption.NONE
        && schema.has("additionalProperties")) {
      JsonNode additionalProps = schema.get("additionalProperties");

      // additionalProperties: false means no additional properties allowed
      if (!additionalProps.isBoolean() || additionalProps.asBoolean()) {
        // Determine how many additional properties to generate
        int additionalCount =
            (additionalPropertiesOption == AdditionalPropertiesOption.GENERATE_ONE) ? 1
                : 2 + random.nextInt(2);

        // Get schema for additional properties (or use empty schema for true)
        JsonNode additionalSchema =
            additionalProps.isBoolean() ? mapper().createObjectNode() : additionalProps;

        // Generate property names (use propertyNames if available)
        for (int i = 0; i < additionalCount; i++) {
          String propName = generatePropertyName(schema, "additional_" + i);
          objectNode.set(propName, generateValue(additionalSchema, recursionTracker));
        }
      }
    }

    return objectNode;
  }

  private int determinePropertyCount(int minProps, int maxProps, int requiredCount,
      int totalAvailable) {
    // Always include at least required fields (unless maxProps limits it)
    int minimum = Math.max(minProps, requiredCount);
    int maximum = Math.min(maxProps, totalAvailable);

    if (propertyScopeOption == PropertyScopeOption.REQUIRED_ONLY) {
      // Only generate required properties, respecting maxProps limit
      return Math.min(requiredCount, maximum);
    }

    // For ALL option, generate at least minimum properties, up to maximum
    return Math.max(minimum, Math.min(maximum, totalAvailable));
  }

  private List<String> selectProperties(List<String> requiredList, List<String> optionalList,
      int targetCount) {
    List<String> result = new ArrayList<>();

    // Always add required properties first (up to target)
    for (String prop : requiredList) {
      if (result.size() >= targetCount) {
        break;
      }
      result.add(prop);
    }

    // Add optional properties if needed and allowed
    if (result.size() < targetCount && propertyScopeOption != PropertyScopeOption.REQUIRED_ONLY) {
      // Shuffle optional properties for variety
      List<String> shuffledOptional = new ArrayList<>(optionalList);
      Collections.shuffle(shuffledOptional, random);

      for (String prop : shuffledOptional) {
        if (result.size() >= targetCount) {
          break;
        }
        result.add(prop);
      }
    }

    return result;
  }

  private String generatePropertyName(JsonNode schema, String fallback) {
    if (schema.has("propertyNames")) {
      JsonNode propertyNames = schema.get("propertyNames");
      if (propertyNames.has("pattern")) {
        String pattern = propertyNames.get("pattern").asString();
        try {
          RgxGen rgxGen = RgxGen.parse(pattern);
          return rgxGen.generate(random);
        } catch (Exception e) {
          // Fallback on error
        }
      }
    }
    return fallback;
  }

  private String generatePatternPropertyName(String pattern) {
    try {
      RgxGen rgxGen = RgxGen.parse(pattern);
      return rgxGen.generate(random);
    } catch (Exception e) {
      // Fallback if pattern is too complex
      return "pattern_prop_" + random.nextInt(1000);
    }
  }

  private JsonNode generateArrayValue(JsonNode schema, Map<String, Integer> recursionTracker) {
    ArrayNode arrayNode = mapper().createArrayNode();

    // Handle prefixItems (tuple-style arrays) - JSON Schema 2020-12
    if (schema.has("prefixItems")) {
      return generatePrefixItemsArray(schema, recursionTracker);
    }

    // Handle legacy tuple format: "items" is an array (draft-04 to draft-07)
    if (schema.has("items") && schema.get("items").isArray()) {
      return generateLegacyTupleArray(schema, recursionTracker);
    }

    // Handle contains constraint
    if (schema.has("contains") && containsOption != ContainsOption.NONE) {
      return generateContainsArray(schema, recursionTracker);
    }

    if (!schema.has("items")) {
      return arrayNode;
    }

    JsonNode itemSchema = schema.get("items");
    boolean isPrimitive = isPrimitiveType(itemSchema);

    // Determine constraints
    int minItems = schema.has("minItems") ? schema.get("minItems").asInt() : 0;
    int maxItems = schema.has("maxItems") ? schema.get("maxItems").asInt() : Integer.MAX_VALUE;

    // Use appropriate option based on item type
    int targetCount = isPrimitive ? determineArrayCount(primitiveArrayOption, minItems, maxItems)
        : determineArrayCount(arrayOption, minItems, maxItems);

    // Check if uniqueItems is required
    boolean requireUnique = uniqueItemsOption == UniqueItemsOption.ENFORCE
        && schema.has("uniqueItems") && schema.get("uniqueItems").asBoolean();

    if (requireUnique) {
      // Track generated values by their JSON string representation
      Set<String> generatedValues = new HashSet<>();
      int maxAttempts = targetCount * 10; // Allow multiple attempts per item
      int attempts = 0;

      while (arrayNode.size() < targetCount && attempts < maxAttempts) {
        JsonNode value = generateValue(itemSchema, recursionTracker);
        String valueKey = value.toString();

        if (!generatedValues.contains(valueKey)) {
          generatedValues.add(valueKey);
          arrayNode.add(value);
        }
        attempts++;
      }
    } else {
      // Generate items without uniqueness constraint
      for (int i = 0; i < targetCount; i++) {
        arrayNode.add(generateValue(itemSchema, recursionTracker));
      }
    }

    return arrayNode;
  }

  private JsonNode generateContainsArray(JsonNode schema, Map<String, Integer> recursionTracker) {
    ArrayNode arrayNode = mapper().createArrayNode();
    JsonNode containsSchema = schema.get("contains");
    JsonNode itemSchema = schema.has("items") ? schema.get("items") : null;

    // Determine constraints
    int minContains = schema.has("minContains") ? schema.get("minContains").asInt() : 1;
    int maxContains =
        schema.has("maxContains") ? schema.get("maxContains").asInt() : Integer.MAX_VALUE;
    int minItems = schema.has("minItems") ? schema.get("minItems").asInt() : minContains;
    int maxItems = schema.has("maxItems") ? schema.get("maxItems").asInt() : Integer.MAX_VALUE;

    // Determine how many items total (at least minContains)
    boolean isPrimitive = itemSchema != null && isPrimitiveType(itemSchema);
    int baseCount = isPrimitive ? determineArrayCount(primitiveArrayOption, minItems, maxItems)
        : determineArrayCount(arrayOption, minItems, maxItems);
    int targetCount = Math.max(baseCount, minContains);

    // Ensure at least minContains items match contains schema
    int containsCount = Math.max(minContains, 1);
    if (maxContains < Integer.MAX_VALUE) {
      containsCount = Math.min(containsCount, maxContains);
    }
    containsCount = Math.min(containsCount, targetCount);

    // Generate contains-matching items
    List<JsonNode> containsItems = new ArrayList<>();
    for (int i = 0; i < containsCount; i++) {
      containsItems.add(generateValue(containsSchema, recursionTracker));
    }

    // Generate regular items (if items schema exists)
    List<JsonNode> regularItems = new ArrayList<>();
    if (itemSchema != null) {
      int regularCount = targetCount - containsCount;
      for (int i = 0; i < regularCount; i++) {
        regularItems.add(generateValue(itemSchema, recursionTracker));
      }
    }

    // Place items according to containsOption
    switch (containsOption) {
      case FIRST -> {
        containsItems.forEach(arrayNode::add);
        regularItems.forEach(arrayNode::add);
      }
      case LAST -> {
        regularItems.forEach(arrayNode::add);
        containsItems.forEach(arrayNode::add);
      }
      case RANDOM, DISTRIBUTE -> {
        // Merge and shuffle
        List<JsonNode> allItems = new ArrayList<>();
        allItems.addAll(containsItems);
        allItems.addAll(regularItems);
        Collections.shuffle(allItems, random);
        allItems.forEach(arrayNode::add);
      }
      default -> {
        containsItems.forEach(arrayNode::add);
        regularItems.forEach(arrayNode::add);
      }
    }

    return arrayNode;
  }

  private JsonNode generatePrefixItemsArray(JsonNode schema, Map<String, Integer> recursionTracker) {
    ArrayNode arrayNode = mapper().createArrayNode();
    JsonNode prefixItems = schema.get("prefixItems");

    int minItems = schema.has("minItems") ? schema.get("minItems").asInt() : 0;

    // Generate items for each schema in prefixItems
    for (int i = 0; i < prefixItems.size(); i++) {
      arrayNode.add(generateValue(prefixItems.get(i), recursionTracker));
    }

    // Determine schema for additional items
    // Priority: "items" (2020-12) > "additionalItems" (legacy)
    JsonNode additionalSchema = null;
    if (schema.has("items") && !schema.get("items").isArray()) {
      // JSON Schema 2020-12: "items" applies to items after prefixItems
      additionalSchema = schema.get("items");
    } else if (schema.has("additionalItems")) {
      JsonNode additionalItems = schema.get("additionalItems");
      if (additionalItems.isBoolean() && !additionalItems.asBoolean()) {
        // additionalItems: false - no more items allowed
        return arrayNode;
      } else if (!additionalItems.isBoolean()) {
        additionalSchema = additionalItems;
      }
      // additionalItems: true - allow any items (use empty schema)
      else {
        additionalSchema = mapper().createObjectNode();
      }
    }

    // Generate additional items if needed
    if (arrayNode.size() < minItems && additionalSchema != null) {
      while (arrayNode.size() < minItems) {
        arrayNode.add(generateValue(additionalSchema, recursionTracker));
      }
    }

    return arrayNode;
  }

  private JsonNode generateLegacyTupleArray(JsonNode schema, Map<String, Integer> recursionTracker) {
    ArrayNode arrayNode = mapper().createArrayNode();
    JsonNode itemsArray = schema.get("items"); // This is an array of schemas (tuple)

    int minItems = schema.has("minItems") ? schema.get("minItems").asInt() : 0;

    // Generate items for each schema in items array (tuple positions)
    for (int i = 0; i < itemsArray.size(); i++) {
      arrayNode.add(generateValue(itemsArray.get(i), recursionTracker));
    }

    // Handle additionalItems for items beyond the tuple
    if (schema.has("additionalItems")) {
      JsonNode additionalItems = schema.get("additionalItems");
      if (additionalItems.isBoolean() && !additionalItems.asBoolean()) {
        // additionalItems: false - no more items allowed
        return arrayNode;
      } else if (!additionalItems.isBoolean()) {
        // additionalItems is a schema - generate items if minItems requires
        while (arrayNode.size() < minItems) {
          arrayNode.add(generateValue(additionalItems, recursionTracker));
        }
      } else {
        // additionalItems: true - allow any items (use empty schema)
        while (arrayNode.size() < minItems) {
          arrayNode.add(generateValue(mapper().createObjectNode(), recursionTracker));
        }
      }
    }

    return arrayNode;
  }

  private int determineArrayCount(ArrayOption option, int minItems, int maxItems) {
    return switch (option) {
      case NULL -> 0;
      case EMPTY -> Math.max(0, minItems);
      case ONE -> Math.max(1, minItems);
      case RANDOM -> {
        int effectiveMax = Math.min(maxItems, minItems + 10);
        yield minItems + random.nextInt(Math.max(1, effectiveMax - minItems + 1));
      }
    };
  }

  private int determineArrayCount(PrimitiveArrayOption option, int minItems, int maxItems) {
    return switch (option) {
      case NULL -> 0;
      case EMPTY -> Math.max(0, minItems);
      case ONE -> Math.max(1, minItems);
      case RANDOM -> {
        int effectiveMax = Math.min(maxItems, minItems + 10);
        yield minItems + random.nextInt(Math.max(1, effectiveMax - minItems + 1));
      }
    };
  }

  private boolean isPrimitiveType(JsonNode schema) {
    if (!schema.has("type")) {
      return false;
    }
    String type = schema.get("type").asString();
    return "string".equals(type) || "number".equals(type) || "integer".equals(type)
        || "boolean".equals(type) || "null".equals(type);
  }

  private JsonNode generateStringValue(JsonNode schema) {
    // Check stringOption first
    if (stringOption == StringOption.NULL) {
      return NullNode.instance;
    }
    if (stringOption == StringOption.EMPTY) {
      return new StringNode("");
    }

    // GENERATED mode - use existing logic
    // Check for pattern first (regex constraint)
    if (schema.has("pattern") && formattedStringOption != FormattedStringOption.NULL) {
      String pattern = schema.get("pattern").asString();
      return new StringNode(ValidValueRandomizer.patternString(pattern));
    }

    // Check for format
    if (schema.has("format")) {
      String format = schema.get("format").asString();
      return generateFormattedString(format);
    }

    // Handle minLength/maxLength for basic strings
    if (formattedStringOption == FormattedStringOption.RANDOM
        || formattedStringOption == FormattedStringOption.DATAFAKER) {
      int minLength = schema.has("minLength") ? schema.get("minLength").asInt() : 0;
      int maxLength = schema.has("maxLength") ? schema.get("maxLength").asInt() : minLength + 20;
      return new StringNode(ValidValueRandomizer.randomString(minLength, maxLength));
    }

    // Fallback - return null if FormattedStringOption is NULL, otherwise empty string
    return formattedStringOption == FormattedStringOption.NULL ? NullNode.instance : new StringNode("");
  }

  private JsonNode generateFormattedString(String format) {
    if (formattedStringOption == FormattedStringOption.NULL) {
      return NullNode.instance;
    }

    return switch (format) {
      case "email" -> new StringNode(ValidValueRandomizer.emailFormatString());
      case "uri", "url" -> new StringNode(ValidValueRandomizer.uriFormatString());
      case "date" -> new StringNode(ValidValueRandomizer.dateFormatString());
      case "date-time" -> new StringNode(ValidValueRandomizer.dateTimeFormatString());
      case "time" -> new StringNode(ValidValueRandomizer.timeFormatString());
      case "uuid" -> new StringNode(ValidValueRandomizer.uuidFormatString());
      case "hostname" -> new StringNode(ValidValueRandomizer.hostnameFormatString());
      case "ipv4" -> new StringNode(ValidValueRandomizer.ipv4FormatString());
      case "ipv6" -> new StringNode(ValidValueRandomizer.ipv6FormatString());
      default -> NullNode.instance;
    };
  }

  private JsonNode generateIntegerValue(JsonNode schema) {
    // Check numberOption first
    if (numberOption == NumberOption.NULL) {
      return NullNode.instance;
    }
    if (numberOption == NumberOption.ZERO) {
      return new BigIntegerNode(BigInteger.ZERO);
    }

    // GENERATED mode - use existing logic
    if (constrainedNumberOption == ConstrainedNumberOption.NULL) {
      return NullNode.instance;
    }

    BigInteger minimum =
        schema.has("minimum") ? schema.get("minimum").bigIntegerValue() : BigInteger.ZERO;
    BigInteger maximum = schema.has("maximum") ? schema.get("maximum").bigIntegerValue()
        : minimum.add(BigInteger.valueOf(1000));

    // Handle exclusive bounds (JSON Schema draft-06+)
    if (schema.has("exclusiveMinimum")) {
      BigInteger excMin = schema.get("exclusiveMinimum").bigIntegerValue().add(BigInteger.ONE);
      minimum = minimum.max(excMin);
    }
    if (schema.has("exclusiveMaximum")) {
      BigInteger excMax = schema.get("exclusiveMaximum").bigIntegerValue().subtract(BigInteger.ONE);
      maximum = maximum.min(excMax);
    }

    BigInteger multipleOf =
        schema.has("multipleOf") ? schema.get("multipleOf").bigIntegerValue() : null;

    BigInteger value = switch (constrainedNumberOption) {
      case NULL -> null;
      case MINIMUM -> adjustForMultipleOf(minimum, multipleOf, minimum, maximum);
      case MAXIMUM -> adjustForMultipleOf(maximum, multipleOf, minimum, maximum);
      case MIDPOINT -> adjustForMultipleOf(minimum.add(maximum).divide(BigInteger.TWO), multipleOf,
          minimum, maximum);
      case DATAFAKER -> ValidValueRandomizer.rangedInteger(minimum, maximum, multipleOf);
      case RANDOM -> BigInteger.valueOf(random.nextLong());
    };

    return value == null ? NullNode.instance : new BigIntegerNode(value);
  }

  private BigInteger adjustForMultipleOf(BigInteger value, BigInteger multipleOf,
      BigInteger minimum, BigInteger maximum) {
    if (multipleOf == null || multipleOf.equals(BigInteger.ZERO)) {
      return value;
    }
    BigInteger remainder = value.mod(multipleOf.abs());
    BigInteger result = value.subtract(remainder);
    if (result.compareTo(minimum) < 0) {
      result = result.add(multipleOf.abs());
    }
    if (result.compareTo(maximum) > 0) {
      result = result.subtract(multipleOf.abs());
    }
    return result;
  }

  private static final BigDecimal EPSILON = new BigDecimal("0.0001");

  private JsonNode generateNumberValue(JsonNode schema) {
    // Check numberOption first
    if (numberOption == NumberOption.NULL) {
      return NullNode.instance;
    }
    if (numberOption == NumberOption.ZERO) {
      return new DecimalNode(BigDecimal.ZERO);
    }

    // GENERATED mode - use existing logic
    if (constrainedNumberOption == ConstrainedNumberOption.NULL) {
      return NullNode.instance;
    }

    BigDecimal minimum =
        schema.has("minimum") ? schema.get("minimum").decimalValue() : BigDecimal.ZERO;
    BigDecimal maximum = schema.has("maximum") ? schema.get("maximum").decimalValue()
        : minimum.add(BigDecimal.valueOf(1000));

    // Handle exclusive bounds (JSON Schema draft-06+)
    if (schema.has("exclusiveMinimum")) {
      BigDecimal excMin = schema.get("exclusiveMinimum").decimalValue().add(EPSILON);
      minimum = minimum.max(excMin);
    }
    if (schema.has("exclusiveMaximum")) {
      BigDecimal excMax = schema.get("exclusiveMaximum").decimalValue().subtract(EPSILON);
      maximum = maximum.min(excMax);
    }

    BigDecimal multipleOf =
        schema.has("multipleOf") ? schema.get("multipleOf").decimalValue() : null;

    BigDecimal value = switch (constrainedNumberOption) {
      case NULL -> null;
      case MINIMUM -> adjustForMultipleOf(minimum, multipleOf, minimum, maximum);
      case MAXIMUM -> adjustForMultipleOf(maximum, multipleOf, minimum, maximum);
      case MIDPOINT -> adjustForMultipleOf(minimum.add(maximum).divide(BigDecimal.valueOf(2)),
          multipleOf, minimum, maximum);
      case DATAFAKER -> ValidValueRandomizer.rangedNumber(minimum, maximum, multipleOf);
      case RANDOM -> BigDecimal.valueOf(random.nextDouble() * 1000);
    };

    return value == null ? NullNode.instance : new DecimalNode(value);
  }

  private BigDecimal adjustForMultipleOf(BigDecimal value, BigDecimal multipleOf,
      BigDecimal minimum, BigDecimal maximum) {
    if (multipleOf == null || multipleOf.compareTo(BigDecimal.ZERO) == 0) {
      return value;
    }
    BigDecimal[] divRem = value.divideAndRemainder(multipleOf.abs());
    BigDecimal result = divRem[0].multiply(multipleOf.abs());
    if (result.compareTo(minimum) < 0) {
      result = result.add(multipleOf.abs());
    }
    if (result.compareTo(maximum) > 0) {
      result = result.subtract(multipleOf.abs());
    }
    return result;
  }

  private JsonNode generateBooleanValue() {
    return switch (booleanOption) {
      case NULL -> NullNode.instance;
      case TRUE -> BooleanNode.TRUE;
      case FALSE -> BooleanNode.FALSE;
      case RANDOM -> BooleanNode.valueOf(random.nextBoolean());
    };
  }

}
