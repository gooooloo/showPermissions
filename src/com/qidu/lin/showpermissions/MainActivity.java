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
import java.util.List;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class MainActivity extends Activity
{

	private final class myadapter implements ExpandableListAdapter
	{
		public myadapter()
		{
			this.keys = new String[permissionToPackagesMap.size()];
			int i = 0;
			for (String k : permissionToPackagesMap.keySet())
			{
				keys[i++] = k;
			}
		}

		String[] keys = null;

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
			return tv;
		}

		@Override
		public long getGroupId(int groupPosition)
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getGroupCount()
		{
			return keys.length;
		}

		@Override
		public Object getGroup(int groupPosition)
		{
			return permissionToPackagesMap.get(keys[groupPosition]);
		}

		@Override
		public long getCombinedGroupId(long groupId)
		{
			// TODO Auto-generated method stub
			return 0;
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
			return getPackageInfoList(groupPosition).size();
		}

		public List<PackageInfo> getPackageInfoList(int groupPosition)
		{
			return (List<PackageInfo>) getGroup(groupPosition);
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
			return tv;
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
			return getPackageInfoList(groupPosition).get(childPosition);
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

		init();

		this.hideGoogle = (CheckBox) this.findViewById(R.id.hideGoogle);
		this.androidPermissionOnly = (CheckBox) this.findViewById(R.id.androidPermissionsOnly);
		this.showPermissionName = (CheckBox) this.findViewById(R.id.showPermissionName);
		this.showPackageName = (CheckBox) this.findViewById(R.id.showPackageName);
		this.dangerousPermissionOnly = (CheckBox) this.findViewById(R.id.dangerousPermissionOnly);

		refresh(null);
	}

	public void refresh(View v)
	{
		StringBuffer string = new StringBuffer();

		for (Entry<String, List<PackageInfo>> entry : this.permissionToPackagesMap.entrySet())
		{
			if (shouldSkipPermission(entry))
			{
				continue;
			}

			StringBuffer sb2 = new StringBuffer();

			for (PackageInfo i : entry.getValue())
			{
				if (shouldSkipForHideGoogle(i))
				{
					continue;
				}
				sb2.append(" -- ");
				sb2.append(this.makeStringForPackage(pm, i));
				sb2.append("\n");
			}

			if (sb2.length() == 0)
			{
				continue;
			}
			string.append(makeStringForPermission(entry.getKey()));
			string.append("\n");
			string.append(sb2);
			string.append("\n");

		}

		TextView tv = (TextView) this.findViewById(R.id.tv);
		tv.setText(string);

		ExpandableListView lv = (ExpandableListView) findViewById(R.id.expandableListView1);
		ExpandableListAdapter adapter = new myadapter();
		lv.setAdapter(adapter);
	}

	private void init()
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

	private boolean shouldSkipPermission(Entry<String, List<PackageInfo>> entry)
	{
		return this.skipForAndroidPermissionsOnly(entry.getKey()) || this.shouldSkipForDangerousPermissionsOnly(entry.getKey());
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