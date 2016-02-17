package org.gesis.reshaperdf.utils;

import java.util.Comparator;
import org.openrdf.model.Statement;

/**
 * Provides a method to compare RDF statements.
 */
public class StatementsComparatorO implements Comparator<Statement> {

    @Override
    public int compare(Statement o1, Statement o2) {
        return StatemensComparatorUtils.compare2Objects(o1.getObject(), o2.getObject());
    }

    

}
