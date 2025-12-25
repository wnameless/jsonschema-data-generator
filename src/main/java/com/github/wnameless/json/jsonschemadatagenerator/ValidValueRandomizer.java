package com.github.wnameless.json.jsonschemadatagenerator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;
import com.github.curiousoddman.rgxgen.RgxGen;
import net.datafaker.Faker;

/**
 * Utility class for generating random valid values that satisfy JSON Schema constraints.
 *
 * <p>
 * This class provides methods for generating random values for various JSON Schema formats (email,
 * uri, date, etc.) and constrained types (strings with length limits, numbers with min/max
 * constraints).
 *
 * <p>
 * Internally uses the DataFaker library for realistic fake data and RgxGen for regex-based string
 * generation.
 *
 * @see FormattedStringOption
 * @see ConstrainedNumberOption
 * @author Wei-Ming Wu
 */
public final class ValidValueRandomizer {

  private static final Faker faker = new Faker();
  private static final Random random = new Random();

  private ValidValueRandomizer() {}

  /**
   * Generates a random string that matches the given regex pattern.
   *
   * @param pattern the regex pattern
   * @return a string matching the pattern
   */
  public static String patternString(String pattern) {
    RgxGen rgxGen = RgxGen.forPattern(pattern).parse();
    return rgxGen.generate();
  }

  /**
   * Generates a random email address.
   *
   * @return a valid email address string
   */
  public static String emailFormatString() {
    return faker.internet().emailAddress();
  }

  /**
   * Generates a random URI/URL.
   *
   * @return a valid URI string
   */
  public static String uriFormatString() {
    return faker.internet().url();
  }

  /**
   * Generates a random date in ISO-8601 format (yyyy-MM-dd).
   *
   * @return a date string in ISO format
   */
  public static String dateFormatString() {
    LocalDate date = LocalDate.now().minusDays(random.nextInt(3650));
    return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
  }

  /**
   * Generates a random date-time in ISO-8601 format (yyyy-MM-ddTHH:mm:ss).
   *
   * @return a date-time string in ISO format
   */
  public static String dateTimeFormatString() {
    LocalDateTime dateTime = LocalDateTime.now().minusDays(random.nextInt(3650))
        .minusHours(random.nextInt(24)).minusMinutes(random.nextInt(60));
    return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
  }

  /**
   * Generates a random time in ISO-8601 format (HH:mm:ss).
   *
   * @return a time string in ISO format
   */
  public static String timeFormatString() {
    LocalTime time = LocalTime.of(random.nextInt(24), random.nextInt(60), random.nextInt(60));
    return time.format(DateTimeFormatter.ISO_LOCAL_TIME);
  }

  /**
   * Generates a random UUID.
   *
   * @return a UUID string
   */
  public static String uuidFormatString() {
    return UUID.randomUUID().toString();
  }

  /**
   * Generates a random hostname/domain name.
   *
   * @return a hostname string
   */
  public static String hostnameFormatString() {
    return faker.internet().domainName();
  }

  /**
   * Generates a random IPv4 address.
   *
   * @return an IPv4 address string
   */
  public static String ipv4FormatString() {
    return faker.internet().ipV4Address();
  }

  /**
   * Generates a random IPv6 address.
   *
   * @return an IPv6 address string
   */
  public static String ipv6FormatString() {
    return faker.internet().ipV6Address();
  }

  /**
   * Generates a random string within the specified length constraints.
   *
   * @param minLength the minimum string length
   * @param maxLength the maximum string length
   * @return a random string of length between minLength and maxLength
   */
  public static String randomString(int minLength, int maxLength) {
    int length = minLength + random.nextInt(Math.max(1, maxLength - minLength + 1));
    return faker.lorem().characters(length);
  }

  /**
   * Generates a random integer within the specified range, optionally respecting a multipleOf
   * constraint.
   *
   * @param minimum the minimum value (inclusive), defaults to 0 if null
   * @param maximum the maximum value (inclusive), defaults to minimum + 1000 if null
   * @param multipleOf if non-null, the result will be a multiple of this value
   * @return a random integer satisfying the constraints
   */
  public static BigInteger rangedInteger(BigInteger minimum, BigInteger maximum,
      BigInteger multipleOf) {
    if (minimum == null) {
      minimum = BigInteger.ZERO;
    }
    if (maximum == null) {
      maximum = minimum.add(BigInteger.valueOf(1000));
    }

    BigInteger range = maximum.subtract(minimum);
    if (range.compareTo(BigInteger.ZERO) < 0) {
      return minimum;
    }

    BigInteger randomOffset;
    if (range.equals(BigInteger.ZERO)) {
      randomOffset = BigInteger.ZERO;
    } else {
      randomOffset = new BigInteger(range.bitLength(), random);
      while (randomOffset.compareTo(range) > 0) {
        randomOffset = new BigInteger(range.bitLength(), random);
      }
    }

    BigInteger result = minimum.add(randomOffset);

    if (multipleOf != null && !multipleOf.equals(BigInteger.ZERO)) {
      BigInteger remainder = result.mod(multipleOf.abs());
      result = result.subtract(remainder);
      if (result.compareTo(minimum) < 0) {
        result = result.add(multipleOf.abs());
      }
      if (result.compareTo(maximum) > 0) {
        result = result.subtract(multipleOf.abs());
      }
    }

    return result;
  }

  /**
   * Generates a random decimal number within the specified range, optionally respecting a
   * multipleOf constraint.
   *
   * @param minimum the minimum value (inclusive), defaults to 0 if null
   * @param maximum the maximum value (inclusive), defaults to minimum + 1000 if null
   * @param multipleOf if non-null, the result will be a multiple of this value
   * @return a random decimal satisfying the constraints
   */
  public static BigDecimal rangedNumber(BigDecimal minimum, BigDecimal maximum,
      BigDecimal multipleOf) {
    if (minimum == null) {
      minimum = BigDecimal.ZERO;
    }
    if (maximum == null) {
      maximum = minimum.add(BigDecimal.valueOf(1000));
    }

    BigDecimal range = maximum.subtract(minimum);
    if (range.compareTo(BigDecimal.ZERO) < 0) {
      return minimum;
    }

    double randomValue = minimum.doubleValue() + (random.nextDouble() * range.doubleValue());
    BigDecimal result = BigDecimal.valueOf(randomValue);

    if (multipleOf != null && multipleOf.compareTo(BigDecimal.ZERO) != 0) {
      result = result.divide(multipleOf.abs(), 0, RoundingMode.HALF_UP).multiply(multipleOf.abs());
      if (result.compareTo(minimum) < 0) {
        result = result.add(multipleOf.abs());
      }
      if (result.compareTo(maximum) > 0) {
        result = result.subtract(multipleOf.abs());
      }
    }

    return result;
  }

}
