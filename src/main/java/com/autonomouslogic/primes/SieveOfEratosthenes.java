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
	private final byte[] field;

	public SieveOfEratosthenes(int memory) {
		field = new byte[memory];
	}

	public List<Long> run() {
		var max = maxNumber();
		var address = new int[]{0,0};
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < 8; j++) {
				address[0] = i;
				address[1] = j;
				if (isPrime(address, field)) {
					var p = addressToNumber(address);
					for (long k = 3L*p; k < max; k+=2*p) {
						numberToAddress(k, address);
						setIsNotPrime(address, field);
					}
				}
			}
		}
		var primes = new ArrayList<Long>(field.length);
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < 8; j++) {
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
		long n = number - 3L;
		address[0] = (int) (n / 16L);
		address[1] = (int) ((n - 16L * address[0]) / 2L);
	}

	protected static long addressToNumber(int[] address) {
		return 3L + address[0] * 16L + 2L * address[1];
	}

	private static  void setIsNotPrime(long number, byte[] field) {
		var address = new int[]{0,0};
		numberToAddress(number, address);
		setIsNotPrime(address, field);
	}

	private static  void setIsNotPrime(int[] address, byte[] field) {
		field[address[0]] = (byte) (field[address[0]] | (1 << address[1]));
	}

	private static boolean isPrime(int[] address, byte[] field) {
		var word = field[address[0]];
		var mask = (byte) (1 << address[1]);
		return (word & mask) == 0x0;
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
