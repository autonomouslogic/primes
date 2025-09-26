package com.autonomouslogic.primes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SieveOfEratosthenesTest {
	@Test
	void shouldSieve() {
		var sieve = new SieveOfEratosthenes(16 << 10);
		sieve.run();
		var primes = sieve.primeStream();
		var test = new TrialDivision();
		primes.forEach(prime -> {
			assertTrue(test.isPrime(prime), String.valueOf(prime));
		});
	}

	@Test
	void shouldSieveKnownPrimes() {
		var expectedPrimes = Arrays.stream(PrimeList.PRIMES).boxed().toList();
		var lastExpectedPrime = expectedPrimes.getLast();
		var sieve = new SieveOfEratosthenes(1 << 20);
		assertTrue(sieve.lastNumber() >= lastExpectedPrime);
		sieve.run();
		var primes = sieve.primeStream()
				.takeWhile(p -> p <= lastExpectedPrime)
				.boxed()
				.toList();
		assertEquals(
				expectedPrimes.stream().map(String::valueOf).collect(Collectors.joining("\n")),
				primes.stream().map(String::valueOf).collect(Collectors.joining("\n")));
	}

	@Test
	void shouldSieveKnownPrimesWithInitialList() {
		var offset = 900;
		var expectedPrimes = Arrays.stream(PrimeList.PRIMES).boxed().toList();
		var lastExpectedPrime = expectedPrimes.getLast();

		var initPrimes = Arrays.stream(PrimeList.PRIMES).takeWhile(n -> n < offset);
		var sieve = new SieveOfEratosthenes(offset, 1 << 20);
		assertTrue(sieve.lastNumber() >= lastExpectedPrime);
		sieve.init(initPrimes);

		sieve.run();
		var primes = sieve.primeStream()
				.takeWhile(p -> p <= lastExpectedPrime)
				.boxed()
				.toList();
		assertEquals(
				expectedPrimes.stream()
						.filter(n -> n >= offset)
						.map(String::valueOf)
						.collect(Collectors.joining("\n")),
				primes.stream().map(String::valueOf).collect(Collectors.joining("\n")));
	}

	@Test
	void shouldSieveLargePrimesWithInitialList() {
		var firstSieve = new SieveOfEratosthenes(1 << 20);
		var offset = firstSieve.lastNumber() - firstSieve.lastNumber() % 30;
		var secondSieve = new SieveOfEratosthenes(offset, 1 << 20);
		firstSieve.run();
		secondSieve.init(firstSieve.primeStream());
		secondSieve.run();
		var lastPrime = secondSieve.primeStream().max().getAsLong();
		assertTrue(new TrialDivision().isPrime(lastPrime), String.valueOf(lastPrime));
	}

	@Test
	@Disabled
	void shouldSieveLargePrimes() {
		var sieve = new SieveOfEratosthenes(PrimeBitSet.MAX_MEMORY);
		sieve.run();
		var max = sieve.primeStream().max().getAsLong();
		assertTrue(new TrialDivision().isPrime(max), String.valueOf(max));
	}
}
