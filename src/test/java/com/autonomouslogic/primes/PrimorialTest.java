package com.autonomouslogic.primes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PrimorialTest {
	@ParameterizedTest
	@MethodSource("orderTests")
	void shouldCreateOrders(int order, Primorial expected) {
		assertEquals(expected, Primorial.ofOrder(order));
	}

	public static Stream<Arguments> orderTests() {
		return Stream.of(
				Arguments.of(0, new Primorial(0, 1, 1, new long[] {0})),
				Arguments.of(1, new Primorial(1, 2, 2, new long[] {1})),
				Arguments.of(2, new Primorial(2, 3, 6, new long[] {1, 5})),
				Arguments.of(3, new Primorial(3, 5, 30, new long[] {1, 7, 11, 13, 17, 19, 23, 29})));
	}

	@ParameterizedTest
	@MethodSource("possiblePrimesTests")
	void shouldReturnPossiblePrimes(int order, List<Long> expected) {
		assertEquals(
				expected.toString(),
				Primorial.ofOrder(order)
						.possiblePrimes()
						.limit(expected.size())
						.boxed()
						.toList()
						.toString());
	}

	public static Stream<Arguments> possiblePrimesTests() {
		return Stream.of(
				Arguments.of(0, List.of(1, 2, 3, 4, 5, 6, 7)),
				Arguments.of(1, List.of(3, 5, 7, 9, 11, 13, 15)),
				Arguments.of(2, List.of(7, 11, 13, 17)),
				Arguments.of(3, List.of(31, 37, 41, 43, 47, 49, 53, 59, 61, 67, 71, 73, 77, 79, 83, 89)));
	}

	@Test
	void shouldReturnAllPossiblePrimes() {
		var expected = Stream.of(
						Primorial.ofOrder(1).possiblePrimes().takeWhile(n -> n < 6),
						Primorial.ofOrder(2).possiblePrimes().takeWhile(n -> n < 30),
						Primorial.ofOrder(3).possiblePrimes().takeWhile(n -> n < 200))
				.flatMapToLong(s -> s)
				.mapToObj(String::valueOf)
				.collect(Collectors.joining("\n"));
		var actual = Primorial.allPossiblePrimes()
				.takeWhile(n -> n <= 200)
				.mapToObj(String::valueOf)
				.collect(Collectors.joining("\n"));
		assertEquals(expected, actual);
	}

	@Test
	void shouldReturnAllPossiblePrimesFromOffset() {
		var expected = Stream.of(
						Primorial.ofOrder(2)
								.possiblePrimes()
								.filter(n -> n >= 15)
								.takeWhile(n -> n < 30),
						Primorial.ofOrder(3).possiblePrimes().takeWhile(n -> n < 200))
				.flatMapToLong(s -> s)
				.mapToObj(String::valueOf)
				.collect(Collectors.joining("\n"));
		var actual = Primorial.allPossiblePrimes(15)
				.takeWhile(n -> n <= 200)
				.mapToObj(String::valueOf)
				.collect(Collectors.joining("\n"));
		assertEquals(expected, actual);
	}

	@Test
	void shouldCalculatePrimorialOfOrder() {
		assertEquals(1, Primorial.primorialOfOrder(0));
		assertEquals(2, Primorial.primorialOfOrder(1));
		assertEquals(6, Primorial.primorialOfOrder(2));
		assertEquals(30, Primorial.primorialOfOrder(3));
		assertEquals(210, Primorial.primorialOfOrder(4));
		assertEquals(2310, Primorial.primorialOfOrder(5));
	}
}
