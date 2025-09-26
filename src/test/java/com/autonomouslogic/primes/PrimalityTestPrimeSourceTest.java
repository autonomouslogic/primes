package com.autonomouslogic.primes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PrimalityTestPrimeSourceTest {
	@Test
	void shouldWrapTrialDivision() {
		var source = new PrimalityTestPrimeSource(new TrialDivision()).iterator();
		var expected = new PrimeList().iterator();
		assertEquals(2, expected.nextLong());
		for (int i = 0; i < 1000; i++) {
			assertEquals(expected.nextLong(), source.nextLong());
		}
	}

	@Test
	void shouldWrapTrialDivisionAtOffset() {
		var source = new PrimalityTestPrimeSource(new TrialDivision(), 5000).iterator();
		var expected = new PrimeList().stream().filter(n -> n > 5000).iterator();
		for (int i = 0; i < 1000; i++) {
			assertEquals(expected.nextLong(), source.nextLong());
		}
	}
}
