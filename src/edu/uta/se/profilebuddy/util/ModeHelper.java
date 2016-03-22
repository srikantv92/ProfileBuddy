package edu.uta.se.profilebuddy.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Util class containing profile related code.
 */
public class ModeHelper
{

	private static final String TAG = ModeHelper.class.getName();

	// -- singleton
	public static ModeHelper instance;

	// -- instance
	private AudioManager audioManager;
	private SharedPreferences preferences;

	private ModeHelper(Context context)
	{
		this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
		this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	public static ModeHelper getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new ModeHelper(context);
		}
		return instance;
	}

	/**
	 * Function to activate a mode in the phone. Uses {@link AudioManager} to
	 * accomplish the task.
	 * 
	 * @param mode
	 *            - The mode to be activated.
	 */
	public void changeProfile(int mode)
	{
		Log.i(TAG, "Entering changeMode(), changing mode - " + mode);
		switch (mode)
		{
		case 0:
			audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			break;
		case 1:
			audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			break;
		case 2:
			audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			break;
		default:

		}
		Log.i(TAG, "Exiting changeMode()");
	}

	/**
	 * Function checks if a default mode has been set for the User. If yes,
	 * activate that mode.
	 */
	public void fallBackMode()
	{
		Log.i(TAG, "Entering fallBackMode()");
		String defaultMode = preferences.getString("list_default_mode", "-1");
		int modeValue = Integer.parseInt(defaultMode);

		Log.i(TAG, "Default mode - " + modeValue);
		if (modeValue > -1)
		{
			changeProfile(modeValue);
		}
		Log.i(TAG, "Exiting fallBackMode()");
	}
}
