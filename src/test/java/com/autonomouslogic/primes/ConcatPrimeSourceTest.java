package com.autonomouslogic.primes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.ref.WeakReference;
import java.util.stream.LongStream;
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
		var expected =
				LongStream.concat(LongStream.of(2), trialDivision.primeStream()).iterator();
		var firstSupplier = spy(new PrimeSourceSupplier(first));
		var secondSupplier = spy(new PrimeSourceSupplier(second));
		var primes = new ConcatPrimeSource(firstSupplier, secondSupplier);
		verify(firstSupplier, never()).get();
		verify(secondSupplier, never()).get();
		var iterator = primes.primeStream().iterator();
		verify(firstSupplier, never()).get();
		verify(secondSupplier, never()).get();
		assertEquals(expected.nextLong(), iterator.nextLong());
		verify(firstSupplier).get();
		verify(secondSupplier, never()).get();
		for (int i = 0; i < 24; i++) {
			assertEquals(expected.nextLong(), iterator.nextLong());
			verify(secondSupplier, never()).get();
		}
		var n = iterator.nextLong();
		assertTrue(n >= 101, Long.toString(n));
		verify(secondSupplier).get();
	}

	@Test
	void shouldLazyConcatManySources() {
		var expected =
				LongStream.concat(LongStream.of(2), trialDivision.primeStream()).iterator();
		var firstSupplier = spy(new PrimeSourceSupplier(first));
		var secondSupplier = spy(new PrimeSourceSupplier(second));
		var thirdSupplier = spy(new PrimeSourceSupplier(third));
		System.out.println(String.format("firstSupplier: %s", System.identityHashCode(firstSupplier)));
		System.out.println(String.format("secondSupplier: %s", System.identityHashCode(secondSupplier)));
		System.out.println(String.format("thirdSupplier: %s", System.identityHashCode(thirdSupplier)));
		var iterator = new ConcatPrimeSource(firstSupplier, () -> new ConcatPrimeSource(secondSupplier, thirdSupplier))
				.primeStream()
				.iterator();
		verify(firstSupplier, never()).get();
		verify(secondSupplier, never()).get();
		verify(thirdSupplier, never()).get();
		assertEquals(expected.nextLong(), iterator.nextLong());
		verify(firstSupplier).get();
		verify(secondSupplier, never()).get();
		verify(thirdSupplier, never()).get();
		for (int i = 0; i < 24; i++) {
			assertEquals(expected.nextLong(), iterator.nextLong());
			verify(secondSupplier, never()).get();
			verify(thirdSupplier, never()).get();
		}
		System.out.println("---------------------- 1");
		assertEquals(expected.nextLong(), iterator.nextLong());
		System.out.println("---------------------- 2");
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
