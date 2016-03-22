package edu.uta.se.profilebuddy.activity;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;

import edu.uta.se.profilebuddy.R;

import edu.uta.se.profilebuddy.adapter.PlacesAutoCompleteAdapter;
import edu.uta.se.profilebuddy.database.ManageLocationDB;
import edu.uta.se.profilebuddy.object.ProfileLocation;
import edu.uta.se.profilebuddy.util.ProfileUtil;

/**
 * Activity for the Edit Location Screen. Accepts any updates to Location
 * parameters and passes them to the final Edit Location screen.
 */
public class EditLocationActivity extends Activity
{

	private static final String TAG = EditLocationActivity.class.getSimpleName();

	// -- constants
	protected static final String LOCATION = "edu.uta.se.profilebuddy.activity.EditLocationActivity.location";

	// -- instance variables
	private long locationId;
	private ProfileLocation location;
	private Address searchedAddress;
	private int selectedProfile;

	// -- views
	private EditText editTitle;
	private AutoCompleteTextView autoEditAddress;
	private Spinner spinnerProfile;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_location);

		this.locationId = getIntent().getExtras().getLong(LocationFragment.LOCATION_ID);
		this.editTitle = (EditText) findViewById(R.id.edit_title_edit_location_activity);
		this.autoEditAddress = (AutoCompleteTextView) findViewById(R.id.auto_edit_address_edd_location_activity);
		this.autoEditAddress.setAdapter(new PlacesAutoCompleteAdapter(this,
				R.layout.autocomplete_list));

		loadContent();
	}

	/**
	 * This method loads data to views attached to the activity.
	 */
	private void loadContent()
	{
		getContent();
		this.editTitle.setText(this.location.getTitle());
		this.autoEditAddress.setText(this.location.getAddress());
		initializeProfileSpinner();
	}

	/**
	 * This method fetches data from database to be loaded to views.
	 */
	private void getContent()
	{
		Log.d(TAG, "Entering getContent(), params - { locationId:" + this.locationId + " }");
		ManageLocationDB locationDB = new ManageLocationDB(this);
		locationDB.open();
		this.location = locationDB.fetchLocation(this.locationId);
		locationDB.close();
		Log.d(TAG, "Exiting getContent()");
	}

	/**
	 * Utility method to validate inputs given by the User.
	 * 
	 * @return status of validation
	 */
	private boolean validateData()
	{
		Log.d(TAG, "Entering validateData()");
		boolean valid = true;
		if (editTitle.getText().toString().isEmpty()
				|| autoEditAddress.getText().toString().isEmpty())
		{
			valid = false;
		}
		Log.d(TAG, "Exiting validateData()");
		return valid;
	}

	/**
	 * Converts a given address string into latitude, longitude coordinates.
	 * 
	 * @return status of the conversion. If no matching results were found,
	 *         false will be returned.
	 */
	private boolean geoCode()
	{
		Log.d(TAG, "Entering geoCode()");
		boolean valid = false;
		try
		{
			Geocoder geoCoder = new Geocoder(this);
			String addressText = autoEditAddress.getText().toString();
			List<Address> addresses = geoCoder.getFromLocationName(addressText, 1);
			if (addresses.size() > 0)
			{
				valid = true;
				searchedAddress = addresses.get(0);
			}
		}
		catch (IOException e)
		{
			Log.e(TAG, "IOexception in geoCode - " + e.getMessage());
		}

		Log.d(TAG, "Is address valid - " + valid);
		Log.d(TAG, "Exiting geoCode()");
		return valid;
	}

	/**
	 * Initializes Profile spinner used in the activity and registers listeners
	 * on it
	 */
	private void initializeProfileSpinner()
	{
		this.spinnerProfile = (Spinner) findViewById(R.id.spinner_mode_edit_location_activity);
		this.spinnerProfile.setSelection(this.location.getMode());
		this.spinnerProfile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				selectedProfile = position;
				Log.d(TAG, "Selected profile - " + position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				// -- nothing to do
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_location, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_next)
		{
			if (validateData())
			{
				if (geoCode())
				{
					startEditLocFinalActivity();
				}
				else
				{
					ProfileUtil.showAlert(this, R.string.error_title,
							R.string.add_location_invalid_entry_message);
				}
			}
			else
			{
				ProfileUtil.showAlert(this, R.string.error_title,
						R.string.add_location_invalid_location_message);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Calls {@link EditLocationFinalActivity} with necessary parameters
	 * required by the new activity
	 */
	private void startEditLocFinalActivity()
	{
		Log.d(TAG, "Entering nextActivity(), params - { " + this.location + " }");
		Intent intent = new Intent(this, EditLocationFinalActivity.class);
		location.setTitle(editTitle.getText().toString());
		location.setAddress(autoEditAddress.getText().toString());
		location.setLatitude(searchedAddress.getLatitude());
		location.setLongitude(searchedAddress.getLongitude());
		location.setMode(selectedProfile);
		intent.putExtra(LOCATION, location);

		startActivity(intent);
		Log.d(TAG, "Exiting nextActivity()");
	}
}
