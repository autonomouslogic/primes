package com.autonomouslogic.primes.meta;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class ChecksumsMeta {
	private String md5;
	private String sha1;
	private String sha256;
	private String sha512;
}
