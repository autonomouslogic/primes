package com.autonomouslogic.primes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PrimeBitSetTest {
	@ParameterizedTest
	@MethodSource("bitsetTests")
	void shouldSetBits(PrimeTestUtil.PrimeTestNumber test) {
		var bitset = new PrimeBitSet(1 << 20);
		for (int i = 30; i < 200; i++) {
			if (i != test.number) {
				bitset.setIsNotPrime(i);
			}
		}
		for (int i = 30; i < 200; i++) {
			if (i == test.number) {
				assertTrue(bitset.isPrime(i), String.valueOf(i));
			} else {
				assertFalse(bitset.isPrime(i), String.valueOf(i));
			}
		}
	}

	static Stream<Arguments> bitsetTests() {
		return PrimeTestUtil.primeTestNumbers()
				.filter(n -> n.isPrime)
				.filter(n -> n.number > 30)
				.takeWhile(n -> n.number < 200)
				.map(Arguments::of);
	}

	//	@Test
	//	void shouldOutputPrimes() {
	//		var bits = new PrimeBitSet(1<<20);
	//		var last = PRIMES[PRIMES.length - 1];
	//		for (int i = 0; i <= last; i++) {
	//			if (Arrays.binarySearch(PRIMES, i) < 0) {
	//				bits.setIsNotPrime(i);
	//			}
	//		}
	//	}
}
