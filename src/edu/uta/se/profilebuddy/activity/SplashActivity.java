package edu.uta.se.profilebuddy.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import edu.uta.se.profilebuddy.R;

/**
 * Activity class for Splash screen. Also can be used to invoke services in the
 * background, before the start of the Home screen.
 *
 */
public class SplashActivity extends Activity
{

	// -- Splash screen timer
	private static int SPLASH_TIME_OUT = 3000;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);

		new Handler().postDelayed(new Runnable()
		{

			// -- Showing splash screen with a timer. This will be useful when
			// -- you want to show case your app logo / company
			@Override
			public void run()
			{
				// -- This method will be executed once the timer is over
				// -- Start your app main activity
				Intent i = new Intent(SplashActivity.this, MainActivity.class);
				startActivity(i);

				// -- close this activity
				finish();
			}
		}, SPLASH_TIME_OUT);
	}
}