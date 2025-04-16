package com.autonomouslogic.primes;

import java.io.FileWriter;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PrimeSearch {
	@SneakyThrows
	public static void main(String[] args) {
		var memory = Configs.MAX_MEMORY.getRequired();
		log.info(String.format("Starting search with %.2f MiB of memory", memory / (double) (1 << 20)));
		var words = (int) (memory / SieveOfEratosthenes.WORD_BYTE_SIZE);
		log.info("Using {} words", words);
		log.info("Running sieve");
		var start = Instant.now();
		var primes = new SieveOfEratosthenes(words).run();
		var time = Duration.between(start, Instant.now()).truncatedTo(ChronoUnit.MILLIS);
		log.info("Sieve completed in {}", time);
		log.info("Primes found: {}", primes.size());
		log.info("Last prime: {}", primes.getLast());
		var file = "/tmp/primes";
		log.info("Writing primes to {}", file);
		try (var out = new FileWriter(file)) {
			for (long prime : primes) {
				out.write(String.valueOf(prime));
				out.write("\n");
			}
		}
		log.info("Completed");
	}
}
