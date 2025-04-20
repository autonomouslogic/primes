package com.autonomouslogic.primes;

public class PrimeUtils {
	/**
	 * Returns the highest number that's required to evaluate when checking for prime numbers.
	 * @param num
	 * @return
	 */
	public static long maxRequiredCheck(long num) {
		return ((long) Math.ceil(Math.sqrt(num)));
	}
}
