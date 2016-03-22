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
import edu.uta.se.profilebuddy.object.ProfileLocation;
import edu.uta.se.profilebuddy.util.ProfileUtil;

/**
 * Activity for the Add Location Screen. Accepts Location parameters and passes
 * to the final Add Location screen.
 */
public class AddLocationActivity extends Activity
{

	private static final String TAG = AddLocationActivity.class.getSimpleName();

	// -- constants
	protected static final String LOCATION = "edu.uta.se.profilebuddy.activity.AddLocationActivity.location";

	// -- instance variables
	private Address searchedAddress;
	private int selectedProfile;

	// -- views
	EditText editTitle;
	AutoCompleteTextView autoEditAddress;
	Spinner spinnerProfile;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_location);

		editTitle = (EditText) findViewById(R.id.edit_title_add_location_activity);
		autoEditAddress = (AutoCompleteTextView) findViewById(R.id.auto_edit_address_add_location_activity);
		autoEditAddress.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list));
		initializeProfileSpinner();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.add_location, menu);
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
					startAddLocFinalActivity();
				}
				else
				{
					ProfileUtil.showAlert(this, R.string.error_title,
							R.string.add_location_invalid_location_message);
				}
			}
			else
			{
				ProfileUtil.showAlert(this, R.string.error_title,
						R.string.add_location_invalid_entry_message);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Initializes Profile spinner used in the activity and registers listeners
	 * on it
	 */
	private void initializeProfileSpinner()
	{
		this.spinnerProfile = (Spinner) findViewById(R.id.spinner_mode_add_location_activity);
		this.selectedProfile = 0;
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

	/**
	 * Utility method to validate inputs given by the User.
	 * 
	 * @return status of validation
	 */
	private boolean validateData()
	{
		boolean valid = true;
		if (editTitle.getText().toString().isEmpty()
				|| autoEditAddress.getText().toString().isEmpty())
		{
			valid = false;
		}
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
		return valid;
	}

	/**
	 * Calls {@link AddLocationFinalActivity} with necessary parameters required
	 * by the new activity
	 */
	private void startAddLocFinalActivity()
	{
		Intent intent = new Intent(this, AddLocationFinalActivity.class);
		ProfileLocation location = new ProfileLocation();
		location.setTitle(editTitle.getText().toString());
		location.setAddress(autoEditAddress.getText().toString());
		location.setLatitude(searchedAddress.getLatitude());
		location.setLongitude(searchedAddress.getLongitude());
		location.setMode(selectedProfile);
		intent.putExtra(LOCATION, location);

		startActivity(intent);
	}
}
