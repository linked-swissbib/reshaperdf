package org.gesis.reshaperdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.gesis.reshaperdf.cmd.boundary.CommandComparator;
import org.gesis.reshaperdf.cmd.boundary.ICMD;

/**
 * Contains the commands in an ArrayList of ICMD interface links.
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
