package edu.uta.se.profilebuddy.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;
import android.util.Log;
import edu.uta.se.profilebuddy.R;
import edu.uta.se.profilebuddy.listener.LocalPhoneListener;
import edu.uta.se.profilebuddy.service.LocationService;
import edu.uta.se.profilebuddy.util.ModeHelper;
import edu.uta.se.profilebuddy.util.ProfileUtil;

/**
 * Child of MainActivity. Displays application settings. Uses Android Preference
 * interface for the implementation.
 */
public class SettingFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
{

	private static final String TAG = SettingFragment.class.getName();

	// -- constants
	private static final String GREEN_MODE = "is_green_mode_active";

	// -- instance
	private Context context;
	private ModeHelper helper;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.context = getActivity();
		this.helper = ModeHelper.getInstance(context);
		// -- Load the preferences from an XML resource
		addPreferencesFromResource(R.layout.fragment_setting);
		// -- listener for onChange in preferences
		PreferenceManager.getDefaultSharedPreferences(getActivity())
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		Log.i(TAG, "Entering onSharedPreferenceChanged - " + key);

		if ("checkbox_location_service_preference".equalsIgnoreCase(key))
		{
			manageLocationService(sharedPreferences.getBoolean(key, false));
		}
		else if ("checkbox_driving_preference".equalsIgnoreCase(key))
		{
			manageDrivingMode(sharedPreferences.getBoolean(key, false));
		}
		else if ("checkbox_green_preference".equalsIgnoreCase(key))
		{
			manageGreenMode(sharedPreferences.getBoolean(key, false));
		}

		Log.i(TAG, "Exiting  onSharedPreferenceChanged");
	}

	/**
	 * Change state of the setting based on the status applied.
	 * 
	 * @param status
	 *            - status which says to switch off/on the mode.
	 */
	private void manageGreenMode(boolean status)
	{
		if (!status)
		{
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			preferences.edit().putBoolean(GREEN_MODE, false).apply();
			boolean serviceRunning = ProfileUtil.isServiceRunning(LocationService.class, context);
			if (!serviceRunning)
			{
				Intent service = new Intent(context, LocationService.class);
				context.startService(service);
			}
		}
	}

	/**
	 * Change state of the setting based on the status applied.
	 * 
	 * @param status
	 *            - status which says to switch off/on the mode.
	 */
	private void manageDrivingMode(boolean status)
	{
		if (status)
		{
			LocalPhoneListener instance = LocalPhoneListener.getInstance(context);
			instance.register();
		}
		else
		{
			LocalPhoneListener instance = LocalPhoneListener.getInstance();
			if (instance != null)
			{
				instance.unregister();
			}
		}
	}

	/**
	 * Change state of the setting based on the status applied.
	 * 
	 * @param status
	 *            - status which says to switch off/on the service.
	 */
	private void manageLocationService(boolean status)
	{
		boolean serviceRunning = ProfileUtil.isServiceRunning(LocationService.class, context);
		if (status)
		{
			if (!serviceRunning)
			{
				Intent service = new Intent(context, LocationService.class);
				context.startService(service);
			}
		}
		else
		{
			helper.fallBackMode();
			Intent service = new Intent(context, LocationService.class);
			context.stopService(service);
		}
	}
}
