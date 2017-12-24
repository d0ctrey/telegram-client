package org.telegram.actors.log;

import java.util.Date;

/**
 * Created by ex3ndr on 03.04.14.
 */
public class Logger {

    private static LogInterface logInterface;

    public static void registerInterface(LogInterface logInterface) {
        Logger.logInterface = logInterface;
    }

    public static void w(String tag, String message) {
        if (logInterface != null) {
            logInterface.w(tag, message);
        } else {
            System.out.println(new Date() + " (" + Thread.currentThread().getName() + ") [WARNING]: " + tag + ":" + message);
        }
    }

    public static void d(String tag, String message) {
        if (logInterface != null) {
            logInterface.d(tag, message);
        } else {
            System.out.println(new Date() + " (" + Thread.currentThread().getName() + ") [DEBUG]: " + tag + ":" + message);
        }
    }

    public static void e(String tag, Throwable t) {
        if (logInterface != null) {
            logInterface.e(tag, t);
        } else {
            t.printStackTrace();
        }
    }
}
