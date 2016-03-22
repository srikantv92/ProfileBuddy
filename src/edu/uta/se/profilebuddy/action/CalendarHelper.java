package edu.uta.se.profilebuddy.action;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import edu.uta.se.profilebuddy.R;
import edu.uta.se.profilebuddy.activity.MainActivity;
import edu.uta.se.profilebuddy.database.ManageEventDB;
import edu.uta.se.profilebuddy.database.ManageNotificationDB;
import edu.uta.se.profilebuddy.object.CalendarEvent;
import edu.uta.se.profilebuddy.service.EventService;

/**
 * Supporting class for Calendar Service. Provides methods to Google Calendar
 * events to local database.
 */
public class CalendarHelper
{

	private static final String TAG = CalendarHelper.class.getSimpleName();

	public static final String[] CALENDAR_COLS = new String[] { Calendars._ID,
			Calendars.ACCOUNT_NAME, Calendars.CALENDAR_DISPLAY_NAME, Calendars.OWNER_ACCOUNT,
			Calendars.IS_PRIMARY };

	public static final String[] EVENT_COLS = new String[] { Events._ID, Events.CALENDAR_ID,
			Events.TITLE, Events.DESCRIPTION, Events.DTSTART, Events.DTEND, Events.DURATION,
			Events.RRULE, Events.LAST_DATE };

	// -- The indices for the projection array above.
	private static final int CALENDAR_ID_IX = 0;
	private static final int CALENDAR_ACCOUNT_NAME_IX = 1;
	private static final int CALENDAR_DISPLAY_NAME_IX = 2;
	private static final int CALENDAR_OWNER_ACCOUNT_IX = 3;
	private static final int CALENDAR_IS_PRIMARY_IX = 4;

	private static final int EVENT_ID_IX = 0;
	private static final int EVENT_CALENDAR_ID_IX = 1;
	private static final int EVENT_TITLE_IX = 2;
	private static final int EVENT_DESCRIPTION_IX = 3;
	private static final int EVENT_DTSTART_IX = 4;
	private static final int EVENT_DTEND_IX = 5;
	private static final int EVENT_DURATION_IX = 6;
	private static final int EVENT_RRULE_IX = 7;
	private static final int EVENT_LAST_DATE_IX = 8;

	public static final String ACTION_NO_MODE = "edu.uta.se.profilebuddy.notification.action.NO_MODE";
	public static final String ACTION_SILENT = "edu.uta.se.profilebuddy.notification.action.SILENT";
	public static final String ACTION_VIBRATE = "edu.uta.se.profilebuddy.notification.action.VIBRATE";
	public static final String ACTION_NORMAL = "edu.uta.se.profilebuddy.notification.action.NORMAL";
	public static final String ACTION_DELETE = "edu.uta.se.profilebuddy.notification.action.DELETE";

	public static final String EVENT_ID = "edu.uta.se.profilebuddy.CalendarHelper.eventId";
	public static final String NOTIFICATION_ID = "edu.uta.se.profilebuddy.CalendarHelper.notificationId";

	// -- singleton instance
	private static volatile CalendarHelper instance;

	// -- Instance variables
	private Context context;
	private ContentResolver contentResolver;
	private long primaryCalendarId;
	private List<CalendarEvent> localEventCache;
	private ManageEventDB eventDB;
	private ManageNotificationDB notificationDB;

	private CalendarHelper(Context context)
	{
		this.context = context;
		contentResolver = this.context.getContentResolver();
		this.eventDB = new ManageEventDB(context);
		this.notificationDB = new ManageNotificationDB(context);
	}

	/**
	 * Singleton object implementation
	 * 
	 * @param context
	 *            - {@link Context}
	 * @return singleton instance of {@link CalendarHelper}
	 */
	public static CalendarHelper getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new CalendarHelper(context);
		}
		return instance;
	}

	/**
	 * Entry point for the sync feature.
	 */
	public void syncCalendar()
	{
		Log.i(TAG, "Entering syncCalendar");

		if (this.primaryCalendarId == 0)
		{
			this.fetchPrimaryCalendar();
		}
		this.cacheLocalEvents();
		List<CalendarEvent> events = this.readCalendarEvents();
		Set<CalendarEvent> newEvents = this.findNewEvents(events);
		this.saveNewEvents(newEvents);
		this.pushNotification(newEvents);

		Set<CalendarEvent> deletedEvents = this.findDeletedEvents(events);
		this.removeDeletedEvents(deletedEvents);

		Log.i(TAG, "Exiting syncCalendar");
	}

	/**
	 * Removes events from local database, which were found to removed from
	 * Google Calendar.
	 * 
	 * @param deletedEvents
	 *            - Set<{@link CalendarEvent}>
	 */
	private void removeDeletedEvents(Set<CalendarEvent> deletedEvents)
	{
		this.eventDB.open();
		for (CalendarEvent event : deletedEvents)
		{
			checkActiveEvent(event.getId());
			event.setStatus(-1);
			this.eventDB.updateEvent(event);
		}
		this.eventDB.close();
	}

	/**
	 * @param eventId
	 */
	private void checkActiveEvent(long eventId)
	{
		if (EventService.ACTIVE && EventService.ACTIVE_EVENT == eventId)
		{
			Intent profileIntent = new Intent(context, EventService.class);
			context.startService(profileIntent);
		}
	}

	/**
	 * Finds events that were removed from Google Calendar.
	 * 
	 * @param foreignEvents
	 *            - The list of Google Calendar events.
	 * @return - The Set<{@link CalendarEvent}> of events to be deleted.
	 */
	private Set<CalendarEvent> findDeletedEvents(List<CalendarEvent> foreignEvents)
	{
		Log.d(TAG, "Entering  findDeletedEvents");
		Set<CalendarEvent> foreignActiveEvents = new TreeSet<CalendarEvent>();
		for (CalendarEvent event : foreignEvents)
		{
			if (event.getStatus() == 1)
			{
				foreignActiveEvents.add(event);
			}
		}
		Set<CalendarEvent> deletedEvents = new TreeSet<CalendarEvent>(
				new Comparator<CalendarEvent>()
				{

					@Override
					public int compare(CalendarEvent lhs, CalendarEvent rhs)
					{
						return (int) (lhs.getForeignId() - rhs.getForeignId());
					}
				});
		for (CalendarEvent event : this.localEventCache)
		{
			deletedEvents.add(event);
		}
		deletedEvents.removeAll(foreignActiveEvents);
		Log.d(TAG, "Exiting findDeletedEvents, record count - " + deletedEvents.size());
		return deletedEvents;
	}

	/**
	 * Saves the new events identified to the database.
	 * 
	 * @param newEvents
	 *            - The Set<{@link CalendarEvent}> of new events.
	 */
	private void saveNewEvents(Set<CalendarEvent> newEvents)
	{
		Log.i(TAG, "Entering saveNewEvents");

		this.eventDB.open();
		for (CalendarEvent event : newEvents)
		{
			this.eventDB.createEvent(event);
		}
		this.eventDB.close();

		Log.i(TAG, "Entering saveNewEvents");
	}

	/**
	 * Fetches all active events from local database.
	 */
	private void cacheLocalEvents()
	{
		Log.i(TAG, "Entering cacheLocalEvents");

		this.eventDB.open();
		this.localEventCache = eventDB.fetchEvents(true, primaryCalendarId);
		this.eventDB.close();

		Log.i(TAG, "Exiting cacheLocalEvents");
	}

	/**
	 * Finds the Primary calendar id from the Google Calendar.
	 */
	private void fetchPrimaryCalendar()
	{
		Log.i(TAG, "Entering fetchPrimaryCalendar");

		Uri uri = Calendars.CONTENT_URI;
		Cursor cursor = contentResolver.query(uri, CALENDAR_COLS, null, null, null);
		Log.d(TAG, "Result count - " + cursor.getCount());

		while (cursor.moveToNext())
		{
			Log.d(TAG, "Calendar - { id: " + cursor.getString(CALENDAR_ID_IX) + ", accountName: "
					+ cursor.getString(CALENDAR_ACCOUNT_NAME_IX) + ", displayName: "
					+ cursor.getString(CALENDAR_DISPLAY_NAME_IX) + ", ownerAccount: "
					+ cursor.getString(CALENDAR_OWNER_ACCOUNT_IX) + ", isPrimary: "
					+ cursor.getString(CALENDAR_IS_PRIMARY_IX) + " }");
			if ("1".equalsIgnoreCase(cursor.getString(CALENDAR_IS_PRIMARY_IX)))
			{
				this.primaryCalendarId = cursor.getLong(CALENDAR_ID_IX);
				break;
			}
		}
		cursor.close();

		Log.i(TAG, "Exiting fetchPrimaryCalendar");
	}

	/**
	 * Fetches all the events related to the Primary Calendar.
	 * 
	 * @return - The List<{@link CalendarEvent}>
	 */
	private List<CalendarEvent> readCalendarEvents()
	{
		Log.i(TAG, "Entering readEvents");

		List<CalendarEvent> events = null;
		Uri uri = Events.CONTENT_URI;
		String selection = Events.CALENDAR_ID + " = ? ";
		String selectionArgs[] = new String[] { String.valueOf(this.primaryCalendarId) };
		Cursor cursor = contentResolver.query(uri, EVENT_COLS, selection, selectionArgs, null);

		events = new ArrayList<CalendarEvent>();
		CalendarEvent event;
		long now = System.currentTimeMillis();
		while (cursor.moveToNext())
		{
			event = new CalendarEvent();
			event.setForeignId(cursor.getLong(EVENT_ID_IX));
			event.setCalendarId(cursor.getInt(EVENT_CALENDAR_ID_IX));
			event.setTitle(cursor.getString(EVENT_TITLE_IX));
			event.setDescription(cursor.getString(EVENT_DESCRIPTION_IX));
			event.setStartTime(cursor.getLong(EVENT_DTSTART_IX));
			event.setEndTime(cursor.isNull(EVENT_DTEND_IX) ? 0 : cursor.getLong(EVENT_DTEND_IX));
			event.setDuration(cursor.isNull(EVENT_DURATION_IX) ? 0 : cursor
					.getLong(EVENT_DURATION_IX));
			event.setStatus(cursor.getLong(EVENT_LAST_DATE_IX) < now ? 0 : 1);
			event.setRecursive(cursor.isNull(EVENT_RRULE_IX) ? false : true);

			events.add(event);
			Log.d(TAG, event.toString());
		}
		cursor.close();

		Log.i(TAG, "Exiting readEvents");
		return events;
	}

	/**
	 * Finds new events added to the Primary calendar.
	 * 
	 * @param foreignEvents
	 *            - The list of events.
	 * @return - The set of events.
	 */
	private Set<CalendarEvent> findNewEvents(List<CalendarEvent> foreignEvents)
	{
		Log.d(TAG, "Entering  findNewEvents");
		Set<CalendarEvent> localEvents = new TreeSet<CalendarEvent>();
		localEvents.addAll(this.localEventCache);

		Set<CalendarEvent> newEvents = new TreeSet<CalendarEvent>(new Comparator<CalendarEvent>()
		{

			@Override
			public int compare(CalendarEvent lhs, CalendarEvent rhs)
			{
				return (int) (lhs.getForeignId() - rhs.getForeignId());
			}
		});
		for (CalendarEvent event : foreignEvents)
		{
			if (event.getStatus() == 1)
			{
				newEvents.add(event);
			}
		}
		newEvents.removeAll(localEvents);
		Log.d(TAG, "Exiting findNewEvents, record count - " + newEvents.size());
		return newEvents;
	}

	/**
	 * Utility method to show notifications whenever new events are created.
	 * Notification is provided with actions to set profile directly.
	 * 
	 * @param events
	 *            - The Set<{@link CalendarEvent}>
	 */
	private void pushNotification(Set<CalendarEvent> events)
	{
		for (CalendarEvent event : events)
		{
			int notificationId = this.createNotification(event.getId());

			NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context)
					.setSmallIcon(R.drawable.ic_action_event).setContentTitle("Profile Buddy")
					.setContentText("A new event \"" + event.getTitle() + "\" was added!")
					.setStyle(
							new NotificationCompat.BigTextStyle().bigText("A new event \""
									+ event.getTitle() + "\" was added!\n"
									+ "\nPlease set the mode for this event.")).setAutoCancel(true)
					.setPriority(Notification.PRIORITY_MAX);

			Bundle bundle = new Bundle();
			bundle.putLong(EVENT_ID, event.getId());
			bundle.putInt(NOTIFICATION_ID, notificationId);

			// -- Silent intent
			Intent silent = new Intent(ACTION_SILENT).putExtras(bundle);
			PendingIntent silentPendingIntent = PendingIntent.getBroadcast(this.context, 0, silent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			builder.addAction(0, "SILENT", silentPendingIntent);

			// -- Vibrate intent
			Intent vibrate = new Intent(ACTION_VIBRATE).putExtras(bundle);
			PendingIntent vibratePendingIntent = PendingIntent.getBroadcast(this.context, 0,
					vibrate, PendingIntent.FLAG_UPDATE_CURRENT);
			builder.addAction(0, "VIBRATE", vibratePendingIntent);

			// -- Normal intent
			Intent normal = new Intent(ACTION_NORMAL).putExtras(bundle);
			PendingIntent normalPendingIntent = PendingIntent.getBroadcast(this.context, 0, normal,
					PendingIntent.FLAG_UPDATE_CURRENT);
			builder.addAction(0, "NORMAL", normalPendingIntent);

			Intent delete = new Intent(ACTION_DELETE).putExtras(bundle);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this.context, 0, delete,
					PendingIntent.FLAG_UPDATE_CURRENT);
			builder.setDeleteIntent(pendingIntent);

			// -- Creates an explicit intent for an Activity in your app
			Intent resultIntent = new Intent(this.context, MainActivity.class);
			resultIntent.setAction("OPEN_EVENT_TAB");

			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.context);
			// -- Adds the back stack for the Intent (but not the Intent itself)
			stackBuilder.addParentStack(MainActivity.class);
			// -- Adds the Intent that starts the Activity to the top of the
			// stack
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
					PendingIntent.FLAG_UPDATE_CURRENT);
			builder.setContentIntent(resultPendingIntent);
			NotificationManager notificationManager = (NotificationManager) this.context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			// -- id allows you to update the notification later on.
			notificationManager.notify(notificationId, builder.build());
		}
	}

	/**
	 * Creates notification in database for an event.
	 * 
	 * @param eventId
	 *            - The id of the event for which notification is created.
	 * @return - The notificationId.
	 */
	private int createNotification(long eventId)
	{
		Log.i(TAG, "Entering createNotification - params { eventId:" + eventId + " }");

		notificationDB.open();
		int notificationId = (int) notificationDB.createNotification(eventId);
		notificationDB.close();

		Log.i(TAG, "Exiting createNotification, notificationId - " + notificationId);
		return notificationId;
	}
}
