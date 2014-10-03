package edu.gatech.sqltutor.util;

import java.io.ByteArrayInputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SaltHasher {
	// PBKDF2 with SHA-1 as the hashing algorithm. Note that the NIST
	// specifically names SHA-1 as an acceptable hashing algorithm for PBKDF2
	private final static String ALGORITHM = "PBKDF2WithHmacSHA1";
	// SHA-1 generates 160 bit hashes, so that's what makes sense here
	private final static int DERIVED_KEY_LENGTH = 160;
	// Pick an iteration count that works for you. The NIST recommends at
	// least 1,000 iterations:
	// http://csrc.nist.gov/publications/nistpubs/800-132/nist-sp800-132.pdf
	private final static int ITERATIONS = 20000;

	public static boolean authenticate(String attemptedValue, byte[] encryptedValue, byte[] salt)
	throws NoSuchAlgorithmException, InvalidKeySpecException {
		// Encrypt the clear-text password using the same salt that was used to
		// encrypt the original password
		byte[] encryptedAttemptedValue = getEncryptedValue(attemptedValue, salt);
		// Authentication succeeds if encrypted password that the user entered
		// is equal to the stored hash
		return Arrays.equals(encryptedValue, encryptedAttemptedValue);
	}

	public static byte[] getEncryptedValue(String value, byte[] salt)
	throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeySpec spec = new PBEKeySpec(value.toCharArray(), salt, ITERATIONS, DERIVED_KEY_LENGTH);
		SecretKeyFactory f = SecretKeyFactory.getInstance(ALGORITHM);
		return f.generateSecret(spec).getEncoded();
	}

	public static byte[] generateSalt() throws NoSuchAlgorithmException {
		// VERY important to use SecureRandom instead of just Random
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		// Generate a 8 byte (64 bit) salt as recommended by RSA PKCS5
		byte[] salt = new byte[8];
		random.nextBytes(salt);
		return salt;
	}
}
