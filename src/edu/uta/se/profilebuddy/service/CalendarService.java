package edu.uta.se.profilebuddy.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.uta.se.profilebuddy.action.CalendarAction;
import edu.uta.se.profilebuddy.action.CalendarHelper;
import edu.uta.se.profilebuddy.object.EventProfile;

/**
 * Custom Intent Service invoked to sync events and sets {@link AlarmManager} to
 * invoke {@link EventService}.
 */
public class CalendarService extends IntentService
{

	private static final String TAG = CalendarService.class.getName();

	private static final String PACKAGE = CalendarService.class.getCanonicalName();
	public static final String NEW_EVENT = PACKAGE + ".NEW_EVENT";
	public static final String EVENT_ID = PACKAGE + ".EVENT_ID";
	public static final String END_TIME = PACKAGE + ".END_TIME";

	public CalendarService()
	{
		super("CalendarService");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Log.i(TAG, "Calendar service started");
		this.start();
	}

	/**
	 * Initiates the sync feature.
	 */
	private void start()
	{
		CalendarHelper helper = CalendarHelper.getInstance(this);
		helper.syncCalendar();

		Log.i(TAG, "Is event profile active -  " + EventService.ACTIVE);
		if (!EventService.ACTIVE)
		{
			setAlarmForNextProfile();
		}
	}

	/**
	 * Finds the next event profile to be activated and sets the
	 * {@link AlarmManager} to invoke {@link EventService} and activate event.
	 */
	private void setAlarmForNextProfile()
	{
		Log.i(TAG, "Entering setAlarmForNextProfile");
		CalendarAction action = CalendarAction.getInstance(this);
		EventProfile eventProfile = action.getNextProfile();

		if (eventProfile != null && !EventService.ACTIVE)
		{
			Intent profileIntent = new Intent(this, EventService.class);
			profileIntent.putExtra(NEW_EVENT, true).putExtra(EVENT_ID, eventProfile.getEventId())
					.putExtra(END_TIME, eventProfile.getEndTime());
			PendingIntent pendingIntent = PendingIntent.getService(this, 0, profileIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			long triggerTime = eventProfile.getTimeToProfile();
			Log.i(TAG, "Next event set to trigger at - " + triggerTime);
			AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
			alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
		}
		Log.i(TAG, "Exiting setAlarmForNextProfile");
	}
}
