package com.autonomouslogic.primes;

public class TrialDivision implements PrimalityTest {
	public boolean isPrime(long number) {
		if (number < 2) {
			return false;
		}
		if (number == 2) {
			return true;
		}
		if (number % 2 == 0) {
			return false;
		}
		var max = PrimeUtils.maxRequiredCheck(number);
		for (int i = 3; i <= max; i += 2) {
			if (number % i == 0) {
				return false;
			}
		}
		return true;
	}
}
