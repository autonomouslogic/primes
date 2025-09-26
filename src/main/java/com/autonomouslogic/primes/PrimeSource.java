package com.autonomouslogic.primes;

import java.util.stream.LongStream;

public interface PrimeSource {
	/**
	 * The first number this prime source will check. This is <b>not</b> the first prime number.
	 * @return
	 */
	long firstNumber();

	LongStream primeStream();
}
