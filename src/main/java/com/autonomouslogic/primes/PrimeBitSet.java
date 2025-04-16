package com.autonomouslogic.primes;

import java.util.Arrays;
import java.util.BitSet;
import lombok.Getter;

/**
 * A wrapper around BitSet for working with primes.
 * Code based on <a href="https://www.rsok.com/~jrm/printprimes.html">Some Prime Numbers</a>.
 */
public class PrimeBitSet {
	private static final long[] FIRST_PRIMES = new long[] {2, 3, 5, 7, 11, 13, 17, 19, 23, 29};
	private static final int NUMBERS_PER_BYTE = 30;
	private static final int[] OFFSETS = new int[] {1, 7, 11, 13, 17, 19, 23, 29};
	private static final int MAX_MEMORY = Integer.MAX_VALUE / Byte.SIZE;

	@Getter
	private final long lastNumber;

	private final BitSet bits = new BitSet();

	public PrimeBitSet(final long maxMemory) {
		if (maxMemory > MAX_MEMORY) {
			throw new IllegalArgumentException(maxMemory + " is too large, maximum allowed: " + MAX_MEMORY);
		}
		lastNumber = NUMBERS_PER_BYTE * maxMemory + OFFSETS[7];
	}

	public void setIsNotPrime(long number) {
		if (number < 30) {
			return;
		}
		if (number % 2 == 0) {
			return;
		}
		int b = Arrays.binarySearch(OFFSETS, (int) (number % NUMBERS_PER_BYTE));
		if (b < 0) {
			return;
		}
		int a = (int) (number / NUMBERS_PER_BYTE) - 1;
		bits.set(a * NUMBERS_PER_BYTE + b);
	}

	public boolean isPrime(long number) {
		if (number < FIRST_PRIMES[0]) {
			return false;
		}
		if (number < 30) {
			for (long prime : FIRST_PRIMES) {
				if (number == prime) {
					return true;
				}
			}
			return false;
		}
		if (number % 2 == 0) {
			return false;
		}
		int b = Arrays.binarySearch(OFFSETS, (int) (number % NUMBERS_PER_BYTE));
		if (b < 0) {
			return false;
		}
		int a = (int) (number / NUMBERS_PER_BYTE) - 1;
		return !bits.get(a * NUMBERS_PER_BYTE + b);
	}

	private int numberToBit(long number) {
		return 0;
	}

	//	public LongStream primeStream() {
	//		var primes = LongStream.range(0, field.length).flatMap(i -> {
	//			return LongStream.range(0, WORD_LEN).flatMap(j -> {
	//				if (isPrime((int) i, (int) j, field)) {
	//					var n = addressToNumber((int) i, (int) j);
	//					return LongStream.of(n);
	//				} else {
	//					return LongStream.empty();
	//				}
	//			});
	//		});
	//		return LongStream.concat(LongStream.of(2L), primes);
	//	}
}
