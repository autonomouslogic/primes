package com.autonomouslogic.primes;

import java.util.Arrays;
import java.util.stream.LongStream;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class PrimorialOrder {
	int order;
	long primeProduct;
	long[] probablePrimeOffsets;

	public LongStream getProbablePrimes() {
		return LongStream.iterate(1, i -> i + 1)
				.flatMap(k -> Arrays.stream(probablePrimeOffsets).map(i -> k * primeProduct + i));
	}

	public static PrimorialOrder ofOrder(int order) {
		var primes = Arrays.stream(PrimeList.PRIMES).limit(order).toArray();
		long product = 1;
		for (int prime : primes) {
			product *= prime;
		}
		final var finalProduct = product;
		var offsets = LongStream.range(product, 2 * product)
				.filter(n -> {
					for (int prime : primes) {
						if ((n % prime) == 0) {
							return false;
						}
					}
					return true;
				})
				.map(n -> n - finalProduct)
				.toArray();
		return new PrimorialOrder(order, product, offsets);
	}

	public static void main(String[] args) {
		for (int i = 0; i <= 4; i++) {
			System.out.println(PrimorialOrder.ofOrder(i));
		}
	}
}
