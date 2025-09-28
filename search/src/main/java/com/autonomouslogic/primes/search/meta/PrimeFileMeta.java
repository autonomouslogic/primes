package com.autonomouslogic.primes.search.meta;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrimeFileMeta {
	private Long firstPrime;
	private Long lastPrime;
	private Long count;
	private Long uncompressedSize;
	private Long compressedSize;
	private String url;
	private ChecksumsMeta checksums;
}
