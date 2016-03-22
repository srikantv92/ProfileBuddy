package com.se.profilebuddy.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.se.profilebuddy.service.CalendarService;
import com.se.profilebuddy.service.LocationService;

/**
 * This class is intended to start the services associated with the application
 * after device boot.
 */
public class ServiceAutoStart extends BroadcastReceiver
{

	private static final String TAG = ServiceAutoStart.class.getName();

	// -- constants
	private static final int CALENDAR_SERV_INTERVAL = 1000 * 60;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i(TAG, "Started BootBroadCast!!");
		// -- Calendar service invocation.
		Intent profileIntent = new Intent(context, LocationService.class);
		context.startService(profileIntent);

		// -- Calendar service invocation. Uses an alarm manager to run the
		// service at regular intervals
		Intent calendarIntent = new Intent(context, CalendarService.class);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0, calendarIntent, 0);
		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				CALENDAR_SERV_INTERVAL, pendingIntent);
	}
}
