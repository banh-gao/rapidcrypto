package net.sf.oneWayCrypto;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.view.LayoutInflater;

public class PasswordDialogBuilder {

	public static AlertDialog getDialog(DecryptionWindow context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(android.R.drawable.ic_lock_lock);

		builder.setMessage(R.string.unlock_info)
				.setTitle(R.string.unlock_title)
				.setView(
						LayoutInflater.from(context).inflate(
								R.layout.password_dialog, null))
				.setNeutralButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								System.exit(0);
							}
						}).setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						System.exit(0);
					}
				}).setPositiveButton(R.string.unlock, null);
		final AlertDialog d = builder.create();
		d.show();
		return d;
	}
}
