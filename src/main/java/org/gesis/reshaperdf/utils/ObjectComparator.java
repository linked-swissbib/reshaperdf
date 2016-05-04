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
package org.gesis.reshaperdf.utils;

import java.util.Comparator;
import org.openrdf.model.Value;

/**
 * Comparator for RDF objects. Comparison by strings in 
 * 1. content(URI, blank node, literal)
 * 2. type (literal)
 * 3. language (literal)
 * Use of unicode order.
 * @author Felix Bensmann
 */
public class ObjectComparator implements Comparator<Value>{

    @Override
    public int compare(Value o1, Value o2) {
        return StatemensComparatorUtils.compare2Objects(o1, o2);
    }
    
}
