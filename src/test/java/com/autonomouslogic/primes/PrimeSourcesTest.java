package com.autonomouslogic.primes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PrimeSourcesTest {
	@Test
	void shouldReturnAllPrimeNumbers() {
		//		int memory = 8;
		//		int sieveSize = memory * 30;
		//		// This is the number at which a single sieve will require a sieve to init itself
		//		long limit = (long) Math.pow(PrimeList.PRIMES[PrimeList.PRIMES.length - 1] + sieveSize, 2) + sieveSize;
		//		assertEquals(11018491201L, limit);

		var expected =
				new PrimalityTestPrimeSource(new TrialDivision()).primeStream().iterator();
		var primes = PrimeSources.all(64).primeStream().filter(n -> n > 2).iterator();
		for (int i = 0; i < 10_000; i++) {
			assertEquals(expected.nextLong(), primes.nextLong());
			System.out.println(i);
		}
	}
}
