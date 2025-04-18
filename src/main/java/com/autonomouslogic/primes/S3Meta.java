package com.autonomouslogic.primes;

import java.time.Duration;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class S3Meta {
	public static final S3Meta INDEX_JSON = new S3Meta(
			"application/json",
			String.format("max-age=%s, public", Duration.ofHours(1).toSeconds()));

	public static final S3Meta INDEX_HTML =
			INDEX_JSON.toBuilder().contentType("text/html").build();

	public static final S3Meta PRIME_FILE_PLAIN = new S3Meta(
			"text/plain",
			String.format("max-age=%s, public, immutable", Duration.ofDays(30).toSeconds()));

	public static final S3Meta PRIME_FILE_XZ =
			PRIME_FILE_PLAIN.toBuilder().contentType("application/x-xz").build();

	String contentType;
	String cacheControl;
}
