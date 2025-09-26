package com.autonomouslogic.primes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

public class ConcatPrimeSourceTest {
	PrimeSource first = new BoundedPrimeSource(new PrimeList(), 2, 100);
	PrimeSource second = new BoundedPrimeSource(new PrimeList(), 101, 200);

	@Test
	void shouldConcatSources() {
		var primes = new ConcatPrimeSource(first, second).primeStream().iterator();
		var expected = new PrimeList().primeStream().iterator();
		while (primes.hasNext()) {
			assertEquals(expected.nextLong(), primes.nextLong());
		}
	}

	@RequiredArgsConstructor
	private class PrimeSourceSupplier implements Supplier<PrimeSource> {
		private final PrimeSource source;

		@Override
		public PrimeSource get() {
			return source;
		}
	}

	@Test
	void shouldLazyConcatSources() {
		var supplier = spy(new PrimeSourceSupplier(second));
		var primes = new ConcatPrimeSource(() -> first, supplier);
		verify(supplier, never()).get();
		var iterator = primes.primeStream().iterator();
		verify(supplier, never()).get();
		iterator.nextLong();
		verify(supplier, never()).get();
		for (int i = 0; i < 24; i++) {
			var n = iterator.nextLong();
			assertTrue(n <= 100, Long.toString(n));
			verify(supplier, never()).get();
		}
		var n = iterator.nextLong();
		assertTrue(n >= 101, Long.toString(n));
		verify(supplier).get();
	}

	@Test
	void shouldAllowGarbageCollection() {
		var firstRef = new WeakReference<PrimeSource>(new BoundedPrimeSource(new PrimeList(), 2, 100));
		var secondRef = new WeakReference<PrimeSource>(new BoundedPrimeSource(new PrimeList(), 101, 200));
		var primes = new ConcatPrimeSource(() -> firstRef.get(), () -> secondRef.get());
		primes.primeStream().boxed().toList();
		System.gc();
		assertNull(firstRef.get());
		assertNull(secondRef.get());
	}
}
