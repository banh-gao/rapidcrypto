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

	public static String PACKAGE_NAME;

	public static final String PREFS_KEY_STRENGTH = "key_strength";

	private static Context context;

	private static ContactsCryptoManager contactsCryptoManager;

	@Override
	public void onCreate() {
		super.onCreate();
		PACKAGE_NAME = getPackageName();
		context = getApplicationContext();
		contactsCryptoManager = new ContactsCryptoManager(context);
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
}