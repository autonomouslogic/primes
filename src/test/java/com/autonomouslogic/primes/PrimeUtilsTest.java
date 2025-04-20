package com.autonomouslogic.primes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PrimeUtilsTest {
	@ParameterizedTest
	@MethodSource("maxRequiredCheck")
	void shouldCalculateMaximumRequiredCheck(long number, long expected) {
		assertTrue(number * number >= expected);
		assertEquals(expected, PrimeUtils.maxRequiredCheck(number));
	}

	static Stream<Arguments> maxRequiredCheck() {
		return Stream.of(
				Arguments.of(9, 3),
				Arguments.of(100, 10),
				Arguments.of(100 + 1, 11),
				Arguments.of(100 - 1, 10),
				Arguments.of(1_000_000_000_000L, 1_000_000),
				Arguments.of(1_000_000_000_000L + 1L, 1_000_001),
				Arguments.of(1_000_000_000_000L - 1L, 1_000_000));
	}
}
