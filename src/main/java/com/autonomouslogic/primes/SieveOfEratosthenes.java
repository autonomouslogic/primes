package com.autonomouslogic.primes;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.PrimitiveIterator;

/**
 * <a href="https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes">Sieve of Eratosthenes</a>
 */
public class SieveOfEratosthenes implements PrimeSource {
	private final PrimeBitSet primeBits;
	private boolean init = false;
	private boolean run = false;

	public SieveOfEratosthenes(int memory) {
		primeBits = new PrimeBitSet(memory);
		init = true;
	}

	public SieveOfEratosthenes(long offset, int memory) {
		primeBits = new PrimeBitSet(offset, memory);
	}

	public void init(PrimitiveIterator.OfLong primes) {
		var firstNumber = firstNumber();
		var lastNumber = lastNumber();
		var lastCheck = lastCheck();
		System.out.println(String.format(
				"Init sieve - first number %s, last number %s, last check %s, offset %s, %s bytes",
				firstNumber, lastNumber, lastCheck, primeBits.offset(), primeBits.maxMemory()));
		var start = Instant.now();
		Util.toStream(primes).filter(n -> n != 2).takeWhile(n -> n <= lastCheck).forEach(n -> {
			for (long k = 3L * n; k <= lastNumber; k += 2L * n) {
				if (k >= firstNumber) {
					primeBits.setIsNotPrime(k);
				}
			}
		});
		var time = Duration.between(start, Instant.now()).truncatedTo(ChronoUnit.MILLIS);
		System.out.println(String.format("Init sieve completed in %s", time));
		init = true;
	}

	public void run() {
		var firstNumber = firstNumber();
		System.out.println(String.format("Running sieve"));
		if (firstNumber == 2) {
			firstNumber = 3;
		}
		var lastNumber = lastNumber();
		var lastCheck = lastCheck();
		var start = Instant.now();
		for (long n = firstNumber; n <= lastCheck; n += 2) {
			if (primeBits.isPrime(n)) {
				for (long k = 3L * n; k <= lastNumber; k += 2L * n) {
					primeBits.setIsNotPrime(k);
				}
			}
		}
		var time = Duration.between(start, Instant.now()).truncatedTo(ChronoUnit.MILLIS);
		System.out.println(String.format("Run sieve completed in %s", time));
		run = true;
	}

	public long lastCheck() {
		return PrimeUtils.maxRequiredCheck(lastNumber());
	}

	public long firstNumber() {
		return primeBits.firstNumber();
	}

	public long lastNumber() {
		return primeBits.lastNumber();
	}

	@Override
	public PrimitiveIterator.OfLong iterator() {
		if (!init || !run) {
			throw new IllegalStateException("Sieve not initialized");
		}
		return primeBits.iterator();
	}
}
