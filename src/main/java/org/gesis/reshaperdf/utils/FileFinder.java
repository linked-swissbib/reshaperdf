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

import java.io.File;
import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * @author Felix Bensmann
 * Utils for finding files in a recursively directory structure
 */
public class FileFinder {

    /**
     * Searches for files with given extension, case insensitive
     *
     * @param rootDir The root directory to start the scan
     * @param extensions Array of file extensions eg. "xml","rdf"
     * @return Array of files found
     */
    public static File[] findFiles(File rootDir, String[] extensions) {

        //parameters
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            throw new InvalidParameterException("The rootDir is invalid: " + rootDir.getAbsolutePath());
        }
        ArrayList<File> findings = new ArrayList<File>();

        //transform all extenstion to lower case
        String[] lcExtensions = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            lcExtensions[i] = extensions[i].toLowerCase();
        }

        //collect file recursively
        collectFilesRecursively(rootDir, findings, extensions);

        return findings.toArray(new File[findings.size()]);
    }

    /**
     * Scans FS recursively and collects files with the given extensions
     *
     * @param rootDir The root directory to start the scan
     * @param findings The list to add the found files to
     * @param extensions Array of file extensions eg. "xml","rdf"
     */
    private static void collectFilesRecursively(File rootDir, ArrayList<File> findings, String[] extensions) {

        File[] files = rootDir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                collectFilesRecursively(f, findings, extensions);
            } else {
                if (isExtenstionConform(f, extensions)) {
                    findings.add(f);
                }
            }
        }
    }

    /**
     * Checks whether a filename has one of the specified extensions
     *
     * @param file File to check
     * @param extensions Extensions to compare; Need to be lower case
     * @return True if the file matches one of the given extension, false
     * otherwise
     */
    private static boolean isExtenstionConform(File file, String[] extensions) {
        for (String ext : extensions) {
            if (file.getName().toLowerCase().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
}
