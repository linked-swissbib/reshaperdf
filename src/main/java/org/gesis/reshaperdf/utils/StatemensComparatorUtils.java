/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gesis.reshaperdf.utils;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 *
 * @author bensmafx
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
    
}
