/*
 * Copyright 2011 Matthew Precious
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mattprecious.smsfix.library;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.InputType;

/**
 * SMS Time Fix main activity window
 * 
 * @author Matthew Precious
 * 
 */
public class SMSFix extends PreferenceActivity {
    static boolean donated = false;
    
    private final String PROPERTIES_FILE = "main.properties";

    private SharedPreferences settings;
    private CheckBoxPreference activeBox;
    private ListPreference offsetMethod;
    private EditTextPreference editOffsetHours;
    private EditTextPreference editOffsetMinutes;
    private CheckBoxPreference cdmaBox;
    private CheckBoxPreference notify;
    private ListPreference notifyIcon;
    
    private PreferenceCategory more;
    private Preference donate;
    private Preference help;
    private Preference about;

    private OnSharedPreferenceChangeListener prefListener;
    
    static final int DIALOG_DONATE_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        settings = ((PreferenceScreen) findPreference("preferences")).getSharedPreferences();
        activeBox = (CheckBoxPreference) findPreference("active");
        offsetMethod = (ListPreference) findPreference("offset_method");
        editOffsetHours = (EditTextPreference) findPreference("offset_hours");
        editOffsetMinutes = (EditTextPreference) findPreference("offset_minutes");
        cdmaBox = (CheckBoxPreference) findPreference("cdma");
        notify = (CheckBoxPreference) findPreference("notify");
        notifyIcon = (ListPreference) findPreference("notify_icon");
        
        more = (PreferenceCategory) findPreference("more");
        donate = (Preference) findPreference("donate");
        help = (Preference) findPreference("help");
        about = (Preference) findPreference("about");
        
        readProperties();
        
        if (donated) {
            more.removePreference(donate);
        } else {
            donate.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    showDialog(DIALOG_DONATE_ID);
                    return true;
                }
            });
        }
        
        help.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                startActivity(new Intent(SMSFix.this, Help.class));
                return true;
            }
        });
        
        about.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                startActivity(new Intent(SMSFix.this, About.class));
                return true;
            }
        });

        // register a listener for changes
        prefListener = new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                // if "Active" has changed, start or stop the service
                if (key.equals("active")) {
                    toggleService(sharedPreferences.getBoolean(key, false));
                } else if (key.equals("notify") || key.equals("notify_icon")) {
                    // if "Notification" has changed, we want to restart the service
                    // also restart if the icon has changed
                    restartService();
                }

                // update offset and CDMA to reflect the new status or method
                // change
                toggleOffset();
                toggleCDMA();
                toggleNotify();
            }
        };
        
        settings.registerOnSharedPreferenceChangeListener(prefListener);
        
        // show the true value of active
        activeBox.setChecked(FixService.isRunning());

        // set the offset field to be a decimal number
        editOffsetHours.getEditText().setInputType(
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editOffsetMinutes.getEditText().setInputType(
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // set the initial status of the offset and CDMA
        toggleOffset();
        toggleCDMA();
        toggleNotify();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    protected void readProperties() {
        Resources resources = this.getResources();
        AssetManager assetManager = resources.getAssets();

        try {
            InputStream inputStream = assetManager.open(PROPERTIES_FILE);
            
            Properties properties = new Properties();
            properties.load(inputStream);
            
            donated = Boolean.valueOf(properties.getProperty("donated"));
        } catch (IOException e) {
            System.err.println("Failed to open property file");
            e.printStackTrace();
        }
    }
    
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
            case DIALOG_DONATE_ID:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.donate_title)
                       .setIcon(R.drawable.ic_dialog_heart)
                       .setMessage(R.string.donate_message)
                       .setPositiveButton(R.string.donate_yes, new DialogInterface.OnClickListener() {
                           
                           public void onClick(DialogInterface dialog, int id) {
                               Intent intent = new Intent(Intent.ACTION_VIEW);
                               intent.setData(Uri.parse("market://details?id=com.mattprecious.smsfixdonate"));
                               startActivity(intent);
                           }
                       })
                       .setNegativeButton(R.string.donate_no, new DialogInterface.OnClickListener() {
                           
                           public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                           }
                       });
                dialog = builder.create();
                break;
            default:
                dialog = null;
        }
        return dialog;
    }

    /**
     * Enable or disable the fixing service.
     * 
     * @param active
     */
    public void toggleService(boolean active) {
        if (active) {
            startService(new Intent(this, FixService.class));
        } else {
            stopService(new Intent(this, FixService.class));
        }
    }
    
    /**
     *  Restart the fixing service
     *  
     */
    public void restartService() {
        stopService(new Intent(this, FixService.class));
        startService(new Intent(this, FixService.class));
    }

    /**
     * Toggle whether or not the "Offset" option should be enabled.
     * If the method is manual and the service is active.
     * 
     */
    public void toggleOffset() {
        editOffsetHours.setEnabled(offsetMethod.getValue().equals("manual") && settings.getBoolean("active", false));
        editOffsetMinutes.setEnabled(offsetMethod.getValue().equals("manual") && settings.getBoolean("active", false));
    }

    /**
     * Toggle whether or not the "CDMA' option should be enabled.
     * If the method is phone and the service is active.
     */
    public void toggleCDMA() {
        cdmaBox.setEnabled(settings.getBoolean("active", false));
    }
    
    /**
     * Toggle whether or not the "Icon Style" option should be enabled.
     */
    public void toggleNotify() {
        notifyIcon.setEnabled(settings.getBoolean("notify", false));
    }

}
