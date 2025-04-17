package com.autonomouslogic.primes;

import com.autonomouslogic.commons.config.Config;

public class Configs {
	public static final Config<Integer> SIEVE_MEMORY_BYTES = Config.<Integer>builder()
			.name("SIEVE_MEMORY_BYTES")
			.type(Integer.class)
			.defaultValue(128 << 20)
			.build();
}
