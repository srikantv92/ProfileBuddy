package edu.uta.se.profilebuddy.adapter;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import edu.uta.se.profilebuddy.R;

import edu.uta.se.profilebuddy.activity.MainActivity;
import edu.uta.se.profilebuddy.database.ManageLocationDB;

/**
 * Adapter for the Location listview. Custom builds views using the data cursor
 * provided.
 *
 */
public class LocationCustomAdapter extends CursorAdapter
{

	private static final String TAG = LocationCustomAdapter.class.getSimpleName();

	public LocationCustomAdapter(Context context, Cursor cursor, int flag)
	{
		super(context, cursor, flag);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent)
	{
		// -- when the view will be created for first time,
		// -- we need to tell the adapters, how each item will look
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(R.layout.location_row, parent, false);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		// -- here we are setting our data
		// -- that means, take the data from the cursor and put it in views
		view.setId(cursor.getInt(0)); // -- id
		TextView editTitle = (TextView) view.findViewById(R.id.text_title_location_fragment);
		editTitle.setText(cursor.getString(1)); // -- title

		Spinner spinnerMode = (Spinner) view.findViewById(R.id.spinner_mode_location_fragment);
		int selectedIndex = cursor.getInt(5); // -- mode
		spinnerMode.setSelection(selectedIndex);
		spinnerMode.setOnItemSelectedListener(spinnerListener(view.getId()));
	}

	private AdapterView.OnItemSelectedListener spinnerListener(final long locationId)
	{
		return new AdapterView.OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				updateLocationMode(locationId, position);
				MainActivity.triggerChangeLocation();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				// -- nothing to do
			}
		};
	}

	/**
	 * Calls DB to update profile of selected listview item
	 * 
	 * @param locationId
	 *            - The id of the location to be updated.
	 * @param mode
	 *            - Mode to be updated.
	 */
	public void updateLocationMode(long locationId, int mode)
	{
		Log.i(TAG, "Entering updateLocationMode(), params - params - { locationId: " + locationId
				+ ", mode:" + mode + " }");
		ManageLocationDB locationDB = new ManageLocationDB(MainActivity.appContext);
		locationDB.open();
		locationDB.updateMode(locationId, mode);
		locationDB.close();
		Log.i(TAG, "Exiting updateLocationMode()");
	}
}
