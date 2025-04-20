package com.autonomouslogic.primes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PrimorialOrderTest {
	@ParameterizedTest
	@MethodSource("orderTests")
	void shouldCreateOrders(int order, PrimorialOrder expected) {
		assertEquals(expected, PrimorialOrder.ofOrder(order));
	}

	public static Stream<Arguments> orderTests() {
		return Stream.of(
				Arguments.of(0, new PrimorialOrder(0, 1, new long[] {0})),
				Arguments.of(1, new PrimorialOrder(1, 2, new long[] {1})),
				Arguments.of(2, new PrimorialOrder(2, 6, new long[] {1, 5})),
				Arguments.of(3, new PrimorialOrder(3, 30, new long[] {1, 7, 11, 13, 17, 19, 23, 29})));
	}

	@ParameterizedTest
	@MethodSource("probablyPrimesTests")
	void shouldReturnProbablePrimes(int order, List<Long> expected) {
		assertEquals(
				expected.toString(),
				PrimorialOrder.ofOrder(order)
						.getProbablePrimes()
						.limit(expected.size())
						.boxed()
						.toList()
						.toString());
	}

	public static Stream<Arguments> probablyPrimesTests() {
		return Stream.of(
				Arguments.of(0, List.of(1, 2, 3, 4, 5, 6, 7)),
				Arguments.of(1, List.of(3, 5, 7, 9, 11, 13, 15)),
				Arguments.of(2, List.of(7, 11, 13, 17)),
				Arguments.of(3, List.of(31, 37, 41, 43, 47, 49, 53, 59, 61, 67, 71, 73, 77, 79, 83, 89)));
	}
}
