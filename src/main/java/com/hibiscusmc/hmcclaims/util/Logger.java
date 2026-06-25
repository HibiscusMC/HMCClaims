package com.hibiscusmc.hmcclaims.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Global utility class for standardized logging across HMCClaims.
 * <p>
 * This class wraps SLF4J logging methods to provide a consistent logger
 * and simplifies log calls throughout the plugin.
 */
@Slf4j(topic = "HMCClaims")
public class Logger {

    /**
     * Logs a general informational message.
     *
     * @param message   The log message pattern (can include {} placeholders)
     * @param arguments Arguments to substitute into the message placeholders
     */
    public static void log(String message, Object... arguments) {
        if (log.isInfoEnabled()) {
            log.info(message, arguments);
        }
    }

    /**
     * Logs a warning message.
     *
     * @param message   The warning message pattern (can include {} placeholders)
     * @param arguments Arguments to substitute into the message placeholders
     */
    public static void warning(String message, Object... arguments) {
        if (log.isWarnEnabled()) {
            log.warn(message, arguments);
        }
    }

    /**
     * Logs an error message.
     *
     * @param message   The error message pattern (can include {} placeholders)
     * @param arguments Arguments to substitute into the message placeholders
     */
    public static void error(String message, Object... arguments) {
        if (log.isErrorEnabled()) {
            log.error(message, arguments);
        }
    }

    /**
     * Logs an error message along with its accompanying exception stack trace.
     * Highly recommended for catching and logging caught exceptions.
     *
     * @param message   The error context message
     * @param throwable The exception/error to log with stack trace
     */
    public static void error(String message, Throwable throwable) {
        if (log.isErrorEnabled()) {
            log.error(message, throwable);
        }
    }
}