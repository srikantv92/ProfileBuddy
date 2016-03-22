package edu.uta.se.profilebuddy.object;

/**
 * Object stores details of next Event profile to be activated.
 */
public class EventProfile
{

	private long eventId;
	private long timeToProfile;
	private long endTime;
	private int mode;

	public long getEventId()
	{
		return eventId;
	}

	public void setEventId(long eventId)
	{
		this.eventId = eventId;
	}

	public long getTimeToProfile()
	{
		return timeToProfile;
	}

	public void setTimeToProfile(long timeToProfile)
	{
		this.timeToProfile = timeToProfile;
	}

	public long getEndTime()
	{
		return endTime;
	}

	public void setEndTime(long endTime)
	{
		this.endTime = endTime;
	}

	public int getMode()
	{
		return mode;
	}

	public void setMode(int mode)
	{
		this.mode = mode;
	}

	@Override
	public String toString()
	{
		return "EventProfile [eventId=" + eventId + ", timeToProfile=" + timeToProfile
				+ ", endTime=" + endTime + ", mode=" + mode + "]";
	}

}
