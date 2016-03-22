package edu.uta.se.profilebuddy.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import edu.uta.se.profilebuddy.object.ProfileLocation;

/**
 * Database object for the ProfileLocation feature of the application. Provides
 * CRUD methods required for managing location objects.
 */
public class ManageLocationDB
{

	private static final String TAG = ManageLocationDB.class.getSimpleName();

	// -- Database objects
	private SQLiteDatabase database;
	private ProfileDBHelper profileDBHelper;

	// -- Location table columns
	public static final String TABLE_LOCATION = "T_LOCATION";
	public static final String LOCATION_ID = "_id";
	public static final String LOCATION_TITLE = "TITLE";
	public static final String LOCATION_ADDRESS = "ADDRESS";
	public static final String LOCATION_LATITUDE = "LATITUDE";
	public static final String LOCATION_LONGITUDE = "LONGITUDE";
	public static final String LOCATION_MODE = "MODE";
	public static final String LOCATION_RADIUS = "RADIUS";

	private String[] locationColumns = { LOCATION_ID, LOCATION_TITLE, LOCATION_ADDRESS,
			LOCATION_LATITUDE, LOCATION_LONGITUDE, LOCATION_MODE, LOCATION_RADIUS };

	public ManageLocationDB(Context context)
	{
		profileDBHelper = new ProfileDBHelper(context);
	}

	public void open() throws SQLException
	{
		database = profileDBHelper.getWritableDatabase();
	}

	public void close()
	{
		database.close();
	}

	/**
	 * Method to create a new location profile in the database.
	 * 
	 * @param location
	 *            - {@link ProfileLocation}
	 */
	public void createProfile(ProfileLocation location)
	{
		Log.i(TAG, "Entering createProfile(), params - " + location);
		ContentValues values = new ContentValues();
		values.put(LOCATION_TITLE, location.getTitle());
		values.put(LOCATION_ADDRESS, location.getAddress());
		values.put(LOCATION_LATITUDE, location.getLatitude());
		values.put(LOCATION_LONGITUDE, location.getLongitude());
		values.put(LOCATION_MODE, location.getMode());
		values.put(LOCATION_RADIUS, location.getRadius());

		long insertId = database.insert(TABLE_LOCATION, null, values);
		Log.i(TAG, "Exiting createProfile(), id after row insertion - " + insertId);
	}

	/**
	 * Method to fetch the details of a given location id.
	 * 
	 * @param locationId
	 *            - The id of the location
	 * @return {@link ProfileLocation}
	 */
	public ProfileLocation fetchLocation(long locationId)
	{
		Log.i(TAG, "Entering fetchLocation(), params - { locationId:" + locationId + " }");
		ProfileLocation location = null;

		Cursor cursor = database.query(TABLE_LOCATION, locationColumns, LOCATION_ID + " = "
				+ locationId, null, null, null, null);
		cursor.moveToFirst();
		if (!cursor.isAfterLast())
		{
			location = cursorToLocation(cursor);
		}
		cursor.close();
		Log.i(TAG, "Exiting fetchLocation() - " + location);
		return location;
	}

	/**
	 * DB method to update location details for given location id.
	 * 
	 * @param location
	 *            - {@link ProfileLocation}
	 */
	public void updateLocation(ProfileLocation location)
	{
		Log.i(TAG, "Entering updateLocation(), params - " + location);
		ContentValues values = new ContentValues();
		values.put(LOCATION_TITLE, location.getTitle());
		values.put(LOCATION_ADDRESS, location.getAddress());
		values.put(LOCATION_LATITUDE, location.getLatitude());
		values.put(LOCATION_LONGITUDE, location.getLongitude());
		values.put(LOCATION_MODE, location.getMode());
		values.put(LOCATION_RADIUS, location.getRadius());

		int rows = database.update(TABLE_LOCATION, values, LOCATION_ID + " = " + location.getId(),
				null);
		Log.i(TAG, "Exiting updateLocation(), rows updated - " + rows);
	}

	/**
	 * Method to delete a location profile row.
	 * 
	 * @param locationId
	 *            - The id of the location to be deleted.
	 */
	public void deleteLocation(long locationId)
	{
		Log.i(TAG, "Entering deleteLocation(), params - { locationId:" + locationId + " }");
		int rows = database.delete(TABLE_LOCATION, LOCATION_ID + " = " + locationId, null);
		Log.i(TAG, "Exiting deleteLocation(), rows deleted - " + rows);
	}

	public void updateMode(long locationId, int mode)
	{
		Log.i(TAG, "Entering updateMode(), params - { locationId: " + locationId + ", mode:" + mode
				+ " }");
		ContentValues values = new ContentValues();
		values.put(LOCATION_MODE, mode);
		int rows = database.update(TABLE_LOCATION, values, LOCATION_ID + " = " + locationId, null);
		Log.i(TAG, "Exiting updateMode(), rows updated - " + rows);
	}

	/**
	 * Method to get a cursor to the all the entries in the Location table
	 * 
	 * @return Cursor to the entire Location table.
	 */
	public Cursor getLocationsCursor()
	{
		Log.i(TAG, "Entering  getLocationsCursor");
		Cursor cursor = database.query(TABLE_LOCATION, locationColumns, null, null, null, null,
				null);
		Log.i(TAG, "Exiting  getLocationsCursor");
		return cursor;
	}

	/**
	 * Method to return all stored location profiles in the DB
	 * 
	 * @return List<{@link ProfileLocation}>
	 */
	public List<ProfileLocation> fetchLocations()
	{
		Log.i(TAG, "Entering  fetchLocations");

		List<ProfileLocation> locations;
		Cursor cursor = database.query(TABLE_LOCATION, locationColumns, null, null, null, null,
				null);

		locations = new ArrayList<ProfileLocation>();
		ProfileLocation location = null;
		cursor.moveToFirst();
		while (!cursor.isAfterLast())
		{
			location = cursorToLocation(cursor);
			locations.add(location);
			cursor.moveToNext();
		}
		// -- closing cursor
		cursor.close();

		Log.i(TAG, "Exiting fetchLocations, rows fetched - " + locations.size());
		return locations;
	}

	/**
	 * Utility method to convert a cursor row to {@link ProfileLocation} object
	 * 
	 * @param cursor
	 *            - A single cursor row
	 * @return {@link ProfileLocation}
	 */
	private ProfileLocation cursorToLocation(Cursor cursor)
	{
		ProfileLocation location = new ProfileLocation();
		Log.i(TAG, "cursor id_" + cursor.getLong(0));
		location.setId(cursor.getLong(0));
		location.setTitle(cursor.getString(1));
		location.setAddress(cursor.getString(2));
		location.setLatitude(cursor.getDouble(3));
		location.setLongitude(cursor.getDouble(4));
		location.setMode(cursor.getInt(5));
		location.setRadius(cursor.getLong(6));

		return location;
	}
}
