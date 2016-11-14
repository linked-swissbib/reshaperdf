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
package org.gesis.reshaperdf.cmd.analyzetype;

import java.util.Comparator;

/**
 *
 * @author Felix Bensmann
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
                return o2.getKey().compareTo(o1.getKey());
            }
        }
    }

}
