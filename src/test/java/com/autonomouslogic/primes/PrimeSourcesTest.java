package com.autonomouslogic.primes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PrimeSourcesTest {
	@Test
	void shouldReturnAllPrimeNumbers() {
		var expected = new PrimalityTestPrimeSource(new TrialDivision()).iterator();
		var primes = PrimeSources.all(64).stream().filter(n -> n > 2).iterator();
		for (int i = 0; i < 20_000; i++) {
			assertEquals(expected.nextLong(), primes.nextLong());
			System.out.println(i);
		}
	}

	@Test
	void shouldReturnLargePrimeNumbers() {
		// This is the number at which a single sieve will require a sieve to init itself
		long start = (long) Math.pow(PrimeList.PRIMES[PrimeList.PRIMES.length - 1], 2);
		assertEquals(10968163441L, start);

		var expected = new PrimalityTestPrimeSource(new TrialDivision(), start).iterator();
		var primes =
				PrimeSources.startingFrom(start, 64).stream().filter(n -> n > 2).iterator();
		for (int i = 0; i < 100; i++) {
			var p = expected.nextLong();
			System.out.println(i + " - " + p);
			assertEquals(p, primes.nextLong());
		}
	}

	@Test
	void shouldReturnPrimesBetweenNumbers() {
		var source = PrimeSources.between(101, 307);
		assertEquals(101, source.stream().min().getAsLong());
		assertEquals(307, source.stream().max().getAsLong());
	}

	@Test
	void shouldReturnPrimesBetweenLargeNumbers() {
		var source = PrimeSources.between(1_000_000L, 1_000_100, 64);
		assertEquals(1000003, source.stream().min().getAsLong());
		assertEquals(1000099, source.stream().max().getAsLong());
	}
}
