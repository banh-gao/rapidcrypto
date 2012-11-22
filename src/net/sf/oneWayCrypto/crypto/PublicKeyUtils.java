package net.sf.oneWayCrypto.crypto;

import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * Provides public key utility functionalities
 */
public class PublicKeyUtils {

	/**
	 * Public key algorithm used in {@link #generateKeyPair(int)} and in
	 * {@link #decryptPrivateKey(byte[], String)}
	 */
	public static final String PUBLIC_KEY_ALG = "RSA";
	/**
	 * Cypher mode used to encrypt private key in
	 * {@link #encryptPrivateKey(PrivateKey, String)}
	 */
	public static final String PRIV_KEY_CYPHER_MODE = "PBEWithSHA1AndDESede";

	/**
	 * Generate key pair with the {@value #PUBLIC_KEY_ALG} algorithm
	 * 
	 * @param keyLength
	 *            the key length in bytes
	 * @return the generated keypair
	 */
	public static KeyPair generateKeyPair(int keyLength) {
		try {
			KeyPairGenerator gen = KeyPairGenerator.getInstance(PUBLIC_KEY_ALG);
			gen.initialize(keyLength);
			return gen.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e);
		}
	}

	/**
	 * Encrypt a DER encoded private key protected by a
	 * password
	 * 
	 * @param cryptedKey
	 *            the encrypted private key
	 * @param password
	 *            the password to decrypt the key
	 * @return the decrypted private key
	 * @throws Exception
	 */
	public static byte[] encryptPrivateKey(PrivateKey privateKey, String password) throws Exception {
		byte[] encodedprivkey = privateKey.getEncoded();

		int count = 20;// hash iteration count
		Random random = new SecureRandom();
		byte[] salt = new byte[8];
		random.nextBytes(salt);

		// Create PBE parameter set
		PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);
		PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
		SecretKeyFactory keyFac = SecretKeyFactory.getInstance(PRIV_KEY_CYPHER_MODE);
		SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

		Cipher pbeCipher = Cipher.getInstance(PRIV_KEY_CYPHER_MODE);

		// Initialize PBE Cipher with key and parameters
		pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

		// Encrypt the encoded Private Key with the PBE key
		byte[] ciphertext = pbeCipher.doFinal(encodedprivkey);

		// Now construct PKCS #8 EncryptedPrivateKeyInfo object
		AlgorithmParameters algparms = AlgorithmParameters.getInstance(PRIV_KEY_CYPHER_MODE);
		algparms.init(pbeParamSpec);
		EncryptedPrivateKeyInfo encinfo = new EncryptedPrivateKeyInfo(algparms, ciphertext);

		return encinfo.getEncoded();
	}

	/**
	 * Decrypt a DER encoded {@value #PUBLIC_KEY_ALG} private key protected by
	 * a password
	 * 
	 * @param encryptedKey
	 *            the encrypted private key
	 * @param password
	 *            the password to decrypt the key
	 * @return the decrypted private key
	 * @throws Exception
	 */
	public static PrivateKey decryptPrivateKey(byte[] encryptedKey, String password) throws Exception {
		EncryptedPrivateKeyInfo encryptPKInfo = new EncryptedPrivateKeyInfo(encryptedKey);

		// Create PBE secret key
		PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
		SecretKeyFactory secFac = SecretKeyFactory.getInstance(encryptPKInfo.getAlgName());
		SecretKey pbeKey = secFac.generateSecret(pbeKeySpec);

		Cipher cipher = Cipher.getInstance(encryptPKInfo.getAlgName());
		cipher.init(Cipher.DECRYPT_MODE, pbeKey, encryptPKInfo.getAlgParameters());

		PKCS8EncodedKeySpec keySpec = encryptPKInfo.getKeySpec(cipher);

		KeyFactory kf = KeyFactory.getInstance(PUBLIC_KEY_ALG);

		return kf.generatePrivate(keySpec);
	}
}
