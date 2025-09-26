package com.autonomouslogic.primes;

import java.util.function.Supplier;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class ConcatPrimeSource implements PrimeSource {
	private final Supplier<PrimeSource> first;
	private final Supplier<PrimeSource> second;

	@Getter
	private long firstNumber = -1;

	@Getter
	private long lastNumber = Long.MAX_VALUE;

	public ConcatPrimeSource(@NonNull Supplier<PrimeSource> first, @NonNull Supplier<PrimeSource> second) {
		this.first = first;
		this.second = second;
	}

	public ConcatPrimeSource(@NonNull PrimeSource first, @NonNull PrimeSource second) {
		this(() -> first, () -> second);
		firstNumber = first.firstNumber();
		lastNumber = second.lastNumber();
		if (first.lastNumber() - second.firstNumber() > 1) {
			throw new IllegalStateException();
		}
	}

	@Override
	public LongStream primeStream() {
		//		return LongStream.concat(
		//		StreamSupport.longStream(() -> first.get().primeStream().spliterator(), 0, false),
		//		StreamSupport.longStream(() -> second.get().primeStream().spliterator(), 0, false)
		//		);
		return Stream.of(first, second)
				.flatMapToLong(supplier -> {
					System.out.println("Getting prime stream from supplier " + System.identityHashCode(supplier));
					//			return supplier.get().primeStream();
					return StreamSupport.longStream(supplier.get().primeStream().spliterator(), false);
				})
				.peek(n -> System.out.println("Got prime " + n));
	}
}
