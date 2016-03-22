package com.se.profilebuddy.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.se.profilebuddy.action.CalendarAction;
import com.se.profilebuddy.action.CalendarHelper;

/**
 * Custom back end service to periodically check the Events and set profile
 * accordingly.
 */
public class CalendarService extends Service
{

	private static final String TAG = CalendarService.class.getSimpleName();

	private static boolean EVENT_PROFILE = false;

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.i(TAG, "Calendar service started");
		this.start();
		return START_NOT_STICKY;
	}

	private void start()
	{
		CalendarHelper helper = CalendarHelper.getInstance(this);
		helper.syncCalendar();

		CalendarAction action = CalendarAction.getInstance(this);
		action.manageProfile();

		if (EVENT_PROFILE)
		{
			// -- nothing to do now.
		}
		else
		{
			// -- nothing to do now.
		}
	}
}
