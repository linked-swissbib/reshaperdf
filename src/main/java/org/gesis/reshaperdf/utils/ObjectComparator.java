/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gesis.reshaperdf.utils;

import java.util.Comparator;
import org.openrdf.model.Value;

/**
 *
 * @author bensmafx
 */
public class ObjectComparator implements Comparator<Value>{

    @Override
    public int compare(Value o1, Value o2) {
        return StatemensComparatorUtils.compare2Objects(o1, o2);
    }
    
}
