package edu.uta.se.profilebuddy.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import edu.uta.se.profilebuddy.object.CalendarEvent;

/**
 * Database object for the Event based profiles feature of the application.
 * Provides CRUD methods required for managing Event objects.
 */
public class ManageEventDB
{

	private static final String TAG = ManageEventDB.class.getSimpleName();

	// -- Database objects
	private SQLiteDatabase database;
	private ProfileDBHelper profileDBHelper;

	// -- Event table columns
	private static final String TABLE = "T_EVENT";
	private static final String EVENT_ID = "_id";
	private static final String FOREIGN_EVENT_ID = "FOREIGN_EVENT_ID";
	private static final String CALENDAR_ID = "CALENDAR_ID";
	private static final String DESCRIPTION = "DESCRIPTION";
	private static final String TITLE = "TITLE";
	private static final String START_TIME = "START_TIME";
	private static final String END_TIME = "END_TIME";
	private static final String DURATION = "DURATION";
	private static final String MODE = "MODE";
	private static final String RECURSIVE = "RECURSIVE";
	private static final String STATUS = "STATUS";

	private static final String[] EVENT_PROJECTION = { EVENT_ID, FOREIGN_EVENT_ID, CALENDAR_ID,
			TITLE, DESCRIPTION, START_TIME, END_TIME, DURATION, MODE, RECURSIVE, STATUS };

	public ManageEventDB(Context context)
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
	 * Method to create a new event in the database.
	 * 
	 * @param event
	 *            - {@link CalendarEvent}
	 */
	public void createEvent(CalendarEvent event)
	{
		Log.i(TAG, "Entering createEvent, params - " + event);

		ContentValues values = new ContentValues();
		values.put(FOREIGN_EVENT_ID, event.getForeignId());
		values.put(CALENDAR_ID, event.getCalendarId());
		values.put(TITLE, event.getTitle());
		values.put(DESCRIPTION, event.getDescription());
		values.put(START_TIME, event.getStartTime());
		values.put(END_TIME, event.getEndTime());
		values.put(DURATION, event.getDuration());
		values.put(STATUS, 1);
		values.put(RECURSIVE, event.isRecursive());
		values.put(MODE, -1);
		long insertId = database.insert(TABLE, null, values);
		// -- Setting the new id onto the object
		event.setId(insertId);

		Log.i(TAG, "Exiting createEvent, id after row insertion - " + insertId);
	}

	/**
	 * DB method to update event details for given event id.
	 * 
	 * @param event
	 *            - {@link CalendarEvent}
	 */
	public void updateEvent(CalendarEvent event)
	{
		Log.i(TAG, "Entering updateEvent(), params - " + event);

		ContentValues values = new ContentValues();
		values.put(TITLE, event.getTitle());
		values.put(DESCRIPTION, event.getDescription());
		values.put(START_TIME, event.getStartTime());
		values.put(END_TIME, event.getEndTime());
		values.put(DURATION, event.getDuration());
		values.put(MODE, event.getMode());
		values.put(STATUS, event.getStatus());
		values.put(RECURSIVE, event.isRecursive() ? 1 : 0);

		String selection = EVENT_ID + " = ? ";
		String selectionArgs[] = new String[] { String.valueOf(event.getId()) };
		int rows = database.update(TABLE, values, selection, selectionArgs);

		Log.i(TAG, "Exiting updateEvent(), rows updated - " + rows);
	}

	/**
	 * DB method to update profile of a given event id.
	 * 
	 * @param eventId
	 *            - The id of the event for which profile needs to be updated.
	 * @param mode
	 *            - The mode to be set.
	 */
	public void updateMode(long eventId, int mode)
	{
		Log.i(TAG, "Entering updateMode(), params - { eventId:" + eventId + ", mode:" + mode + " }");

		ContentValues values = new ContentValues();
		values.put(MODE, mode);
		String selection = EVENT_ID + " = ? ";
		String selectionArgs[] = new String[] { String.valueOf(eventId) };
		int rows = database.update(TABLE, values, selection, selectionArgs);

		Log.i(TAG, "Exiting updateMode(), rows updated - " + rows);
	}

	/**
	 * Method to return all stored event profiles in the DB
	 * 
	 * @return List<{@link CalendarEvent}>
	 */
	public List<CalendarEvent> fetchEvents(boolean active)
	{
		Log.i(TAG, "Entering  fetchEvents");

		List<CalendarEvent> events;
		String selection = STATUS + " = ? ";
		String selectionArgs[] = new String[] { "1" };
		Cursor cursor = database.query(TABLE, EVENT_PROJECTION, selection, selectionArgs, null,
				null, null);

		events = new ArrayList<CalendarEvent>();
		CalendarEvent event = null;
		while (cursor.moveToNext())
		{
			event = new CalendarEvent();
			event.setId(cursor.getLong(0));
			event.setForeignId(cursor.getLong(1));
			event.setCalendarId(cursor.getInt(2));
			event.setTitle(cursor.getString(3));
			event.setDescription(cursor.getString(4));
			event.setStartTime(cursor.getLong(5));
			event.setEndTime(cursor.getLong(6));
			event.setDuration(cursor.getLong(7));
			event.setMode(cursor.getInt(8));
			event.setRecursive(cursor.getInt(9) == 1 ? true : false);
			event.setStatus(cursor.getInt(10));

			events.add(event);
		}
		cursor.close();

		Log.i(TAG, "Exiting fetchEvents, rows fetched - " + events.size());
		return events;
	}

	/**
	 * Method to return all stored event profiles in the DB
	 * 
	 * @return List<{@link CalendarEvent}>
	 */
	public List<CalendarEvent> fetchEvents(boolean active, long calendarId)
	{
		Log.i(TAG, "Entering  fetchEvents");

		List<CalendarEvent> events;
		String selection = STATUS + " = ? AND " + CALENDAR_ID + " = ? ";
		String selectionArgs[] = new String[] { "1", String.valueOf(calendarId) };
		Cursor cursor = database.query(TABLE, EVENT_PROJECTION, selection, selectionArgs, null,
				null, null);

		events = new ArrayList<CalendarEvent>();
		CalendarEvent event = null;
		while (cursor.moveToNext())
		{
			event = new CalendarEvent();
			event.setId(cursor.getLong(0));
			event.setForeignId(cursor.getLong(1));
			event.setCalendarId(calendarId);
			event.setTitle(cursor.getString(3));
			event.setDescription(cursor.getString(4));
			event.setStartTime(cursor.getLong(5));
			event.setEndTime(cursor.getLong(6));
			event.setDuration(cursor.getLong(7));
			event.setMode(cursor.getInt(8));
			event.setRecursive(cursor.getInt(9) == 1 ? true : false);
			event.setStatus(cursor.getInt(10));

			events.add(event);
		}
		cursor.close();

		Log.i(TAG, "Exiting fetchEvents, rows fetched - " + events.size());
		return events;
	}

	/**
	 * Method to get details of a given eventId.
	 * 
	 * @param eventId
	 *            - The id for which the details needs to be retrieved.
	 * @return - The {@link CalendarEvent}
	 */
	public CalendarEvent fetchEvent(long eventId)
	{
		Log.i(TAG, "Entering  fetchEvent, params - { eventId:" + eventId + " }");

		CalendarEvent event = null;
		String selection = EVENT_ID + " = ? ";
		String selectionArgs[] = new String[] { String.valueOf(eventId) };
		Cursor cursor = database.query(TABLE, EVENT_PROJECTION, selection, selectionArgs, null,
				null, null);
		Log.d(TAG, "Cursor count - " + cursor.getCount());

		if (cursor.moveToNext())
		{
			event = new CalendarEvent();
			event.setId(cursor.getLong(0));
			event.setForeignId(cursor.getLong(1));
			event.setCalendarId(cursor.getInt(2));
			event.setTitle(cursor.getString(3));
			event.setDescription(cursor.getString(4));
			event.setStartTime(cursor.getLong(5));
			event.setEndTime(cursor.getLong(6));
			event.setDuration(cursor.getLong(7));
			event.setMode(cursor.getInt(8));
			event.setRecursive(cursor.getInt(9) == 1 ? true : false);
			event.setStatus(cursor.getInt(10));
		}
		cursor.close();

		Log.i(TAG, "Exiting fetchEvent, rows fetched - " + event);
		return event;
	}

}
