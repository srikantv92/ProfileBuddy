package com.se.profilebuddy.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.se.profilebuddy.R;

/**
 * 
 *
 */
public class SettingFragment extends Fragment
{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View settingView = inflater.inflate(R.layout.fragment_setting, container, false);
		return settingView;
	}
}
