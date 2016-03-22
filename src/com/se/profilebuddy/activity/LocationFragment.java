package com.se.profilebuddy.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.se.profilebuddy.R;
import com.se.profilebuddy.adapter.LocationCustomAdapter;
import com.se.profilebuddy.database.ManageLocationDB;

/**
 * Child of MainActivity. Lists the location profiles defined by User. Provides
 * Users to option to add, edit and remove location profiles.
 */
public class LocationFragment extends Fragment
{

	// -- constants
	protected static final String LOCATION_ID = "com.se.profilebuddy.activity.LocationFragment.locationId";

	// -- instance variables
	private long selectedId = -1;
	private Menu customMenu;
	private ManageLocationDB locationDB;
	private LocationCustomAdapter customAdapter;

	// -- views
	ListView locationListView;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_location, container, false);

		intitializeListView(view);
		buildListView();
		return view;
	}

	private void intitializeListView(View view)
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
		customMenu.findItem(R.id.action_new).setVisible(toggle);
		customMenu.findItem(R.id.action_discard).setVisible(!toggle);
		customMenu.findItem(R.id.action_edit).setVisible(!toggle);
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		customAdapter.getCursor().close();
		locationDB.close();
	}

}
