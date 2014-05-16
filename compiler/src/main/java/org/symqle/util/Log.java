/*
   Copyright 2011-2014 Alexander Izyurov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.package org.symqle.common;
*/

package org.symqle.util;

/**
 * Simple delegating logger. Actual implementation can be set in runtime.
 * If it is not set, log messages go to System.err.
 * @author lvovich
 */
public abstract class Log {

    private static Log instance;

    /**
     * Set Log instance, which will do the actual logging.
     * @param log the logger to set.
     */
    public static void setLog(final Log log) {
        instance = log;
    }

    /**
     * Implementation of logging at info level.
     * @param message log message
     */
    public abstract void logInfo(String message);
    /**
     * Implementation of logging at debug level.
     * @param message log message
     */
    public abstract void logDebug(String message);

    /**
     * Write a message to log at info level.
     * @param message log message.
     */
    public static void info(final String message) {
        if (instance != null) {
            instance.logInfo(message);
        } else {
            System.err.println(message);
        }
    }

    /**
     * Write a message to log at debug level.
     * @param message log message.
     */
    public static void debug(final String message) {
        if (instance != null) {
            instance.logDebug(message);
        } else {
            System.err.println(message);
        }
    }
}
