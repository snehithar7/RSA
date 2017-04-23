package security;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class RSAKey {
	public static void main(String[] a) {
		for (int i = 0; i < 10; i++) {
			generateKey(1024);
		}
	}

	public static void generateKey(int size) {

		Random rnd = new Random();

		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			KeyPair myPair = kpg.generateKeyPair();

			BigInteger p = BigInteger.probablePrime(size / 2, rnd);
			BigInteger q = p.nextProbablePrime();
			/*
			 * BigInteger p = BigInteger.valueOf(7); BigInteger q =
			 * BigInteger.valueOf(11);
			 */
			BigInteger n = p.multiply(q);
			BigInteger m = (p.subtract(BigInteger.ONE)).multiply(q
					.subtract(BigInteger.ONE));
			BigInteger e = getCoprime(m);
			BigInteger d = e.modInverse(m);
			
			
			Cipher c = Cipher.getInstance("RSA");
			// Initiate the Cipher, telling it that it is going to Encrypt, giving it the public key
			

			System.out.println("p: " + p);
			System.out.println("q: " + q);
			// System.out.println("m: "+m);
			// System.out.println("Modulus: "+n);
			System.out.println("Key size: " + n.bitLength());
			System.out.println("Public key: " + e);
			System.out.println("Private key: " + d);
			System.out
					.println("----------------------------------------------------------------------------------------------------\n");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static BigInteger getCoprime(BigInteger m) {
		Random rnd = new Random();
		int length = m.bitLength() - 1;
		BigInteger e = BigInteger.probablePrime(length, rnd);
		while (!(m.gcd(e)).equals(BigInteger.ONE)) {
			e = BigInteger.probablePrime(length, rnd);
		}
		return e;
	}

}
