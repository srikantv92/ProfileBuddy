package edu.uta.se.profilebuddy.object;

/**
 * Object holds event data to be displayed to the User.
 */
public class EventInfo
{

	private String title;
	private String description;
	private long startTime;
	private long endTime;
	private long duration;
	private boolean recursive;
	private String frequency;

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

	public boolean isRecursive()
	{
		return recursive;
	}

	public void setRecursive(boolean recursive)
	{
		this.recursive = recursive;
	}

	public String getFrequency()
	{
		return frequency;
	}

	public void setFrequency(String frequency)
	{
		this.frequency = frequency;
	}

	@Override
	public String toString()
	{
		return "EventInfo [title=" + title + ", description=" + description + ", startTime="
				+ startTime + ", endTime=" + endTime + ", duration=" + duration + ", recursive="
				+ recursive + ", frequency=" + frequency + "]";
	}

}
