package net.sf.oneWayCrypto.dirService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DirListener extends BroadcastReceiver {

	public static final String ACTION_REFRESH_DIRS = "net.sf.oneWayCrypto.dirService.REFRESH_DIRS";
	public static final String DIR_FILE = "watchedDirs";

	List<Timer> watchers = new ArrayList<Timer>();

	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (intent.getAction().equals(ACTION_REFRESH_DIRS) || intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.d("ONE_WAY_CRYPTO", watchers.toString());
			refreshDirs(context);
		} else if (intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED))
			unmountedMedia(intent.getExtras().getString("mData"));
		else if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED))
			mountedMedia(intent.getExtras().getString("mData"));
	}

	private void refreshDirs(Context context) {
		for (Timer t : watchers) {
			t.cancel();
		}
		watchers.clear();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(DIR_FILE)));

			for (String line = br.readLine(); line != null; line = br
					.readLine()) {

				File directory = new File(line);
				Timer t = new Timer();
				t.scheduleAtFixedRate(new FilesProcessor(directory), 0, 0);
				watchers.add(t);
			}

			br.close();
			Log.d("ONE_WAY_CRYPTO", watchers.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void mountedMedia(String path) {
		System.out.println("mounted: " + path);
	}

	private void unmountedMedia(String path) {
		System.out.println("Unmounted: " + path);
	}
}
