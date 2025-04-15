package com.autonomouslogic.primes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TrialDivisionTest {
	@Test
	public void testTrialDivision() {
		var test = new TrialDivision();
		PrimeTestUtil.primeTestNumbers()
				.forEach(n -> assertEquals(n.isPrime, test.isPrime(n.number), Long.toString(n.number)));
	}
}
