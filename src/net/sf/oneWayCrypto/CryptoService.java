package net.sf.oneWayCrypto;

import java.io.File;
import java.security.PublicKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.sf.oneWayCrypto.crypto.FileCryptoUtils;
import net.sf.oneWayCrypto.crypto.FileCryptoUtils.ProgressListener;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class CryptoService extends Service {

	public static final String ACTION_ENCRYPT = "net.sf.oneWayCrypto.action.ENCRYPT";
	public static final String ACTION_ENCRYPT_FAILED = "net.sf.oneWayCrypto.action.ENCRYPT_FAILED";
	public static final String ACTION_ENCRYPT_COMPLETED = "net.sf.oneWayCrypto.action.ENCRYPT_COMPLETED";

	public static final String EXTRA_RAW_CONTACT_ID = "net.sf.oneWayCrypto.extra.RAW_CONTACTS_ID";

	private EncryptNotificationUpdater notificator;

	int MAX_THREADS = 2;

	private final ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

	@Override
	public void onCreate() {
		super.onCreate();
		this.notificator = new EncryptNotificationUpdater(getApplicationContext(), true);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		executor.execute(new EncryptionTask(intent));

		return Service.START_REDELIVER_INTENT;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	class EncryptionTask implements Runnable {

		private final Uri data;
		private final Intent startIntent;

		public EncryptionTask(Intent intent) {
			this.data = intent.getData();
			this.startIntent = intent;
		}

		public void run() {
			if (data == null) {
				broadcastFailure();
				return;
			}

			try {
				notificator.updateProgress(data, 0);
				Uri encryptedData = encrypt();
				notificator.updateProgress(data, 100);
				broadcastCompletion(encryptedData);
			} catch (Exception e) {
				Log.e(AppUtils.PACKAGE_NAME, e.toString());
				broadcastFailure();
			}
		}

		private Uri encrypt() throws Exception {
			if (true)
				return Uri.fromFile(new File("testEncryptedFileUri"));

			int rawContactId = startIntent.getIntExtra(CryptoService.EXTRA_RAW_CONTACT_ID, 0);
			if (rawContactId == 0)
				throw new Exception("Invalid raw contact id " + rawContactId);

			File sourceData = new File(data.toString());

			ContactsCryptoManager contactsMgr = AppUtils.getContactsCryptoManager();

			PublicKey pubKey = contactsMgr.getPublicKey(rawContactId);

			Uri preparedFile = (sourceData.isDirectory()) ? prepareDirectory(sourceData) : data;

			ProgressListener l = new ProgressListener() {

				@Override
				public void onProgressUpdate(Uri sourceFile, int progress) {
					notificator.updateProgress(sourceFile, progress);
				}
			};

			return FileCryptoUtils.encryptFile(preparedFile, pubKey, l);
		}

		private Uri prepareDirectory(File sourceDirectory) {
			return null;
		}

		private void broadcastFailure() {
			Intent outcome = new Intent(CryptoService.ACTION_ENCRYPT_FAILED);
			outcome.setData(data);
			getApplicationContext().sendBroadcast(outcome);
		}

		private void broadcastCompletion(Uri encryptedData) {
			Intent outcome = new Intent(CryptoService.ACTION_ENCRYPT_COMPLETED);
			outcome.setData(encryptedData);
			getApplicationContext().sendBroadcast(outcome);
		}
	}
}
