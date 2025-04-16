package com.autonomouslogic.primes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class SieveOfEratosthenesTest {
	@ParameterizedTest
	@MethodSource("addressingTests")
	void shouldConvertNumbersToAddresses(long number, int address0, int address1) {
		var sieve = new SieveOfEratosthenes(16);
		var address = new int[]{0,0};
		sieve.numberToAddress(number, address);
		assertEquals(address0, address[0]);
		assertEquals(address1, address[1]);
	}

	@ParameterizedTest
	@MethodSource("addressingTests")
	void shouldConvertAddressesToNumbers(long number, int address0, int address1) {
		var sieve = new SieveOfEratosthenes(16);
		var address = new int[]{address0,address1};
		assertEquals(number, sieve.addressToNumber(address));
	}

	static Stream<Arguments> addressingTests() {
		return Stream.of(
		Arguments.of(3, 0, 0),
		Arguments.of(5, 0, 1),
		Arguments.of(17, 0, 7),
		Arguments.of(19, 1, 0),
		Arguments.of(33, 1, 7),
		Arguments.of(35, 2, 0),
		Arguments.of(49, 2, 7)
		);
	}

	@Test
	void shouldReturnMaxNumber() {
		var sieve = new SieveOfEratosthenes(16);
		assertEquals(3 + 15 * 16 + 2 * 7, sieve.maxNumber());
	}

	@Test
	void shouldSieve() {
		var expectedPrimes = PrimeList.PRIMES;
		var lastExpectedPrime = expectedPrimes[expectedPrimes.length-1];
		var sieve = SieveOfEratosthenes.forMaxNumber(lastExpectedPrime);
		var primes = sieve.run().filter(p -> p<=lastExpectedPrime).toList();
		assertEquals(
			Arrays.stream(expectedPrimes).mapToObj(Integer::toString).collect(Collectors.joining("\n")),
			primes.stream().map(Object::toString).collect(Collectors.joining("\n"))
		);
	}
}
