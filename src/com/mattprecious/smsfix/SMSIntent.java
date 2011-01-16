package com.mattprecious.smsfix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SMSIntent extends BroadcastReceiver {
	public SMSIntent() {
	}

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Log.i("RECEIVER", "Received");
	}
}
