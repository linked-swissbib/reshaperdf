/*
 * To change o1 license header, choose License Headers in Project Properties.
 * To change o1 template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gesis.reshaperdf.cmd.analyzetype;

import java.util.Comparator;

/**
 *
 * @author bensmafx
 */
public class OccurenceRowComparator implements Comparator<OccurenceRow> {

    private boolean ascending = false;

    public OccurenceRowComparator(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(OccurenceRow oc1, OccurenceRow oc2) {
        OccurenceRow o1 = oc1;
        OccurenceRow o2 = oc2;
        //exchange comparables in case of reverse order
        if (!ascending) {
            o1 = oc2;
            o2 = oc1;
        }

        //compare totals in 1st step
        if (o1.getTotal() < o2.getTotal()) {
            return -1;
        } else if (o1.getTotal() > o2.getTotal()) {
            return 1;
        } else {
            //compare alternative Spelling in 2nd step
            if (o1.getAlternatives() < o2.getAlternatives()) {
                return -1;
            } else if (o1.getAlternatives() > o2.getAlternatives()) {
                return 1;
            } else {
                //compare keys
                return o1.getKey().compareTo(o2.getKey());
            }
        }
    }

}
