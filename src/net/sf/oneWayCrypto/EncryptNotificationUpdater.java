package net.sf.oneWayCrypto;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class EncryptNotificationUpdater {

	static final int STATUS_QUEUED = 1;
	static final int STATUS_RUNNING = 2;
	static final int STATUS_COMPLETED = 3;
	static final int STATUS_FAILED = 4;

	private final Context context;

	private final NotificationManager notifMgr;

	private final int notificationId;
	private final String title;
	private final String bigTitle;
	private final PendingIntent resultPendingIntent;
	private final Notification notification;

	private final Map<Uri, Integer> progresses = new HashMap<Uri, Integer>();
	private int maxProgress = 0;
	private int globalProgress = 0;
	private int completed = 0;

	public EncryptNotificationUpdater(Context context, boolean isEncryption) {
		this.context = context;
		Intent resultIntent = new Intent(context, MainActivity.class);
		if (isEncryption) {
			title = context.getResources().getString(R.string.notification_title_encryption);
			bigTitle = context.getResources().getString(R.string.notification_details_title_encryption);
			notificationId = 1;
			resultIntent.setAction(MainActivity.ACTION_SHOW_ENCRYPTION);
		} else {
			title = context.getResources().getString(R.string.notification_title_decryption);
			bigTitle = context.getResources().getString(R.string.notification_details_title_decryption);
			notificationId = 2;
			resultIntent.setAction(MainActivity.ACTION_SHOW_DECRYPTION);
		}

		resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		notifMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notification = buildNotification(context);
	}

	private Notification buildNotification(Context context) {
		Notification.Builder b = new Notification.Builder(context);
		b.setSmallIcon(android.R.drawable.ic_lock_lock);
		b.setContentIntent(resultPendingIntent);
		b.setContentTitle(title);

		b.setProgress(maxProgress, globalProgress, false);

		b.setContentText(context.getResources().getQuantityString(R.plurals.notification_text, completed, completed));

		if (globalProgress == maxProgress) {
			progresses.clear();
			b.setAutoCancel(true);
			return b.build();
		}

		Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
		inboxStyle.setBigContentTitle(bigTitle);
		int completed = 0;
		for (Entry<Uri, Integer> e : progresses.entrySet()) {
			inboxStyle.addLine(context.getResources().getString(R.string.notification_details_text, e.getKey().getLastPathSegment(), e.getValue()));
			if (e.getValue() == 100)
				completed++;
		}
		this.completed = completed;
		b.setStyle(inboxStyle);

		b.setOngoing(true);

		return b.build();
	}

	public synchronized void updateProgress(Uri sourceUri, int progress) {
		Integer oldProgress = progresses.put(sourceUri, progress);
		if (oldProgress == null)
			oldProgress = 0;

		maxProgress = progresses.size() * 100;
		globalProgress += progress - oldProgress;

		Notification newNot = buildNotification(context);
		notifMgr.notify(notificationId, newNot);
	}

	public void cancelNotification() {
		notifMgr.cancel(notificationId);
	}

	public int getNotificationId() {
		return notificationId;
	}

	public Notification getNotification() {
		return notification;
	}
}
