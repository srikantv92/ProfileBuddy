package edu.uta.se.profilebuddy.adapter;

/**
 * Adapter for the Event Expandable ListView. Custom builds views using the data provided.
 *
 */
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import edu.uta.se.profilebuddy.R;

import edu.uta.se.profilebuddy.activity.MainActivity;
import edu.uta.se.profilebuddy.database.ManageEventDB;
import edu.uta.se.profilebuddy.object.CalendarEvent;

public class EventCustomAdapter extends BaseExpandableListAdapter
{

	private static final String TAG = EventCustomAdapter.class.getSimpleName();

	private Context context;
	private List<String> headers;
	private Map<String, List<CalendarEvent>> data;

	public EventCustomAdapter(Context context, String[] headers,
			Map<String, List<CalendarEvent>> data)
	{
		this.context = context;
		this.headers = Arrays.asList(headers);
		this.data = data;
	}

	public void refreshData(Map<String, List<CalendarEvent>> data)
	{
		this.data = data;
	}

	@Override
	public int getGroupCount()
	{
		return this.headers.size();
	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		return this.data.get(this.headers.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		return this.headers.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		return this.data.get(this.headers.get(groupPosition)).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition)
	{
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition)
	{
		return childPosition;
	}

	@Override
	public boolean hasStableIds()
	{
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
			ViewGroup parent)
	{
		if (convertView == null)
		{
			LayoutInflater infalInflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.group_fragment_event, parent, false);
		}
		String headerText = (String) getGroup(groupPosition);

		TextView headerView = (TextView) convertView
				.findViewById(R.id.text_group_title_event_fragment);
		headerView.setTypeface(null, Typeface.BOLD_ITALIC);
		headerView.setText(headerText);
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
			View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			LayoutInflater infalInflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.item_fragment_event, parent, false);
		}
		CalendarEvent event = (CalendarEvent) getChild(groupPosition, childPosition);

		convertView.setId((int) event.getId());
		TextView editTitle = (TextView) convertView.findViewById(R.id.text_title_event_fragment);
		editTitle.setText(event.getTitle());

		Spinner spinnerMode = (Spinner) convertView.findViewById(R.id.spinner_mode_event_fragment);
		spinnerMode.setSelection(event.getMode() + 1);
		spinnerMode.setOnItemSelectedListener(spinnerListener(convertView.getId()));

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return true;
	}

	private AdapterView.OnItemSelectedListener spinnerListener(final long locationId)
	{
		return new AdapterView.OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				changeMode(locationId, position - 1);
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
	 * Calls DB to update profile of selected ListView item
	 * 
	 * @param eventId
	 *            - The id of the event to be updated.
	 * @param mode
	 *            - Mode to be updated.
	 */
	public void changeMode(long eventId, int mode)
	{
		Log.i(TAG, "Entering changeMode(), params - params - { eventId: " + eventId + ", mode:"
				+ mode + " }");

		ManageEventDB eventDB = new ManageEventDB(this.context);
		eventDB.open();
		eventDB.updateMode(eventId, mode);
		eventDB.close();

		Log.i(TAG, "Exiting changeMode()");
	}
}
