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
		return new ConcatPrimeSource(() -> init, next);
	}

	private static Supplier<PrimeSource> nextSieve(PrimeSource previous, int memoryIncrements) {
		return () -> {
			System.out.println(
					String.format("Creating new sieve from %s with %s bytes", previous.lastNumber(), memoryIncrements));
			var start = previous.lastNumber() - previous.lastNumber() % 30;
			var sieve = new SieveOfEratosthenes(start, memoryIncrements);
			var initMemoryIncrements = (int) Math.sqrt(memoryIncrements);
			var init = all(initMemoryIncrements);
			sieve.init(init.primeStream());
			sieve.run();
			var current = new BoundedPrimeSource(sieve, previous.lastNumber() + 2, sieve.lastNumber());
			Supplier<PrimeSource> currentSupplier = () -> current;
			Supplier<PrimeSource> nextSupplier = nextSieve(current, memoryIncrements);
			System.out.println("currentSupplier " + System.identityHashCode(currentSupplier));
			System.out.println("nextSupplier " + System.identityHashCode(nextSupplier));
			return new ConcatPrimeSource(currentSupplier, nextSupplier);
		};
	}

	public static PrimeSource all() {
		return all(16 << 20);
	}
}
