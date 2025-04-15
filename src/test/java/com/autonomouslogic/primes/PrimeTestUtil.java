package com.autonomouslogic.primes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class PrimeTestUtil {
	public static Stream<PrimeTestNumber> primeTestNumbers() {
		var last = PrimeList.PRIMES[PrimeList.PRIMES.length - 1];
		return Stream.concat(Stream.of(-1, 0, 1, 2), Stream.iterate(3, n -> n <= last, n -> n + 1))
				.map(n -> new PrimeTestNumber(n, Arrays.binarySearch(PrimeList.PRIMES, n) >= 0));
	}

	@Test
	void testPrimeTestNumbers() {
		var actual = primeTestNumbers()
				.filter(n -> n.isPrime)
				.map(n -> Long.toString(n.number))
				.collect(Collectors.joining("\n"));
		var expected =
				Arrays.stream(PrimeList.PRIMES).mapToObj(n -> Long.toString(n)).collect(Collectors.joining("\n"));
		assertEquals(expected, actual);
	}

	public static class PrimeTestNumber {
		public final long number;
		public final boolean isPrime;

		public PrimeTestNumber(long number, boolean isPrime) {
			this.number = number;
			this.isPrime = isPrime;
		}
	}
}
