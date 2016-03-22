package edu.uta.se.profilebuddy.object;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Object maps profile location details to Database.
 *
 */
public class ProfileLocation implements Parcelable
{

	private long id;
	private String title;
	private String address;
	private double latitude;
	private double longitude;
	private int mode;
	private long radius;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}

	public int getMode()
	{
		return mode;
	}

	public void setMode(int mode)
	{
		this.mode = mode;
	}

	public long getRadius()
	{
		return radius;
	}

	public void setRadius(long radius)
	{
		this.radius = radius;
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	public static final Parcelable.Creator<ProfileLocation> CREATOR = new Creator<ProfileLocation>()
	{

		public ProfileLocation createFromParcel(Parcel source)
		{
			ProfileLocation location = new ProfileLocation();
			location.id = source.readLong();
			location.title = source.readString();
			location.address = source.readString();
			location.latitude = source.readDouble();
			location.longitude = source.readDouble();
			location.mode = source.readInt();
			location.radius = source.readLong();

			return location;
		}

		public ProfileLocation[] newArray(int size)
		{
			return new ProfileLocation[size];
		}
	};

	@Override
	public void writeToParcel(Parcel parcel, int flags)
	{
		parcel.writeLong(id);
		parcel.writeString(title);
		parcel.writeString(address);
		parcel.writeDouble(latitude);
		parcel.writeDouble(longitude);
		parcel.writeInt(mode);
		parcel.writeLong(radius);

	}

	@Override
	public String toString()
	{
		return "Location [locationId:" + id + ", title:" + title + ", address:" + address
				+ ", latitude:" + latitude + ", longitude:" + longitude + ", mode:" + mode
				+ ", radius:" + radius + "]";
	}
}
