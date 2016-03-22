package edu.uta.se.profilebuddy.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import edu.uta.se.profilebuddy.action.ProfileAction;

/**
 * Custom back end service to periodically check the User location and set
 * profile accordingly.
 */
public class LocationService extends Service implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener
{

	private static final String TAG = LocationService.class.getName();

	// -- enum
	public enum ServiceState
	{
		OFF, ON
	}

	// -- static
	public static ServiceState SERV_STATE = ServiceState.OFF;

	// -- Constants
	private static final int UPDATE_INTERVAL = 5000; // -- 10 sec
	private static final int FASTEST_INTERVAL = 5000; // -- 5 sec
	private static final int DISPLACEMENT = 1; // -- 5 meters

	// -- instance variables
	public static Location lastLocation;
	public static LocationService thisObject;
	private LocationRequest locationRequest;
	private GoogleApiClient googleApiClient;
	private ProfileAction profileAction;

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.i(TAG, "ProfileLocation service started");
		SERV_STATE = ServiceState.ON;
		Log.i(TAG, "ProfileLocation service started - " + SERV_STATE);
		if (checkPlayServices())
		{
			if (googleApiClient == null)
			{
				buildGoogleApiClient();
				googleApiClient.connect();
				createLocationRequest();
			}
		}
		this.profileAction = ProfileAction.getInstance(this);
		thisObject = this;
		return START_STICKY;
	}

	private synchronized void buildGoogleApiClient()
	{
		googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
	}

	private void createLocationRequest()
	{
		locationRequest = new LocationRequest();
		locationRequest.setInterval(UPDATE_INTERVAL);
		locationRequest.setFastestInterval(FASTEST_INTERVAL);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
	}

	private boolean checkPlayServices()
	{
		boolean serviceAvailable = true;
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
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

	private void startLocationUpdates()
	{
		LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest,
				this);
	}

	@Override
	public void onLocationChanged(Location location)
	{
		lastLocation = location;
		new ServiceTasker().execute(location);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result)
	{
		Log.i(TAG, "Service failed to connect to API services - " + result.getErrorCode());
	}

	@Override
	public void onConnected(Bundle arg0)
	{
		Log.i(TAG, "Service connected to API Services.");
		startLocationUpdates();
	}

	@Override
	public void onConnectionSuspended(int arg0)
	{
		googleApiClient.connect();
	}

	@Override
	public void onDestroy()
	{
		Log.i(TAG, "ProfileLocation service destroyed.");
		if (googleApiClient.isConnected())
		{
			LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
			googleApiClient.disconnect();
		}
		SERV_STATE = ServiceState.OFF;
		super.onDestroy();
	}

	private class ServiceTasker extends AsyncTask<Location, String, String>
	{

		@Override
		protected String doInBackground(Location... params)
		{
			profileAction.setCurrentLocation(params[0]);
			profileAction.manageProfile();
			return null;
		}
	}

}
