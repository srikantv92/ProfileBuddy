package edu.uta.se.profilebuddy.adapter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import edu.uta.se.profilebuddy.activity.AddLocationActivity;

/**
 * Custom Adapter, fetches data from Google Places API and binds it to the
 * AutoComplete field.
 * 
 * Code Reference -
 * https://developers.google.com/places/training/autocomplete-android
 */
public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable
{

	private static final String TAG = AddLocationActivity.class.getSimpleName();

	// -- constants
	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
	private static final String OUT_JSON = "/json";
	private static final String API_KEY = "AIzaSyDvJOL4SkxWsBQFyzMW_eh99dESG0rHe8o";

	// -- instance variables
	private List<String> resultList;

	public PlacesAutoCompleteAdapter(Context context, int textViewResourceId)
	{
		super(context, textViewResourceId);
	}

	@Override
	public int getCount()
	{
		return resultList.size();
	}

	@Override
	public String getItem(int index)
	{
		return resultList.get(index);
	}

	@Override
	public Filter getFilter()
	{
		Filter filter = new Filter()
		{

			@Override
			protected FilterResults performFiltering(CharSequence constraint)
			{
				FilterResults filterResults = new FilterResults();
				if (constraint != null)
				{
					// -- Retrieve the autocomplete results.
					resultList = autoComplete(constraint.toString());

					// -- Assign the data to the FilterResults
					filterResults.values = resultList;
					filterResults.count = resultList.size();
					Log.i(TAG, "result size - " + filterResults.count);
				}
				return filterResults;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results)
			{
				if (results != null && results.count > 0)
				{
					notifyDataSetChanged();
				}
				else
				{
					notifyDataSetInvalidated();
				}
			}
		};
		return filter;
	}

	/**
	 * This method calls the Google Places API and returns the results.
	 * 
	 * @param input
	 *            - The query string
	 * @return List containing the search results.
	 */
	private List<String> autoComplete(String input)
	{
		Log.i(TAG, "Entering autoComplete(), params - { input:" + input + " }");

		List<String> resultList = null;
		HttpURLConnection connection = null;
		StringBuilder jsonResults = new StringBuilder();
		try
		{
			StringBuilder strBuilder = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE
					+ OUT_JSON);
			strBuilder.append("?key=" + API_KEY);
			strBuilder.append("&input=" + URLEncoder.encode(input, "utf8"));

			URL url = new URL(strBuilder.toString());
			connection = (HttpURLConnection) url.openConnection();
			InputStreamReader in = new InputStreamReader(connection.getInputStream());

			// -- Load the results into a StringBuilder
			int read;
			char[] buff = new char[1024];
			while ((read = in.read(buff)) != -1)
			{
				jsonResults.append(buff, 0, read);
			}
		}
		catch (MalformedURLException mue)
		{
			Log.e(TAG, "MalformedURLException while processing Places API URL - "
					+ mue.getMessage());
			return resultList;
		}
		catch (IOException ioe)
		{
			Log.e(TAG, "IOException connecting to Places API - " + ioe.getMessage());
			return resultList;
		}
		finally
		{
			if (connection != null)
			{
				connection.disconnect();
			}
		}

		try
		{
			// -- Create a JSON object hierarchy from the results
			JSONObject jsonObj = new JSONObject(jsonResults.toString());
			JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

			// -- Extract the Place descriptions from the results
			resultList = new ArrayList<String>(predsJsonArray.length());
			for (int i = 0; i < predsJsonArray.length(); i++)
			{
				resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
			}
		}
		catch (JSONException jsone)
		{
			Log.e(TAG, "Cannot process JSON results - " + jsone.getMessage());
		}
		Log.i(TAG, "Exiting autoComplete() - " + resultList);
		return resultList;
	}
}