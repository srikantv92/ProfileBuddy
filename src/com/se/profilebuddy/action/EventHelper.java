package com.se.profilebuddy.action;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Events;
import android.util.Log;

import com.se.profilebuddy.database.ManageEventDB;
import com.se.profilebuddy.object.CalendarEvent;
import com.se.profilebuddy.object.EventInfo;

/**
 * Helper Class to get event details.
 *
 */
public class EventHelper
{

	private static final String TAG = "com.se.profilebuddy.action.EventHelper";

	private static final String[] EVENT_COLS = new String[] { Events.TITLE, Events.DESCRIPTION,
			Events.DTSTART, Events.DTEND, Events.DURATION, Events.RRULE };

	private static final int EVENT_TITLE_IX = 0;
	private static final int EVENT_DESCRIPTION_IX = 1;
	private static final int EVENT_DTSTART_IX = 2;
	private static final int EVENT_DTEND_IX = 3;
	private static final int EVENT_DURATION_IX = 4;
	private static final int EVENT_RRULE_IX = 5;

	private static EventHelper instance;
	private ContentResolver contentResolver;
	private ManageEventDB eventDB;

	private EventHelper(Context context)
	{
		contentResolver = context.getContentResolver();
		this.eventDB = new ManageEventDB(context);
	}

	/**
	 * Singleton object implementation
	 * 
	 * @param context
	 *            - {@link Context}
	 * @return singleton instance of {@link EventHelper}
	 */
	public static EventHelper getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new EventHelper(context);
		}
		return instance;
	}

	/**
	 * Gets details of an event from local database as well as from
	 * GoogleProvider.Instances.
	 * 
	 * @param eventId
	 *            - The id of the event for which information needs to be
	 *            retrieved.
	 * @return - The {@link EventInfo}
	 */
	public EventInfo getEventDetails(long eventId)
	{
		EventInfo eventInfo = null;

		eventDB.open();
		CalendarEvent event = eventDB.fetchEvent(eventId);
		eventDB.close();

		Uri uri = Events.CONTENT_URI;
		String selection = Events._ID + " = ? ";
		String selectionArgs[] = new String[] { String.valueOf(event.getForeignId()) };
		Cursor cursor = contentResolver.query(uri, EVENT_COLS, selection, selectionArgs, null);

		if (cursor.moveToNext())
		{
			eventInfo = new EventInfo();
			eventInfo.setTitle(cursor.getString(EVENT_TITLE_IX));
			eventInfo.setDescription(cursor.getString(EVENT_DESCRIPTION_IX));
			eventInfo.setStartTime(cursor.getLong(EVENT_DTSTART_IX));
			eventInfo
					.setEndTime(cursor.isNull(EVENT_DTEND_IX) ? 0 : cursor.getLong(EVENT_DTEND_IX));
			eventInfo.setDuration(cursor.isNull(EVENT_DURATION_IX) ? 0 : cursor
					.getLong(EVENT_DURATION_IX));
			eventInfo.setRecursive(cursor.isNull(EVENT_RRULE_IX) ? false : true);

			Log.d(TAG, eventInfo.toString());
		}
		cursor.close();

		return eventInfo;
	}

}
