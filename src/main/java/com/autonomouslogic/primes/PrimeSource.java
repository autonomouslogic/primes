package com.autonomouslogic.primes;

import java.util.stream.LongStream;

public interface PrimeSource {
	/**
	 * The first number this prime source will check. This is <b>not</b> the first prime number.
	 * @return
	 */
	long firstNumber();

	/**
	 * The last number this prime source will check. This is <b>not</b> the last prime number.
	 * @return
	 */
	default long lastNumber() {
		return Long.MAX_VALUE;
	}

	LongStream primeStream();

	default PrimeSource concat(PrimeSource other) {
		return new ConcatPrimeSource(this, other);
	}
}
