package edu.uta.se.profilebuddy.activity;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import edu.uta.se.profilebuddy.R;

import edu.uta.se.profilebuddy.adapter.TabsPagerAdapter;
import edu.uta.se.profilebuddy.service.LocationService;
import edu.uta.se.profilebuddy.service.LocationService.ServiceState;

/**
 * Starting activity of the application. Initializes Service.
 */
public class MainActivity extends FragmentActivity implements TabListener
{

	private static final String TAG = MainActivity.class.getSimpleName();

	private static final String INVOKE_SERVICE = "edu.uta.se.profilebuddy.activity.action.INVOKE_SERVICE";

	// -- static
	public static Context appContext;

	// -- views
	private ActionBar actionBar;
	private ViewPager viewPager;
	private TabsPagerAdapter pagerAdapter;

	// -- Tab titles
	private String[] tabs = { "Locations", "Events", "Settings" };

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		appContext = getApplicationContext();

		// -- Service start
		Log.i(TAG, "Calling service " + LocationService.SERV_STATE);
		if (LocationService.SERV_STATE == ServiceState.OFF)
		{
			Intent receiverIntent = new Intent();
			receiverIntent.setAction(INVOKE_SERVICE);
			sendBroadcast(receiverIntent);
		}
		if (LocationService.lastLocation != null)
		{
			triggerChangeLocation();
		}
		// -- Initialization
		actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		initializeViewPager();
	}

	public static void triggerChangeLocation()
	{
		// -- Trigger location change once in Main
		if (LocationService.lastLocation != null)
		{
			Log.i(TAG, "Calling Location service trigger");
			LocationService.thisObject.onLocationChanged(LocationService.lastLocation);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// -- Inflate the menu; this adds items to the action bar if it is
		// present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// -- Handle action bar item clicks here. The action bar will
		// -- automatically handle clicks on the Home/Up button, so long
		// -- as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}

	private void initializeViewPager()
	{
		pagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(pagerAdapter);

		// -- Adding Tabs
		for (String tabName : tabs)
		{
			actionBar.addTab(actionBar.newTab().setText(tabName).setTabListener(this));
		}

		// -- on swiping the viewpager make respective tab selected
		viewPager.setOnPageChangeListener(new OnPageChangeListener()
		{

			@Override
			public void onPageSelected(int position)
			{
				// -- on changing the page make respected tab selected
				actionBar.setSelectedNavigationItem(position);
				switch (position)
				{
				case 0:
					LocationFragment locationFragment = (LocationFragment) viewPager.getAdapter()
							.instantiateItem(viewPager, viewPager.getCurrentItem());
					locationFragment.intializeTextView();
					break;
				default:
					break;
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2)
			{
			}

			@Override
			public void onPageScrollStateChanged(int arg0)
			{
			}
		});
		String action = getIntent().getAction();
		if (action != null && action.equalsIgnoreCase("OPEN_EVENT_TAB"))
		{
			actionBar.setSelectedNavigationItem(1);
		}
	}

	// -- Viewpager events
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft)
	{
		// -- on tab selected show respected fragment view
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft)
	{
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft)
	{
	}

	@Override
	public void onBackPressed()
	{
		moveTaskToBack(true);
	}
}
