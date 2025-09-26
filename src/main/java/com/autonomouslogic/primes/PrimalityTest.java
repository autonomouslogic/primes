package com.autonomouslogic.primes;

import java.util.function.LongPredicate;

public interface PrimalityTest extends LongPredicate {
	boolean isPrime(long number);

	@Override
	default boolean test(long number) {
		return isPrime(number);
	}
}
