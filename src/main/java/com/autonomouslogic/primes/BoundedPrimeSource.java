package com.autonomouslogic.primes;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.stream.LongStream;

@Accessors(fluent = true)
public class BoundedPrimeSource implements PrimeSource {
	private final PrimeSource source;

	@Getter
	private final long firstNumber;

	@Getter
	private final long lastNumber;

	public BoundedPrimeSource(PrimeSource source, long firstNumber, long lastNumber) {
		this.source = source;
		this.firstNumber = Math.max(firstNumber, source.firstNumber());
		this.lastNumber = Math.min(lastNumber, source.lastNumber());
	}

	public BoundedPrimeSource(PrimeSource source, long lastNumber) {
		this(source, source.firstNumber(), lastNumber);
	}

	@Override
	public LongStream primeStream() {
		return source.primeStream()
			.filter(n -> n >= firstNumber)
			.takeWhile(n -> n <= lastNumber);
	}
}
