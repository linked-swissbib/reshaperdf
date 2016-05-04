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

import java.util.Comparator;
import org.openrdf.model.Statement;

/**
 * @author Felix Bensmann
 * Provides a method to compare RDF statements.
 */
public class StatementsComparatorSPO implements Comparator<Statement>{

    
    
    @Override
    public int compare(Statement o1, Statement o2) {
        int res = o1.getSubject().stringValue().compareTo(o2.getSubject().stringValue());
        if(res == 0){
            res = o1.getPredicate().stringValue().compareTo(o2.getPredicate().stringValue());
            if(res == 0){
                res = StatemensComparatorUtils.compare2Objects(o1.getObject(), o2.getObject()); 
            }
        }
        return res;
    }
    
}
