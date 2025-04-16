package com.autonomouslogic.primes;

import java.util.stream.LongStream;

/**
 * <ul>
 *     <li><a href="https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes">Sieve of Eratosthenes</a></li>
 *     <li><a href="https://www.rsok.com/~jrm/printprimes.html">Some Prime Numbers</a></li>
 * </ul>
 */
public class SieveOfEratosthenes {
	private static final long WORD_LEN = Long.SIZE;
	public static final long WORD_BYTE_SIZE = WORD_LEN / Byte.SIZE;
	private static final long NUMBERS_PER_WORD = WORD_LEN * 2L;
	private static final long FIRST_FIELD_NUMBER = 3;
	private final long[] field;

	public SieveOfEratosthenes(int memory) {
		field = new long[memory];
	}

	public LongStream run() {
		var max = maxNumber();
		var address = new int[] {0, 0};
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < WORD_LEN; j++) {
				address[0] = i;
				address[1] = j;
				if (isPrime(address, field)) {
					var p = addressToNumber(address);
					for (long k = 3L * p; k <= max; k += 2 * p) {
						numberToAddress(k, address);
						setIsNotPrime(address, field);
					}
				}
			}
		}
		var primes = LongStream.range(0, field.length).flatMap(i -> {
			return LongStream.range(0, WORD_LEN).flatMap(j -> {
				if (isPrime((int) i, (int) j, field)) {
					var n = addressToNumber((int) i, (int) j);
					return LongStream.of(n);
				} else {
					return LongStream.empty();
				}
			});
		});
		return LongStream.concat(LongStream.of(2L), primes);
	}

	protected static void numberToAddress(long number, int[] address) {
		if (number % 2 == 0) {
			throw new IllegalArgumentException(number + " is even");
		}
		if (number < 3) {
			throw new IllegalArgumentException(number + " is less than 3");
		}
		long n = number - FIRST_FIELD_NUMBER;
		address[0] = (int) (n / NUMBERS_PER_WORD);
		address[1] = (int) ((n - NUMBERS_PER_WORD * address[0]) / 2L);
	}

	protected static long addressToNumber(int[] address) {
		return addressToNumber(address[0], address[1]);
	}

	protected static long addressToNumber(int a, int b) {
		return FIRST_FIELD_NUMBER + a * NUMBERS_PER_WORD + 2L * b;
	}

	protected static void setIsNotPrime(int[] address, long[] field) {
		field[address[0]] |= (1L << address[1]);
	}

	protected static boolean isPrime(int[] address, long[] field) {
		return isPrime(address[0], address[1], field);
	}

	protected static boolean isPrime(int a, int b, long[] field) {
		var word = field[a];
		var mask = 1L << b;
		return (word & mask) == 0L;
	}

	public long minNumber() {
		return 3L;
	}

	public long maxNumber() {
		return addressToNumber(new int[] {field.length - 1, (int) WORD_LEN - 1});
	}

	public static SieveOfEratosthenes forMaxNumber(long number) {
		var address = new int[] {0, 0};
		numberToAddress(number, address);
		return new SieveOfEratosthenes(address[0] + 1);
	}
}
