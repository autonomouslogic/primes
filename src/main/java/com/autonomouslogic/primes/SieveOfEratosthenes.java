package com.autonomouslogic.primes;

import java.util.ArrayList;
import java.util.List;

/**
 * <ul>
 *     <li><a href="https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes">Sieve of Eratosthenes</a></li>
 *     <li><a href="https://www.rsok.com/~jrm/printprimes.html">Some Prime Numbers</a></li>
 * </ul>
 */
public class SieveOfEratosthenes {
	private static final long WORD_LEN = Long.SIZE;
	private static final long NUMBERS_PER_WORD = WORD_LEN * 2L;
	private static final long FIRST_FIELD_NUMBER = 3;
	private final long[] field;

	public SieveOfEratosthenes(int memory) {
		field = new long[memory];
	}

	public List<Long> run() {
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
		var primes = new ArrayList<Long>(field.length);
		primes.add(2L);
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < WORD_LEN; j++) {
				address[0] = i;
				address[1] = j;
				if (isPrime(address, field)) {
					primes.add(addressToNumber(address));
				}
			}
		}
		return primes;
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
		return FIRST_FIELD_NUMBER + address[0] * NUMBERS_PER_WORD + 2L * address[1];
	}

	private static void setIsNotPrime(int[] address, long[] field) {
		field[address[0]] = (byte) (field[address[0]] | (1 << address[1]));
	}

	private static boolean isPrime(int[] address, long[] field) {
		var word = field[address[0]];
		var mask = 1L << (long) address[1];
		return (word & mask) == 0x0;
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
