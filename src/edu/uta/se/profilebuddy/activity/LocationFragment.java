package edu.uta.se.profilebuddy.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import edu.uta.se.profilebuddy.R;
import edu.uta.se.profilebuddy.adapter.LocationCustomAdapter;
import edu.uta.se.profilebuddy.database.ManageLocationDB;

/**
 * Child of MainActivity. Lists the location profiles defined by User. Provides
 * Users to option to add, edit and remove location profiles.
 */
public class LocationFragment extends Fragment
{

	// -- constants
	protected static final String LOCATION_ID = "edu.uta.se.profilebuddy.activity.LocationFragment.locationId";
	private static final String PREF_LOCATION_SERVICE = "checkbox_location_service_preference";
	private static final String GREEN_MODE = "is_green_mode_active";

	// -- instance variables
	private long selectedId = -1;
	private Menu customMenu;
	private ManageLocationDB locationDB;
	private LocationCustomAdapter customAdapter;
	private Context context;

	// -- views
	private View view;
	private TextView disabledTextView;
	private TextView pausedTextView;
	private ListView locationListView;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		this.context = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		this.view = inflater.inflate(R.layout.fragment_location, container, false);
		intializeTextView();
		intitializeListView();
		buildListView();
		return view;
	}

	protected void intializeTextView()
	{
		Log.i("LocationFragment", "intializeTextView");
		disabledTextView = (TextView) view.findViewById(R.id.text_disabled_location_fragment);
		pausedTextView = (TextView) view.findViewById(R.id.text_paused_location_fragment);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean serviceActive = preferences.getBoolean(PREF_LOCATION_SERVICE, false);
		boolean greenMode = preferences.getBoolean(GREEN_MODE, false);

		if (serviceActive)
		{
			disabledTextView.setVisibility(View.GONE);
			if (greenMode)
			{
				pausedTextView.setVisibility(View.VISIBLE);
			}
			else
			{
				pausedTextView.setVisibility(View.GONE);
			}
		}
		else
		{
			disabledTextView.setVisibility(View.VISIBLE);
			pausedTextView.setVisibility(View.GONE);
		}
	}

	private void intitializeListView()
	{
		locationListView = (ListView) view.findViewById(R.id.list_location_fragment);
		locationListView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if (selectedId == id)
				{
					toggleFragmentMenu(true);
					selectedId = -1;
				}
				else
				{
					view.setSelected(true);
					toggleFragmentMenu(false);
					selectedId = id;
				}
			}
		});
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		this.customMenu = menu;
		inflater.inflate(R.menu.location_fragment, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.action_new:
			openAddLocationActivity();
			return true;
		case R.id.action_edit:
			editSelectedLocation();
			return true;
		case R.id.action_discard:
			deleteSelectedLocation();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Calls {@link EditLocationActivity}
	 */
	private void editSelectedLocation()
	{
		if (selectedId > 0)
		{
			Intent intent = new Intent(getActivity(), EditLocationActivity.class);
			intent.putExtra(LOCATION_ID, selectedId);
			startActivity(intent);
		}
	}

	/**
	 * Calls DB to delete a record and updates ListView.
	 */
	private void deleteSelectedLocation()
	{
		if (selectedId > 0)
		{
			locationDB.deleteLocation(this.selectedId);
			Cursor cursor = locationDB.getLocationsCursor();
			customAdapter.swapCursor(cursor).close();
			customAdapter.notifyDataSetChanged();
			toggleFragmentMenu(true);
		}
	}

	/**
	 * ListView is created using the customer adapter defined.
	 */
	private void buildListView()
	{
		locationDB = new ManageLocationDB(getActivity());

		locationDB.open();
		Cursor cursor = locationDB.getLocationsCursor();
		customAdapter = new LocationCustomAdapter(getActivity(), cursor, 0);
		locationListView.setAdapter(customAdapter);
	}

	/**
	 * Method calls {@link AddLocationActivity}
	 */
	private void openAddLocationActivity()
	{
		Intent intent = new Intent(getActivity(), AddLocationActivity.class);
		startActivity(intent);
	}

	private void toggleFragmentMenu(boolean toggle)
	{
		try
		{
			customMenu.findItem(R.id.action_new).setVisible(toggle);
			customMenu.findItem(R.id.action_discard).setVisible(!toggle);
			customMenu.findItem(R.id.action_edit).setVisible(!toggle);
		}
		catch (NullPointerException npe)
		{
			// -- nothing to do here. To handle NPE in extreme condition.
		}
	}

	@Override
	public void onDestroyView()
	{
		customAdapter.getCursor().close();
		locationDB.close();
		super.onDestroyView();
	}
}
