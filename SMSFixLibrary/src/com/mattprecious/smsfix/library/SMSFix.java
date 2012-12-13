/*
 * Copyright 2012 Matthew Precious
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.mattprecious.smsfix.library;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
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
import android.util.Log;

/**
 * SMS Time Fix main activity window
 * 
 * @author Matthew Precious
 * 
 */
public class SMSFix extends PreferenceActivity {
    static boolean donated = false;

    private final static String TAG = "SMSFix";
    private final String PROPERTIES_FILE = "main.properties";

    private SharedPreferences settings;
    private CheckBoxPreference activeBox;
    private ListPreference offsetMethod;
    private EditTextPreference editOffsetHours;
    private EditTextPreference editOffsetMinutes;
    private CheckBoxPreference cdmaBox;
    private CheckBoxPreference roamingBox;
    private ListPreference notifyIcon;

    private PreferenceCategory more;
    private Preference donate;
    private Preference fixOld;
    private Preference help;
    private Preference about;
    private Preference translate;
    private Preference emailDev;

    private OnSharedPreferenceChangeListener prefListener;

    static final int DIALOG_DONATE_ID = 0;
    static final int DIALOG_ROAMING_ID = 1;
    static final int DIALOG_CHANGE_LOG_ID = 2;
    static final int DIALOG_BACKUP_WARNING = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "SMSFix Activity started. Preparing the view");

        addPreferencesFromResource(R.xml.preferences);

        settings = ((PreferenceScreen) findPreference("preferences")).getSharedPreferences();

        activeBox = (CheckBoxPreference) findPreference("active");

        offsetMethod = (ListPreference) findPreference("offset_method");
        editOffsetHours = (EditTextPreference) findPreference("offset_hours");
        editOffsetMinutes = (EditTextPreference) findPreference("offset_minutes");

        cdmaBox = (CheckBoxPreference) findPreference("cdma");
        roamingBox = (CheckBoxPreference) findPreference("roaming");

        notifyIcon = (ListPreference) findPreference("notify_icon");

        more = (PreferenceCategory) findPreference("more");
        donate = (Preference) findPreference("donate");
        fixOld = (Preference) findPreference("fix_old");
        help = (Preference) findPreference("help");
        about = (Preference) findPreference("about");
        translate = (Preference) findPreference("translate");
        emailDev = (Preference) findPreference("email_dev");

        adjustMethodLabels();

        roamingBox.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {
                if (roamingBox.isChecked()) {
                    showDialog(DIALOG_ROAMING_ID);
                }
                return true;
            }
        });

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

        fixOld.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {
                startActivity(new Intent(SMSFix.this, FixOld.class));
                return true;
            }
        });

        help.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
                        .parse("http://www.mattprecious.com/help/smsfix.html"));
                startActivity(browserIntent);
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

        translate.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://crowdin.net/project/sms-time-fix"));
                startActivity(intent);

                return true;
            }
        });

        emailDev.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                sendEmailToDev();
                return true;
            }
        });

        // register a listener for changes
        prefListener = new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.d(TAG, "Preference changed: " + key);

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
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editOffsetMinutes.getEditText().setInputType(
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // set the initial status of the offset and CDMA
        toggleOffset();
        toggleCDMA();
        toggleNotify();

        // debug the change log
//        settings.edit().putInt("version_code", 0).commit();

        if (!settings.getBoolean("seen_backup_warning", false)) {
            showDialog(DIALOG_BACKUP_WARNING);
        }

        checkAndShowChangeLog();

        Log.d(TAG, "SMSFix Activity initialization complete");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "SMSFix Activity destroy");

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
            Log.e(TAG, "Failed to open properties file");
            e.printStackTrace();
        }
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (id) {
            case DIALOG_CHANGE_LOG_ID:
                builder.setTitle(R.string.whats_new).setIcon(android.R.drawable.ic_dialog_info)
                        .setMessage(R.string.change_log)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                dialog = builder.create();
                break;
            case DIALOG_BACKUP_WARNING:
                builder.setTitle(R.string.warning).setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.backup_warning_message)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {
                                settings.edit().putBoolean("seen_backup_warning", true).commit();
                                dialog.cancel();
                            }
                        });
                dialog = builder.create();
                break;
            case DIALOG_DONATE_ID:
                builder.setTitle(R.string.donate_title)
                        .setIcon(R.drawable.ic_dialog_heart)
                        .setMessage(R.string.donate_message)
                        .setPositiveButton(R.string.donate_yes,
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri
                                                .parse("market://details?id=com.mattprecious.smsfixdonate"));
                                        startActivity(intent);
                                    }
                                })
                        .setNegativeButton(R.string.donate_no,
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                dialog = builder.create();
                break;
            case DIALOG_ROAMING_ID:
                builder.setTitle(R.string.roaming_title)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setMessage(R.string.roaming_message)
                        .setPositiveButton(R.string.roaming_ok,
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                        .setNegativeButton(R.string.roaming_no,
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int id) {
                                        roamingBox.setChecked(false);
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
     * Restart the fixing service
     * 
     */
    public void restartService() {
        stopService(new Intent(this, FixService.class));
        startService(new Intent(this, FixService.class));
    }

    /**
     * Toggle whether or not the "Offset" option should be enabled. If the method is manual and the
     * service is active.
     * 
     */
    public void toggleOffset() {
        editOffsetHours.setEnabled(offsetMethod.getValue().equals("manual")
                && settings.getBoolean("active", false));
        editOffsetMinutes.setEnabled(offsetMethod.getValue().equals("manual")
                && settings.getBoolean("active", false));
    }

    /**
     * Toggle whether or not the "CDMA' option should be enabled. If the method is phone and the
     * service is active.
     */
    public void toggleCDMA() {
        cdmaBox.setEnabled(settings.getBoolean("active", false));
    }

    /**
     * Toggle whether or not the "Icon Style" option should be enabled.
     */
    public void toggleNotify() {
        notifyIcon.setEnabled(settings.getBoolean("notify", true));
    }

    public void sendEmailToDev() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("vnd.android.cursor.dir/email");

        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "matt@mattprecious.com" });
        intent.putExtra(Intent.EXTRA_SUBJECT, "SMS Time Fix Feedback");

        StringBuffer body = new StringBuffer("\n\n\n------------------------------");
        body.append("\nAndroid release: ").append(Build.VERSION.RELEASE);
        body.append("\nAndroid SDK: ").append(getSdkVersion());

        String appVersion = "";

        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);

            appVersion = packageInfo.versionName + " (" + packageInfo.versionCode + ")";
        } catch (NameNotFoundException e) {

        }

        body.append("\nSMSFix package: ").append(getPackageName());
        body.append("\nSMSFix Version: ").append(appVersion);
        body.append("\nPreferences:").append(settings.getAll().toString());
        intent.putExtra(Intent.EXTRA_TEXT, body.toString());

        startActivity(intent);
    }

    @SuppressWarnings("deprecation")
    private int getSdkVersion() {
        int sdkVersion;

        try {
            // works for level 4 and up
            Field SDK_INT_field = Build.VERSION.class.getField("SDK_INT");
            sdkVersion = (Integer) SDK_INT_field.get(null);
        } catch (Exception e) {
            sdkVersion = Integer.parseInt(Build.VERSION.SDK);
        }

        return sdkVersion;
    }

    /**
     * Update the adjustment method labels in two ways:
     * 
     * 1. Swap the "Add" and "Subtract" time zone names if we're GMT+X 2. Add the current time zone
     * offset to the Time Zone methods
     */
    private void adjustMethodLabels() {
        // the labels for the offset methods
        CharSequence[] offsetMethodEntries = offsetMethod.getEntries();

        int gmtOffset = TimeZone.getDefault().getRawOffset() / 3600000;

        // account for DST
        if (TimeZone.getDefault().useDaylightTime()
                && TimeZone.getDefault().inDaylightTime(new Date())) {
            gmtOffset += 1;
        }

        int absGMTOffset = Math.abs(gmtOffset);

        // swap the Add and Subtract time zone method names if we're GMT+X
        if (gmtOffset >= 0) {
            CharSequence temp = offsetMethodEntries[0];
            offsetMethodEntries[0] = offsetMethodEntries[1];
            offsetMethodEntries[1] = temp;
        }

        // add the time zone offset to the labels
        offsetMethodEntries[0] = offsetMethodEntries[0] + " (" + absGMTOffset + ")";
        offsetMethodEntries[1] = offsetMethodEntries[1] + " (" + absGMTOffset + ")";

        // set them
        offsetMethod.setEntries(offsetMethodEntries);
    }

    private void checkAndShowChangeLog() {
        PackageManager packageManager = getPackageManager();

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);

            if (settings.getInt("version_code", 0) != packageInfo.versionCode) {
                showDialog(DIALOG_CHANGE_LOG_ID);

                Editor editor = settings.edit();
                editor.putInt("version_code", packageInfo.versionCode);
                editor.commit();
            }

            if (settings.contains("log_to_sd")) {
                settings.edit().remove("log_to_sd").commit();
            }
        } catch (NameNotFoundException e) {

        }
    }

}
