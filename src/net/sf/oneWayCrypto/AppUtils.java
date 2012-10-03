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

	public static final String TAG = "ONE_WAY_CRYPTO";
	
	private static Context context;

	@Override
	public void onCreate() {
		context = getApplicationContext();
		super.onCreate();
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
}