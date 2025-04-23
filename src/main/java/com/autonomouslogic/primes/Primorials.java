package com.autonomouslogic.primes;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * <p>
 * Functions for working with <a href="https://en.wikipedia.org/wiki/Primorial">primorials</a>.
 * Each order <code>n</code> represents the primorial <code>p_n# = c#</code>, which is the sum of all primes between 2 and <code>c</code>.
 * Each order consists of the order <code>n</code>, the last prime <code>c</code>, the product,
 * and all the coprime offsets from <code>product</code>.
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
 * Order 0: c=1, product=1, 1 coprime offsets, 100.0% space
 * Order 1: c=2, product=2, 1 coprime offsets, 50.0% space
 * Order 2: c=3, product=6, 2 coprime offsets, 33.3% space
 * Order 3: c=5, product=30, 8 coprime offsets, 26.7% space
 * Order 4: c=7, product=210, 48 coprime offsets, 22.9% space
 * Order 5: c=11, product=2310, 480 coprime offsets, 20.8% space
 * Order 6: c=13, product=30030, 5760 coprime offsets, 19.2% space
 * Order 7: c=17, product=510510, 92160 coprime offsets, 18.1% space
 * Order 8: c=19, product=9699690, 1658880 coprime offsets, 17.1% space
 * Order 9: c=23, product=223092870, 36495360 coprime offsets, 16.4% space
 * </pre>
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Primorials {
	@Value
	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	public static class Order {
		/**
		 * The order of the primorial.
		 */
		int n;

		/**
		 * The last prime of the product.
		 */
		int c;

		/**
		 * The product of the primorial.
		 */
		long product;

		/**
		 * The coprime offsets which when added to multiples of the product are not divisible by any of the primes
		 * less than or equal to <code>c</code>.
		 */
		long[] coprimeOffsets;

		/**
		 * Returns all possible primes associated with this primorial order.
		 * Each number returned is in the form <code>product * k + i</code>, where <code>k</code> is an incrementing
		 * positive integer and <code>i</code> is each possible coprime offset.
		 * For instance, order 1 will return all odd numbers.
		 * Order 2 will first return 3 and 5, then 7 and 9, and so on.
		 * Order 3 will first return 31, 37, 41, 43, 47, 49, 53, 59, then 61, 67, 71, 73, 77, 79, 83, 89, and so on.
		 * Each order will return a higher start number, but the stream will contain fewer and fewer numbers.
		 * This can be used when searching for prime numbers.
		 *
		 * @return
		 */
		public LongStream possiblePrimes() {
			return LongStream.iterate(1, i -> i + 1)
					.flatMap(k -> Arrays.stream(coprimeOffsets).map(i -> k * this.product + i));
		}
	}

	/**
	 * Returns the primorial <code>p_n#</code>
	 * @param n
	 * @return
	 */
	public static long ofOrder(int n) {
		return Arrays.stream(PrimeList.PRIMES).limit(n).asLongStream().reduce(1, (left, right) -> left * right);
	}

	/**
	 * Returns a primorial order with all the coprime offsets calculated.
	 * @param n
	 * @return
	 */
	public static Primorials.Order ofOrderWithCoprimes(int n) {
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
		return new Primorials.Order(n, c, product, offsets);
	}

	/**
	 * @see #allPossiblePrimes(long)
	 * @return
	 */
	public static LongStream allPossiblePrimes() {
		return allPossiblePrimes(2);
	}

	/**
	 * Returns all the possible primes from each primorial order in sequence, starting from the supplied number.
	 * The sequence will gradually contain fewer and fewer numbers as the orders are able to advance.
	 * @param from the number to start from, must be at least 2
	 * @return
	 */
	public static LongStream allPossiblePrimes(long from) {
		if (from < 2) {
			throw new IllegalArgumentException("from must be at least 2");
		}
		return IntStream.rangeClosed(1, 8)
				.mapToObj(Primorials::ofOrderWithCoprimes)
				.filter(o -> ofOrder(o.getN() + 1) >= from)
				.flatMapToLong(order -> {
					if (order.getProduct() == 8) {
						return order.possiblePrimes();
					}
					var nextK = ofOrder(order.getN() + 1);
					var stream = order.possiblePrimes().takeWhile(num -> num < nextK);
					if (order.getProduct() < from) {
						stream = stream.filter(n -> n >= from);
					}
					return stream;
				});
	}

	public static void main(String[] args) {
		for (int i = 0; i <= 9; i++) {
			var order = Primorials.ofOrderWithCoprimes(i);
			var offsetsLen = order.getCoprimeOffsets().length;
			var space = 100.0 * offsetsLen / order.getProduct();
			System.out.printf(
					"Order %d: c=%d, product=%d, %d coprime offsets, %.1f%% space%n",
					order.getN(), order.getC(), order.getProduct(), offsetsLen, space);
		}
	}
}
