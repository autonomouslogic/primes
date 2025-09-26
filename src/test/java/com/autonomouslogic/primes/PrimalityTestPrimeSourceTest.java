package com.autonomouslogic.primes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrimalityTestPrimeSourceTest {
	@Test
	void shouldWrapTrialDivision() {
		var source = new PrimalityTestPrimeSource(new TrialDivision()).primeStream().iterator();
		var expected = new PrimeList().primeStream()
			.filter(n -> n != 2)
			.iterator();
		for (int i = 0; i < 1000; i++) {
			assertEquals(expected.nextLong(), source.nextLong());
		}
	}

	@Test
	void shouldWrapTrialDivisionAtOffset() {
		var source = new PrimalityTestPrimeSource(new TrialDivision(), 5000).primeStream().iterator();
		var expected = new PrimeList().primeStream()
			.filter(n -> n > 5000)
			.iterator();
		for (int i = 0; i < 1000; i++) {
			assertEquals(expected.nextLong(), source.nextLong());
		}
	}
}
