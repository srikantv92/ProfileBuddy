package com.se.profilebuddy.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;

import com.se.profilebuddy.R;

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
}
