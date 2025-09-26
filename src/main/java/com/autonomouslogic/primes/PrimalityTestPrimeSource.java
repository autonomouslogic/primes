package com.autonomouslogic.primes;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.stream.LongStream;

/**
 * Wraps any primality test to produce a prime source.
 */
@Accessors(fluent = true)
public class PrimalityTestPrimeSource implements PrimeSource {
	private final PrimalityTest test;

	@Getter
	private final long firstNumber;

	public PrimalityTestPrimeSource(PrimalityTest test, long firstNumber) {
		if (firstNumber < 3) {
			throw new IllegalArgumentException("First number must be at least 3");
		}
		this.test = test;
		this.firstNumber = firstNumber % 2 == 0 ? firstNumber + 1 : firstNumber;
	}

	public PrimalityTestPrimeSource(TrialDivision test) {
		this(test, 3);
	}

	@Override
	public LongStream primeStream() {
		return LongStream.iterate(firstNumber, n -> n + 2)
			.filter(n -> test.isPrime(n) && n >= firstNumber);
	}
}
