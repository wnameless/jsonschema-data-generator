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

public final class ValidValueRandomizer {

  private static final Faker faker = new Faker();
  private static final Random random = new Random();

  public static String patternString(String pattern) {
    RgxGen rgxGen = RgxGen.forPattern(pattern).parse();
    return rgxGen.generate();
  }

  public static String emailFormatString() {
    return faker.internet().emailAddress();
  }

  public static String uriFormatString() {
    return faker.internet().url();
  }

  public static String dateFormatString() {
    LocalDate date = LocalDate.now().minusDays(random.nextInt(3650));
    return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
  }

  public static String dateTimeFormatString() {
    LocalDateTime dateTime = LocalDateTime.now().minusDays(random.nextInt(3650))
        .minusHours(random.nextInt(24)).minusMinutes(random.nextInt(60));
    return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
  }

  public static String timeFormatString() {
    LocalTime time = LocalTime.of(random.nextInt(24), random.nextInt(60), random.nextInt(60));
    return time.format(DateTimeFormatter.ISO_LOCAL_TIME);
  }

  public static String uuidFormatString() {
    return UUID.randomUUID().toString();
  }

  public static String hostnameFormatString() {
    return faker.internet().domainName();
  }

  public static String ipv4FormatString() {
    return faker.internet().ipV4Address();
  }

  public static String ipv6FormatString() {
    return faker.internet().ipV6Address();
  }

  public static String randomString(int minLength, int maxLength) {
    int length = minLength + random.nextInt(Math.max(1, maxLength - minLength + 1));
    return faker.lorem().characters(length);
  }

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
