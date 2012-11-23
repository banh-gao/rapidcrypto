package net.sf.oneWayCrypto;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class DBHelper extends SQLiteOpenHelper {

	public static final String DB_NAME = "OWC_DB";
	public static final String TABLE_FILES = "FILES";
	public static final String COLUMN_URI = "uri";

	public DBHelper(Context context, int version) {
		super(context, DB_NAME, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Create database structure
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}

	public Cursor getMonitoredFiles() {
		SQLiteDatabase db = getWritableDatabase();
		return db.query(TABLE_FILES, new String[]{COLUMN_URI}, null, null, null, null, null);
	}

	public void addMonitoredPath(Uri path) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COLUMN_URI, path.toString());
		db.insert(TABLE_FILES, null, values);
	}

	public void deleteMonitoredFile(Uri file) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(TABLE_FILES, COLUMN_URI + "=?", new String[]{file.toString()});
	}
}
