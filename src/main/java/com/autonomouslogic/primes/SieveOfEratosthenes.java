package com.autonomouslogic.primes;

import java.util.stream.Stream;

/**
 * <ul>
 *     <li><a href="https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes">Sieve of Eratosthenes</a></li>
 *     <li><a href="https://www.rsok.com/~jrm/printprimes.html">Some Prime Numbers</a></li>
 * </ul>
 */
public class SieveOfEratosthenes {
	private final byte[] field;

	public SieveOfEratosthenes(int memory) {
		field = new byte[memory];
		setIsPrime(3, field);
	}

	public Stream<Long> run() {
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < 8; j++) {

			}
		}
	}

	protected static void numberToAddress(long number, int[] address) {
		if (number % 2 == 0) {
			throw new IllegalArgumentException(number + " is even");
		}
		if (number < 3) {
			throw new IllegalArgumentException(number + " is less than 3");
		}
		long n = number - 3L;
		address[0] = (int) (n / 16L);
		address[1] = (int) ((n - 16L * address[0]) / 2L);
	}

	protected static long addressToNumber(int[] address) {
		return 3L + address[0] * 16L + 2L * address[1];
	}

	private static  void setIsPrime(long number, byte[] field) {
		var address = new int[]{0,0};
		numberToAddress(number, address);
		setIsPrime(address, field);
	}

	private static  void setIsPrime(int[] address, byte[] field) {
		field[address[0]] = (byte) (field[address[0]] | (1 << address[1]));
	}

	private static boolean isPrime(int[] address, byte[] field) {
		return field[address[0]] & (1 << address[1]) > 0;
	}

	public long minNumber() {
		return 3L;
	}

	public long maxNumber() {
		return addressToNumber(new int[]{field.length -1, 7});
	}

	public static SieveOfEratosthenes forMaxNumber(long number) {
		var address = new int[]{0,0};
		numberToAddress(number, address);
		return new SieveOfEratosthenes(address[0] + 1);
	}
}
