package org.gesis.reshaperdf.utils;

/**
 * Utils to determine the plattform the program runs on.
 */
public class OSUtils {

    public static boolean isWindows(String os) {
        return (os.contains("win"));

    }

    public static boolean isMac(String os) {

        return (os.contains("mac"));

    }

    public static boolean isUnix(String os) {

        return (os.contains("nix") || os.contains("nux") || os.contains("aix"));

    }
}
