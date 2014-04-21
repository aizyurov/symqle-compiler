package org.symqle.util;

/**
 * @author lvovich
 */
public abstract class Log {

    private static Log instance;

    public static void setLog(final Log log) {
        instance = log;
    }

    public abstract void logInfo(String message);
    public abstract void logDebug(String message);

    public static void info(final String message) {
        if (instance != null) {
            instance.logInfo(message);
        } else {
            System.err.println(message);
        }
    }

    public static void debug(final String message) {
        if (instance != null) {
            instance.logDebug(message);
        } else {
            System.err.println(message);
        }
    }
}
