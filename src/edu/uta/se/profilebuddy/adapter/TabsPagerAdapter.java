package edu.uta.se.profilebuddy.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import edu.uta.se.profilebuddy.activity.EventFragment;
import edu.uta.se.profilebuddy.activity.LocationFragment;
import edu.uta.se.profilebuddy.activity.SettingFragment;

/**
 * Custom PagerAdapter to handle Tab and Swipe features.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter
{

	public TabsPagerAdapter(FragmentManager fragmentManager)
	{
		super(fragmentManager);
	}

	@Override
	public Fragment getItem(int index)
	{
		switch (index)
		{
		case 0:// -- Profile fragment
			return new LocationFragment();
		case 1: // -- Event fragment
			return new EventFragment();
		case 2: // -- Setting fragment
			return new SettingFragment();
		default:
		}
		return null;
	}

	@Override
	public int getCount()
	{
		return 3;
	}
}
