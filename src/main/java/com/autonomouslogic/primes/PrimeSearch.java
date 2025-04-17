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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.stream.LongStream;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
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

	private final Instant currentTime = Instant.now().truncatedTo(ChronoUnit.SECONDS);
	private IndexMeta indexMeta;
	private PrimeFileMeta fileMeta;
	private boolean isFirstFile;
	private SieveOfEratosthenes sieve;
	private long lastPrime;

	@SneakyThrows
	private void run() {
		initMeta();
		createSieve();
		if (!isFirstFile) {
			initSieve();
		}
		var primes = runSieve();
		fileMeta = new PrimeFileMeta().setCreated(currentTime);
		var primeFile = writePrimeFile(primes);
		if (!isFirstFile) {
			primeFile = compressFile(primeFile);
		}
		fileMeta.setUrl("https://data.kennethjorgensen.com/primes/" + primeFile.getName());
		fileMeta.setChecksums(createChecksums(primeFile));
		indexMeta.setUpdated(currentTime).getPrimeFiles().add(fileMeta);
		writeIndexMeta();
	}

	private void initMeta() throws IOException {
		indexMeta = metaFile.exists()
				? objectMapper.readValue(metaFile, IndexMeta.class)
				: new IndexMeta().setPrimeFiles(new ArrayList<>());
		isFirstFile = indexMeta.getPrimeFiles().isEmpty();
	}

	private void createSieve() {
		long offset = 30;
		if (!isFirstFile) {
			lastPrime = indexMeta.getPrimeFiles().getLast().getLastPrime();
			offset = lastPrime - (lastPrime % 30);
			log.info("Previous files detected, using offset {} and lastPrime {}", offset, lastPrime);
		}
		var memory = Configs.SIEVE_MEMORY_BYTES.getRequired();
		log.info(String.format("Preparing search with %.2f MiB of memory", memory / (double) (1 << 20)));
		sieve = new SieveOfEratosthenes(offset, memory * 8);
	}

	@SneakyThrows
	private void initSieve() {
		for (var primeFile : indexMeta.getPrimeFiles()) {
			var file = new File("/tmp", new File(URI.create(primeFile.getUrl()).getPath()).getName());
			log.info("Initialising sieve from {}", file);
			try (var fin = new FileInputStream(file)) {
				InputStream in = fin;
				if (file.getName().endsWith(".xz")) {
					in = new XZCompressorInputStream(in);
				}
				var reader = new BufferedReader(new InputStreamReader(in));
				var stream = reader.lines().filter(s -> !s.isEmpty()).mapToLong(Long::valueOf);
				sieve.init(stream);
			}
		}
	}

	private LongStream runSieve() {
		var start = Instant.now();
		var primes = sieve.run();
		var time = Duration.between(start, Instant.now()).truncatedTo(ChronoUnit.MILLIS);
		log.info("Sieve completed in {}", time);

		if (lastPrime != 0) {
			log.info("Filtering primes starting from {}", lastPrime);
			primes = primes.filter(n -> n > lastPrime);
		}
		if (isFirstFile) {
			log.info("First time, truncating to one million");
			primes = primes.filter(n -> n < 1000000);
		}
		return primes;
	}

	@SneakyThrows
	private File writePrimeFile(LongStream primes) {
		var primeFile = new File(
				String.format("/tmp/primes-%02d.txt", indexMeta.getPrimeFiles().size()));
		log.info("Writing primes to {}", primeFile);
		long n = 0;
		try (var out = new FileWriter(primeFile, StandardCharsets.UTF_8)) {
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
		fileMeta.setCount(n).setUncompressedSize(primeFile.length());
		log.info("Wrote {} primes", n);
		return primeFile;
	}

	@SneakyThrows
	private File compressFile(File primeFile) {
		var start = Instant.now();
		log.info("Compressing {}", primeFile);
		var xz = new ProcessBuilder("xz", primeFile.getPath()).start();
		var exit = xz.waitFor();
		if (exit != 0) {
			throw new RuntimeException("XZ failed: " + exit);
		}
		var time = Duration.between(start, Instant.now()).truncatedTo(ChronoUnit.MILLIS);
		log.info("Compression completed in {}", time);
		primeFile = new File(primeFile.getPath() + ".xz");
		fileMeta.setCompressedSize(primeFile.length());
		return primeFile;
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

	private void writeIndexMeta() throws IOException {
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(metaFile, indexMeta);
	}

	public static void main(String[] args) {
		new PrimeSearch().run();
	}
}
