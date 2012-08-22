/**
 * Copyright (C) 2012 Qidu Lin
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
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class MainActivity extends Activity
{

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
