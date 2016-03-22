package edu.uta.se.profilebuddy.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.uta.se.profilebuddy.action.CalendarHelper;
import edu.uta.se.profilebuddy.database.ManageEventDB;
import edu.uta.se.profilebuddy.database.ManageNotificationDB;

/**
 * Receives actions triggered from Event notifications.
 */
public class NotificationReceiver extends BroadcastReceiver
{

	private static final String TAG = NotificationReceiver.class.getName();

	// -- instance variables
	private Context context;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i(TAG, "Entering onRecieve, params - { action:" + intent.getAction() + " }");

		this.context = context;
		String action = intent.getAction();
		long eventId = intent.getExtras().getLong(CalendarHelper.EVENT_ID);

		if (CalendarHelper.ACTION_SILENT.equalsIgnoreCase(action))
		{
			updateEventMode(eventId, 0);
		}
		else if (CalendarHelper.ACTION_VIBRATE.equalsIgnoreCase(action))
		{
			updateEventMode(eventId, 1);
		}
		else if (CalendarHelper.ACTION_NORMAL.equalsIgnoreCase(action))
		{
			updateEventMode(eventId, 2);
		}
		int notificationId = intent.getExtras().getInt(CalendarHelper.NOTIFICATION_ID);
		removeNotification(notificationId);

		Log.i(TAG, "Exiting onRecieve");
	}

	/**
	 * @param eventId
	 * @param mode
	 */
	private void updateEventMode(long eventId, int mode)
	{
		Log.i(TAG, "Entering updateEventMode, params - { eventId:" + eventId + ", mode:" + mode
				+ " }");

		ManageEventDB eventDB = new ManageEventDB(context);
		eventDB.open();
		eventDB.updateMode(eventId, mode);
		eventDB.close();

		Log.i(TAG, "Exiting updateEventMode");
	}

	/**
	 * @param notificationId
	 */
	private void removeNotification(int notificationId)
	{
		Log.i(TAG, "Entering removeNotification, params - { notificationId:" + notificationId
				+ " }");

		// -- Update status of notification in database
		ManageNotificationDB notificationDB = new ManageNotificationDB(context);
		notificationDB.open();
		notificationDB.updateNotification(notificationId);
		notificationDB.close();

		// -- Remove notification from notification bar
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(notificationId);

		Log.i(TAG, "Exiting removeNotification");
	}
}
