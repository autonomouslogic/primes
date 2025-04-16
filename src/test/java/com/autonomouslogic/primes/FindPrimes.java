package com.autonomouslogic.primes;

import java.io.FileWriter;
import java.util.ArrayList;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FindPrimes {
	@Test
	@Disabled
	void findPrimes() throws Exception {
		var out = new FileWriter("/tmp/primes");
		try {
			var primes = new ArrayList<Long>();
			primes.add(2L);
			primes.add(3L);
			for (long prime : primes) {
				System.out.println(String.format("Prime: %d", prime));
			}
			long check = primes.getLast() + 2;
			while (true) {
				long last = check / 2;
				boolean isPrime = true;
				for (long prime : primes) {
					if (prime >= last) {
						break;
					}
					if (check % prime == 0) {
						isPrime = false;
						break;
					}
				}
				if (isPrime) {
					primes.add(check);
					out.write(Long.toString(check) + '\n');
					System.out.println(String.format("Prime: %d", check));
				}
				check += 2;
			}
		} finally {
			out.close();
		}
	}
}
