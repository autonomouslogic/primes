package com.autonomouslogic.primes;

import java.util.function.Supplier;

/**
 * Constructs prime sources.
 */
public class PrimeSources {
	/**
	 * Returns unbounded prime source of all primes.
	 * @return
	 */
	public static PrimeSource all(int memoryIncrements) {
		var init = new PrimeList();
		var next = nextSieve(init, memoryIncrements);
		return new ConcatPrimeSource(init, next);
	}

	private static Supplier<PrimeSource> nextSieve(PrimeSource previous, int memoryIncrements) {
		return () -> {
			var start = previous.lastNumber() - previous.lastNumber() % 30;
			var sieve = new SieveOfEratosthenes(start, memoryIncrements);
			var initMemoryIncrements = (int) Math.sqrt(memoryIncrements);
			var init = all(initMemoryIncrements);
			sieve.init(init.primeStream());
			sieve.run();
			var current = new BoundedPrimeSource(sieve, previous.lastNumber() + 2, sieve.lastNumber());
			return new ConcatPrimeSource(current, nextSieve(current, memoryIncrements));
		};
	}

	public static PrimeSource all() {
		return all(16 << 20);
	}
}
