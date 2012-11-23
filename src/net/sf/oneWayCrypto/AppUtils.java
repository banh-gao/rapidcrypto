package net.sf.oneWayCrypto;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Class called when the application start, provide utils for the whole app
 * 
 */
public class AppUtils extends Application {

	private static final int DB_VERSION = 1;
	public static final String PREFS_KEY_STRENGTH = "key_strength";

	public static String PACKAGE_NAME;

	private static Context context;
	private static ContactsCryptoManager contactsCryptoManager;
	private static DBHelper dbHelper;

	@Override
	public void onCreate() {
		super.onCreate();
		PACKAGE_NAME = getPackageName();
		context = getApplicationContext();
		contactsCryptoManager = new ContactsCryptoManager(context);
		dbHelper = new DBHelper(context, DB_VERSION);
	}

	public static SharedPreferences getPrefs() {
		return PreferenceManager.getDefaultSharedPreferences(getContext());
	}

	/**
	 * Returns the application context
	 * 
	 * @return
	 */
	public static Context getContext() {
		return context;
	}

	public static ContactsCryptoManager getContactsCryptoManager() {
		return contactsCryptoManager;
	}

	public static DBHelper getDatabase() {
		return dbHelper;
	}
}