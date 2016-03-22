package edu.uta.se.profilebuddy.object;

/**
 * Object maps notification details to database.
 */
public class Notification
{

	private int id;
	private long eventId;
	private int status;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public long getEventId()
	{
		return eventId;
	}

	public void setEventId(long eventId)
	{
		this.eventId = eventId;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	@Override
	public String toString()
	{
		return "Notification [id=" + id + ", eventId=" + eventId + ", status=" + status + "]";
	}

}
