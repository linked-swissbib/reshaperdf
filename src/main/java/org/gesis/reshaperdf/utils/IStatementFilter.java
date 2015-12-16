package org.gesis.reshaperdf.utils;

import org.openrdf.model.Statement;

/**
 * An interface to implement a statement filter.
 */
public interface IStatementFilter {
    
    /**
     * Return true if the statement is accepted.
     * @param stmt
     * @return 
     */
    public boolean accept(Statement stmt);
    
    /**
     * Return the last error message, in case a statement was declined.
     * @return 
     */
    public String getLastCause();
}
