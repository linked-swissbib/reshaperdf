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

import com.github.jsonldjava.core.RDFDataset;
import java.net.MalformedURLException;
import java.net.URL;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

/**
 * @author Felix Bensmann
 * A filter that identifies statements that use invalid character sequences.
 */
public class StrictStatementFilter implements IStatementFilter {

    private String lastCause = "Undefined";

    public StrictStatementFilter() {

    }

    @Override
    public boolean accept(Statement stmt) {
        if ( !(stmt.getSubject() instanceof org.openrdf.model.BNode) ) {
            try {
                new URL(stmt.getSubject().stringValue());
            } catch (MalformedURLException ex) {
                lastCause = "Dropped statement " + stmt.toString() + " due to invalid format in subject.";
                return false;
            }
        }
        //no checks on predicates
        
        //check only objects that are resources
        if (stmt.getObject() instanceof URI) {
            URL object = null;

            try {
                object = new URL(stmt.getObject().stringValue());
            } catch (MalformedURLException ex) {
                lastCause = "Dropped statement " + stmt.toString() + " due to invalid format in resource object.";
                return false;
            }
            if (containsSpecialChar(object)) {
                lastCause = "Dropped statement " + stmt.toString() + " due to invalid format in resource object";
                return false;
            }
            if (containsInvalidBSSequence(object)) {
                lastCause = "Dropped statement " + stmt.toString() + " due to invalid format in resource object. Found sequence with \\ but no following u.";
                return false;
            }
        }
        lastCause = "Statement ok";
        return true;
    }

    @Override
    public String getLastCause() {
        return lastCause;
    }

    /**
     * Checks URL whether it contains spaces.
     *
     * @param url
     * @return True if it contains spaces, otherwise false.
     */
    private boolean containsSpecialChar(URL url) {
        String str = url.toString();
        if (str.contains(" ")) {
            return true;
        }
        return false;
    }

    /**
     * Examines a URL for sequences with \ but no following u.
     *
     * @param url
     * @return
     */
    private boolean containsInvalidBSSequence(URL url) {
        String str = url.toString();
        int idx = str.indexOf("\\", 0);

        while (idx >= 0) {

            if (idx < str.length() - 1) {
                char c = str.charAt(idx + 1);
                if (c != 'u') {
                    return true;
                }
            } else {
                return true;
            }
            idx = str.indexOf("\\", idx + 1);
        }
        return false;
    }

}
