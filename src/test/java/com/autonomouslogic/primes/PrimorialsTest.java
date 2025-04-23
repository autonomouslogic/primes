package com.autonomouslogic.primes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PrimorialsTest {
	@ParameterizedTest
	@MethodSource("orderTests")
	void shouldCreateOrdersWithCoprimeOffsets(int order, Primorials.Order expected) {
		assertEquals(expected, Primorials.ofOrderWithCoprimes(order));
	}

	public static Stream<Arguments> orderTests() {
		return Stream.of(
				Arguments.of(0, new Primorials.Order(0, 1, 1, new long[] {0})),
				Arguments.of(1, new Primorials.Order(1, 2, 2, new long[] {1})),
				Arguments.of(2, new Primorials.Order(2, 3, 6, new long[] {1, 5})),
				Arguments.of(3, new Primorials.Order(3, 5, 30, new long[] {1, 7, 11, 13, 17, 19, 23, 29})));
	}

	@ParameterizedTest
	@MethodSource("possiblePrimesTests")
	void shouldReturnPossiblePrimes(int order, List<Long> expected) {
		assertEquals(
				expected.toString(),
				Primorials.ofOrderWithCoprimes(order)
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
						LongStream.of(2),
						Primorials.ofOrderWithCoprimes(1).possiblePrimes().takeWhile(n -> n < 6),
						Primorials.ofOrderWithCoprimes(2).possiblePrimes().takeWhile(n -> n < 30),
						Primorials.ofOrderWithCoprimes(3).possiblePrimes().takeWhile(n -> n < 200))
				.flatMapToLong(s -> s)
				.mapToObj(String::valueOf)
				.collect(Collectors.joining("\n"));
		var actual = Primorials.allPossiblePrimes()
				.takeWhile(n -> n <= 200)
				.mapToObj(String::valueOf)
				.collect(Collectors.joining("\n"));
		assertEquals(expected, actual);
	}

	@Test
	void shouldReturnAllPossiblePrimesFromOffset() {
		var expected = Stream.of(
						Primorials.ofOrderWithCoprimes(2)
								.possiblePrimes()
								.filter(n -> n >= 15)
								.takeWhile(n -> n < 30),
						Primorials.ofOrderWithCoprimes(3).possiblePrimes().takeWhile(n -> n < 200))
				.flatMapToLong(s -> s)
				.mapToObj(String::valueOf)
				.collect(Collectors.joining("\n"));
		var actual = Primorials.allPossiblePrimes(15)
				.takeWhile(n -> n <= 200)
				.mapToObj(String::valueOf)
				.collect(Collectors.joining("\n"));
		assertEquals(expected, actual);
	}

	@ParameterizedTest
	@MethodSource("primorialTests")
	void shouldCalculatePrimorialOfOrder(int order, int expected) {
		assertEquals(expected, Primorials.ofOrder(order));
	}

	public static Stream<Arguments> primorialTests() {
		return Stream.of(
				Arguments.of(0, 1),
				Arguments.of(1, 2),
				Arguments.of(2, 6),
				Arguments.of(3, 30),
				Arguments.of(4, 210),
				Arguments.of(5, 2310));
	}
}
