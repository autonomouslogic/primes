package com.autonomouslogic.primes;

import com.autonomouslogic.primes.meta.FileMeta;
import com.autonomouslogic.primes.meta.IndexMeta;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.FileWriter;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PrimeSearch {
	@SneakyThrows
	public static void main(String[] args) {
		var memory = Configs.SIEVE_MEMORY_BYTES.getRequired();
		log.info(String.format("Starting search with %.2f MiB of memory", memory / (double) (1 << 20)));
		var words = (int) (memory / SieveOfEratosthenes.WORD_BYTE_SIZE);
		log.info("Using {} words", words);

		log.info("Running sieve");
		var start = Instant.now();
		var primes = new SieveOfEratosthenes(words).run();
		var time = Duration.between(start, Instant.now()).truncatedTo(ChronoUnit.MILLIS);
		log.info("Sieve completed in {}", time);

		var fileMeta = new FileMeta();
		fileMeta.setTime(Instant.now().truncatedTo(ChronoUnit.SECONDS));

		var file = "/tmp/primes";
		log.info("Writing primes to {}", file);
		int n = 0;
		try (var out = new FileWriter(file)) {
			var iterator = primes.iterator();
			while (iterator.hasNext()) {
				var prime = iterator.next();
				out.write(String.valueOf(prime));
				out.write("\n");

				if (n == 0) {
					fileMeta.setFirst(prime);
				}
				fileMeta.setLast(prime);

				n++;
			}
		}
		fileMeta.setCount(n);

		log.info("Wrote {} primes", n);

		var index = new IndexMeta();
		index.setFiles(List.of(fileMeta));
		new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
				.disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS)
				.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
				.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
				.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
				.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
				.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
				.writeValue(new File("/tmp/primes.json"), index);
	}
}
