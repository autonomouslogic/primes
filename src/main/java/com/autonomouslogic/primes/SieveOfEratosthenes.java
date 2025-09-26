package com.autonomouslogic.primes;

import java.util.stream.LongStream;

/**
 * <a href="https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes">Sieve of Eratosthenes</a>
 */
public class SieveOfEratosthenes implements BoundedPrimeSource {
	private final PrimeBitSet primeBits;
	private boolean init = false;
	private boolean run = false;

	public SieveOfEratosthenes(int memory) {
		primeBits = new PrimeBitSet(memory);
		init = true;
	}

	public SieveOfEratosthenes(long offset, int memory) {
		primeBits = new PrimeBitSet(offset, memory);
	}

	public void init(LongStream primes) {
		var firstNumber = firstNumber();
		var lastNumber = lastNumber();
		var lastCheck = lastCheck();
		primes.filter(n -> n != 2).takeWhile(n -> n <= lastCheck).forEach(n -> {
			for (long k = 3L * n; k <= lastNumber; k += 2L * n) {
				if (k >= firstNumber) {
					primeBits.setIsNotPrime(k);
				}
			}
		});
		init = true;
	}

	public void run() {
		var firstNumber = firstNumber();
		if (firstNumber == 2) {
			firstNumber = 3;
		}
		var lastNumber = lastNumber();
		var lastCheck = lastCheck();
		for (long n = firstNumber; n <= lastCheck; n += 2) {
			if (primeBits.isPrime(n)) {
				for (long k = 3L * n; k <= lastNumber; k += 2L * n) {
					primeBits.setIsNotPrime(k);
				}
			}
		}
		run = true;
	}

	public long lastCheck() {
		return PrimeUtils.maxRequiredCheck(lastNumber());
	}

	public long firstNumber() {
		return primeBits.firstNumber();
	}

	public long lastNumber() {
		return primeBits.lastNumber();
	}

	@Override
	public LongStream primeStream() {
		if (!init || !run) {
			throw new IllegalStateException("Sieve not initialized");
		}
		return primeBits.primeStream();
	}
}
