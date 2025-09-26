package com.autonomouslogic.primes;

import java.util.function.Supplier;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class ConcatPrimeSource implements PrimeSource {
	private final PrimeSource first;
	private final Supplier<PrimeSource> second;

	@Getter
	private long lastNumber = Long.MAX_VALUE;

	public ConcatPrimeSource(@NonNull PrimeSource first, @NonNull Supplier<PrimeSource> second) {
		this.first = first;
		this.second = second;
	}

	public ConcatPrimeSource(@NonNull PrimeSource first, @NonNull PrimeSource second) {
		this(first, () -> second);
		lastNumber = second.lastNumber();
		if (first.lastNumber() - second.firstNumber() > 1) {
			throw new IllegalStateException();
		}
	}

	@Override
	public long firstNumber() {
		return first.firstNumber();
	}

	@Override
	public LongStream primeStream() {
		return Stream.of(first, second).flatMapToLong(source -> {
			if (source == first) {
				return first.primeStream();
			} else if (source == second) {
				return second.get().primeStream();
			} else {
				throw new IllegalStateException();
			}
		});
	}
}
