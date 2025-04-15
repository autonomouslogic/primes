package com.autonomouslogic.primes;

import java.util.stream.Stream;

/**
 * https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes
 */
public class SieveOfEratosthenes {
	private final byte[] field;

	public SieveOfEratosthenes(int memory) {
		field = new byte[memory];
	}

	public Stream<Integer> run() {
		return Stream.empty();
	}
}
