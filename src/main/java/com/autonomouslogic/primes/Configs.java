package com.autonomouslogic.primes;

import com.autonomouslogic.commons.config.Config;

public class Configs {
	public static final Config<Integer> MAX_MEMORY = Config.<Integer>builder()
			.name("MAX_MEMORY")
			.type(Integer.class)
			.defaultValue(1 << 20)
			.build();
}
