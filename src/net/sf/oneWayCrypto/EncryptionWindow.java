package net.sf.oneWayCrypto;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class EncryptionWindow extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!getIntent().getAction().equals(Intent.ACTION_SEND))
			return;

		storeFile(getIntent());
		finish();
	}

	private void storeFile(Intent intent) {
		try {
			Uri uri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
			File f = new File(new URI(uri.toString()));
			EncryptionTask t = new EncryptionTask();
			t.execute(f);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
