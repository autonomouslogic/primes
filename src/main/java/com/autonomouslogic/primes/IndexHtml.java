package com.autonomouslogic.primes;

import com.autonomouslogic.primes.meta.IndexMeta;
import com.autonomouslogic.primes.meta.PrimeFileMeta;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

@RequiredArgsConstructor
public class IndexHtml {
	private static final DecimalFormat numberFormat = new DecimalFormat("#,###.#");
	private static final DecimalFormat sizeFormat = new DecimalFormat("#,###.00");
	private static final double kilo = 1 << 10;
	private static final double mega = 1 << 20;
	private static final double giga = 1 << 30;

	private final IndexMeta indexMeta;

	public String generate() {
		return header() + table() + footer();
	}

	@SneakyThrows
	public void generate(File file) {
		try (var out = new FileOutputStream(file)) {
			IOUtils.copy(new StringReader(generate()), out, StandardCharsets.UTF_8);
		}
	}

	private String header() {
		var title = "Huge Lists of Prime Numbers";
		return String.format(
				"""
			<!DOCTYPE html>
			<html lang="en">
			<head>
				<script data-goatcounter="https://kennethjorgensen-data.goatcounter.com/count" async src="//gc.zgo.at/count.js"></script>
				<title>%s - data.kennethjorgensen.com</title>
				<meta charset="utf-8">
				<meta name="viewport" content="width=device-width, initial-scale=1.0, shrink-to-fit=no">
				<meta property="og:title" content="%s - data.kennethjorgensen.com">
				<meta property="twitter:title" content="%s - data.kennethjorgensen.com">
				<style>
					.text-right { text-align: right; }
					.text-left { text-align: left; }
					.text-italic { font-style: italic; }
					th, td { padding: 5px; }
				</style>
			</head>
			<body>
			<h1>%s</h1>
			<p>
				The files in the table below contain lists of prime numbers.
				The first file is uncompressed to provide an easy example of the format, which is just one number per line.
				The rest are xz compressed.
				On Windows, 7-Zip should be able to decompress them.
				See <a href="https://github.com/autonomouslogic/primes">this repo</a> for details on how they're calculated.
			</p>
			<p>
				This index is also available as <a href="primes.json">JSON</a>.
			</p>
			<hr/>
			""",
				title, title, title, title);
	}

	private String table() {
		return String.format(
				"""
			<table>
				<thead>
					<tr>
						<th class="text-left">File</th>
						<th class="text-right">First prime</th>
						<th class="text-right">Last prime</th>
						<th class="text-right">Count</th>
						<th class="text-right">Compressed size</th>
						<th class="text-right">Uncompressed size</th>
					</tr>
				</thead>
				<tbody>
				%s
				%s
				</tbody>
			</table>
			""",
				tableRows(), totalRow());
	}

	private String tableRows() {
		return indexMeta.getPrimeFiles().stream()
				.map(file -> String.format(
						"""
			<tr>
				<td class="text-left"><a href="%s">%s</a></td>
				<td class="text-right">%s</td>
				<td class="text-right">%s</td>
				<td class="text-right">%s</td>
				<td class="text-right">%s</td>
				<td class="text-right">%s</td>
			</tr>
			""",
						file.getUrl(),
						new File(URI.create(file.getUrl()).getPath()).getName(),
						numberFormat.format(file.getFirstPrime()),
						numberFormat.format(file.getLastPrime()),
						numberFormat.format(file.getCount()),
						formatSize(file.getCompressedSize()),
						formatSize(file.getUncompressedSize())))
				.collect(Collectors.joining("\n"));
	}

	private String totalRow() {
		var totalPrimes = indexMeta.getPrimeFiles().stream()
				.mapToLong(PrimeFileMeta::getCount)
				.sum();
		var totalCompressedSize = indexMeta.getPrimeFiles().stream()
				.filter(f -> f.getCompressedSize() != null)
				.mapToLong(PrimeFileMeta::getCompressedSize)
				.sum();
		var totalUncompressedSize = indexMeta.getPrimeFiles().stream()
				.mapToLong(PrimeFileMeta::getUncompressedSize)
				.sum();
		return String.format(
				"""
			<tr>
				<td class="text-left text-italic">Totals</td>
				<td></td>
				<td></td>
				<td class="text-right text-italic">%s</td>
				<td class="text-right text-italic">%s</td>
				<td class="text-right text-italic">%s</td>
			</tr>
			""",
				numberFormat.format(totalPrimes), formatSize(totalCompressedSize), formatSize(totalUncompressedSize));
	}

	private String footer() {
		return """
			</body>
			</html>
			""";
	}

	private String formatSize(Long size) {
		if (size == null) {
			return "";
		}
		if (size >= giga * 0.9) {
			return sizeFormat.format(size / giga) + " GiB";
		}
		if (size >= mega * 0.9) {
			return sizeFormat.format(size / mega) + " MiB";
		}
		return sizeFormat.format(size / kilo) + " KiB";
	}
}
