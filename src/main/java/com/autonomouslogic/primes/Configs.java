package com.autonomouslogic.primes;

import com.autonomouslogic.commons.config.Config;

public class Configs {
	public static final Config<Long> MAX_MEMORY = Config.<Long>builder()
			.name("MAX_MEMORY")
			.type(Long.class)
			.defaultValue(1L << 20)
			.build();
}
