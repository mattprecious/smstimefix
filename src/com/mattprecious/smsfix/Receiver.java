package com.mattprecious.smsfix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Receiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		
        if (settings.getBoolean("active", false)) {
			Intent svc = new Intent(context, FixService.class);
	        context.startService(svc);
        }
	}
}
