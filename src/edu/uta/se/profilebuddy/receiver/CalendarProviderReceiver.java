package edu.uta.se.profilebuddy.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.uta.se.profilebuddy.service.CalendarService;

/**
 * Receives broadcasts from Calendar Provider on change in Calendar events.
 */
public class CalendarProviderReceiver extends BroadcastReceiver
{

	private static final String TAG = CalendarProviderReceiver.class.getName();

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i(TAG, "Calendar provider change detected.");
		Intent calendarIntent = new Intent(context, CalendarService.class);
		context.startService(calendarIntent);
	}
}
