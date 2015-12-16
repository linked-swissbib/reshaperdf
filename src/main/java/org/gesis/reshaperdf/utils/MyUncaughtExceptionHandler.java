package org.gesis.reshaperdf.utils;

/**
 * Handler for uncaught exceptions. Handles them by printing them to System.out.
 */
public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.err.println(t.getName() + "threw an exception: " + e);
    }

}
