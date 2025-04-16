package com.autonomouslogic.primes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TrialDivisionTest {
	@Test
	void testTrialDivision() {
		var test = new TrialDivision();
		PrimeTestUtil.primeTestNumbers()
				.forEach(n -> assertEquals(n.isPrime, test.isPrime(n.number), Long.toString(n.number)));
	}

	@Test
	@Disabled
	void findLargePrimes() {
		var test = new TrialDivision();
		var rng = new SecureRandom();
		var nums = new HashSet<Long>();
		while (nums.size() < 5) {
			var i = rng.nextLong(10_000_000_000_000_000L) + 10_000_000_000_000_000L;
			if (i % 2 == 0) {
				continue;
			}
			if (test.isPrime(i)) {
				nums.add(i);
			}
		}
		System.out.println(
				nums.stream().sorted().map(obj -> String.valueOf(obj) + "L").collect(Collectors.joining(", ")));
	}
}
