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

import org.openrdf.model.Statement;

/**
 * @author Felix Bensmann
 * Rejects all statement whose predicate is not within a given list.
 */
public class WhiteListPropertyFilter implements IStatementFilter{

    private String lastCause = "No error";
    private String[] acceptableProperties = null;
    
    public WhiteListPropertyFilter(String[] acceptableProperties){
        this.acceptableProperties=acceptableProperties;
    }
    
    
    @Override
    public boolean accept(Statement stmt) {
        lastCause="No error";
        if(acceptableProperties != null && acceptableProperties.length > 0){
            String predicate = stmt.getPredicate().stringValue();
            for(String s : acceptableProperties){
                if(s.equals(predicate)){
                    return true;
                }
            }
            lastCause = "Property "+predicate+ " of statement "+stmt.toString()+" not in white list.";
            return false;
        }
        else{
            return true;
        }
    }

    @Override
    public String getLastCause() {
        return lastCause;
    }
    
    
}
