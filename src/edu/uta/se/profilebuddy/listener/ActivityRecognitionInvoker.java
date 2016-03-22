package edu.uta.se.profilebuddy.listener;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionApi;

import edu.uta.se.profilebuddy.service.ActivityRecognitionService;

/**
 * Invoker - initializes required objects before invoking the
 * {@link ActivityRecognitionService}. Manages the life cycle of the
 * {@link ActivityRecognitionService}.
 */
public class ActivityRecognitionInvoker implements ConnectionCallbacks, OnConnectionFailedListener
{

	private static final String TAG = ActivityRecognitionInvoker.class.getName();

	// -- constants
	private static final long REQUEST_INTERVAL = 0; // -- ms

	// -- singleton instance
	private static volatile ActivityRecognitionInvoker instance;

	// -- instance variables
	private GoogleApiClient googleApiClient;
	private Context context;
	private PendingIntent pendingIntent;

	private ActivityRecognitionInvoker(Context context)
	{
		this.context = context;
		Intent intent = new Intent(context, ActivityRecognitionService.class);
		this.pendingIntent = PendingIntent.getService(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public static ActivityRecognitionInvoker getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new ActivityRecognitionInvoker(context);
		}
		return instance;
	}

	public static ActivityRecognitionInvoker getInstance()
	{
		return instance;
	}

	/**
	 * Wrapper - initializes {@link GoogleApiClient} and creates a connection to
	 * Google Play Services which is needed to retrieve updates from
	 * {@link ActivityRecognitionApi>
	 */
	public void connect()
	{
		if (checkPlayServices())
		{
			buildGoogleApiClient();
			if (!googleApiClient.isConnected())
			{
				googleApiClient.connect();
			}
		}
	}

	/**
	 * Wrapper - removes registered {@link ActivityRecognitionApi} service,
	 * disconnects {@link GoogleApiClient} and removes its reference object.
	 */
	public void suspend()
	{
		if (googleApiClient != null && pendingIntent != null)
		{
			Log.i(TAG, "De-activating activity recognition service");
			ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(googleApiClient,
					pendingIntent);
			googleApiClient.disconnect();
			googleApiClient = null;
		}
	}

	/**
	 * Initializes {@link GoogleApiClient}
	 */
	private synchronized void buildGoogleApiClient()
	{
		if (googleApiClient == null)
		{
			googleApiClient = new GoogleApiClient.Builder(context).addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this).addApi(ActivityRecognition.API).build();
		}
	}

	/**
	 * Utility method to check if Google Play services is available.
	 * 
	 * @return - status says if service available or not
	 */
	private boolean checkPlayServices()
	{
		boolean serviceAvailable = true;
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		if (resultCode != ConnectionResult.SUCCESS)
		{
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
			{
				// -- nothing to do
			}
			else
			{
				// -- nothing to do
			}
			serviceAvailable = false;
		}
		return serviceAvailable;
	}

	@Override
	public void onConnectionFailed(ConnectionResult result)
	{
		Log.i(TAG, "Service failed to connect to API services - " + result.getErrorCode());
	}

	@Override
	public void onConnected(Bundle bundle)
	{
		Log.i(TAG, "Activating recognition service");
		ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(googleApiClient,
				REQUEST_INTERVAL, pendingIntent);
	}

	@Override
	public void onConnectionSuspended(int arg0)
	{
		Log.i(TAG, "Google API disconnected");
	}

}