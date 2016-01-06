package org.gesis.reshaperdf.utils;

import org.openrdf.model.Statement;

/**
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
