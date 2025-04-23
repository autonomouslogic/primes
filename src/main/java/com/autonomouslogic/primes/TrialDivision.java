package com.autonomouslogic.primes;

public class TrialDivision implements PrimalityTest {
	public boolean isPrime(long number) {
		if (number < 2) {
			return false;
		}
		if (number == 2) {
			return true;
		}
		var maxCheck = PrimeUtils.maxRequiredCheck(number);

		// This is so much faster than using the stream of possible primes below. It can probably be optimised.
		if (number % 2 == 0) {
			return false;
		}
		for (int n = 3; n <= maxCheck; n += 2) {
			if (number % n == 0) {
				return false;
			}
		}

		//		var iterator = Primorials.allPossiblePrimes().iterator();
		//		while (iterator.hasNext()) {
		//			var n = iterator.next();
		//			if (n > maxCheck) {
		//				break;
		//			}
		//			if (number % n == 0) {
		//				return false;
		//			}
		//		}

		return true;

		//		return Primorials.allPossiblePrimes()
		//			.takeWhile(n -> n <= maxCheck)
		//			.noneMatch(n -> number % n == 0);
	}
}
