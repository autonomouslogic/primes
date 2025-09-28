package com.autonomouslogic.primes;

import java.util.function.Supplier;

/**
 * Constructs prime sources.
 */
public class PrimeSources {
	private static final int DEFAULT_MEMORY = 16 << 20;

	public static PrimeSource startingFrom(long start, int memoryIncrements) {
		if (start <= PrimeList.PRIMES[PrimeList.PRIMES.length - 1]) {
			return new BoundedPrimeSource(all(memoryIncrements), start, Long.MAX_VALUE);
		}
		return nextSieve(start, memoryIncrements).get();
	}

	public static PrimeSource startingFrom(long start) {
		return startingFrom(start, DEFAULT_MEMORY);
	}

	public static PrimeSource between(long start, long end) {
		return between(start, end, DEFAULT_MEMORY);
	}

	public static PrimeSource between(long start, long end, int memoryIncrements) {
		return new BoundedPrimeSource(startingFrom(start, memoryIncrements), end);
	}

	/**
	 * Returns unbounded prime source of all primes.
	 * @return
	 */
	public static PrimeSource all(int memoryIncrements) {
		var init = new PrimeList();
		var next = nextSieve(init.lastNumber() + 2, memoryIncrements);
		return new ConcatPrimeSource(() -> init, next);
	}

	public static PrimeSource all() {
		return all(DEFAULT_MEMORY);
	}

	private static Supplier<PrimeSource> nextSieve(long start, int memoryIncrements) {
		return () -> {
			var alignedStart = (start - 2) - (start - 2) % 30;
			var sieve = new SieveOfEratosthenes(alignedStart, memoryIncrements);
			var initMemoryIncrements = (int) Math.sqrt(memoryIncrements);
			var init = all(initMemoryIncrements);
			sieve.init(init.iterator());
			sieve.run();
			var current = new BoundedPrimeSource(sieve, start, sieve.lastNumber());
			Supplier<PrimeSource> currentSupplier = () -> current;
			Supplier<PrimeSource> nextSupplier = nextSieve(current.lastNumber() + 2, memoryIncrements);
			return new ConcatPrimeSource(currentSupplier, nextSupplier);
		};
	}
}
