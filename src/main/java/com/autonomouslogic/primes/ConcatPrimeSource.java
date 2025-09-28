package com.autonomouslogic.primes;

import java.util.PrimitiveIterator;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
	public PrimitiveIterator.OfLong iterator() {
		return new ConcatLongIterator(new LazyIterator(first), new LazyIterator(second));
	}

	@RequiredArgsConstructor
	static class ConcatLongIterator implements PrimitiveIterator.OfLong {
		private final PrimitiveIterator.OfLong first;
		private final PrimitiveIterator.OfLong second;

		@Override
		public long nextLong() {
			if (first.hasNext()) {
				return first.nextLong();
			} else {
				return second.nextLong();
			}
		}

		@Override
		public boolean hasNext() {
			return first.hasNext() || second.hasNext();
		}
	}

	@RequiredArgsConstructor
	static class LazyIterator implements PrimitiveIterator.OfLong {
		private final Supplier<PrimeSource> supplier;
		private PrimitiveIterator.OfLong iterator;

		@Override
		public boolean hasNext() {
			return getIterator().hasNext();
		}

		@Override
		public long nextLong() {
			return getIterator().nextLong();
		}

		private PrimitiveIterator.OfLong getIterator() {
			if (iterator == null) {
				iterator = supplier.get().iterator();
			}
			return iterator;
		}
	}
}
