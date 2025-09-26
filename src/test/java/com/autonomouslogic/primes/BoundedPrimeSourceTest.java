package com.autonomouslogic.primes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class BoundedPrimeSourceTest {
	@Test
	void shouldMinMax() {
		var source = new BoundedPrimeSource(new PrimeList(), 1000, 10000);
		assertEquals(1000, source.firstNumber());
		assertEquals(10000, source.lastNumber());
		assertEquals(1009, source.stream().min().getAsLong());
		assertEquals(9973, source.stream().max().getAsLong());
	}

	@Test
	void shouldMax() {
		var source = new BoundedPrimeSource(new PrimeList(), 10000);
		assertEquals(2, source.firstNumber());
		assertEquals(10000, source.lastNumber());
		assertEquals(9973, source.stream().max().getAsLong());
	}
}
