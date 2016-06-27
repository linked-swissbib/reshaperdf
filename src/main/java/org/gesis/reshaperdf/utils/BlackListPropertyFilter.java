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
public class BlackListPropertyFilter implements IStatementFilter{

    private String lastCause = "No error";
    private String[] unacceptableProperties = null;
    
    public BlackListPropertyFilter(String[] unacceptableProperties){
        this.unacceptableProperties=unacceptableProperties;
    }
    
    
    @Override
    public boolean accept(Statement stmt) {
        lastCause="No error";
        if(unacceptableProperties != null && unacceptableProperties.length > 0){
            String predicate = stmt.getPredicate().stringValue();
            for(String s : unacceptableProperties){
                if(s.equals(predicate)){
                    lastCause = "Property "+predicate+ " of statement "+stmt.toString()+" present in black list.";
                    return false;
                }
            }
            return true;
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
