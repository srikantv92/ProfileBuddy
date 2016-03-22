package edu.uta.se.profilebuddy.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.uta.se.profilebuddy.database.ManageEventDB;
import edu.uta.se.profilebuddy.exception.NoDataException;
import edu.uta.se.profilebuddy.object.CalendarEvent;
import edu.uta.se.profilebuddy.util.ModeHelper;
import edu.uta.se.profilebuddy.util.ProfileUtil;

/**
 * IntentService invoked by {@link AlarmManager} at regular intervals to
 * activate a profile and exit from it.
 *
 */
public class EventService extends IntentService
{

	private static final String TAG = EventService.class.getName();

	// -- static
	public static volatile long ACTIVE_EVENT = 0;
	public static volatile boolean ACTIVE = false;

	public EventService()
	{
		super("IntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Log.i(TAG, "Profile service started");
		boolean newEvent = intent.getExtras().getBoolean(CalendarService.NEW_EVENT, false);
		if (newEvent)
		{
			long eventId = intent.getLongExtra(CalendarService.EVENT_ID, 0);
			long endTime = intent.getLongExtra(CalendarService.END_TIME, 0);
			turnOnProfile(eventId, endTime);
		}
		else
		{
			turnOffProfile();
		}
	}

	/**
	 * Finds the mode of the event to be set.
	 * 
	 * @param eventId
	 *            - The id of the event.
	 * @return - mode
	 * @throws NoDataException
	 */
	private int getEventMode(long eventId) throws NoDataException
	{
		Log.i(TAG, "Entering getEventMode");
		int mode;
		try
		{
			ManageEventDB eventDB = new ManageEventDB(this);
			eventDB.open();
			CalendarEvent event = eventDB.fetchEvent(eventId);
			eventDB.close();

			mode = event.getMode();
		}
		catch (NullPointerException npe)
		{
			throw new NoDataException("No data found for the event");
		}
		Log.i(TAG, "Exiting getEventMode - " + mode);
		return mode;
	}

	/**
	 * Activates the profile when called.
	 * 
	 * @param eventId
	 *            - The id of the event for which profile to be activated.
	 * @param endTime
	 *            - The endTime of the event.
	 */
	private void turnOnProfile(long eventId, long endTime)
	{
		Log.i(TAG, "Entering turnOnProfile, params - { eventId:" + eventId + ", endTime:" + endTime
				+ " }");
		try
		{
			int mode = getEventMode(eventId);
			ModeHelper helper = ModeHelper.getInstance(this);
			helper.changeProfile(mode);

			ACTIVE_EVENT = eventId;
			ACTIVE = true;

			Intent eventIntent = new Intent(this, EventService.class);
			PendingIntent pendingIntent = PendingIntent.getService(this, 0, eventIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			long triggerTime = endTime;
			AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
			alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);

			manageLocationService(false);
		}
		catch (NoDataException nde)
		{
			Log.e(TAG, nde.getMessage());
		}
		Log.i(TAG, "Exiting turnOnProfile");
	}

	/**
	 * Function to deactivate the event profile. Also invokes fallBack method to
	 * set the default mode for the application.
	 */
	private void turnOffProfile()
	{
		Log.i(TAG, "Entering turnOffProfile");

		ACTIVE_EVENT = 0;
		ACTIVE = false;

		Intent eventIntent = new Intent(this, EventService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, eventIntent,
				PendingIntent.FLAG_NO_CREATE);
		AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);

		Intent calendarIntent = new Intent(this, CalendarService.class);
		this.startService(calendarIntent);

		ModeHelper helper = ModeHelper.getInstance(this);
		helper.fallBackMode();
		manageLocationService(true);

		Log.i(TAG, "Exiting turnOffProfile");
	}

	/**
	 * Change state of the setting based on the status applied.
	 * 
	 * @param status
	 *            - status which says to switch off/on the service.
	 */
	private void manageLocationService(boolean toggle)
	{
		boolean serviceRunning = ProfileUtil.isServiceRunning(LocationService.class, this);
		if (toggle)
		{
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			boolean activeSetting = preferences.getBoolean("checkbox_location_service_preference",
					false);
			Log.i(TAG, "Location service setting active - " + activeSetting);
			if (!serviceRunning && activeSetting)
			{
				Intent service = new Intent(this, LocationService.class);
				this.startService(service);
			}
		}
		else
		{
			Intent service = new Intent(this, LocationService.class);
			this.stopService(service);
		}
	}
}
