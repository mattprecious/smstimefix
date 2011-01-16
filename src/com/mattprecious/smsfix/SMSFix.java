/*
 * Copyright 2011 Matthew Precious
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mattprecious.smsfix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SMSFix extends PreferenceActivity {
	
	private SharedPreferences	settings;
	private ListPreference		offsetMethod;
	private EditTextPreference	editOffset;
	private CheckBoxPreference	cdmaBox;
	
	static final int MENU_HELP_ID		= 0;
	static final int MENU_ABOUT_ID		= 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
		
		settings		= ((PreferenceScreen)	findPreference("preferences")).getSharedPreferences();
		offsetMethod	= (ListPreference)		findPreference("offset_method");
		editOffset		= (EditTextPreference)	findPreference("offset");
		cdmaBox			= (CheckBoxPreference)	findPreference("cdma");
		
		settings.edit()
			.putBoolean("active", FixService.running)
			.commit();
		
		settings.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (key.equals("active")) {
					toggleService(sharedPreferences.getBoolean(key, false));
				}
				
				toggleOffset();
				toggleCDMA();
			}
		});
		
		editOffset.getEditText()
			.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		
		toggleOffset();
		toggleCDMA();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, MENU_HELP_ID, 0, R.string.menu_help)
			.setIcon(android.R.drawable.ic_menu_help);
		
		menu.add(0, MENU_ABOUT_ID, 0, R.string.menu_about)
			.setIcon(android.R.drawable.ic_menu_info_details);
		
        return true;
	}

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case MENU_HELP_ID:
        	startActivity(new Intent(SMSFix.this, Help.class));
            return true;
        
        case MENU_ABOUT_ID:
        	startActivity(new Intent(SMSFix.this, About.class));
            return true;
        }
       
        return super.onMenuItemSelected(featureId, item);
    }
	
	public void toggleService(boolean active) {
		if (active) {
	        startService(new Intent(this, FixService.class));
		} else {
		    stopService(new Intent(this, FixService.class));
		}
	}
	
	public void toggleOffset() {
		editOffset.setEnabled(offsetMethod.getValue().equals("manual") && offsetMethod.isEnabled());
	}
	
	public void toggleCDMA() {
		cdmaBox.setEnabled(!offsetMethod.getValue().equals("phone") && offsetMethod.isEnabled());
	}
	
}
