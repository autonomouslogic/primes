package com.autonomouslogic.primes;

import java.util.stream.LongStream;

/**
 * <a href="https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes">Sieve of Eratosthenes</a>
 */
public class SieveOfEratosthenes {
	private final PrimeBitSet primeBits;

	public SieveOfEratosthenes(int memory) {
		primeBits = new PrimeBitSet(memory);
	}

	public SieveOfEratosthenes(long offset, int memory) {
		primeBits = new PrimeBitSet(offset, memory);
	}

	public void init(LongStream primes) {
		var firstNumber = getFirstNumber();
		var lastNumber = getLastNumber();
		var lastCheck = getLastCheck();
		primes.filter(n -> n != 2).takeWhile(n -> n <= lastCheck).forEach(n -> {
			for (long k = 3L * n; k <= lastNumber; k += 2L * n) {
				if (k >= firstNumber) {
					primeBits.setIsNotPrime(k);
				}
			}
		});
	}

	public LongStream run() {
		var firstNumber = getFirstNumber();
		if (firstNumber == 2) {
			firstNumber = 3;
		}
		var lastNumber = getLastNumber();
		var lastCheck = getLastCheck();
		for (long n = firstNumber; n <= lastCheck; n += 2) {
			if (primeBits.isPrime(n)) {
				for (long k = 3L * n; k <= lastNumber; k += 2L * n) {
					primeBits.setIsNotPrime(k);
				}
			}
		}

		return primeBits.primeStream();
	}

	public long getLastCheck() {
		return PrimeUtils.maxRequiredCheck(getLastNumber());
	}

	public long getFirstNumber() {
		return primeBits.getFirstNumber();
	}

	public long getLastNumber() {
		return primeBits.getLastNumber();
	}
}
