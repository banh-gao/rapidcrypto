package net.sf.oneWayCrypto;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import net.sf.oneWayCrypto.crypto.PublicKeyUtils;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;

public class ContactsCryptoManager {

	private final Context context;

	private static final int PROFILE_RAW_ID = -1;

	// TODO: set correct mime types
	private static final String PUB_KEYs_MIME_TYPE = "";
	private static final String PRIV_KEY_MIME_TYPE = "";
	private static final String DATA = Data.DATA15;

	public ContactsCryptoManager(Context context) {
		this.context = context;
	}

	public void setPublicKey(int rawContactId, PublicKey pubKey) throws Exception {
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

		ContentProviderOperation.Builder b = ContentProviderOperation.newInsert(Data.CONTENT_URI);
		b.withValue(Data.RAW_CONTACT_ID, rawContactId);
		b.withValue(Data.MIMETYPE, PUB_KEYs_MIME_TYPE);
		b.withValue(DATA, pubKey.getEncoded());

		context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
	}

	public void setProfilePublicKey(PublicKey pubKey) throws Exception {
		setPublicKey(PROFILE_RAW_ID, pubKey);
	}

	public PublicKey getPublicKey(int rawContactId) throws Exception {
		Cursor c = context.getContentResolver().query(Data.CONTENT_URI, new String[]{DATA}, Data.RAW_CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='" + PUB_KEYs_MIME_TYPE + "'", new String[]{String.valueOf(rawContactId)}, null);
		// TODO: Use only the first certificate
		c.moveToNext();
		byte[] encodedKey = c.getBlob(c.getColumnIndexOrThrow(DATA));
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
		PublicKey pubKey = KeyFactory.getInstance("rsa").generatePublic(keySpec);
		return pubKey;
	}

	public void setProfilePrivateKey(PrivateKey privateKey, String password) throws Exception {
		setPrivateKey(PROFILE_RAW_ID, privateKey, password);
	}

	public void setPrivateKey(int rawContactId, PrivateKey privateKey, String password) throws Exception {
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

		ContentProviderOperation.Builder b = ContentProviderOperation.newInsert(Data.CONTENT_URI);
		b.withValue(Data.RAW_CONTACT_ID, rawContactId);
		b.withValue(Data.MIMETYPE, PRIV_KEY_MIME_TYPE);
		byte[] cryptedPrivate = PublicKeyUtils.encryptPrivateKey(privateKey, password);
		b.withValue(DATA, cryptedPrivate);

		context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
	}

	public PrivateKey getPrivateKey(int rawContactId, String password) throws Exception {
		Cursor c = context.getContentResolver().query(Data.CONTENT_URI, new String[]{DATA}, Data.RAW_CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='" + PRIV_KEY_MIME_TYPE + "'", new String[]{String.valueOf(rawContactId)}, null);
		// TODO: Use only the first private key
		c.moveToNext();
		return PublicKeyUtils.decryptPrivateKey(c.getBlob(c.getColumnIndexOrThrow(DATA)), password);
	}

	public PrivateKey getProfilePrivateKey(String password) throws Exception {
		return getPrivateKey(PROFILE_RAW_ID, password);
	}

	public PublicKey getProfilePublicKey() throws Exception {
		return getPublicKey(PROFILE_RAW_ID);
	}
}
