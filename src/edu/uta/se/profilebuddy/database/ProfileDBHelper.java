package edu.uta.se.profilebuddy.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * DBHelper to setup the application database and data objects on
 * initialization.
 */
public class ProfileDBHelper extends SQLiteOpenHelper
{

	private static final String TAG = ProfileDBHelper.class.getName();

	// -- Database params
	private static final String DATABASE_NAME = "PROFILE_BUDDY.DB";
	private static final int DATABASE_VERSION = 1;

	public ProfileDBHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// -- Database create SQL command for T_LOCATION table
	private static final String T_LOCATION_CREATE = "CREATE TABLE T_LOCATION "
			+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, TITLE TEXT NOT NULL, "
			+ "ADDRESS TEXT NOT NULL, LATITUDE REAL NOT NULL, LONGITUDE REAL NOT NULL, "
			+ "MODE INTEGER NOT NULL, RADIUS INTEGER NOT NULL);";

	// -- Database create SQL command for T_EVENT table
	private static final String T_EVENT_CREATE = "CREATE TABLE T_EVENT "
			+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, FOREIGN_EVENT_ID INTEGER NOT NULL UNIQUE, CALENDAR_ID INTEGER NOT NULL, "
			+ "TITLE TEXT NOT NULL, DESCRIPTION TEXT NULL, START_TIME INTEGER NOT NULL, END_TIME INTEGER NOT NULL, "
			+ "DURATION INTGER NOT NULL, MODE INTEGER NOT NULL, RECURSIVE INTEGER NOT NULL, STATUS INTEGER NOT NULL);";

	private static final String T_NOTIFICATION_CREATE = "CREATE TABLE T_NOTIFICATION "
			+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, EVENT_ID INTEGER NOT NULL, STATUS INTEGER NOT NULL, FOREIGN KEY (EVENT_ID) REFERENCES T_EVENT (_id));";

	private static final String INDEX_T_EVENT_FOREIGN_EVENT = "CREATE INDEX FORIEGN_EVENT_ID_INDEX ON T_EVENT (FOREIGN_EVENT_ID);";
	private static final String INDEX_T_EVENT_CALENDAR = "CREATE INDEX CALENDAR_ID_INDEX ON T_EVENT (CALENDAR_ID);";
	private static final String INDEX_T_NOTIFICATION_CREATE = "CREATE INDEX EVENT_ID_INDEX ON T_NOTIFICATION (EVENT_ID);";

	@Override
	public void onCreate(SQLiteDatabase database)
	{
		Log.i(TAG, "Creating databases and indexes");
		database.execSQL(T_LOCATION_CREATE);
		database.execSQL(T_EVENT_CREATE);
		database.execSQL(T_NOTIFICATION_CREATE);
		database.execSQL(INDEX_T_EVENT_FOREIGN_EVENT);
		database.execSQL(INDEX_T_EVENT_CALENDAR);
		database.execSQL(INDEX_T_NOTIFICATION_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
				+ ", which will destroy all old data");

		db.execSQL("DROP TABLE IF EXISTS " + T_LOCATION_CREATE);
		db.execSQL("DROP TABLE IF EXISTS " + T_EVENT_CREATE);
		db.execSQL("DROP TABLE IF EXISTS " + T_NOTIFICATION_CREATE);
		onCreate(db);
	}

}
