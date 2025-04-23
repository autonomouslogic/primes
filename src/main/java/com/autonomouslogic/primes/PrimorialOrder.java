package com.autonomouslogic.primes;

import java.util.Arrays;
import java.util.stream.LongStream;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * <p>
 * An order of priomorial.
 * Each order <code>n</code> is the primorial <code>p_n#</code>.
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
	 * @return
	 */
	public LongStream possiblePrimes() {
		return LongStream.iterate(1, i -> i + 1)
				.flatMap(k -> Arrays.stream(iOffsets).map(i -> k * this.k + i));
	}

	/**
	 * Returns a specific primorial order.
	 * @param order
	 * @return
	 */
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
		var c = primes.length == 0 ? 1 : primes[primes.length - 1];
		return new PrimorialOrder(order, c, product, offsets);
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
