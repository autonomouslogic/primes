package com.autonomouslogic.primes;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * A hard-coded list of primes.
 */
public class PrimeList {
	/**
	 * The first 10,000 primes.
	 */
	public static final int[] PRIMES;

	static {
		final int n = 10000;
		try (var in = new BufferedReader(
				new InputStreamReader(PrimeList.class.getResourceAsStream("/first-10k-primes.txt")))) {
			PRIMES = new int[n];
			int i = 0;
			String line;
			while ((line = in.readLine()) != null) {
				if (!line.isEmpty()) {
					PRIMES[i++] = Integer.parseInt(line);
				}
			}
			assert i == n;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
