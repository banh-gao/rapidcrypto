package net.sf.oneWayCrypto;

import java.io.File;
import java.io.IOException;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String ACTION_SHOW_ENCRYPTION = AppUtils.PACKAGE_NAME + ".action.SHOW_ENCRYPTION";
	public static final String ACTION_SHOW_DECRYPTION = AppUtils.PACKAGE_NAME + ".action.SHOW_DECRYPTION";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		IntentFilter filter = new IntentFilter(EncryptionService.ACTION_ENCRYPT_COMPLETED);
		filter.addDataScheme("file");
		registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Toast.makeText(context, intent.getDataString(), Toast.LENGTH_SHORT).show();
			}
		}, filter);

		try {
			Intent encrIntent = new Intent(this, EncryptionService.class);
			encrIntent.setAction(EncryptionService.ACTION_ENCRYPT);
			encrIntent.putExtra(EncryptionService.EXTRA_RAW_CONTACT_ID, 10);

			encrIntent.setData(Uri.fromFile(File.createTempFile("test1", "bla")));
			startService(encrIntent);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
