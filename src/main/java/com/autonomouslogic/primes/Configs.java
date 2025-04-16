package com.autonomouslogic.primes;

import com.autonomouslogic.commons.config.Config;

public class Configs {
	public static final Config<Long> SIEVE_MEMORY_BYTES = Config.<Long>builder()
			.name("SIEVE_MEMORY_BYTES")
			.type(Long.class)
			.defaultValue(1L << 20)
			.build();
}
