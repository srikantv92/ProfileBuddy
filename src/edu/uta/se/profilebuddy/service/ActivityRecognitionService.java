package edu.uta.se.profilebuddy.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionApi;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * Service called to collect information related to User activity. Created as an
 * pending intent and passed to {@link ActivityRecognitionApi}. Upon updates
 * this service is called with the activity information. Updates are assigned to
 * static objects to allow other components to read the {@link DetectedActivity}
 */
public class ActivityRecognitionService extends IntentService
{

	private static final String TAG = ActivityRecognitionService.class.getName();

	// -- static variables
	public static volatile String ACTIVITY;
	public static volatile int CONFIDENCE;

	public ActivityRecognitionService()
	{
		super("ActivityRecognitionService");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		if (ActivityRecognitionResult.hasResult(intent))
		{
			Log.i(TAG, "Got new activity update!!!");
			ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
			DetectedActivity probableActivity = result.getMostProbableActivity();
			ACTIVITY = getNameFromType(probableActivity.getType());
			CONFIDENCE = probableActivity.getConfidence();

			Log.i(TAG, "Updating user activity - { activity:" + ACTIVITY + ", confidence:"
					+ CONFIDENCE + " }");
		}
		else
		{
			Log.i(TAG, "No activity update!!!");
		}
	}

	/**
	 * Utility method to convert activity code into identifiable defined name.
	 * 
	 * @param activityCode
	 *            - The activity code.
	 * @return - Identifiable activity name.
	 */
	private String getNameFromType(int activityCode)
	{
		String activity = null;
		switch (activityCode)
		{
		case DetectedActivity.IN_VEHICLE:
			activity = "in_vehicle";
			break;
		case DetectedActivity.ON_BICYCLE:
			activity = "on_bicycle";
			break;
		case DetectedActivity.ON_FOOT:
			activity = "on_foot";
			break;
		case DetectedActivity.STILL:
			activity = "still";
			break;
		case DetectedActivity.TILTING:
			activity = "tilting";
			break;
		case DetectedActivity.UNKNOWN:
			activity = "unknown";
			break;
		default:
			activity = "unknown";
		}
		return activity;
	}
}
