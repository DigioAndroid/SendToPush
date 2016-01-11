package com.accesium.sendtopush.tools;

/**
 * Class that encapsulates the logging system, so that we can enable or disable
 * the debug system.
 * 
 * @author Isidoro Castell
 * 
 */
public class Log {

	private final static String LOG_TAG = "SendToPush";

	private static boolean sDebug;

	/**
	 * Initialize the class
	 * 
	 * @param debug
	 *            Whether is debug enabled or not
	 */
	public static void initialize(boolean debug) {
		sDebug = debug;
	}

	/**
	 * Print a log with the info verbosity
	 * 
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void i(String msg) {
		if (sDebug) {
			android.util.Log.i(LOG_TAG, msg);
		}
	}

	/**
	 * Print a log with the info verbosity
	 * 
	 * @param msg
	 *            The message you would like logged.
	 * @param e
	 *            An exception to log
	 */
	public static void i(String msg, Exception e) {
		if (sDebug) {
			android.util.Log.i(LOG_TAG, msg, e);
		}
	}

	/**
	 * Print a log with the error verbosity
	 * 
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void e(String msg) {
		android.util.Log.e(LOG_TAG, msg);
	}

	/**
	 * Print a log with the error verbosity
	 * 
	 * @param msg
	 *            The message you would like logged.
	 * @param e
	 *            An exception to log
	 */
	public static void e(String msg, Exception e) {
		android.util.Log.e(LOG_TAG, msg, e);
	}

	/**
	 * Print a log with the warning verbosity
	 * 
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void w(String msg) {
		android.util.Log.w(LOG_TAG, msg);
	}

	/**
	 * Print a log with the warning verbosity
	 * 
	 * @param msg
	 *            The message you would like logged.
	 * @param e
	 *            An exception to log
	 */
	public static void w(String msg, Exception e) {
		android.util.Log.w(LOG_TAG, msg, e);
	}

	/**
	 * Print a log with the debug tag
	 * 
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void d(String msg) {
		if (sDebug) {
			android.util.Log.d(LOG_TAG, msg);
		}
	}

	/**
	 * Print a log with the debug tag
	 * 
	 * @param msg
	 *            The message you would like logged.
	 * @param e
	 *            An exception to log
	 */
	public static void d(String msg, Exception e) {
		if (sDebug) {
			android.util.Log.d(LOG_TAG, msg, e);
		}
	}
}
