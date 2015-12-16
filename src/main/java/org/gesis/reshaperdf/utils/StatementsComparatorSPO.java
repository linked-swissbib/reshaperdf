package org.gesis.reshaperdf.utils;

import java.util.Comparator;
import org.openrdf.model.Statement;

/**
 * Provides a method to compare RDF statements.
 */
public class StatementsComparatorSPO implements Comparator<Statement>{

    @Override
    public int compare(Statement o1, Statement o2) {
        int a = o1.getSubject().toString().compareTo(o2.getSubject().toString());
        if(a == 0){
            int b = o1.getPredicate().toString().compareTo(o2.getPredicate().toString());
            if(b == 0){
                int c = o1.getObject().toString().compareTo(o2.getObject().toString());
                return c;
            }
            else{
                return b;
            }
        }
        else{
            return a;
        }
    }
    
}
