package com.autonomouslogic.primes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.ref.WeakReference;
import org.junit.jupiter.api.Test;

public class ConcatPrimeSourceTest {
	PrimeSource trialDivision = new PrimalityTestPrimeSource(new TrialDivision());
	PrimeSource first = new BoundedPrimeSource(new PrimeList(), 2, 100);
	PrimeSource second = new BoundedPrimeSource(new PrimeList(), 101, 200);
	PrimeSource third = new BoundedPrimeSource(new PrimeList(), 201, 300);

	@Test
	void shouldConcatSources() {
		var primes = new ConcatPrimeSource(first, second).primeStream().iterator();
		var expected = new PrimeList().primeStream().iterator();
		while (primes.hasNext()) {
			assertEquals(expected.nextLong(), primes.nextLong());
		}
	}

	@Test
	void shouldLazyConcatSources() {
		var firstSupplier = spy(new PrimeSourceSupplier(first));
		var secondSupplier = spy(new PrimeSourceSupplier(second));
		var primes = new ConcatPrimeSource(firstSupplier, secondSupplier);
		verify(firstSupplier, never()).get();
		verify(secondSupplier, never()).get();
		var iterator = primes.primeStream().iterator();
		verify(firstSupplier, never()).get();
		verify(secondSupplier, never()).get();
		iterator.nextLong();
		verify(firstSupplier).get();
		verify(secondSupplier, never()).get();
		for (int i = 0; i < 24; i++) {
			var n = iterator.nextLong();
			assertTrue(n <= 100, Long.toString(n));
			verify(secondSupplier, never()).get();
		}
		var n = iterator.nextLong();
		assertTrue(n >= 101, Long.toString(n));
		verify(secondSupplier).get();
	}

	@Test
	void shouldLazyConcatManySources() {
		var verify = trialDivision.primeStream().iterator();
		var firstSupplier = spy(new PrimeSourceSupplier(first));
		var secondSupplier = spy(new PrimeSourceSupplier(second));
		var thirdSupplier = spy(new PrimeSourceSupplier(third));
		var iterator = new ConcatPrimeSource(firstSupplier, () -> new ConcatPrimeSource(secondSupplier, thirdSupplier))
				.primeStream()
				.filter(n -> n > 2)
				.iterator();
		verify(firstSupplier, never()).get();
		verify(secondSupplier, never()).get();
		verify(thirdSupplier, never()).get();
		assertEquals(verify.nextLong(), iterator.nextLong());
		verify(firstSupplier).get();
		verify(secondSupplier, never()).get();
		verify(thirdSupplier, never()).get();
		for (int i = 0; i < 23; i++) {
			assertEquals(verify.nextLong(), iterator.nextLong());
			verify(secondSupplier, never()).get();
			verify(thirdSupplier, never()).get();
		}
		assertEquals(verify.nextLong(), iterator.nextLong());
		verify(secondSupplier).get();
		verify(thirdSupplier, never()).get();
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
