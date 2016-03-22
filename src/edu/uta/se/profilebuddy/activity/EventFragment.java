package edu.uta.se.profilebuddy.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import edu.uta.se.profilebuddy.R;
import edu.uta.se.profilebuddy.adapter.EventCustomAdapter;
import edu.uta.se.profilebuddy.database.ManageEventDB;
import edu.uta.se.profilebuddy.object.CalendarEvent;
import edu.uta.se.profilebuddy.util.ProfileUtil;

/**
 * Child of MainActivity. Lists the event profiles defined by User. Provides
 * Users to option edit profiles related to events.
 */
public class EventFragment extends Fragment
{

	// -- constants
	private static final String[] GROUP_HEADERS = { "RECURRING EVENTS", "ONE TIME EVENTS" };

	// -- instance variables
	private long selectedId = -1;
	private Menu customMenu;
	private ManageEventDB eventDB;
	private ExpandableListView expandableListView;
	private EventCustomAdapter customAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_event, container, false);
		this.eventDB = new ManageEventDB(getActivity());
		intitializeExpandableListView(view);
		buildExpandableListView();
		return view;
	}

	/**
	 * Initializes {@link ExpandableListView} for the Event tab, sets the
	 * indicator position and adds listeners.
	 * 
	 * @param view
	 */
	private void intitializeExpandableListView(View view)
	{
		expandableListView = (ExpandableListView) view
				.findViewById(R.id.expandable_list_event_fragment);
		DisplayMetrics metrics = new DisplayMetrics();
		this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		// -- this code for adjusting the group indicator into right side of the
		// view
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
		{
			expandableListView.setIndicatorBounds(width - getDpsFromPixel(50), width
					- getDpsFromPixel(15));
		}
		else
		{
			// -- If this line of code is causing compilation issues, please
			// comment.
			expandableListView.setIndicatorBoundsRelative(width - getDpsFromPixel(50), width
					- getDpsFromPixel(15));
		}

		expandableListView.setOnChildClickListener(new OnChildClickListener()
		{

			@Override
			public boolean onChildClick(ExpandableListView parent, View view, int groupPosition,
					int childPosition, long id)
			{
				if (selectedId == view.getId())
				{
					toggleFragmentMenu(true);
					selectedId = -1;
				}
				else
				{
					view.setSelected(true);
					toggleFragmentMenu(false);
					selectedId = view.getId();
				}
				return true;
			}
		});
	}

	/**
	 * ListView is created using the customer adapter defined.
	 */
	private void buildExpandableListView()
	{
		new DataLoader().execute(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		this.customMenu = menu;
		inflater.inflate(R.menu.event_fragment, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.action_event:
			showEventInfo();
			return true;
		case R.id.action_refresh:
			refreshData();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Refreshes data in the Event screen.
	 */
	private void refreshData()
	{
		new DataLoader().execute(false);
	}

	/**
	 * Retrieves event information to be dispalyed to the User.
	 */
	private void showEventInfo()
	{
		eventDB.open();
		CalendarEvent eventInfo = eventDB.fetchEvent(selectedId);
		eventDB.close();

		String message;
		Resources resources = this.getActivity().getResources();
		if (eventInfo.isRecursive())
		{
			message = String.format(resources.getString(R.string.event_info_recursive_content),
					eventInfo.getTitle(), eventInfo.getDescription());
		}
		else
		{
			message = String.format(resources.getString(R.string.event_info_content), eventInfo
					.getTitle(), eventInfo.getDescription(),
					getTimeStamp(eventInfo.getStartTime()), getTimeStamp(eventInfo.getEndTime()));
		}
		ProfileUtil.showAlert(this.getActivity(), R.string.event_info_title, message);
	}

	/**
	 * Utility method to convert milliseconds to TimeStamp.
	 * 
	 * @param milliseconds
	 *            .
	 * @return - The TimeStamp.
	 */
	private String getTimeStamp(long milliseconds)
	{
		DateFormat formatter = new SimpleDateFormat("HH:mm a MM/dd/yy", Locale.US);
		String formattedDate = formatter.format(milliseconds);
		return formattedDate;
	}

	private void toggleFragmentMenu(boolean toggle)
	{
		try
		{
			customMenu.findItem(R.id.action_refresh).setVisible(toggle);
			customMenu.findItem(R.id.action_event).setVisible(!toggle);
		}
		catch (NullPointerException npe)
		{
			// -- nothing to do here. To handle NPE in extreme condition.
		}
	}

	private int getDpsFromPixel(float pixels)
	{
		// -- Get the screen's density scale
		final float scale = getResources().getDisplayMetrics().density;
		// -- Convert the dps to pixels, based on density scale
		return (int) (pixels * scale + 0.5f);
	}

	private class DataLoader extends AsyncTask<Boolean, String, List<CalendarEvent>>
	{

		private boolean init = true;

		@Override
		protected List<CalendarEvent> doInBackground(Boolean... params)
		{
			this.init = params[0];
			eventDB.open();
			List<CalendarEvent> events = eventDB.fetchEvents(true);
			eventDB.close();

			return events;
		}

		private Map<String, List<CalendarEvent>> categorizeData(List<CalendarEvent> events)
		{
			List<CalendarEvent> recursiveEvents = new ArrayList<CalendarEvent>();
			List<CalendarEvent> normalEvents = new ArrayList<CalendarEvent>();
			for (CalendarEvent event : events)
			{
				if (event.isRecursive())
				{
					recursiveEvents.add(event);
				}
				else
				{
					normalEvents.add(event);
				}
			}
			Map<String, List<CalendarEvent>> data = new HashMap<String, List<CalendarEvent>>();
			data.put(GROUP_HEADERS[0], recursiveEvents);
			data.put(GROUP_HEADERS[1], normalEvents);

			return data;
		}

		@Override
		protected void onPostExecute(List<CalendarEvent> events)
		{
			Map<String, List<CalendarEvent>> data = categorizeData(events);
			if (init)
			{
				customAdapter = new EventCustomAdapter(getActivity(), GROUP_HEADERS, data);
				expandableListView.setAdapter(customAdapter);
				// -- To have the groups expanded by default.
				for (int i = 0; i < customAdapter.getGroupCount(); i++)
				{
					expandableListView.expandGroup(i);
				}
			}
			else
			{
				customAdapter.refreshData(data);
				customAdapter.notifyDataSetChanged();
			}
		}
	}

}
