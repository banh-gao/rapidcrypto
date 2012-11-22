package net.sf.oneWayCrypto.crypto;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import android.net.Uri;

/**
 * Allows to efficently encrypt and decrypt a file using asymmetric cryptography
 * keys
 */
public class FileCryptoUtils {

	/**
	 * Secret key algorithm used to encrypt file content
	 */
	public static final String SECRET_KEY_ALG = "AES";
	/**
	 * Secret key size
	 */
	public static final int SECRET_KEY_SIZE = 256;
	/**
	 * Cypher mode used to encrypt secret key with a public key used in
	 * {@link #encryptSecretKey(SecretKey, PublicKey)}
	 */
	public static final String SECRET_KEY_CYPHER_MODE = "AES/ECB/PKCS5Padding";
	/**
	 * Cypher mode used to encrypt file content in
	 * {@link #encryptFileContent(SecretKey, URI, DataOutputStream, ProgressListener)}
	 */
	public static final String CONTENT_CYPHER_MODE = SECRET_KEY_ALG + "/CBC/PKCS5Padding";

	private static final int BUF_SIZE = 8192;

	/**
	 * Encrypt the sourceFile and returns the uri to the encrypted file
	 * 
	 * @param sourceUri
	 *            the file to be encrypted
	 * @param publicKey
	 *            the public key to be used
	 * @param progressListener
	 *            the listener to be notified about the encryption progresses or
	 *            null
	 * @return The encrypted file uri
	 * @throws Exception
	 */
	public static Uri encryptFile(Uri sourceUri, PublicKey publicKey, ProgressListener progressListener) throws Exception {

		File outputFile = File.createTempFile("OWC_encrypt", null);

		outputFile.createNewFile();
		outputFile.deleteOnExit();

		DataOutputStream encryptedOutputStream = new DataOutputStream(new FileOutputStream(outputFile));

		SecretKey secretKey = generateRandomKey();

		// Write encrypted simmetric key using destination public key
		byte[] encryptedKey = encryptSecretKey(secretKey, publicKey);
		encryptedOutputStream.writeInt(encryptedKey.length);
		encryptedOutputStream.write(encryptedKey);

		// Write encrypted data
		encryptFileContent(secretKey, sourceUri, encryptedOutputStream, progressListener);
		encryptedOutputStream.close();

		return Uri.fromFile(outputFile);
	}

	/**
	 * Generate a random symmetric key to use with encryption
	 */
	private static SecretKey generateRandomKey() {
		KeyGenerator kgen;
		try {
			kgen = KeyGenerator.getInstance(FileCryptoUtils.SECRET_KEY_ALG);
			kgen.init(FileCryptoUtils.SECRET_KEY_SIZE, new SecureRandom());
			return new SecretKeySpec(kgen.generateKey().getEncoded(), FileCryptoUtils.SECRET_KEY_ALG);
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e);
		}
	}

	private static byte[] encryptSecretKey(SecretKey secretKey, PublicKey publicKey) {
		try {
			Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(secretKey.getEncoded());
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e);
		} catch (InvalidKeyException e) {
			throw new Error(e);
		} catch (NoSuchPaddingException e) {
			throw new Error(e);
		} catch (IllegalBlockSizeException e) {
			throw new Error(e);
		} catch (BadPaddingException e) {
			throw new Error(e);
		}
	}

	private static void encryptFileContent(SecretKey key, Uri sourceUri, DataOutputStream out, ProgressListener progressListener) throws Exception {
		File sourceFile = new File(new URI(sourceUri.toString()));
		if (progressListener == null)
			progressListener = new ProgressListener() {

				@Override
				public void onProgressUpdate(Uri sourceFile, int progress) {
				}
			};

		long inputLength = sourceFile.length();

		Cipher cipher = Cipher.getInstance(FileCryptoUtils.CONTENT_CYPHER_MODE);
		byte[] iv = generateIV(key);
		cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

		InputStream plainStream = new FileInputStream(sourceFile);

		try {
			byte[] buffer = new byte[BUF_SIZE];
			int readed = 0, lastProgress = 0, tmp = 0;
			progressListener.onProgressUpdate(sourceUri, 0);
			while (tmp > -1) {
				tmp = plainStream.read(buffer);
				if (tmp > -1) {
					byte[] block = cipher.update(buffer, 0, tmp);
					if (block != null)
						out.write(block);
				}
				readed += tmp;

				int progress = (int) Math.floor((readed / (double) inputLength) * 100);
				if (lastProgress < progress && progress < 100) {
					progressListener.onProgressUpdate(sourceUri, progress);
					lastProgress = progress;
				}
			}
			out.write(cipher.doFinal());
		} finally {
			plainStream.close();
		}
		progressListener.onProgressUpdate(sourceUri, 100);
	}

	private static byte[] generateIV(SecretKey key) {
		byte[] iv = new byte[16];
		System.arraycopy(key.getEncoded(), 0, iv, 0, iv.length);
		return iv;
	}

	/**
	 * Decrypt the sourceFile and returns the uri to the decrypted version
	 * 
	 * @param sourceUri
	 *            the file to be decrypted
	 * @param privateKey
	 *            the private key to be used
	 * @param progressListener
	 *            the listener to be notified about the decryption progresses or
	 *            null
	 * @return The decrypted file uri
	 * @throws Exception
	 */
	public static Uri decryptFile(Uri sourceUri, PrivateKey privateKey, ProgressListener progressListener) throws Exception {
		File sourceFile = new File(new URI(sourceUri.toString()));

		DataInputStream cryptedInputStream = new DataInputStream(new FileInputStream(sourceFile));

		int keyLength = cryptedInputStream.readInt();
		byte[] encryptedKey = new byte[keyLength];
		int readed = 0;
		while (readed < keyLength)
			readed += cryptedInputStream.read(encryptedKey, readed, keyLength - readed);
		SecretKey secretKey = decryptSecretKey(privateKey, encryptedKey);

		long contentLength = sourceFile.length() - keyLength;

		File outputFile = decryptFileContent(secretKey, cryptedInputStream, contentLength, sourceUri, progressListener);
		cryptedInputStream.close();

		return Uri.fromFile(outputFile);
	}

	private static SecretKey decryptSecretKey(PrivateKey privateKey, byte[] encryptedKey) throws IOException, InvalidKeyException {
		try {
			Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] plainSecret = cipher.doFinal(encryptedKey);

			return new SecretKeySpec(plainSecret, FileCryptoUtils.SECRET_KEY_ALG);
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e);
		} catch (NoSuchPaddingException e) {
			throw new Error(e);
		} catch (IllegalBlockSizeException e) {
			throw new Error(e);
		} catch (BadPaddingException e) {
			throw new Error(e);
		}
	}

	/**
	 * Decrypt the file content using simmetric encryption
	 * 
	 * @return
	 */
	private static File decryptFileContent(SecretKey secretKey, InputStream in, long contentLength, Uri sourceUri, ProgressListener progressListener) throws Exception {
		if (progressListener == null)
			progressListener = new ProgressListener() {

				@Override
				public void onProgressUpdate(Uri sourceFile, int progress) {
				}
			};

		byte[] iv = generateIV(secretKey);
		Cipher cipher = Cipher.getInstance(FileCryptoUtils.CONTENT_CYPHER_MODE);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

		File outputFile = File.createTempFile("OWC_decrypt", null);
		outputFile.deleteOnExit();
		OutputStream out = new FileOutputStream(outputFile);
		try {
			progressListener.onProgressUpdate(sourceUri, 0);
			int readed = 0, lastProgress = 0, tmp = 0;
			byte[] buffer = new byte[BUF_SIZE];
			while (tmp > -1) {
				tmp = in.read(buffer);
				if (tmp > -1) {
					byte[] block = cipher.update(buffer, 0, tmp);
					if (block != null)
						out.write(block);
				}
				readed += tmp;
				int progress = (int) Math.floor((readed / (double) contentLength) * 100);
				if (lastProgress < progress && progress < 100) {
					progressListener.onProgressUpdate(sourceUri, progress);
					lastProgress = progress;
				}
			}
			out.write(cipher.doFinal());
		} finally {
			out.close();
		}
		progressListener.onProgressUpdate(sourceUri, 100);
		return outputFile;
	}

	/**
	 * Listener for progress updates
	 */
	public interface ProgressListener {

		public void onProgressUpdate(Uri sourceFile, int progress);
	}
}