package com.autonomouslogic.primes;

import java.util.PrimitiveIterator;
import java.util.Spliterator;
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

	PrimitiveIterator.OfLong iterator();

	default LongStream stream() {
		return Util.toStream(iterator());
	}

	default Spliterator.OfLong splierator() {
		return Util.toSpliterator(iterator());
	}

	default PrimeSource concat(PrimeSource other) {
		return new ConcatPrimeSource(this, other);
	}
}
