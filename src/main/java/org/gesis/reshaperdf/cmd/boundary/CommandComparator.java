package org.gesis.reshaperdf.cmd.boundary;

import java.util.Comparator;

/**
 * Comparator for classes that implement ICMD.
 */
public class CommandComparator implements Comparator<ICMD>{

    @Override
    public int compare(ICMD o1, ICMD o2) {
        return o1.getName().compareTo(o2.getName());
    }
    
}
