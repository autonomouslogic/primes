package com.autonomouslogic.primes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PrimeSourcesTest {
	@Test
	void shouldReturnAllPrimeNumbers() {
		var expected =
				new PrimalityTestPrimeSource(new TrialDivision()).primeStream().iterator();
		var primes = PrimeSources.all(512).primeStream().filter(n -> n > 2).iterator();
		for (int i = 0; i < 20_000; i++) {
			assertEquals(expected.nextLong(), primes.nextLong());
			System.out.println(i);
		}
	}
}
