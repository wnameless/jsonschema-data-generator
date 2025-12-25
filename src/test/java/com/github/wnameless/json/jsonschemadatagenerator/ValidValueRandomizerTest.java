package com.github.wnameless.json.jsonschemadatagenerator;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class ValidValueRandomizerTest {

  @Nested
  class PatternStringTests {

    @Test
    void simplePattern_generatesMatchingString() {
      String pattern = "[A-Z]{3}";
      String result = ValidValueRandomizer.patternString(pattern);

      assertTrue(result.matches(pattern), "Result should match pattern: " + result);
      assertEquals(3, result.length());
    }

    @Test
    void alphanumericPattern_generatesMatchingString() {
      String pattern = "[A-Za-z0-9]{5,10}";
      String result = ValidValueRandomizer.patternString(pattern);

      assertTrue(result.matches(pattern), "Result should match pattern: " + result);
      assertTrue(result.length() >= 5 && result.length() <= 10);
    }

    @Test
    void emailLikePattern_generatesMatchingString() {
      String pattern = "[a-z]+@[a-z]+\\.[a-z]{2,3}";
      String result = ValidValueRandomizer.patternString(pattern);

      assertTrue(result.matches(pattern), "Result should match pattern: " + result);
      assertTrue(result.contains("@"));
      assertTrue(result.contains("."));
    }

    @Test
    void numericPattern_generatesMatchingString() {
      String pattern = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
      String result = ValidValueRandomizer.patternString(pattern);

      assertTrue(result.matches(pattern), "Result should match pattern: " + result);
    }

    @Test
    void optionalPattern_generatesMatchingString() {
      String pattern = "prefix-[a-z]*-suffix";
      String result = ValidValueRandomizer.patternString(pattern);

      assertTrue(result.matches(pattern), "Result should match pattern: " + result);
      assertTrue(result.startsWith("prefix-"));
      assertTrue(result.endsWith("-suffix"));
    }

    @RepeatedTest(10)
    void repeatedGeneration_producesVariedResults() {
      String pattern = "[a-z]{5}";
      Set<String> results = new HashSet<>();

      for (int i = 0; i < 20; i++) {
        results.add(ValidValueRandomizer.patternString(pattern));
      }

      // Should generate at least a few different values
      assertTrue(results.size() > 1, "Should generate varied results");
    }
  }

  @Nested
  class EmailFormatTests {

    @RepeatedTest(5)
    void generatesValidEmail() {
      String email = ValidValueRandomizer.emailFormatString();

      assertNotNull(email);
      assertTrue(email.contains("@"), "Email should contain @: " + email);
      assertTrue(email.indexOf("@") > 0, "Email should have local part before @");
      assertTrue(email.indexOf("@") < email.length() - 1, "Email should have domain after @");
    }
  }

  @Nested
  class UriFormatTests {

    @RepeatedTest(5)
    void generatesValidUri() {
      String uri = ValidValueRandomizer.uriFormatString();

      assertNotNull(uri);
      // DataFaker generates URLs that typically start with www. or http
      assertTrue(uri.contains("."), "URI should contain domain separator: " + uri);
    }
  }

  @Nested
  class DateFormatTests {

    @RepeatedTest(5)
    void generatesValidIsoDate() {
      String date = ValidValueRandomizer.dateFormatString();

      assertNotNull(date);
      // Should be parseable as ISO date
      assertDoesNotThrow(() -> LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE),
          "Should be valid ISO date: " + date);
    }

    @Test
    void dateIsWithinReasonableRange() {
      String date = ValidValueRandomizer.dateFormatString();
      LocalDate parsed = LocalDate.parse(date);
      LocalDate now = LocalDate.now();

      // Should be within last 10 years (3650 days)
      assertTrue(parsed.isBefore(now.plusDays(1)), "Date should not be in future");
      assertTrue(parsed.isAfter(now.minusDays(3651)), "Date should be within last 10 years");
    }
  }

  @Nested
  class DateTimeFormatTests {

    @RepeatedTest(5)
    void generatesValidIsoDateTime() {
      String dateTime = ValidValueRandomizer.dateTimeFormatString();

      assertNotNull(dateTime);
      assertDoesNotThrow(() -> LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
          "Should be valid ISO date-time: " + dateTime);
    }

    @Test
    void dateTimeContainsTimeComponent() {
      String dateTime = ValidValueRandomizer.dateTimeFormatString();

      assertTrue(dateTime.contains("T"), "DateTime should contain 'T' separator: " + dateTime);
    }
  }

  @Nested
  class TimeFormatTests {

    @RepeatedTest(5)
    void generatesValidIsoTime() {
      String time = ValidValueRandomizer.timeFormatString();

      assertNotNull(time);
      assertDoesNotThrow(() -> LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME),
          "Should be valid ISO time: " + time);
    }

    @Test
    void timeHasCorrectFormat() {
      String time = ValidValueRandomizer.timeFormatString();

      // Format: HH:mm:ss
      assertTrue(time.matches("\\d{2}:\\d{2}:\\d{2}"), "Time should match HH:mm:ss format: " + time);
    }
  }

  @Nested
  class UuidFormatTests {

    @RepeatedTest(5)
    void generatesValidUuid() {
      String uuid = ValidValueRandomizer.uuidFormatString();

      assertNotNull(uuid);
      assertDoesNotThrow(() -> UUID.fromString(uuid), "Should be valid UUID: " + uuid);
    }

    @Test
    void uuidHasCorrectFormat() {
      String uuid = ValidValueRandomizer.uuidFormatString();

      assertEquals(36, uuid.length(), "UUID should be 36 characters");
      assertTrue(uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"),
          "UUID should match expected format: " + uuid);
    }

    @RepeatedTest(5)
    void generatesUniqueUuids() {
      Set<String> uuids = new HashSet<>();

      for (int i = 0; i < 10; i++) {
        uuids.add(ValidValueRandomizer.uuidFormatString());
      }

      assertEquals(10, uuids.size(), "All UUIDs should be unique");
    }
  }

  @Nested
  class HostnameFormatTests {

    @RepeatedTest(5)
    void generatesValidHostname() {
      String hostname = ValidValueRandomizer.hostnameFormatString();

      assertNotNull(hostname);
      assertTrue(hostname.contains("."), "Hostname should contain domain separator: " + hostname);
      assertFalse(hostname.contains(" "), "Hostname should not contain spaces");
    }
  }

  @Nested
  class Ipv4FormatTests {

    private static final Pattern IPV4_PATTERN =
        Pattern.compile("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$");

    @RepeatedTest(5)
    void generatesValidIpv4() {
      String ipv4 = ValidValueRandomizer.ipv4FormatString();

      assertNotNull(ipv4);
      assertTrue(IPV4_PATTERN.matcher(ipv4).matches(), "Should be valid IPv4: " + ipv4);
    }

    @Test
    void ipv4HasFourOctets() {
      String ipv4 = ValidValueRandomizer.ipv4FormatString();
      String[] octets = ipv4.split("\\.");

      assertEquals(4, octets.length, "IPv4 should have 4 octets");
      for (String octet : octets) {
        int value = Integer.parseInt(octet);
        assertTrue(value >= 0 && value <= 255, "Each octet should be 0-255: " + value);
      }
    }
  }

  @Nested
  class Ipv6FormatTests {

    @RepeatedTest(5)
    void generatesValidIpv6() {
      String ipv6 = ValidValueRandomizer.ipv6FormatString();

      assertNotNull(ipv6);
      // IPv6 contains colons
      assertTrue(ipv6.contains(":"), "IPv6 should contain colons: " + ipv6);
    }

    @Test
    void ipv6HasCorrectStructure() {
      String ipv6 = ValidValueRandomizer.ipv6FormatString();

      // Standard IPv6 has 8 groups separated by colons (may have :: for compression)
      String[] groups = ipv6.split(":");
      assertTrue(groups.length >= 2, "IPv6 should have multiple groups: " + ipv6);
    }
  }

  @Nested
  class RandomStringTests {

    @Test
    void respectsMinLength() {
      String result = ValidValueRandomizer.randomString(10, 20);

      assertTrue(result.length() >= 10, "Length should be >= 10: " + result.length());
    }

    @Test
    void respectsMaxLength() {
      String result = ValidValueRandomizer.randomString(5, 10);

      assertTrue(result.length() <= 10, "Length should be <= 10: " + result.length());
    }

    @Test
    void respectsBothConstraints() {
      for (int i = 0; i < 20; i++) {
        String result = ValidValueRandomizer.randomString(5, 15);

        assertTrue(result.length() >= 5 && result.length() <= 15,
            "Length should be 5-15: " + result.length());
      }
    }

    @Test
    void zeroMinLength_works() {
      String result = ValidValueRandomizer.randomString(0, 10);

      assertTrue(result.length() <= 10);
    }

    @Test
    void sameMinMaxLength_returnsExactLength() {
      String result = ValidValueRandomizer.randomString(5, 5);

      assertEquals(5, result.length());
    }

    @RepeatedTest(10)
    void generatesVariedLengths() {
      Set<Integer> lengths = new HashSet<>();

      for (int i = 0; i < 50; i++) {
        lengths.add(ValidValueRandomizer.randomString(1, 20).length());
      }

      assertTrue(lengths.size() > 1, "Should generate varied lengths");
    }
  }

  @Nested
  class RangedIntegerTests {

    @Test
    void respectsMinimum() {
      BigInteger min = BigInteger.valueOf(100);
      BigInteger max = BigInteger.valueOf(200);

      for (int i = 0; i < 20; i++) {
        BigInteger result = ValidValueRandomizer.rangedInteger(min, max, null);
        assertTrue(result.compareTo(min) >= 0, "Result should be >= min: " + result);
      }
    }

    @Test
    void respectsMaximum() {
      BigInteger min = BigInteger.valueOf(100);
      BigInteger max = BigInteger.valueOf(200);

      for (int i = 0; i < 20; i++) {
        BigInteger result = ValidValueRandomizer.rangedInteger(min, max, null);
        assertTrue(result.compareTo(max) <= 0, "Result should be <= max: " + result);
      }
    }

    @Test
    void nullMinimum_defaultsToZero() {
      BigInteger max = BigInteger.valueOf(100);
      BigInteger result = ValidValueRandomizer.rangedInteger(null, max, null);

      assertTrue(result.compareTo(BigInteger.ZERO) >= 0, "Result should be >= 0: " + result);
      assertTrue(result.compareTo(max) <= 0, "Result should be <= max: " + result);
    }

    @Test
    void nullMaximum_defaultsToMinPlusThousand() {
      BigInteger min = BigInteger.valueOf(50);
      BigInteger result = ValidValueRandomizer.rangedInteger(min, null, null);

      assertTrue(result.compareTo(min) >= 0, "Result should be >= min: " + result);
      assertTrue(result.compareTo(min.add(BigInteger.valueOf(1000))) <= 0,
          "Result should be <= min + 1000: " + result);
    }

    @Test
    void negativeRange_returnsMinimum() {
      BigInteger min = BigInteger.valueOf(200);
      BigInteger max = BigInteger.valueOf(100); // Invalid: max < min
      BigInteger result = ValidValueRandomizer.rangedInteger(min, max, null);

      assertEquals(min, result, "Should return minimum when range is negative");
    }

    @Test
    void zeroRange_returnsMinimum() {
      BigInteger value = BigInteger.valueOf(42);
      BigInteger result = ValidValueRandomizer.rangedInteger(value, value, null);

      assertEquals(value, result, "Should return the value when min == max");
    }

    @Test
    void multipleOf_respectsConstraint() {
      BigInteger min = BigInteger.valueOf(0);
      BigInteger max = BigInteger.valueOf(100);
      BigInteger multipleOf = BigInteger.valueOf(5);

      for (int i = 0; i < 20; i++) {
        BigInteger result = ValidValueRandomizer.rangedInteger(min, max, multipleOf);
        assertEquals(BigInteger.ZERO, result.mod(multipleOf),
            "Result should be multiple of 5: " + result);
      }
    }

    @Test
    void multipleOf_staysWithinBounds() {
      BigInteger min = BigInteger.valueOf(10);
      BigInteger max = BigInteger.valueOf(50);
      BigInteger multipleOf = BigInteger.valueOf(7);

      for (int i = 0; i < 20; i++) {
        BigInteger result = ValidValueRandomizer.rangedInteger(min, max, multipleOf);
        assertTrue(result.compareTo(min) >= 0, "Result should be >= min: " + result);
        assertTrue(result.compareTo(max) <= 0, "Result should be <= max: " + result);
        assertEquals(BigInteger.ZERO, result.mod(multipleOf),
            "Result should be multiple of 7: " + result);
      }
    }

    @Test
    void multipleOfZero_isIgnored() {
      BigInteger min = BigInteger.valueOf(10);
      BigInteger max = BigInteger.valueOf(20);
      BigInteger result = ValidValueRandomizer.rangedInteger(min, max, BigInteger.ZERO);

      assertTrue(result.compareTo(min) >= 0 && result.compareTo(max) <= 0);
    }

    @Test
    void largeNumbers_work() {
      BigInteger min = new BigInteger("1000000000000");
      BigInteger max = new BigInteger("9999999999999");
      BigInteger result = ValidValueRandomizer.rangedInteger(min, max, null);

      assertTrue(result.compareTo(min) >= 0);
      assertTrue(result.compareTo(max) <= 0);
    }

    @Test
    void negativeNumbers_work() {
      BigInteger min = BigInteger.valueOf(-100);
      BigInteger max = BigInteger.valueOf(-50);

      for (int i = 0; i < 20; i++) {
        BigInteger result = ValidValueRandomizer.rangedInteger(min, max, null);
        assertTrue(result.compareTo(min) >= 0, "Result should be >= -100: " + result);
        assertTrue(result.compareTo(max) <= 0, "Result should be <= -50: " + result);
      }
    }
  }

  @Nested
  class RangedNumberTests {

    @Test
    void respectsMinimum() {
      BigDecimal min = BigDecimal.valueOf(10.5);
      BigDecimal max = BigDecimal.valueOf(20.5);

      for (int i = 0; i < 20; i++) {
        BigDecimal result = ValidValueRandomizer.rangedNumber(min, max, null);
        assertTrue(result.compareTo(min) >= 0, "Result should be >= min: " + result);
      }
    }

    @Test
    void respectsMaximum() {
      BigDecimal min = BigDecimal.valueOf(10.5);
      BigDecimal max = BigDecimal.valueOf(20.5);

      for (int i = 0; i < 20; i++) {
        BigDecimal result = ValidValueRandomizer.rangedNumber(min, max, null);
        assertTrue(result.compareTo(max) <= 0, "Result should be <= max: " + result);
      }
    }

    @Test
    void nullMinimum_defaultsToZero() {
      BigDecimal max = BigDecimal.valueOf(100.0);
      BigDecimal result = ValidValueRandomizer.rangedNumber(null, max, null);

      assertTrue(result.compareTo(BigDecimal.ZERO) >= 0, "Result should be >= 0: " + result);
      assertTrue(result.compareTo(max) <= 0, "Result should be <= max: " + result);
    }

    @Test
    void nullMaximum_defaultsToMinPlusThousand() {
      BigDecimal min = BigDecimal.valueOf(50.0);
      BigDecimal result = ValidValueRandomizer.rangedNumber(min, null, null);

      assertTrue(result.compareTo(min) >= 0, "Result should be >= min: " + result);
      assertTrue(result.compareTo(min.add(BigDecimal.valueOf(1000))) <= 0,
          "Result should be <= min + 1000: " + result);
    }

    @Test
    void negativeRange_returnsMinimum() {
      BigDecimal min = BigDecimal.valueOf(200.0);
      BigDecimal max = BigDecimal.valueOf(100.0); // Invalid: max < min
      BigDecimal result = ValidValueRandomizer.rangedNumber(min, max, null);

      assertEquals(min, result, "Should return minimum when range is negative");
    }

    @Test
    void multipleOf_respectsConstraint() {
      BigDecimal min = BigDecimal.valueOf(0);
      BigDecimal max = BigDecimal.valueOf(100);
      BigDecimal multipleOf = BigDecimal.valueOf(0.5);

      for (int i = 0; i < 20; i++) {
        BigDecimal result = ValidValueRandomizer.rangedNumber(min, max, multipleOf);
        BigDecimal remainder = result.remainder(multipleOf);
        assertTrue(remainder.compareTo(BigDecimal.ZERO) == 0,
            "Result should be multiple of 0.5: " + result);
      }
    }

    @Test
    void multipleOf_staysWithinBounds() {
      BigDecimal min = BigDecimal.valueOf(10.0);
      BigDecimal max = BigDecimal.valueOf(50.0);
      BigDecimal multipleOf = BigDecimal.valueOf(7.0);

      for (int i = 0; i < 20; i++) {
        BigDecimal result = ValidValueRandomizer.rangedNumber(min, max, multipleOf);
        assertTrue(result.compareTo(min) >= 0, "Result should be >= min: " + result);
        assertTrue(result.compareTo(max) <= 0, "Result should be <= max: " + result);
      }
    }

    @Test
    void multipleOfZero_isIgnored() {
      BigDecimal min = BigDecimal.valueOf(10.0);
      BigDecimal max = BigDecimal.valueOf(20.0);
      BigDecimal result = ValidValueRandomizer.rangedNumber(min, max, BigDecimal.ZERO);

      assertTrue(result.compareTo(min) >= 0 && result.compareTo(max) <= 0);
    }

    @Test
    void negativeNumbers_work() {
      BigDecimal min = BigDecimal.valueOf(-100.5);
      BigDecimal max = BigDecimal.valueOf(-50.5);

      for (int i = 0; i < 20; i++) {
        BigDecimal result = ValidValueRandomizer.rangedNumber(min, max, null);
        assertTrue(result.compareTo(min) >= 0, "Result should be >= -100.5: " + result);
        assertTrue(result.compareTo(max) <= 0, "Result should be <= -50.5: " + result);
      }
    }

    @Test
    void decimalPrecision_maintained() {
      BigDecimal min = BigDecimal.valueOf(0.001);
      BigDecimal max = BigDecimal.valueOf(0.999);
      BigDecimal result = ValidValueRandomizer.rangedNumber(min, max, null);

      assertTrue(result.compareTo(min) >= 0);
      assertTrue(result.compareTo(max) <= 0);
    }
  }
}
