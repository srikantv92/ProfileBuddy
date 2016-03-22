package edu.uta.se.profilebuddy.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.BatteryManager;
import edu.uta.se.profilebuddy.R;

/**
 * Class contains common utility functions
 */
public class ProfileUtil
{

	/**
	 * Utility method to display an alert box.
	 * 
	 * @param activity
	 *            - {@link Activity}
	 * @param titleCode
	 *            - The title for the alert box.
	 * @param messageCode
	 *            - The message to be displayed.
	 */
	public static void showAlert(Activity activity, int titleCode, int messageCode)
	{
		Resources resources = activity.getResources();
		String title = resources.getString(titleCode);
		String message = resources.getString(messageCode);

		AlertDialog.Builder alertbuilder = new AlertDialog.Builder(activity);
		alertbuilder.setTitle(title).setMessage(message).setIcon(R.drawable.ic_action_error);
		alertbuilder.setNeutralButton("OK", new DialogInterface.OnClickListener()
		{

			public void onClick(DialogInterface dialog, int id)
			{
				// -- Nothing to do
			}
		});
		alertbuilder.create().show();
	}

	/**
	 * Utility method to display an alert box.
	 * 
	 * @param activity
	 *            - {@link Activity}
	 * @param titleCode
	 *            - The title for the alert box.
	 * @param message
	 *            - The message to be displayed.
	 */
	public static void showAlert(Activity activity, int titleCode, String message)
	{
		Resources resources = activity.getResources();
		String title = resources.getString(titleCode);

		AlertDialog.Builder alertbuilder = new AlertDialog.Builder(activity);
		alertbuilder.setTitle(title).setMessage(message).setIcon(R.drawable.ic_action_error);
		alertbuilder.setNeutralButton("OK", new DialogInterface.OnClickListener()
		{

			public void onClick(DialogInterface dialog, int id)
			{
				// -- Nothing to do
			}
		});
		alertbuilder.create().show();
	}

	/**
	 * Utility method to find if a given service is active or not.
	 * 
	 * @param serviceClass
	 *            - The service class to be identified.
	 * @param context
	 *            - The {@link Context} object.
	 * @return - The status of the Service
	 */
	public static boolean isServiceRunning(Class<?> serviceClass, Context context)
	{
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
		{
			if (serviceClass.getName().equals(service.service.getClassName()))
			{
				return true;
			}
		}
		return false;
	}

	public static float getBatteryLevel(Context context)
	{
		Intent batteryIntent = context.registerReceiver(null, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		// Error checking that probably isn't needed but I added just in case.
		if (level == -1 || scale == -1)
		{
			return 50.0f;
		}

		return ((float) level / (float) scale) * 100.0f;
	}
}
