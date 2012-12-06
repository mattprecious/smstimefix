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

package com.mattprecious.smsfix.library.util;

import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class TimeHelper {
    private final static String TAG = "TimeHelper";
    /**
     * Get the desired offset change based on the user's preferences
     * 
     * @param context
     * @return long
     */
    public static long getOffset(Context context) {
        long offset = 0;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        // if the user wants us to auto-determine the offset use the negative of
        // their GMT offset
        String method = settings.getString("offset_method", "manual");

        Log.d(TAG, "Adjustment method: " + method);

        if (method.equals("automatic") || method.equals("neg_automatic")) {
            offset = TimeZone.getDefault().getRawOffset();

            Log.d(TAG, "Raw offset: " + offset);

            // account for DST
            if (TimeZone.getDefault().useDaylightTime()
                    && TimeZone.getDefault().inDaylightTime(new Date())) {
                offset += 3600000;

                Log.d(TAG, "Adjusting for DST: " + offset);
            }

            if (method.equals("automatic")) {
                offset *= -1;

                Log.d(TAG, "Negate the offset: " + offset);
            }

            // otherwise, use the offset the user has specified
        } else {
            double offsetHours = Double.parseDouble(settings.getString("offset_hours", "0"));
            double offsetMinutes = Double.parseDouble(settings.getString("offset_minutes", "0"));

            Log.d(TAG, "Offset Hours: " + offsetHours);
            Log.d(TAG, "Offset Minutes: " + offsetMinutes);

            offset = (long) (offsetHours * 3600000);
            offset += (long) (offsetMinutes * 60000);
        }

        Log.d(TAG, "Final offset: " + offset);
        return offset;
    }
}
