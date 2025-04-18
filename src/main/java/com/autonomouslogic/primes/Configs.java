package com.autonomouslogic.primes;

import com.autonomouslogic.commons.config.Config;
import java.util.Optional;

public class Configs {
	public static final Config<Integer> SIEVE_MEMORY_BYTES = Config.<Integer>builder()
			.name("SIEVE_MEMORY_BYTES")
			.type(Integer.class)
			.defaultValue(128 << 20) // 128 MiB produces files around 2 GiB uncompressed and 200 MiB compressed
			.build();

	public static final Config<String> TMP_DIR = Config.<String>builder()
			.name("TMP_DIR")
			.type(String.class)
			.defaultMethod(() -> Optional.ofNullable(System.getProperty("java.io.tmpdir")))
			.build();

	public static final Config<String> HTTP_BASE_PATH = Config.<String>builder()
			.name("HTTP_BASE_PATH")
			.type(String.class)
			.defaultValue("https://data.kennethjorgensen.com/primes")
			.build();

	public static final Config<String> S3_BASE_URL =
			Config.<String>builder().name("S3_BASE_URL").type(String.class).build();

	public static final Config<String> S3_ENDPOINT_URL =
			Config.<String>builder().name("S3_ENDPOINT_URL").type(String.class).build();
}
