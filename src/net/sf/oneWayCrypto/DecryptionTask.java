package net.sf.oneWayCrypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import javax.crypto.SecretKey;
import android.os.AsyncTask;

class DecryptionTask extends AsyncTask<File, Integer, File> {

	private PrivateKey privateKey;

	int totalFiles = 1;

	private File currentFile;

	private DecryptionNotifier notifier = new DecryptionNotifier();

	private volatile boolean success = true;

	public DecryptionTask(String password) throws InvalidKeyException, IOException {
		privateKey = KeyProvider.getPrivateKey(password);
	}

	@Override
	protected void onPreExecute() {
		notifier.notifyStarted();
	}

	@Override
	protected File doInBackground(File... params) {
		publishProgress(0);

		currentFile = params[0];

		File lastDecrypted = null;
		try {
			lastDecrypted = doDecryption(currentFile);
		} catch (InvalidKeyException e) {
			success = false;
			return null;
		} catch (IOException e) {
			success = false;
			return null;
		}

		if (AppUtils.getPrefs().getBoolean("deleteEncrypted", false))
			currentFile.delete();

		publishProgress(1);

		return lastDecrypted;
	}

	public File doDecryption(File cryptedFile) throws InvalidKeyException, IOException {

		FileInputStream cryptedStream = new FileInputStream(cryptedFile);
		byte[] cryptedKey = new byte[KeyProvider.SYMMETRIC_KEYSIZE];
		cryptedStream.read(cryptedKey);

		String newFileName = cryptedFile.getName();

		File newFile = new File(cryptedFile.getParentFile(), newFileName);

		String name = newFileName.substring(0, newFileName.lastIndexOf('.'));
		String ext = newFileName.substring(newFileName.lastIndexOf('.'));
		int i = 1;
		while (newFile.exists()) {
			newFile = new File(newFile.getParentFile(), name + '_' + i + ext);
			i++;
		}

		newFile.createNewFile();

		FileOutputStream fos = new FileOutputStream(newFile);

		SecretKey secretKey = KeyProvider.recoverSecretKey(privateKey, cryptedKey);
		byte[] decryptedFile = KeyProvider.symmetricDecrypt(secretKey, cryptedStream);

		fos.write(decryptedFile);
		fos.close();
		cryptedStream.close();
		return newFile;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		notifier.notifyProgress(values[0]);
	}

	@Override
	protected void onCancelled() {
	}

	@Override
	protected void onPostExecute(File result) {
		if (success)
			notifier.notifyComplete(result, totalFiles);
		else
			notifier.notifyFailed(currentFile);
	}
}