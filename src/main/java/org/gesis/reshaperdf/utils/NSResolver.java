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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;

/**
 * @author Felix Bensmann
 * Manages a list with rdf namespaces and it short forms. Implements singleton.
 */
public class NSResolver {

    private static final String FILEPATH = "namespaces.txt";
    private static final String ABR_RDF = "rdf";
    private static final String FULL_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String ABR_RDFS = "rdfs";
    private static final String FULL_RDFS = "http://www.w3.org/2000/01/rdf-schema#";

    private static NSResolver instance = null;

    private ArrayList<KVP> nsList = null;

    /**
     * Private inner class to store the namespaces and its shorts.
     */
    private class KVP {

        public String shortName = null;
        public String longName = null;

        public KVP(String shortName, String longName) {
            this.shortName = shortName;
            this.longName = longName;
        }

    }

    /**
     * Private ctor.
     */
    private NSResolver() {
        nsList = new ArrayList<KVP>();
        nsList.add(new KVP(ABR_RDF, FULL_RDF));
        nsList.add(new KVP(ABR_RDFS, FULL_RDFS));

        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            InputStream input = classLoader.getResourceAsStream(FILEPATH);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String line = reader.readLine();
            while (line != null) {
                String[] keyValueString = line.split(" ");
                nsList.add(new KVP(keyValueString[0].trim(), keyValueString[1].trim()));
                line = reader.readLine();
            }
            reader.close();
        } catch (FileNotFoundException ex) {
            LogManager.getLogger(NSResolver.class).log(org.apache.logging.log4j.Level.ERROR, ex);
        } catch (IOException ex) {
            LogManager.getLogger(NSResolver.class).log(org.apache.logging.log4j.Level.ERROR, ex);
        }

    }

    /**
     * Singleton
     *
     * @return
     */
    public static NSResolver getInstance() {
        if (instance == null) {
            instance = new NSResolver();
        }
        return instance;
    }

    /**
     * Determines whether a URL can be shortened with the data at hand.
     *
     * @param longName
     * @return
     */
    public boolean canBeShortened(String longName) {
        for (KVP kvp : nsList) {
            if (longName.startsWith(kvp.longName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Actually shortens a URL with the data at hand.
     *
     * @param longName
     * @return
     */
    public String shorten(String longName) {
        for (KVP kvp : nsList) {
            if (longName.startsWith(kvp.longName)) {
                String retVal = longName.replaceFirst(kvp.longName, kvp.shortName + ":");
                return retVal;
            }
        }
        return longName;
    }

    /**
     * Extends a URL with its namespace using the data at hand.
     *
     * @param longName
     * @return
     */
    public String blowUp(String shortName) {
        for (KVP kvp : nsList) {
            if (shortName.startsWith(kvp.shortName)) {
                String retVal = shortName.replaceFirst(kvp.shortName + ":", kvp.longName);
                return retVal;
            }
        }
        return shortName;
    }

}
