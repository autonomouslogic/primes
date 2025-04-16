package com.autonomouslogic.primes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class SieveOfEratosthenesTest {
	@ParameterizedTest
	@MethodSource("addressingTests")
	void shouldConvertNumbersToAddresses(long number, int address0, int address1) {
		var address = new int[] {0, 0};
		SieveOfEratosthenes.numberToAddress(number, address);
		assertEquals(address0, address[0]);
		assertEquals(address1, address[1]);
	}

	@ParameterizedTest
	@MethodSource("addressingTests")
	void shouldConvertAddressesToNumbers(long number, int address0, int address1) {
		var address = new int[] {address0, address1};
		assertEquals(number, SieveOfEratosthenes.addressToNumber(address));
	}

	static Stream<Arguments> addressingTests() {
		return Stream.of(
				Arguments.of(3, 0, 0),
				Arguments.of(5, 0, 1),
				Arguments.of(17, 0, 7),
				Arguments.of(129, 0, 63),
				Arguments.of(131, 1, 0),
				Arguments.of(145, 1, 7),
				Arguments.of(257, 1, 63),
				Arguments.of(259, 2, 0),
				Arguments.of(273, 2, 7),
				Arguments.of(385, 2, 63));
	}

	@Test
	void shouldReturnMaxNumber() {
		assertEquals(129L, new SieveOfEratosthenes(1).maxNumber());
		assertEquals(257L, new SieveOfEratosthenes(2).maxNumber());
	}

	@Test
	void shouldConstructForMaxNumber() {
		assertEquals(129L, SieveOfEratosthenes.forMaxNumber(129L).maxNumber());
		assertEquals(257L, SieveOfEratosthenes.forMaxNumber(257L).maxNumber());
	}

	@Test
	void shouldSieveKnownPrimes() {
		var expectedPrimes = Arrays.stream(PrimeList.PRIMES).boxed().toList();
		var lastExpectedPrime = expectedPrimes.getLast();
		var sieve = SieveOfEratosthenes.forMaxNumber(lastExpectedPrime);
		var primes = sieve.run().stream().filter(p -> p <= lastExpectedPrime).toList();
		assertEquals(
				expectedPrimes.stream().map(String::valueOf).collect(Collectors.joining("\n")),
				primes.stream().map(String::valueOf).collect(Collectors.joining("\n")));
	}

	@Test
	void shouldSieve() {
		var primes = new SieveOfEratosthenes(16 << 10).run();
		System.out.println(primes.size());
		System.out.println(primes.stream().skip(primes.size() - 10).toList());
		var test = new TrialDivision();
		for (long prime : primes) {
			assertTrue(test.isPrime(prime), String.valueOf(prime));
		}
	}
}
