package edu.uta.se.profilebuddy.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.uta.se.profilebuddy.listener.LocalPhoneListener;
import edu.uta.se.profilebuddy.service.CalendarService;
import edu.uta.se.profilebuddy.service.LocationService;

/**
 * This class is intended to start the services associated with the application
 * after device boot as well as at the start of the application.
 */
public class ServiceAutoStart extends BroadcastReceiver
{

	private static final String TAG = ServiceAutoStart.class.getName();

	// -- constants
	private static final int CALENDAR_SERV_INTERVAL = 86400000;

	// -- instances
	private Context context;
	private SharedPreferences preferences;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i(TAG, "ServiceAutoStart invoked");
		this.context = context;
		this.preferences = PreferenceManager.getDefaultSharedPreferences(context);

		// -- services
		activateLocationService();
		activateCalendarService();
		activateDrivingMode();

		Log.i(TAG, "End of ServiceAutoStart");
	}

	/**
	 * Utility method to activate {@link LocationService} at startup of the
	 * Application.
	 */
	private void activateLocationService()
	{
		// -- Location service invocation.
		boolean activeSetting = preferences.getBoolean("checkbox_location_service_preference",
				false);
		Log.i(TAG, "Location service setting active - " + activeSetting);
		if (activeSetting)
		{
			Intent profileIntent = new Intent(context, LocationService.class);
			context.startService(profileIntent);
		}
	}

	/**
	 * Utility method to activate {@link CalendarService} at startup of the
	 * Application.
	 */
	private void activateCalendarService()
	{
		// -- Calendar service invocation. Uses an alarm manager to run the
		// service at regular intervals
		Intent calendarIntent = new Intent(context, CalendarService.class);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0, calendarIntent, 0);
		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				CALENDAR_SERV_INTERVAL, pendingIntent);
	}

	/**
	 * Utility method to initialize and register {@link LocalPhoneListener},
	 * needed for Driving mode at startup of the Application.
	 */
	private void activateDrivingMode()
	{
		boolean activeSetting = preferences.getBoolean("checkbox_driving_preference", false);
		Log.i(TAG, "Driving mode setting active - " + activeSetting);
		if (activeSetting)
		{
			LocalPhoneListener localCallListener = LocalPhoneListener.getInstance(context);
			localCallListener.register();
		}
	}
}
