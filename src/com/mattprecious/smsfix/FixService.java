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

import java.util.Date;
import java.util.TimeZone;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class FixService extends Service {
	private SharedPreferences settings;
	private Editor editor;
	
	private Uri URI = Uri.parse("content://sms");
	private FixServiceObserver observer = new FixServiceObserver();
	private Cursor c;
	
	public static long lastSMSId = 0;
	public static boolean running = false;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		running = true;
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		editor = settings.edit();
		
		editor.putBoolean("active", true);
		editor.commit();
		
		String[] columns = {"_id", "date"};
		c = getContentResolver().query(URI, columns, "type=?", new String[]{"1"}, "_id DESC");
		c.registerContentObserver(observer);
		
		lastSMSId = getLastMessageId();
		
		Log.i(getClass().getSimpleName(), "SMS messages now being monitored");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		running = false;
		
		editor.putBoolean("active", false);
		editor.commit();
		
		Log.i(getClass().getSimpleName(), "SMS messages are no longer being monitored. Good-bye.");
	}
	
	private long getLastMessageId() {
        long ret = -1;
        if (c.getCount() > 0) {
	        c.moveToFirst();
	        
	        ret = c.getLong(c.getColumnIndex("_id"));
        }
        
        return ret;
	}
	
	private void fixLastMessage() {
        if (c.getCount() > 0) {
	        c.moveToFirst();
	        
	        long id = c.getLong(c.getColumnIndex("_id"));
	        Log.d(getClass().getSimpleName(), "lastid:" + lastSMSId + "   id:" + id);
	        long oldLastChanged = lastSMSId;
	        lastSMSId = id;
	        
	        while (id > oldLastChanged) {
	        	alterMessage(id);
		        
		        if (c.isLast()) {
		        	break;
		        }
		        
	        	c.moveToNext();
	        	id = c.getLong(c.getColumnIndex("_id"));
	        }
        } else {
        	lastSMSId = -1;
        }
	}
	
	private long getOffset() {
		long offset = 0;
		if (settings.getString("offset_method", "automatic").equals("automatic")) {
			offset = TimeZone.getDefault().getRawOffset() * -1;
		} else {
			offset = Integer.parseInt(settings.getString("offset", "0")) * 3600000;
		}
		
		return offset;
	}
	
	private void alterMessage(long id) {
		Log.i(getClass().getSimpleName(), "Adjusting timestamp for message: " + id);
    	
        Date date;
        if (settings.getString("offset_method", "automatic").equals("phone")) {
        	date = new Date();
        } else {
        	date = new Date(c.getLong(c.getColumnIndex("date")));
        	
        	if (!settings.getBoolean("cdma", false) || (date.getTime() - (new Date()).getTime() > 5000)) {
        		date.setTime(date.getTime() + getOffset());
        	}
        }
        
        ContentValues values = new ContentValues();
        values.put("date", date.getTime());
        getContentResolver().update(URI, values, "_id = " + id, null);
	}
	
	private class FixServiceObserver extends ContentObserver {
		
		public FixServiceObserver() {
			super(null);
		}
		
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			
			if (!selfChange) {
				Log.i(getClass().getSimpleName(), "SMS database altered, checking...!");
				c.requery();
				fixLastMessage();
			}
		}
	}
	
}
