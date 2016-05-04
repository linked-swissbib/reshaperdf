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
package org.gesis.reshaperdf.cmd.analyzetype;

/**
 *
 * @author Felix Bensmann
 */
public class OccurenceRow{

    private String[] arr = null;
    private int total = 0;
    private int alternatives = 0;

  
    public OccurenceRow(String[] literalArr) {
        arr = new String[literalArr.length];
        
        for (int i = 0; i < literalArr.length; i++) {
            arr[i] = literalArr[i];
        }
    }

    public int getPredicateCount() {
        return arr.length;
    }

    public void setLiteral(int index, String value) {
        arr[index] = value;
    }

    public String getLiteral(int index) {
        return arr[index];
    }

    public int getTotal() {
        return total;
    }

    public int getAlternatives() {
        return alternatives;
    }

    public String getKey() {
        String key = "";
        for (int i = 0; i < arr.length; i++) {
            key += arr[i];
        }
        return key;
    }

    public static String generateKey(String[] predicateArr) {
        String key = "";
        for (int i = 0; i < predicateArr.length; i++) {
            key += predicateArr[i];
        }
        return key;
    }

    public void increaseTotal() {
        total++;
    }

    public void increaseAlternatives(int alternateCount) {
        alternatives += alternateCount;
    }

    

   

}
