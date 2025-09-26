package com.autonomouslogic.primes;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.stream.LongStream;

public class ConcatPrimeSource implements PrimeSource {
	private final PrimeSource first;
	private final PrimeSource second;

	public ConcatPrimeSource(@NonNull PrimeSource first, @NonNull PrimeSource second) {
		if (first.firstNumber() >= second.firstNumber()) {
			throw new IllegalArgumentException("First source must be smaller than second source");
		}
		this.first = first;
		this.second = second;
	}

	@Override
	public long firstNumber() {
		return first.firstNumber();
	}

	@Override
	public long lastNumber() {
		return second.lastNumber();
	}

	@Override
	public LongStream primeStream() {
		// @todo optimise this so the second stream isn't initialised until needed
		return LongStream.concat(first.primeStream(), second.primeStream());
	}
}
