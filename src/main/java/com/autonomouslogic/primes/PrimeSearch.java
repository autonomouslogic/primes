package com.autonomouslogic.primes;

import com.autonomouslogic.primes.meta.ChecksumsMeta;
import com.autonomouslogic.primes.meta.IndexMeta;
import com.autonomouslogic.primes.meta.PrimeFileMeta;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

@Log4j2
public class PrimeSearch {
	private static final File metaFile = new File("/tmp/primes.json");
	private static final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS)
			.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
			.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
			.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
			.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
			.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);

	@SneakyThrows
	public static void main(String[] args) {
		var currentTime = Instant.now().truncatedTo(ChronoUnit.SECONDS);

		var indexMeta = metaFile.exists()
				? objectMapper.readValue(metaFile, IndexMeta.class)
				: new IndexMeta().setPrimeFiles(new ArrayList<>());
		var isFirstFile = indexMeta.getPrimeFiles().isEmpty();

		var memory = Configs.SIEVE_MEMORY_BYTES.getRequired();
		log.info(String.format("Starting search with %.2f MiB of memory", memory / (double) (1 << 20)));

		log.info("Running sieve");
		var start = Instant.now();
		var primes = new SieveOfEratosthenes(memory * 8).run();
		var time = Duration.between(start, Instant.now()).truncatedTo(ChronoUnit.MILLIS);
		log.info("Sieve completed in {}", time);

		var fileMeta = new PrimeFileMeta().setCreated(currentTime);

		var primeFile = new File(
				String.format("/tmp/primes-%02d.txt", indexMeta.getPrimeFiles().size()));
		fileMeta.setUrl("https://data.kennethjorgensen.com/primes/" + primeFile.getName());
		log.info("Writing primes to {}", primeFile);
		int n = 0;
		try (var out = new FileWriter(primeFile)) {
			var iterator = primes.iterator();
			while (iterator.hasNext()) {
				var prime = iterator.next();
				out.write(String.valueOf(prime));
				out.write("\n");

				if (n == 0) {
					fileMeta.setFirstPrime(prime);
				}
				fileMeta.setLastPrime(prime);

				n++;
			}
		}
		fileMeta.setCount(n).setSize(primeFile.length());

		log.info("Wrote {} primes", n);

		fileMeta.setChecksums(createChecksums(primeFile));

		indexMeta.setUpdated(currentTime).getPrimeFiles().add(fileMeta);
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(metaFile, indexMeta);
	}

	private static ChecksumsMeta createChecksums(File primeFile) {
		return new ChecksumsMeta()
				.setMd5(createChecksum(primeFile, Hashing.md5()))
				.setSha1(createChecksum(primeFile, Hashing.sha1()))
				.setSha256(createChecksum(primeFile, Hashing.sha256()))
				.setSha512(createChecksum(primeFile, Hashing.sha512()));
	}

	@SneakyThrows
	private static String createChecksum(File primeFile, HashFunction hashFunction) {
		try (var in = new FileInputStream(primeFile);
				var out = new HashingOutputStream(hashFunction, new NullOutputStream())) {
			IOUtils.copy(in, out);
			return out.hash().toString();
		}
	}
}
