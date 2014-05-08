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
