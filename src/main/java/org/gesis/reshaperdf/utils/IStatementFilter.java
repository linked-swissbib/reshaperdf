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

import org.openrdf.model.Statement;

/**
 * @author Felix Bensmann
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
