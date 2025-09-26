package com.autonomouslogic.primes;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class PrimeSourceSupplier implements Supplier<PrimeSource> {
	private final PrimeSource source;

	@Override
	public PrimeSource get() {
		return source;
	}
}
