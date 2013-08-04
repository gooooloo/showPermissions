/**
 * Copyright (C) 2012,2013 Qidu Lin
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qidu.lin.showpermissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;

public class MainActivity extends SlidingActivity
{

	private final class myadapter implements ExpandableListAdapter
	{
		public myadapter()
		{
			LinkedList<String> list = new LinkedList<String>();
			for (String key : permissionToPackagesMap.keySet())
			{

				if (shouldSkipPermission(key))
				{
					continue;
				}

				LinkedList<PackageInfo> piList = new LinkedList<PackageInfo>();
				for (PackageInfo appName : permissionToPackagesMap.get(key))
				{
					if (shouldSkipForHideGoogle(appName))
					{
						continue;
					}

					piList.add(appName);
				}

				if (piList.isEmpty())
				{
					continue;
				}

				PackageInfo s[] = piList.toArray(new PackageInfo[0]);
				amp.put(key, s);
				list.add(key);
			}

			keys = list.toArray(new String[0]);
		}

		String[] keys = null;

		HashMap<String, PackageInfo[]> amp = new HashMap<String, PackageInfo[]>();

		@Override
		public void unregisterDataSetObserver(DataSetObserver observer)
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void registerDataSetObserver(DataSetObserver observer)
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void onGroupExpanded(int groupPosition)
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void onGroupCollapsed(int groupPosition)
		{
			// TODO Auto-generated method stub
		}

		@Override
		public boolean isEmpty()
		{
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition)
		{
			return true;
		}

		@Override
		public boolean hasStableIds()
		{
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
		{

			TextView tv = null;
			if (convertView == null)
			{
				tv = new TextView(MainActivity.this);
			}
			else
			{
				tv = (TextView) convertView;
			}
			tv.setText(makeStringForPermission(keys[groupPosition]));
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			tv.setLayoutParams(lp);
			tv.setPadding(getGroupViewLeftPadding(), 0, 0, 0);
			tv.setTextSize(getGroupViewTextSize());
			tv.setMinHeight(getMinHeight());
			tv.setBackgroundResource(android.R.color.background_light);
			tv.setGravity(Gravity.CENTER_VERTICAL);
			return tv;
		}

		public int getGroupViewTextSize()
		{
			return 18;
		}

		public int getChildViewTextSize()
		{
			return 16;
		}

		public int getGroupViewLeftPadding()
		{
			return 70;
		}

		public int getChildViewLeftPadding()
		{
			return 100;
		}

		@Override
		public long getGroupId(int groupPosition)
		{
			return groupPosition;
		}

		@Override
		public int getGroupCount()
		{
			return keys.length;
		}

		@Override
		public Object getGroup(int groupPosition)
		{
			return amp.get(keys[groupPosition]);
		}

		@Override
		public long getCombinedGroupId(long groupPosition)
		{
			return groupPosition;
		}

		@Override
		public long getCombinedChildId(long groupId, long childId)
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getChildrenCount(int groupPosition)
		{
			return getPackageInfoList(groupPosition).length;
		}

		public PackageInfo[] getPackageInfoList(int groupPosition)
		{
			return (PackageInfo[]) getGroup(groupPosition);
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
		{
			TextView tv = null;
			if (convertView == null)
			{
				tv = new TextView(MainActivity.this);
			}
			else
			{
				tv = (TextView) convertView;
			}
			tv.setText(makeStringForPackage(pm, getPackageInfo(groupPosition, childPosition)));
			tv.setTextSize(getChildViewTextSize());
			tv.setPadding(getChildViewLeftPadding(), 0, 0, 0);
			tv.setMinHeight(getMinHeight());

			tv.setGravity(Gravity.CENTER_VERTICAL);
			return tv;
		}

		public int getMinHeight()
		{
			return 80;
		}

		public PackageInfo getPackageInfo(int groupPosition, int childPosition)
		{
			return (PackageInfo) getChild(groupPosition, childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition)
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition)
		{
			return getPackageInfoList(groupPosition)[childPosition];
		}

		@Override
		public boolean areAllItemsEnabled()
		{
			// TODO Auto-generated method stub
			return false;
		}
	}

	HashMap<String, List<PackageInfo>> permissionToPackagesMap = new HashMap<String, List<PackageInfo>>();
	private PackageManager pm;
	private CheckBox hideGoogle;
	private CheckBox androidPermissionOnly;
	private CheckBox showPermissionName;
	private CheckBox showPackageName;
	private CheckBox dangerousPermissionOnly;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setBehindContentView(R.layout.activity_behind);

		initSlidingMenu();
		initData();

		this.hideGoogle = (CheckBox) this.findViewById(R.id.hideGoogle);
		this.androidPermissionOnly = (CheckBox) this.findViewById(R.id.androidPermissionsOnly);
		this.showPermissionName = (CheckBox) this.findViewById(R.id.showPermissionName);
		this.showPackageName = (CheckBox) this.findViewById(R.id.showPackageName);
		this.dangerousPermissionOnly = (CheckBox) this.findViewById(R.id.dangerousPermissionOnly);

		refresh(null);
	}

	public void initSlidingMenu()
	{
		SlidingMenu slidingMenu = getSlidingMenu();
		slidingMenu.setBehindWidth(300);
		slidingMenu.setShadowWidth(2);
		slidingMenu.setShadowDrawable(android.R.color.darker_gray);

	}

	public void refresh(View v)
	{
		ExpandableListView lv = (ExpandableListView) findViewById(R.id.expandableListView1);
		ExpandableListAdapter adapter = new myadapter();
		lv.setAdapter(adapter);
	}

	private void initData()
	{
		pm = this.getPackageManager();
		List<PackageInfo> allPackages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
		for (PackageInfo packageInfo : allPackages)
		{
			if (packageInfo == null || packageInfo.requestedPermissions == null)
			{
				continue;
			}
			for (String permission : packageInfo.requestedPermissions)
			{
				List<PackageInfo> as = null;
				if (this.permissionToPackagesMap.containsKey(permission))
				{
					as = this.permissionToPackagesMap.get(permission);
				}
				else
				{
					as = new ArrayList<PackageInfo>();
				}

				as.add(packageInfo);
				this.permissionToPackagesMap.put(permission, as);
			}
		}
	}

	private boolean shouldSkipPermission(String permission)
	{
		return this.skipForAndroidPermissionsOnly(permission) || this.shouldSkipForDangerousPermissionsOnly(permission);
	}

	private boolean shouldSkipForHideGoogle(PackageInfo i)
	{
		if (!this.hideGoogle.isChecked())
		{
			return false;
		}
		return i.packageName.startsWith("com.google") || i.packageName.startsWith("com.android");
	}

	private boolean shouldSkipForDangerousPermissionsOnly(String permission)
	{
		if (!this.dangerousPermissionOnly.isChecked())
		{
			return false;
		}
		boolean dangerous = isSpecificPermissionLevel(permission, PermissionInfo.PROTECTION_DANGEROUS)
				|| isSpecificPermissionLevel(permission, PermissionInfo.PROTECTION_SIGNATURE)
				|| isSpecificPermissionLevel(permission, PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM);
		return !dangerous;
	}

	private boolean isSpecificPermissionLevel(String permission, int level)
	{
		try
		{
			return pm.getPermissionInfo(permission, 0).protectionLevel == level;
		}
		catch (NameNotFoundException e)
		{
			return false;
		}
	}

	private boolean skipForAndroidPermissionsOnly(String permission)
	{
		if (!this.androidPermissionOnly.isChecked())
		{
			return false;
		}
		return !permission.startsWith("android.permission.");
	}

	private String makeStringForPermission(String permission)
	{
		try
		{
			String s = pm.getPermissionInfo(permission, 0).loadLabel(pm).toString();
			if (this.showPermissionName.isChecked())
			{
				s += "(" + permission + ")";
			}
			return s;
		}
		catch (NameNotFoundException e)
		{
			return permission;
		}
	}

	private String makeStringForPackage(PackageManager pm, PackageInfo packageInfo)
	{
		String s = packageInfo.applicationInfo.loadLabel(pm).toString();
		if (this.showPackageName.isChecked())
		{
			s += " (" + packageInfo.packageName + ")";
		}
		return s;
	}
}
