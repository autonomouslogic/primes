package com.autonomouslogic.primes;

import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

public class Util {
	public static Spliterator.OfLong toSpliterator(PrimitiveIterator.OfLong iterator) {
		return Spliterators.spliteratorUnknownSize(iterator, 0);
	}

	public static LongStream toStream(PrimitiveIterator.OfLong iterator) {
		return StreamSupport.longStream(toSpliterator(iterator), false);
	}
}
