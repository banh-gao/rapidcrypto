package net.sf.oneWayCrypto;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class PreferenceWindow extends PreferenceActivity implements
		OnPreferenceClickListener {

	private final int ALERT_DIALOG = 1;
	private final int PASSWORD_DIALOG = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		Preference customPref = (Preference) findPreference("regenerateKeys");
		customPref.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		showDialog(ALERT_DIALOG);
		return false;
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		if (id == ALERT_DIALOG)
			return getAlertDialog();
		else if (id == PASSWORD_DIALOG)
			return getPasswordDialog();
		else
			return super.onCreateDialog(id, args);
	}

	private Dialog getAlertDialog() {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle(R.string.prefs_regenerate_keys);
		b.setMessage(R.string.prefs_regenerate_keys_alert);
		b.setPositiveButton(R.string.prefs_regenerate,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						showDialog(PASSWORD_DIALOG);
						dialog.dismiss();
					}
				});
		b.setNeutralButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		return b.create();
	}

	private Dialog getPasswordDialog() {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		final View dialogView = getLayoutInflater().inflate(
				R.layout.new_keys_dialog, null);
		b.setView(dialogView);
		b.setTitle(R.string.prefs_regenerate_keys);

		b.setPositiveButton(R.string.prefs_regenerate, null);
		b.setNeutralButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});

		final AlertDialog d = b.create();

		d.show();
		d.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						EditText p = (EditText) dialogView
								.findViewById(R.id.password);
						EditText p_conf = (EditText) dialogView
								.findViewById(R.id.password_confirm);

						String password = p.getText().toString();

						if (password.length() < 8) {
							p.setError(getResources().getString(
									R.string.prefs_regenerate_password_error));
							return;
						}

						if (!password.equals(p_conf.getText().toString())) {
							p_conf.setError(getResources()
									.getString(
											R.string.prefs_regenerate_password_confirm_error));
							return;
						}

						d.dismiss();
						new GenerationTask(PreferenceWindow.this).execute(password);
					}
				});
		return d;
	}
}

class GenerationTask extends AsyncTask<String, Integer, Void> {
	ProgressDialog progress;
	Context c;
	public GenerationTask(Context c) {
		this.c = c;
		progress = ProgressDialog.show(c,
				c.getString(R.string.prefs_regenerate_keys),
				c.getString(R.string.prefs_generating), true);
	}
	
	@Override
	protected void onPreExecute() {
		progress.show();
	}

	@Override
	protected Void doInBackground(String... params) {
		KeyProvider.newKeyPair(params[0]);
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		progress.dismiss();
		Toast.makeText(c, R.string.prefs_regenerate_completed, Toast.LENGTH_LONG).show();
	}
}