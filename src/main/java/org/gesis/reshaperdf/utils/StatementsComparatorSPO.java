package org.gesis.reshaperdf.utils;

import java.util.Comparator;
import org.openrdf.model.Statement;

/**
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
