package com.autonomouslogic.primes.search;

import com.autonomouslogic.primes.PrimeBitSet;
import com.autonomouslogic.primes.PrimeSources;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.PrimitiveIterator;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Log4j2
public class PrimeSearch {
	private static final File tmpDir = new File(Configs.TMP_DIR.getRequired());
	private static final File indexJsonFile = new File(tmpDir, "primes.json");
	private static final File indexHtmlFile = new File(tmpDir, "primes.html");

	private static final int memory = PrimeBitSet.MAX_MEMORY;

	private static final long firstTargetFileCount = (long) Math.floor(Math.PI * 10_000.0);
	private static final long targetFileCount = (long) Math.floor(Math.PI * 100_000_000.0);
	//	private static final long firstTargetFileCount = 100;
	//	private static final long targetFileCount = firstTargetFileCount;
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

	private IndexMeta indexMeta;
	private PrimeFileMeta fileMeta;
	private boolean isFirstFile;

	@SneakyThrows
	private void run() {
		log.info("Starting search");
		initTmpDir();
		initMeta();
		if (!isFirstFile && getLastPrime() >= searchTarget) {
			log.info("Target {} reached", searchTarget);
			return;
		}
		var source = isFirstFile ? PrimeSources.all(memory) : PrimeSources.startingFrom(getLastPrime() + 2, memory);
		var iterator = source.iterator();
		while (isFirstFile || getLastPrime() < searchTarget) {
			fileMeta = new PrimeFileMeta().setCreated(currentTime());
			var primeFile = writePrimeFile(iterator);
			if (!isFirstFile) {
				primeFile = compressFile(primeFile);
			}
			fileMeta.setUrl(Configs.HTTP_BASE_PATH.getRequired() + "/" + primeFile.getName());
			fileMeta.setChecksums(createChecksums(primeFile));
			indexMeta.setUpdated(currentTime());
			indexMeta.getPrimeFiles().add(fileMeta);
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

	@SneakyThrows
	private File writePrimeFile(PrimitiveIterator.OfLong iterator) {
		var primeFile = new File(
				tmpDir,
				String.format("primes-%03d.txt", indexMeta.getPrimeFiles().size()));
		log.info("Writing primes to {}", primeFile);
		long n = 0;
		try (var out = new OutputStreamWriter(
				new BufferedOutputStream(new FileOutputStream(primeFile)), StandardCharsets.UTF_8)) {
			while (iterator.hasNext()) {
				var prime = iterator.next();
				if (n == 0) {
					fileMeta.setFirstPrime(prime);
				}
				fileMeta.setLastPrime(prime);

				out.write(String.valueOf(prime));
				out.write("\n");
				n++;

				if (isFirstFile) {
					if (n == firstTargetFileCount) {
						break;
					}
				} else if (n == targetFileCount) {
					break;
				}
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
		var xz = new ProcessBuilder("xz", "-f", "-T", "0", primeFile.getPath()).start();
		var exit = xz.waitFor();
		if (exit != 0) {
			var err = IOUtils.toString(xz.getErrorStream());
			throw new RuntimeException("XZ failed: " + exit + "\n" + err);
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

	private static final Instant currentTime() {
		return Instant.now().truncatedTo(ChronoUnit.SECONDS);
	}

	public static void main(String[] args) {
		new PrimeSearch().run();
	}
}
