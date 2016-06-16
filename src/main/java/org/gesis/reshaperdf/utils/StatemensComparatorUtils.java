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

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * Utils class for statements comparisons.
 * @author Felix Bensmann
 */
public class StatemensComparatorUtils {
    
    
    public static int compare2Objects(Value o1, Value o2){
    
        if ((o1 instanceof URI) && (o2 instanceof URI)) {
            return compare2Strings(o1.stringValue(), o2.stringValue());
        } else if ((o1 instanceof Literal) && (o2 instanceof Literal)) {
            Literal lit1 = (Literal) o1;
            Literal lit2 = (Literal) o2;
            int res = compare2Strings(lit1.getLabel(), lit2.getLabel());
            if (res == 0) {
                res = compare2Strings(lit1.getDatatype().stringValue(), lit2.getDatatype().stringValue());
                if (res == 0) {
                    res = compare2Strings(lit1.getLanguage(), lit2.getLanguage());
                }
            }
            return res;
        } else {
            return o1.toString().compareTo(o2.toString());
        }
    }
    
    
    /**
     * Null conscious comparator for Strings. null is bigger than !null.
     *
     * @param s1
     * @param s2
     * @return
     */
    public static int compare2Strings(String s1, String s2) {
        if (s1 != null && s2 != null) {
            return s1.compareTo(s2);
        } else if (s1 == null && s2 == null) {
            return 0;
        } else if (s1 == null && s2 != null) {
            return 1;
        } else {
            return -1;
        }
    }
    
    public static int compare2Resources(Resource r1, Resource r2){
        return r1.stringValue().compareTo(r2.stringValue());
    }
    
    public static int compare2URIs(URI u1, URI u2){
        return u1.stringValue().compareTo(u2.stringValue());
    }
    
    
}
