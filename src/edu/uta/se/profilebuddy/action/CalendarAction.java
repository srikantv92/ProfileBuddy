package edu.uta.se.profilebuddy.action;

import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Instances;
import android.util.Log;
import edu.uta.se.profilebuddy.database.ManageEventDB;
import edu.uta.se.profilebuddy.object.CalendarEvent;
import edu.uta.se.profilebuddy.object.EventProfile;

/**
 * Supporting class for Calendar Service. Provides methods to fetch, check and
 * manage event profiles.
 */
public class CalendarAction
{

	private static final String TAG = CalendarAction.class.getName();

	private static volatile CalendarAction instance;
	private static final long RANGE = 86400000; // -- 24 hrs

	public static final String[] INSTANCE_COLS = new String[] { Instances.BEGIN, Instances.END };

	// -- The indices for the projection array above.
	private static final int INSTANCE_START_IX = 0;
	private static final int INSTANCE_END_IX = 1;

	// -- instance variables
	private long now;
	private ManageEventDB eventDB;
	private List<CalendarEvent> localEvents;
	private ContentResolver contentResolver;

	private CalendarAction(Context context)
	{
		this.eventDB = new ManageEventDB(context);
		this.contentResolver = context.getContentResolver();
	}

	/**
	 * Singleton object implementation
	 * 
	 * @param context
	 *            - {@link Context}
	 * @return singleton instance of {@link CalendarAction}
	 */
	public static CalendarAction getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new CalendarAction(context);
		}
		instance.now = System.currentTimeMillis();
		return instance;
	}

	/**
	 * This method creates fetches all the defined events and calls helper
	 * methods - loadEvents, checkEventActive and changeProfile
	 */
	public EventProfile getNextProfile()
	{
		Log.i(TAG, "Entering getNextProfile");

		this.loadEvents();
		boolean eventAvailable = false;
		long eventStartTime = 0, nextEventTime = now + RANGE, endTime = 0, selectedEvent = 0;
		for (CalendarEvent event : this.localEvents)
		{
			if (event.isRecursive())
			{
				CalendarEvent recurciveEvent = getEventInstance(event.getForeignId());
				if (recurciveEvent != null)
				{
					eventStartTime = recurciveEvent.getStartTime();
					endTime = recurciveEvent.getEndTime();
				}
			}
			else
			{
				eventStartTime = event.getStartTime();
				endTime = event.getEndTime();
			}
			Log.i(TAG, "profile info - { eventStartTime:" + eventStartTime + ", endTime:" + endTime
					+ " }");
			if (eventStartTime > 0 && (eventStartTime < nextEventTime))
			{
				selectedEvent = event.getId();
				nextEventTime = eventStartTime;
				eventAvailable = true;
			}
			eventStartTime = 0;
		}
		EventProfile eventProfile = null;
		if (eventAvailable)
		{
			eventProfile = new EventProfile();
			eventProfile.setEventId(selectedEvent);
			eventProfile.setTimeToProfile(nextEventTime);
			eventProfile.setEndTime(endTime);
		}

		Log.i(TAG, "Exiting getNextProfile -  " + eventProfile);
		return eventProfile;
	}

	/**
	 * Fetches all active events in the database
	 */
	private void loadEvents()
	{
		Log.i(TAG, "Entering loadEvents");

		this.eventDB.open();
		this.localEvents = this.eventDB.fetchEvents(true);
		this.eventDB.close();

		Log.i(TAG, "Exiting loadEvents");
	}

	/**
	 * Gets a single instance of a given event from Google Calendar Provider.
	 * Used for recurring events.
	 * 
	 * @param foreignId
	 *            - The id of the event in Google calendar.
	 * @return - {@link CalendarEvent}
	 */
	private CalendarEvent getEventInstance(long foreignId)
	{
		Log.i(TAG, "Entering getEventInstance, params - { foreignId:" + foreignId + " }");

		CalendarEvent event = null;

		Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, now);
		ContentUris.appendId(builder, now + RANGE);

		String selection = Instances.EVENT_ID + " = ?";
		String[] selectionArgs = new String[] { String.valueOf(foreignId) };
		Cursor cursor = contentResolver.query(builder.build(), INSTANCE_COLS, selection,
				selectionArgs, null);
		Log.i(TAG, "Instance cursor -  " + cursor.getCount());
		if (cursor.moveToNext())
		{
			event = new CalendarEvent();
			event.setStartTime(cursor.getLong(INSTANCE_START_IX));
			event.setEndTime(cursor.getLong(INSTANCE_END_IX));
			Log.i(TAG, "Instance - { foreignId:" + foreignId + ", startDate:"
					+ event.getStartTime() + ", endDate:" + event.getEndTime() + " }");
		}

		Log.i(TAG, "Exiting getEventInstance");
		return event;
	}
}
