package com.autonomouslogic.primes;

import java.util.stream.LongStream;

/**
 * <a href="https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes">Sieve of Eratosthenes</a>
 */
public class SieveOfEratosthenes {
	private static final long WORD_LEN = Long.SIZE;
	public static final long WORD_BYTE_SIZE = WORD_LEN / Byte.SIZE;
	private final PrimeBitSet primeBits;

	public SieveOfEratosthenes(int memory) {
		primeBits = new PrimeBitSet(memory);
	}

	public LongStream run() {
		var lastNumber = lastNumber();
		for (long n = 3; n <= lastNumber; n += 2) {
			if (primeBits.isPrime(n)) {
				for (long k = 3L * n; k <= lastNumber; k += 2L * n) {
					primeBits.setIsNotPrime(n);
				}
			}
		}

		return primeBits.primeStream();
	}

	public long lastNumber() {
		return primeBits.getLastNumber();
	}
}
