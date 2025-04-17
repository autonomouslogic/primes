package com.autonomouslogic.primes.meta;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class PrimeFileMeta {
	private long firstPrime;
	private long lastPrime;
	private long count;
	private long size;
	private String url;
	private ChecksumsMeta checksums;
	private Instant created;
}
