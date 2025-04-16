package com.autonomouslogic.primes.meta;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class FileMeta {
	private long first;
	private long last;
	private long count;
	private String url;
	private ChecksumsMeta checksums;
	private Instant time;
}
