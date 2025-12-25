module com.github.wnameless.json.jsonschemadatagenerator {

  // Lombok (compile-time only)
  requires static lombok;

  // Jackson 3 for JSON processing
  requires transitive tools.jackson.databind;

  // JSON Schema $ref parser (automatic module name from JAR filename)
  requires transitive json.schema.ref.parser.jvm;

  // Regex-based string generation (automatic module name from JAR filename)
  requires rgxgen;

  // DataFaker for realistic fake data
  requires net.datafaker;

  // Export our package
  exports com.github.wnameless.json.jsonschemadatagenerator;

}
