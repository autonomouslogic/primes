package com.autonomouslogic.primes;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * <p>
 * An order of priomorial.
 * Each order <code>n</code> is the primorial <code>p_n# = c#</code>.
 * Each order consists of the order <code>n</code>, the last prime <code>c</code>, the product <code>k</code>,
 * and all the coprime offsets <code>iOffsets</code> from <code>k</code>.
 * </p>
 *
 * <p>
 * See <a href="https://en.wikipedia.org/wiki/Primorial">Primorial</a>
 * and <a href="https://en.wikipedia.org/wiki/Primality_test#Simple_methods">Primality test (Simple methods)</a>.
 * </p>
 *
 * <p>
 * The main function in this class summarises the orders with the output:
 * <pre>
 * Order 0, c=1, k=1, 1 offsets, 100.0% space
 * Order 1, c=2, k=2, 1 offsets, 50.0% space
 * Order 2, c=3, k=6, 2 offsets, 33.3% space
 * Order 3, c=5, k=30, 8 offsets, 26.7% space
 * Order 4, c=7, k=210, 48 offsets, 22.9% space
 * Order 5, c=11, k=2310, 480 offsets, 20.8% space
 * Order 6, c=13, k=30030, 5760 offsets, 19.2% space
 * Order 7, c=17, k=510510, 92160 offsets, 18.1% space
 * Order 8, c=19, k=9699690, 1658880 offsets, 17.1% space
 * Order 9, c=23, k=223092870, 36495360 offsets, 16.4% space
 * </pre>
 * </p>
 */
@Value
@AllArgsConstructor
public class PrimorialOrder {
	int n;
	int c;
	long k;
	long[] iOffsets;

	/**
	 * Returns all possible primes associated with this order.
	 * Each number returned is in the form <code>n * k + i</code> for each possible integer <code>n</code> and each <code>i</code>.
	 * For instance, order 1 will return all odd numbers.
	 * Order 2 will first return 3 and 5, then 7 and 9, and so on.
	 * Order 3 will first return 31, 37, 41, 43, 47, 49, 53, 59, then 61, 67, 71, 73, 77, 79, 83, 89, and so on.
	 * Each order will return a higher start number, but the stream will contain less and less numbers.
	 * This can be used when searching for prime numbers.
	 * @return
	 */
	public LongStream possiblePrimes() {
		return LongStream.iterate(1, i -> i + 1)
				.flatMap(k -> Arrays.stream(iOffsets).map(i -> k * this.k + i));
	}

	/**
	 * Returns the primorial <code>p_n#</code>
	 * @param n
	 * @return
	 */
	public static long primorialOfOrder(int n) {
		return Arrays.stream(PrimeList.PRIMES).limit(n).asLongStream().reduce(1, (left, right) -> left * right);
	}

	/**
	 * Returns a specific primorial order.
	 * @param n
	 * @return
	 */
	public static PrimorialOrder ofOrder(int n) {
		var primes = Arrays.stream(PrimeList.PRIMES).limit(n).toArray();
		long product = 1;
		for (int prime : primes) {
			product *= prime;
		}
		final var finalProduct = product;
		var offsets = LongStream.range(product, 2 * product)
				.filter(num -> {
					for (int prime : primes) {
						if ((num % prime) == 0) {
							return false;
						}
					}
					return true;
				})
				.map(num -> num - finalProduct)
				.toArray();
		var c = primes.length == 0 ? 1 : primes[primes.length - 1];
		return new PrimorialOrder(n, c, product, offsets);
	}

	/**
	 * @see #allPossiblePrimes(long)
	 * @return
	 */
	public static LongStream allPossiblePrimes() {
		return allPossiblePrimes(2);
	}

	/**
	 * Returns all the possible primes from each primorial order in sequence, starting from the supplied number
	 * @param from the number to start from, must be at least 2
	 * @return
	 */
	public static LongStream allPossiblePrimes(long from) {
		if (from < 2) {
			throw new IllegalArgumentException("from must be at least 2");
		}
		return IntStream.rangeClosed(1, 8)
				.mapToObj(PrimorialOrder::ofOrder)
				.filter(o -> primorialOfOrder(o.getN() + 1) >= from)
				.flatMapToLong(order -> {
					if (order.getK() == 8) {
						return order.possiblePrimes();
					}
					var nextK = primorialOfOrder(order.getN() + 1);
					var stream = order.possiblePrimes().takeWhile(num -> num < nextK);
					if (order.getK() < from) {
						stream = stream.filter(n -> n >= from);
					}
					return stream;
				});
	}

	public static void main(String[] args) {
		for (int i = 0; i <= 9; i++) {
			var order = PrimorialOrder.ofOrder(i);
			var offsetsLen = order.getIOffsets().length;
			var space = 100.0 * offsetsLen / order.getK();
			System.out.printf(
					"Order %d, c=%d, k=%d, %d offsets, %.1f%% space%n",
					order.getN(), order.getC(), order.getK(), offsetsLen, space);
		}
	}
}
