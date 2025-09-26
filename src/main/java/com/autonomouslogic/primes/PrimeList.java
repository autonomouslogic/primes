package com.autonomouslogic.primes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.LongStream;

/**
 * A hard-coded list of primes.
 */
public class PrimeList implements PrimeSource {
	/**
	 * The first 10,000 primes.
	 */
	public static final long[] PRIMES;

	static {
		final int n = 10000;
		try (var in = new BufferedReader(
				new InputStreamReader(PrimeList.class.getResourceAsStream("/first-10k-primes.txt")))) {
			PRIMES = new long[n];
			int i = 0;
			String line;
			while ((line = in.readLine()) != null) {
				if (!line.isEmpty()) {
					PRIMES[i++] = Long.parseLong(line);
				}
			}
			assert i == n;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long firstNumber() {
		return PRIMES[0];
	}

	@Override
	public long lastNumber() {
		return PRIMES[PRIMES.length - 1];
	}

	@Override
	public LongStream primeStream() {
		return LongStream.of(PRIMES);
	}
}
