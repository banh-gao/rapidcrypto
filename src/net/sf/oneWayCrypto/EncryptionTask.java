package net.sf.oneWayCrypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.PublicKey;
import javax.crypto.SecretKey;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

class EncryptionTask extends AsyncTask<File, Integer, Void> {

	private static int NEXT_NOTIFICATION_ID = 1;

	private static final NotificationManager nm = (NotificationManager) AppUtils
			.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

	private Notification statusNotification;
	private static Notification completeNotification;

	static {
		completeNotification = new Notification(R.drawable.icon, AppUtils
				.getContext().getString(R.string.encryption_started),
				System.currentTimeMillis());
	}

	private int totalFiles;
	private int progress = 0;

	private final int notificationID;

	public EncryptionTask() {
		notificationID = NEXT_NOTIFICATION_ID++;
		statusNotification = new Notification(R.drawable.icon, AppUtils
				.getContext().getString(R.string.encryption_started),
				System.currentTimeMillis());

		statusNotification.flags = Notification.FLAG_ONGOING_EVENT;
	}

	@Override
	protected void onPreExecute() {
		
		Toast.makeText(AppUtils.getContext(), R.string.encryption_started , Toast.LENGTH_SHORT).show();
		
		statusNotification.setLatestEventInfo(AppUtils.getContext(), AppUtils
				.getContext().getString(R.string.encryption_started), AppUtils
				.getContext().getString(R.string.encryption_started),
				PendingIntent.getActivity(AppUtils.getContext(), 0, new Intent(
						AppUtils.getContext(), AlertDialog.class), 0));

		nm.notify(notificationID, statusNotification);
	}

	@Override
	protected Void doInBackground(File... params) {
		totalFiles = params.length;

		publishProgress(0);

		for (File f : params) {
			doEncryption(f, f.getParentFile());

			if (AppUtils.getPrefs().getBoolean("deletePlain", false))
				f.delete();

			publishProgress(progress + 1);
			System.out.println("Encrypted file " + progress + " of "
					+ totalFiles);
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		progress = values[0];
		statusNotification.setLatestEventInfo(AppUtils.getContext(), AppUtils
				.getContext().getString(R.string.encryption_started), AppUtils
				.getContext().getString(R.string.progress_status,progress),
				PendingIntent.getActivity(AppUtils.getContext(), 0, new Intent(
						AppUtils.getContext(), AlertDialog.class), 0));
		nm.notify(notificationID, statusNotification);
	}

	/**
	 * Encrypt the sourceFile to the destinationDir
	 */
	private void doEncryption(File sourceFile, File destinationDir) {

		try {
			FileInputStream plainFileStream = new FileInputStream(sourceFile);

			if (!destinationDir.exists()) {
				destinationDir.mkdir();
			}

			File destinationFile = new File(destinationDir,
					sourceFile.getName() + ".owc");
			System.out.println(destinationFile);
			destinationFile.createNewFile();

			FileOutputStream encryptedFileStream = new FileOutputStream(
					destinationFile);

			// Get keys for encryption
			PublicKey publicKey = KeyProvider.getPublicKey();
			SecretKey secretKey = KeyProvider.getRandomKey();
			// Write encrypted simmetric key
			encryptedFileStream.write(KeyProvider.cryptSecretKey(secretKey,
					publicKey));
			// Write encrypted data
			encryptedFileStream.write(KeyProvider.symmetricEncrypt(secretKey,
					plainFileStream));
			encryptedFileStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);

		// Remove the ongoing notification and send the complete notification
		nm.cancel(notificationID);

		Intent i = new Intent(AppUtils.getContext(), EncryptionTask.class)
				.setAction(Intent.ACTION_MAIN).setFlags(
						Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(
				AppUtils.getContext(), 0, i, 0);

		String status = AppUtils
				.getContext()
				.getResources()
				.getQuantityString(R.plurals.encryption_completed_details,
						totalFiles);

		status = String.format(status, totalFiles);

		completeNotification.setLatestEventInfo(AppUtils.getContext(), AppUtils
				.getContext().getString(R.string.encryption_completed), status,
				contentIntent);

		nm.notify(notificationID, completeNotification);
	}
}