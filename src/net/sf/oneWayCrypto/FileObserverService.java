package net.sf.oneWayCrypto;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.FileObserver;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

/**
 * Monitor for file opening requests
 */
public class FileObserverService extends Service {

	private final Map<String, FileObserver> observers = new HashMap<String, FileObserver>();
	private Cursor cur;

	/**
	 * Start service at boot
	 */
	public static class CryptedObserverStarter extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Intent service = new Intent(context, FileObserverService.class);
			context.startService(service);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		updateObservers();
		return START_STICKY;
	}

	private synchronized void updateObservers() {
		updateCursor();
		while (cur.moveToNext()) {
			String filePath = cur.getString(cur.getColumnIndexOrThrow(DBHelper.COLUMN_URI));
			observers.put(filePath, new CryptedObserver(this, filePath));
		}
	}

	private void updateCursor() {
		DBHelper db = AppUtils.getDatabase();
		cur = db.getMonitoredFiles();
		cur.registerContentObserver(new ContentObserver(null) {

			@Override
			public void onChange(boolean selfChange) {
				updateObservers();
			}
		});
	}

	static class CryptedObserver extends FileObserver {

		private static int nextNotificationID = 1;
		private final Context context;
		private final int notId;

		public CryptedObserver(Context context, String path) {
			super(path);
			this.context = context;
			notId = nextNotificationID++;
		}

		@Override
		public void onEvent(int event, String path) {
			Uri file = Uri.fromFile(new File(path));
			if (event == FileObserver.OPEN) {
				Intent i = new Intent(context, DecryptActivity.class);
				i.setData(file);
				sendDecryptNotification(i);
			} else if (event == FileObserver.DELETE_SELF) {
				DBHelper db = AppUtils.getDatabase();
				db.deleteMonitoredFile(file);
			}
		}

		private void sendDecryptNotification(Intent intent) {
			NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			NotificationCompat.Builder b = new NotificationCompat.Builder(context);
			b.setSmallIcon(android.R.drawable.ic_lock_lock);
			b.setContentTitle(context.getResources().getString(R.string.notification_decryption_requested));
			b.setContentText(intent.getData().getLastPathSegment());
			b.setContentIntent(PendingIntent.getActivity(context, 0, intent, 0));
			mgr.notify(notId, b.build());
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
