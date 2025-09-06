package com.autonomouslogic.primes.search;

import com.autonomouslogic.primes.SieveOfEratosthenes;
import com.autonomouslogic.primes.search.meta.ChecksumsMeta;
import com.autonomouslogic.primes.search.meta.IndexMeta;
import com.autonomouslogic.primes.search.meta.PrimeFileMeta;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.stream.LongStream;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Log4j2
public class PrimeSearch {
	private static final File tmpDir = new File(Configs.TMP_DIR.getRequired());
	private static final File indexJsonFile = new File(tmpDir, "primes.json");
	private static final File indexHtmlFile = new File(tmpDir, "primes.html");

	private static final long firstTargetFileCount = (long) Math.floor(Math.PI * 10_000.0);
//	private static final long targetFileCount = (long) Math.floor(Math.PI * 100_000_000.0);
	private static final long targetFileCount = (long) Math.floor(Math.PI * 100_000.0);
	private static final long searchTarget = (long) 1e12;

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
		initTmpDir();
		initMeta();
		if (!isFirstFile && getLastPrime() >= searchTarget) {
			log.info("Target {} reached", searchTarget);
			return;
		}
		createSieve();
		if (!isFirstFile) {
			initSieve();
		}
		var iterator = runSieve().iterator();
		fileMeta = new PrimeFileMeta().setCreated(currentTime);
		while (isFirstFile || getLastPrime() < searchTarget) {
			var primeFile = writePrimeFile(iterator);
			if (!isFirstFile) {
				primeFile = compressFile(primeFile);
			}
			fileMeta.setUrl(Configs.HTTP_BASE_PATH.getRequired() + "/" + primeFile.getName());
			fileMeta.setChecksums(createChecksums(primeFile));
			indexMeta.setUpdated(currentTime).getPrimeFiles().add(fileMeta);
			upload(primeFile, primeFile.getPath().endsWith(".xz") ? S3Meta.PRIME_FILE_XZ : S3Meta.PRIME_FILE_PLAIN);
			writeIndexJson();
			writeIndexHtml();
			upload(indexJsonFile, S3Meta.INDEX_JSON);
			upload(indexHtmlFile, S3Meta.INDEX_HTML);
			isFirstFile = false;
		}
	}

	private long getLastPrime() {
		return indexMeta.getPrimeFiles().getLast().getLastPrime();
	}

	private void initTmpDir() {
		if (!tmpDir.exists()) {
			log.info("Creating tmp dir: {}", tmpDir);
			if (!tmpDir.mkdirs()) {
				throw new RuntimeException("Failed to create tmp dir: " + tmpDir);
			}
		}
	}

	private void initMeta() throws IOException {
		indexMeta = indexJsonFile.exists()
				? objectMapper.readValue(indexJsonFile, IndexMeta.class)
				: new IndexMeta().setPrimeFiles(new ArrayList<>());
		isFirstFile = indexMeta.getPrimeFiles().isEmpty();
	}

	private void createSieve() {
		long offset = 30;
		if (!isFirstFile) {
			lastPrime = getLastPrime();
			offset = lastPrime - (lastPrime % 30);
			log.info("Previous files detected, using offset {} and lastPrime {}", offset, lastPrime);
		}
		var memory = isFirstFile ? 128 << 10 : Configs.SIEVE_MEMORY_BYTES.getRequired();
		log.info(String.format("Preparing search with %.2f MiB of memory", memory / (double) (1 << 20)));
		sieve = new SieveOfEratosthenes(offset, memory);
	}

	@SneakyThrows
	private void initSieve() {
		var lastCheck = sieve.getLastCheck();
		for (var primeFile : indexMeta.getPrimeFiles()) {
			var filename = new File(URI.create(primeFile.getUrl()).getPath()).getName();
			if (primeFile.getFirstPrime() > lastCheck) {
				log.info(
						"Skipping {} as first prime {} is larger than last check {}",
						filename,
						primeFile.getFirstPrime(),
						lastCheck);
				continue;
			}
			var start = Instant.now();
			var file = new File(tmpDir, filename);
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
			var time = Duration.between(start, Instant.now()).truncatedTo(ChronoUnit.MILLIS);
			log.info("File initialisation complete in {}", time);
		}
	}

	private LongStream runSieve() {
		var start = Instant.now();
		log.info("Running sieve");
		var primes = sieve.run();
		var time = Duration.between(start, Instant.now()).truncatedTo(ChronoUnit.MILLIS);
		log.info("Sieve completed in {}", time);

		if (lastPrime != 0) {
			log.info("Filtering primes starting from {}", lastPrime);
			primes = primes.filter(n -> n > lastPrime);
		}
		return primes;
	}

	@SneakyThrows
	private File writePrimeFile(PrimitiveIterator.OfLong iterator) {
		var primeFile = new File(
				tmpDir,
				String.format("primes-%03d.txt", indexMeta.getPrimeFiles().size()));
		log.info("Writing primes to {}", primeFile);
		long n = 0;
		try (var counting = new CountingOutputStream(new BufferedOutputStream(new FileOutputStream(primeFile)));
				var out = new OutputStreamWriter(counting, StandardCharsets.UTF_8)) {
			while (iterator.hasNext()) {
				var prime = iterator.next();
				out.write(String.valueOf(prime));
				out.write("\n");

				if (n == 0) {
					fileMeta.setFirstPrime(prime);
				}
				fileMeta.setLastPrime(prime);

				if (isFirstFile) {
					if (n == firstTargetFileCount) {
						break;
					}
				} else if (n == targetFileCount) {
					break;
				}

				n++;
			}
		}
		fileMeta.setCount(n).setUncompressedSize(primeFile.length());
		if (n == 0) {
			throw new IllegalStateException("No primes written");
		}
		log.info("Wrote {} primes", n);
		return primeFile;
	}

	@SneakyThrows
	private File compressFile(File primeFile) {
		var start = Instant.now();
		log.info("Compressing {}", primeFile);
		var xz = new ProcessBuilder("xz", "-T", "0", primeFile.getPath()).start();
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

	private void writeIndexJson() throws IOException {
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(indexJsonFile, indexMeta);
	}

	private void writeIndexHtml() {
		log.info("Rendering HTML index to {}", indexHtmlFile);
		new IndexHtml(indexMeta).generate(indexHtmlFile);
	}

	private void upload(File file, S3Meta s3Meta) {
		if (Configs.S3_BASE_URL.get().isEmpty()) {
			log.info("S3_BASE_URL not set, skipping upload");
			return;
		}
		var baseUri = URI.create(Configs.S3_BASE_URL.getRequired());
		if (!baseUri.getScheme().equals("s3")) {
			throw new RuntimeException(baseUri + " is not an S3 URL");
		}
		var url = baseUri.resolve(file.getName());
		log.info("Uploading {} to {} with meta {}", file, url, s3Meta);

		var req = PutObjectRequest.builder()
				.bucket(url.getHost())
				.key(StringUtils.stripStart(url.getPath(), "/"))
				.contentType(s3Meta.getContentType())
				.cacheControl(s3Meta.getCacheControl())
				.build();

		var clientBuilder = S3Client.builder();
		Configs.S3_ENDPOINT_URL.get().ifPresent(endpointUrl -> clientBuilder.endpointOverride(URI.create(endpointUrl)));
		var client = clientBuilder.build();

		var res = client.putObject(req, file.toPath());
		log.info("{}/{} uploaded with version {}", req.bucket(), req.key(), res.versionId());
	}

	public static void main(String[] args) {
		new PrimeSearch().run();
	}
}
