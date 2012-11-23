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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.mattprecious.smsfix.library.util.LoggerHelper;

public class Receiver extends BroadcastReceiver {
    
    private LoggerHelper logger;

    @Override
    public void onReceive(Context context, Intent intent) {
        logger = LoggerHelper.getInstance(context);
        logger.info("Received " + intent.getAction() + " intent");
        
        String name = context.getPackageName() + "_preferences";
        
        logger.info("Using preferences: " + name);
        
        SharedPreferences settings = context.getSharedPreferences(name, 0);
        
        boolean active = settings.getBoolean("active", false);
        
        logger.info("Active preference: " + Boolean.toString(active));

        if (active) {
            logger.info("Starting FixService");
            Intent svc = new Intent(context, FixService.class);
            context.startService(svc);
        }
    }
}