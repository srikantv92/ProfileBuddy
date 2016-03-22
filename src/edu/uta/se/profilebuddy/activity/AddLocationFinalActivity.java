package edu.uta.se.profilebuddy.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import edu.uta.se.profilebuddy.R;

import edu.uta.se.profilebuddy.database.ManageLocationDB;
import edu.uta.se.profilebuddy.object.ProfileLocation;
import edu.uta.se.profilebuddy.util.ProfileUtil;

/**
 * Activity for the Add Location Final screen. Captures the radius and displays
 * the Map to User. It also saves the data into Database.
 */
public class AddLocationFinalActivity extends FragmentActivity implements OnMapReadyCallback,
		TextWatcher
{

	private static final String TAG = AddLocationFinalActivity.class.getSimpleName();

	// -- constants
	private static final int DEFAULT_ZOOM_LEVEL = 17;

	// -- instance variables
	private ProfileLocation location;
	private LatLng coordinates;
	private GoogleMap locationMap;
	private Circle circle;

	// -- views
	private EditText editRadius;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_location_final);

		location = getIntent().getExtras().getParcelable(AddLocationActivity.LOCATION);
		coordinates = new LatLng(location.getLatitude(), location.getLongitude());
		editRadius = (EditText) findViewById(R.id.edit_radius_add_location_activity);
		editRadius.addTextChangedListener(this);

		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
		Log.i(TAG, "onCreate method end");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// -- Inflate the menu; this adds items to the action bar if it is
		// present.
		getMenuInflater().inflate(R.menu.add_location_final, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// -- Handle action bar item clicks here. The action bar will
		// -- automatically handle clicks on the Home/Up button, so long
		// -- as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_save)
		{
			if (validateData())
			{
				saveLocation();
				startMainActivity();
				return true;
			}
			else
			{
				ProfileUtil.showAlert(this, R.string.error_title,
						R.string.add_location_invalid_radius_message);
			}
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Utility method to validate inputs given by the User.
	 * 
	 * @return status of validation
	 */
	private boolean validateData()
	{
		boolean valid = true;
		if (editRadius.getText().toString().isEmpty())
		{
			valid = false;
		}
		return valid;
	}

	/**
	 * Calls DB with the captured parameters and saves it.
	 */
	private void saveLocation()
	{
		location.setRadius(Long.parseLong(editRadius.getText().toString()));

		ManageLocationDB locationDB = new ManageLocationDB(this);
		locationDB.open();
		locationDB.createProfile(location);
		locationDB.close();
	}

	/**
	 * Called after location profile is saved to the Database. Calls
	 * {@link MainActivity} to display home screen.
	 */
	private void startMainActivity()
	{
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	@Override
	public void onMapReady(GoogleMap map)
	{
		this.locationMap = map;
		locationMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		locationMap.getUiSettings().setCompassEnabled(true);
		locationMap.getUiSettings().setZoomControlsEnabled(true);
		locationMap
				.addMarker(new MarkerOptions().position(coordinates).title("Selected location!"));

		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom((coordinates),
				DEFAULT_ZOOM_LEVEL);
		locationMap.animateCamera(cameraUpdate);
		drawCircle(0);
	}

	/**
	 * This method is used to draw a circle on the map object. Once the
	 * {@link Circle} object is created its setRadius method is used in
	 * subsequent calls to redraw the circle.
	 * 
	 * @param radius
	 *            - The radius of the circle to be drawn.
	 */
	private void drawCircle(int radius)
	{
		Log.d(TAG, "Entering drawCircle(), inputs - { radius:" + radius + " }");
		if (this.circle == null)
		{
			CircleOptions circleOptions = new CircleOptions().center(coordinates).radius(radius)
					.fillColor(0x40ff0000).strokeColor(Color.RED).strokeWidth(1);
			circle = locationMap.addCircle(circleOptions);
		}
		else
		{
			circle.setRadius(radius);
		}
		Log.d(TAG, "Exiting drawCircle()");
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{

	}

	@Override
	public void afterTextChanged(Editable text)
	{
		Log.d(TAG, "Entering afterTextChanged(), inputs - { text:" + text.toString() + " }");
		String radius = text.toString();
		if (radius.isEmpty())
		{
			drawCircle(0);
		}
		else
		{
			drawCircle(Integer.parseInt(radius));
		}
		Log.d(TAG, "Exiting afterTextChanged()");
	}
}
