package com.autonomouslogic.primes.meta;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class IndexMeta {
	private List<PrimeFileMeta> primeFiles;
	private Instant updated;
}
