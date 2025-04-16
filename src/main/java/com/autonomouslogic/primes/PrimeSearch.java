package com.autonomouslogic.primes;

public class PrimeSearch {
	public static void main(String[] args) {
		var primes = new SieveOfEratosthenes(Integer.MAX_VALUE).run();
		System.out.println("Primes found: " + primes.size());
		System.out.println("Last prime: " + primes.getLast());
	}
}
