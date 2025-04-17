package com.autonomouslogic.primes;

import java.util.Arrays;
import java.util.BitSet;
import java.util.stream.LongStream;
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
	private final long firstNumber;

	@Getter
	private final long lastNumber;

	private final long offset;

	private final BitSet bits = new BitSet();

	public PrimeBitSet(final long maxMemory) {
		this(0, maxMemory);
	}

	public PrimeBitSet(final long offset, final long maxMemory) {
		if (maxMemory > MAX_MEMORY) {
			throw new IllegalArgumentException(maxMemory + " is too large, maximum allowed: " + MAX_MEMORY);
		}
		if (offset < 0) {
			throw new IllegalArgumentException(offset + " is negative");
		}
		if (offset % NUMBERS_PER_BYTE != 0) {
			throw new IllegalArgumentException(offset + " is not a multiple of " + NUMBERS_PER_BYTE);
		}
		if (offset == 0) {
			this.firstNumber = FIRST_PRIMES[0];
		} else {
			this.firstNumber = offset + OFFSETS[0];
		}
		this.offset = offset;
		lastNumber = offset + NUMBERS_PER_BYTE * maxMemory + OFFSETS[7];
	}

	public void setIsNotPrime(long number) {
		checkNumber(number);
		if (number <= 30) {
			return;
		}
		var address = numberToAddress(offset, number);
		if (address < 0) {
			return;
		}
		bits.set(address);
	}

	private void checkNumber(long number) {
		if (number < firstNumber) {
			throw new IllegalArgumentException(number + " is below offset");
		}
		if (number > lastNumber) {
			throw new IllegalArgumentException(number + " is above max");
		}
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
		var address = numberToAddress(offset, number);
		if (address < 0) {
			return false;
		}
		return !bits.get(address);
	}

	protected static int numberToAddress(long offset, long number) {
		if (number % 2 == 0) {
			return -1;
		}
		number -= offset;
		int b = Arrays.binarySearch(OFFSETS, (int) (number % NUMBERS_PER_BYTE));
		if (b < 0) {
			return -1;
		}
		int a = (int) (number / NUMBERS_PER_BYTE) - 1;
		return a * 8 + b;
	}

	protected static long addressToNumber(long offset, int address) {
		long b = address / 8;
		return (b + 1) * (long) NUMBERS_PER_BYTE + (long) OFFSETS[address % 8] + offset;
	}

	public LongStream primeStream() {
		var primes = LongStream.range(0, lastNumber / (long) NUMBERS_PER_BYTE).flatMap(b -> {
			var p = new long[8];
			var i = 0;
			for (int j = 0; j < 8; j++) {
				int address = (int) (b * 8 + j);
				if (!bits.get(address)) {
					p[i++] = addressToNumber(offset, address);
				}
			}
			return Arrays.stream(p).filter(n -> n > 0);
		});
		if (offset == 0) {
			return LongStream.concat(Arrays.stream(FIRST_PRIMES), primes);
		} else {
			return primes;
		}
	}
}
