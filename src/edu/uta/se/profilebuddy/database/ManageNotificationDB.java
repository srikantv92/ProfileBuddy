package edu.uta.se.profilebuddy.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import edu.uta.se.profilebuddy.object.Notification;

/**
 * Database object for the associated Notification feature of the application.
 * Provides CRUD methods required for managing Notification objects.
 */
public class ManageNotificationDB
{

	private static final String TAG = ManageNotificationDB.class.getSimpleName();

	// -- Database objects
	private SQLiteDatabase database;
	private ProfileDBHelper profileDBHelper;

	// -- Event table columns
	private static final String TABLE_LOCATION = "T_NOTIFICATION";
	private static final String NOTIFICATION_ID = "_id";
	private static final String EVENT_ID = "EVENT_ID";
	private static final String STATUS = "STATUS";

	public ManageNotificationDB(Context context)
	{
		profileDBHelper = new ProfileDBHelper(context);
	}

	public void open() throws SQLException
	{
		database = profileDBHelper.getWritableDatabase();
	}

	public void close()
	{
		database.close();
	}

	/**
	 * Method to create a new location Notification - Event mapping in the
	 * database.
	 * 
	 * @param notification
	 *            - {@link Notification}
	 */
	public long createNotification(long eventId)
	{
		Log.i(TAG, "Entering createNotification, params - { eventId:" + eventId + " }");

		ContentValues values = new ContentValues();
		values.put(EVENT_ID, eventId);
		values.put(STATUS, 1);
		long notificationId = database.insert(TABLE_LOCATION, null, values);

		Log.i(TAG, "Exiting createNotification, id after row insertion - " + notificationId);
		return notificationId;
	}

	/**
	 * DB method to update Notification associated with an event.
	 * 
	 * @param notification
	 *            - {@link Notification}
	 */
	public void updateNotification(long notificationId)
	{
		Log.i(TAG, "Entering updateNotification(), params - { " + notificationId + " }");

		ContentValues values = new ContentValues();
		values.put(STATUS, 0);

		String selection = NOTIFICATION_ID + " = ? ";
		String selectionArgs[] = new String[] { String.valueOf(notificationId) };
		int rows = database.update(TABLE_LOCATION, values, selection, selectionArgs);

		Log.i(TAG, "Exiting updateEvent(), rows updated - " + rows);
	}

}
