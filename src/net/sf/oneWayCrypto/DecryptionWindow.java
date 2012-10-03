package net.sf.oneWayCrypto;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class DecryptionWindow extends Activity implements View.OnClickListener {

	private static final int PASSWORD_DIALOG = 1;

	private AlertDialog passwordDialog;

	private File lastFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		passwordDialog = PasswordDialogBuilder.getDialog(this);
		passwordDialog.getButton(DialogInterface.BUTTON_POSITIVE)
				.setOnClickListener(this);

		showDialog(PASSWORD_DIALOG);
	}

	@Override
	protected void onResume() {
		super.onResume();
		showDialog(PASSWORD_DIALOG);
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		if (id == PASSWORD_DIALOG)
			return passwordDialog;
		return super.onCreateDialog(id, args);
	}

	@Override
	public void onClick(View v) {
		String password = tryUnlock();

		if (password == null)
			return;

		passwordDialog.dismiss();
		try {
			startDecryption(password);
		} catch (InvalidKeyException e) {
			Toast.makeText(this, getString(R.string.unlock_error),
					Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Toast.makeText(this, getString(R.string.unlock_file_error),
					Toast.LENGTH_LONG).show();
		}
		finish();
	}

	public void startDecryption(String password) throws InvalidKeyException,
			IOException {
		try {
			Uri uri = getIntent().getData();
			lastFile = new File(new URI(uri.toString()));
			DecryptionTask t = new DecryptionTask(password);
			t.execute(lastFile);
		} catch (URISyntaxException e) {
			throw new Error(e);
		}
	}

	private String tryUnlock() {
		TextView password = (TextView) passwordDialog
				.findViewById(R.id.unlockPassword);

		try {
			KeyProvider.getPrivateKey(password.getText().toString());
			return password.getText().toString();
		} catch (InvalidKeyException e) {
			password.setError(getString(R.string.unlock_error));
		} catch (IOException e) {
			password.setError(getString(R.string.unlock_file_error));
		}
		return null;
	}
}