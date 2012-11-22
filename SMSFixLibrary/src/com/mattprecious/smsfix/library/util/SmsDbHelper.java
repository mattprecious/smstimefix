package com.mattprecious.smsfix.library.util;

import android.net.Uri;


public class SmsDbHelper {
	public static Uri getObservingUri() {
		Uri uri = Uri.parse("content://mms-sms/conversations");
		
        if (uri == null) {
        	uri = getEditingUri();
        }
        
        return uri;
	}
	
	public static Uri getEditingUri() {
		Uri uri = Uri.parse("content://sms");
		return uri;
	}
}
