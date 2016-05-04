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
package org.gesis.reshaperdf.cmd.boundary;

import java.util.Comparator;

/**
 * @author Felix Bensmann
 * Comparator for classes that implement ICMD.
 * Comparation by name in unicode order.
 */
public class CommandComparator implements Comparator<ICMD>{

    @Override
    public int compare(ICMD o1, ICMD o2) {
        return o1.getName().compareTo(o2.getName());
    }
    
}
