package edu.uta.se.profilebuddy.object;

/**
 * Object maps Event profile details to the database.
 */
public class CalendarEvent implements Comparable<CalendarEvent>
{

	private long id;
	private long foreignId;
	private long calendarId;
	private String title;
	private String description;
	private long startTime;
	private long endTime;
	private long duration;
	private int status;
	private boolean recursive;
	private int mode;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public long getForeignId()
	{
		return foreignId;
	}

	public void setForeignId(long foreignId)
	{
		this.foreignId = foreignId;
	}

	public long getCalendarId()
	{
		return calendarId;
	}

	public void setCalendarId(long calendarId)
	{
		this.calendarId = calendarId;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	public long getEndTime()
	{
		return endTime;
	}

	public void setEndTime(long endTime)
	{
		this.endTime = endTime;
	}

	public long getDuration()
	{
		return duration;
	}

	public void setDuration(long duration)
	{
		this.duration = duration;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public boolean isRecursive()
	{
		return recursive;
	}

	public void setRecursive(boolean recursive)
	{
		this.recursive = recursive;
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
		return "CalendarEvent [id=" + id + ", foreignId=" + foreignId + ", title=" + title
				+ ", startDate=" + startTime + ", endDate=" + endTime + ", duration=" + duration
				+ ", status=" + status + ", recursive=" + recursive + ", mode=" + mode + "]";
	}

	@Override
	public int compareTo(CalendarEvent another)
	{
		return (int) (this.foreignId - another.getForeignId());
	}

}
