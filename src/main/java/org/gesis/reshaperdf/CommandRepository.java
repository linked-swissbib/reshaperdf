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
package org.gesis.reshaperdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.gesis.reshaperdf.cmd.boundary.CommandComparator;
import org.gesis.reshaperdf.cmd.boundary.ICMD;

/**
 * @author Felix Bensmann
 * Contains the commands in an ArrayList.
 */
public class CommandRepository extends ArrayList<ICMD>{
    
    private ICMD helpCommand = null;
    
    public CommandRepository(){
        super();
        
    }
    
    /**
     * Get an array of available command names.
     * @return 
     */
    public String[] getCommandList(){
        this.sort();
        String[] arr = new String[this.size()];
        for(int i=0; i<this.size();i++){
            arr[i] = this.get(i).getName();
        }
        return arr;
    }
    
    
    /**
     * Get the command to the name.
     * @param name
     * @return 
     */
    public ICMD getCommand(String name){
        for(int i=0; i<this.size();i++){
            if(this.get(i).getName().equalsIgnoreCase(name))
                return this.get(i);
        }
        return null;
    }
    
    
    /**
     * Sorts the commands in this repository alphabetically.
     */
    public void sort(){
        Collections.sort(this, new CommandComparator());
    }
    
    
    
    
    
}
