package com.autonomouslogic.primes;

public interface BoundedPrimeSource extends PrimeSource {
	/**
	 * The last number this prime source will check. This is <b>not</b> the last prime number.
	 * @return
	 */
	long lastNumber();
}
