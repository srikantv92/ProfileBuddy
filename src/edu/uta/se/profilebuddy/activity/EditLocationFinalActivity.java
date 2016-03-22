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
 * Activity for the Edit Location Final screen. Captures the radius and displays
 * the Map to User. On save, data is updated in the Database.
 */
public class EditLocationFinalActivity extends FragmentActivity implements OnMapReadyCallback,
		TextWatcher
{

	private static final String TAG = EditLocationFinalActivity.class.getSimpleName();

	// -- constants
	private static final int DEFAULT_ZOOM_LEVEL = 17;

	// -- instance variables
	private ProfileLocation location;
	private LatLng coordinates;
	private GoogleMap locationMap;
	private Circle circle;

	// --views
	private EditText editRadius;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_location_final);
		this.location = getIntent().getExtras().getParcelable(EditLocationActivity.LOCATION);

		this.coordinates = new LatLng(location.getLatitude(), location.getLongitude());
		this.editRadius = (EditText) findViewById(R.id.edit_radius_edit_location_activity);
		loadContent();

		this.editRadius.addTextChangedListener(this);
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	private void loadContent()
	{
		this.editRadius.setText(String.valueOf(this.location.getRadius()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_location_final, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_save)
		{
			if (validateData())
			{
				updateLocation();
				startMainActivity();
			}
			else
			{
				ProfileUtil.showAlert(this, R.string.error_title,
						R.string.add_location_invalid_radius_message);
			}
			return true;
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
	 * Calls DB with the captured parameters and updates the previous record.
	 */
	private void updateLocation()
	{
		Log.i(TAG, "Entering updateLocation(), params - " + this.location);
		location.setRadius(Long.parseLong(editRadius.getText().toString()));
		ManageLocationDB locationDB = new ManageLocationDB(this);
		locationDB.open();
		locationDB.updateLocation(location);
		locationDB.close();
		Log.i(TAG, "Exiting updateLocation()");
	}

	/**
	 * Called after location profile is updated in the Database. Calls
	 * {@link MainActivity} to display home screen.
	 */
	private void startMainActivity()
	{
		Log.i(TAG, "Entering nextActivity()");
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		Log.i(TAG, "Exiting nextActivity()");
	}

	@Override
	public void onMapReady(GoogleMap map)
	{
		this.locationMap = map;
		locationMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		locationMap.getUiSettings().setCompassEnabled(true);
		locationMap.getUiSettings().setZoomControlsEnabled(true);
		locationMap.addMarker(new MarkerOptions().position(coordinates).title("Marker"));

		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom((coordinates),
				DEFAULT_ZOOM_LEVEL);
		locationMap.animateCamera(cameraUpdate);
		drawCircle(location.getRadius());
	}

	/**
	 * This method is used to draw a circle on the map object. Once the
	 * {@link Circle} object is created its setRadius method is used in
	 * subsequent calls to redraw the circle.
	 * 
	 * @param radius
	 *            - The radius of the circle to be drawn.
	 */
	private void drawCircle(long radius)
	{
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
		Log.d(TAG, "Entering afterTextChanged(), params - { text:" + text + " }");
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
