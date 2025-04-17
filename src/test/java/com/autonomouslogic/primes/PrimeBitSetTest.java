package com.autonomouslogic.primes;

import static com.autonomouslogic.primes.PrimeList.PRIMES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PrimeBitSetTest {
	@ParameterizedTest
	@MethodSource("addressingTests")
	void shouldConvertNumbersToAddresses(long number, int address) {
		assertEquals(address, PrimeBitSet.numberToAddress(0, number));
	}

	@ParameterizedTest
	@MethodSource("addressingTests")
	void shouldConvertAddressesToNumbers(long number, int address) {
		assertEquals(number, PrimeBitSet.addressToNumber(0, address));
	}

	@ParameterizedTest
	@MethodSource("addressingTests")
	void shouldConvertNumbersToAddressesWithOffsets(long number, int address) {
		assertEquals(address, PrimeBitSet.numberToAddress(120, number + 120));
	}

	@ParameterizedTest
	@MethodSource("addressingTests")
	void shouldConvertAddressesToNumbersWithOffsets(long number, int address) {
		assertEquals(number + 120, PrimeBitSet.addressToNumber(120, address));
	}

	static Stream<Arguments> addressingTests() {
		return Stream.of(
				Arguments.of(31, 0),
				Arguments.of(37, 1),
				Arguments.of(41, 2),
				Arguments.of(43, 3),
				Arguments.of(47, 4),
				Arguments.of(49, 5),
				Arguments.of(53, 6),
				Arguments.of(59, 7),
				Arguments.of(61, 8),
				Arguments.of(67, 9),
				Arguments.of(71, 10),
				Arguments.of(73, 11),
				Arguments.of(77, 12),
				Arguments.of(79, 13),
				Arguments.of(83, 14),
				Arguments.of(89, 15),
				Arguments.of(91, 16),
				Arguments.of(97, 17),
				Arguments.of(101, 18),
				Arguments.of(103, 19),
				Arguments.of(107, 20),
				Arguments.of(109, 21),
				Arguments.of(113, 22),
				Arguments.of(119, 23));
	}

	@Test
	void shouldReturnLastNumber() {
		assertEquals(59L, new PrimeBitSet(1).getLastNumber());
		assertEquals(89L, new PrimeBitSet(2).getLastNumber());
		assertEquals(59L + 30, new PrimeBitSet(30, 1).getLastNumber());
		assertEquals(89L + 60, new PrimeBitSet(60, 2).getLastNumber());
	}

	@Test
	void shouldReturnFirstNumber() {
		assertEquals(2L, new PrimeBitSet(1).getFirstNumber());
		assertEquals(2L, new PrimeBitSet(100).getFirstNumber());
		assertEquals(31L, new PrimeBitSet(30, 1).getFirstNumber());
		assertEquals(61L, new PrimeBitSet(60, 1).getFirstNumber());
	}

	@ParameterizedTest
	@MethodSource("bitsetTests")
	void shouldSetBits(PrimeTestUtil.PrimeTestNumber test) {
		var bitset = new PrimeBitSet(1 << 20);
		for (int i = 30; i < 200; i++) {
			if (i != test.number) {
				bitset.setIsNotPrime(i);
			}
		}
		for (int i = 30; i < 200; i++) {
			if (i == test.number) {
				assertTrue(bitset.isPrime(i), String.valueOf(i));
			} else {
				assertFalse(bitset.isPrime(i), String.valueOf(i));
			}
		}
	}

	static Stream<Arguments> bitsetTests() {
		return PrimeTestUtil.primeTestNumbers()
				.filter(n -> n.isPrime)
				.filter(n -> n.number > 30)
				.takeWhile(n -> n.number < 200)
				.map(Arguments::of);
	}

	@Test
	void shouldOutputPrimes() {
		var bits = new PrimeBitSet(1 << 20);
		assertEquals(2, bits.getFirstNumber());
		assertEquals((1 << 20) * 30 + 29, bits.getLastNumber());
		var last = PRIMES[PRIMES.length - 1];
		for (int i = 2; i <= last; i++) {
			if (Arrays.binarySearch(PRIMES, i) < 0) {
				bits.setIsNotPrime(i);
			}
		}
		assertEquals(
				Arrays.stream(PRIMES).mapToObj(String::valueOf).collect(Collectors.joining("\n")),
				bits.primeStream()
						.takeWhile(n -> n <= last)
						.mapToObj(String::valueOf)
						.collect(Collectors.joining("\n")));
	}

	@Test
	void shouldOutputPrimesWithOffset() {
		var bits = new PrimeBitSet(3000, 1 << 20);
		assertEquals(3000 + 1, bits.getFirstNumber());
		assertEquals((1 << 20) * 30 + 29 + 3000, bits.getLastNumber());
		var last = PRIMES[PRIMES.length - 1];
		for (int i = 3001; i <= last; i++) {
			if (Arrays.binarySearch(PRIMES, i) < 0) {
				bits.setIsNotPrime(i);
			}
		}
		assertEquals(
				Arrays.stream(PRIMES)
						.filter(p -> p > 3000)
						.mapToObj(String::valueOf)
						.collect(Collectors.joining("\n")),
				bits.primeStream()
						.takeWhile(n -> n <= last)
						.mapToObj(String::valueOf)
						.collect(Collectors.joining("\n")));
	}
}
