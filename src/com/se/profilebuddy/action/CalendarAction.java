package com.se.profilebuddy.action;

import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.CalendarContract.Instances;
import android.util.Log;

import com.se.profilebuddy.database.ManageEventDB;
import com.se.profilebuddy.object.CalendarEvent;

/**
 * Supporting class for Calendar Service. Provides methods to fetch, check and
 * manage event profiles.
 */
public class CalendarAction
{

	private static final String TAG = "com.se.profilebuddy.action.CalendarAction";

	private static volatile CalendarAction instance;
	private static final long RANGE = 86400000;

	public static final String[] INSTANCE_COLS = new String[] { Instances.BEGIN, Instances.END };

	// -- The indices for the projection array above.
	private static final int INSTANCE_START_IX = 0;
	private static final int INSTANCE_END_IX = 1;

	// -- instance variables
	private long now;
	private ManageEventDB eventDB;
	private List<CalendarEvent> localEvents;
	private AudioManager audioManager;
	private ContentResolver contentResolver;

	private CalendarAction(Context context)
	{
		this.eventDB = new ManageEventDB(context);
		this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
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
	public void manageProfile()
	{
		Log.i(TAG, "Entering manageProfile");

		this.loadEvents();
		boolean status = false;
		for (CalendarEvent event : this.localEvents)
		{
			status = this.checkEventActive(event);
			if (status)
			{
				this.changeProfile(event.getMode());
				break;
			}
		}

		Log.i(TAG, "Exiting manageProfile");
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
	 * Checks if a given event is active at the moment.
	 * 
	 * @param event
	 *            - {@link CalendarEvent}
	 * @return - status of the check
	 */
	private boolean checkEventActive(CalendarEvent event)
	{
		Log.i(TAG, "Entering checkEventActive, params - { eventId:" + event.getId()
				+ " , foreignId:" + event.getForeignId() + ", now:" + this.now + ", startTime:"
				+ event.getStartTime() + ", endTime:" + event.getEndTime() + " }");

		boolean active = false;
		if (event.isRecursive())
		{
			CalendarEvent eventInstance = this.getEventInstance(event.getForeignId());
			if (eventInstance != null && eventInstance.getStartTime() <= now
					&& eventInstance.getEndTime() > now)
			{
				active = true;
			}
		}
		else
		{
			if (event.getStartTime() <= now && event.getEndTime() > now)
			{
				active = true;
			}
		}

		Log.i(TAG, "Exiting checkEventActive");
		return active;
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

	/**
	 * This method calls the AudioManager to set defined modes.
	 * 
	 * @param mode
	 *            - Mode to be set
	 */
	private void changeProfile(int mode)
	{
		Log.i(TAG, "Entering changeProfile, params - { mode:" + mode + " }");

		switch (mode)
		{
		case 0:
			audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			break;
		case 1:
			audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			break;
		case 2:
			audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			break;
		default:

		}

		Log.i(TAG, "Exiting changeProfile");
	}
}
