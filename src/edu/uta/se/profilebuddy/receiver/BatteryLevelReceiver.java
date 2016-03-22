package edu.uta.se.profilebuddy.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.uta.se.profilebuddy.service.LocationService;
import edu.uta.se.profilebuddy.util.ProfileUtil;

/**
 * BroadCastReceiver registered to receive Battery related status. Used to
 * manage Green mode feature of the application.
 */
public class BatteryLevelReceiver extends BroadcastReceiver
{

	private static final String TAG = BroadcastReceiver.class.getName();

	// -- constants
	private static final String PREF_LOCATION_SERVICE = "checkbox_location_service_preference";
	private static final String PREF_GREEN = "checkbox_green_preference";
	private static final String GREEN_MODE = "is_green_mode_active";

	// -- instance variables
	private Context context;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i(TAG, "Recieved battery update - { "
				+ intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) + ", level:"
				+ intent.getAction());
		this.context = context;

		if (intent.getAction().equalsIgnoreCase(android.content.Intent.ACTION_BATTERY_LOW))
		{
			batteryLowAction();
		}
		else if (intent.getAction().equalsIgnoreCase(android.content.Intent.ACTION_BATTERY_OKAY))
		{
			batteryOkayAction();
		}
	}

	/**
	 * Performs actions required to start/re-start the suspended Services
	 * {@link LocationService} attached to Green mode.
	 */
	private void batteryOkayAction()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean serviceSetting = preferences.getBoolean(PREF_LOCATION_SERVICE, false);
		boolean serviceRunning = ProfileUtil.isServiceRunning(LocationService.class, context);
		if (serviceSetting && !serviceRunning)
		{
			Intent serviceIntent = new Intent(context, LocationService.class);
			this.context.startService(serviceIntent);
		}
		preferences.edit().putBoolean(GREEN_MODE, false).apply();
	}

	/**
	 * Performs actions required to suspend the Services {@link LocationService}
	 * attached to Green mode.
	 */
	private void batteryLowAction()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean serviceSetting = preferences.getBoolean(PREF_LOCATION_SERVICE, false);
		boolean serviceRunning = ProfileUtil.isServiceRunning(LocationService.class, context);
		if (serviceSetting && serviceRunning)
		{
			boolean greenSettings = preferences.getBoolean(PREF_GREEN, false);
			if (greenSettings)
			{
				Intent serviceIntent = new Intent(context, LocationService.class);
				this.context.stopService(serviceIntent);
			}
		}
		preferences.edit().putBoolean(GREEN_MODE, true).apply();
	}
}
