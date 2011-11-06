package com.mattprecious.smsfix.library;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LogRolloverReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LoggerHelper.checkForRollover(context);

    }

}
