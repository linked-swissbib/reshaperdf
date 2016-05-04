/*
 * Copyright (C) 2016 GESIS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, see 
 * http://www.gnu.org/licenses/ .
 */
package org.gesis.reshaperdf.utils;

/**
 * @author Felix Bensmann
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
