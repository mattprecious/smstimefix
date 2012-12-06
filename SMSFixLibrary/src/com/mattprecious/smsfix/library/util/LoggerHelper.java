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

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class LoggerHelper {
    private static final String ROOT_DIR = Environment.getExternalStorageDirectory()
            .getAbsolutePath();
    private static final String LOG_FILE = "/Android/data/com.mattprecious.smsfix/files/smsfix.log";
    private static final String ROLLOVER_SUFFIX = ".old";

    public static void clearLog(Context context) {
        File logFile = getLogFile();
        File rolloverLogFile = getRolloverLogFile();

        logFile.delete();
        rolloverLogFile.delete();
        logFile.getParentFile().delete();
        logFile.getParentFile().getParentFile().delete();
    }

    private static File getLogFile() {
        return new File(ROOT_DIR + LOG_FILE);
    }

    private static File getRolloverLogFile() {
        return new File(ROOT_DIR + LOG_FILE + ROLLOVER_SUFFIX);
    }
}
