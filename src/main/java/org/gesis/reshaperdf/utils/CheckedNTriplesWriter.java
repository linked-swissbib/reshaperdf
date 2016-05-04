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

import java.io.FileNotFoundException;
import java.io.OutputStream;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.ntriples.NTriplesWriter;

/**
 * @author Felix Bensmann
 * Writer for NTriples. Checks given NTriples for correct format, then writes a
 * correct statement or drops an invalid one.
 */
public class CheckedNTriplesWriter extends NTriplesWriter {

    private IStatementFilter filter = null;
    
    /**
     * Ctor
     *
     * @param out Output stream to write to.
     * @param filter A specialized filter to check statemenst for validity. Use
     * null if no filtering is to be applied.
     */
    public CheckedNTriplesWriter(OutputStream out, IStatementFilter filter) throws FileNotFoundException {
        super(out);
        this.filter = filter;
    }
    
    

    /**
     * Processes the statements. Filters invalid statements. Statements that
     * pass the filter are written with extended namespace forms.
     *
     * @param stmt
     * @throws RDFHandlerException
     */
    @Override
    public void handleStatement(Statement stmt) throws RDFHandlerException {
        //Statement st = correctObject(stmt);
        Statement st = stmt;

        if (filter != null) { //if a filter is set...

            if (filter.accept(st)) { // ...use it
                super.handleStatement(getExtStmt(st));
            } else {
                //Message to System.out if a statements was sorted out.
                System.out.println(filter.getLastCause()); 
            }
        } else {
            super.handleStatement(getExtStmt(st));
        }

    }

    /**
     * Replaces the namespace in a statement with is long form.
     * @param st
     * @return 
     */
    private Statement getExtStmt(Statement st) {

        //Examine subject
        Resource subj = null;
        //URI case
        if( (st.getSubject() instanceof URI) && !(st.getSubject() instanceof BNode)){ //could be blank node
            String s = NSResolver.getInstance().blowUp(st.getSubject().stringValue());
            subj = new URIImpl(s);
        }
        //BNode case
        else if(st.getSubject() instanceof BNode){
            subj = st.getSubject();
        }
        //Examine predicate - always URI
        String p = NSResolver.getInstance().blowUp(st.getPredicate().stringValue());
        URI pred = new URIImpl(p);
        //Examine object
        Value obj=null;
        if (st.getObject() instanceof URI) {
            String o = NSResolver.getInstance().blowUp(st.getObject().stringValue());
            obj = new URIImpl(o);
        } else if(st.getObject() instanceof BNode){
            obj = st.getObject();
        }
        else if(st.getObject() instanceof Literal){
            obj = st.getObject();
        }
        return new StatementImpl(subj, pred, obj);
    }

 

    /**
     * Searches in a String for surrogate sequences \uD800 - \uFFFF in plain
     * text these are not valid in UTF-8 (only in UTF-16) Source:
     * https://de.wikipedia.org/wiki/UTF-8
     *
     * @param s The String to examine.
     * @return true if a surrogate character is present, false otherwise.
     */
    private static boolean containsBadSequence(String s) {

        int idx = 0;
        idx = s.indexOf("\\u", idx);
        while (idx != -1) {
            String subsequ = s.substring(idx + 2, idx + 6);
            int hex = 0;
            try {
                hex = Integer.valueOf(subsequ, 16);
            } catch (NumberFormatException nfe) {
                return true;
            }
            if (hex >= 0xD800 && hex <= 0xFFFF) {
                return true;
            }
            idx = s.indexOf("\\u", idx + 7);
        }
        return false;
    }

    /**
     * Searches in a String for surrogate sequences and replaces the surrogates
     * with '?' \uD800 - \uFFFF in plain text these are not valid in UTF-8 (only
     * in UTF-16) Source: https://de.wikipedia.org/wiki/UTF-8
     *
     * @param s The String to examine
     * @return The manipulated string
     */
    private static String replaceBadSequence(String s) {

        String str = s;
        int idx = 0;
        idx = str.indexOf("\\u", idx);
        while (idx != -1) {
            String subsequ = str.substring(idx + 2, idx + 6);
            int hex = 0;
            try {
                hex = Integer.valueOf(subsequ, 16);
            } catch (NumberFormatException nfe) {
                return null;
            }
            if (hex >= 0xD800 && hex <= 0xFFFF) {
                str = str.replace("\\u" + subsequ, "?");
            }
            idx = str.indexOf("\\u", idx + 1);
        }
        return str;

    }
    
    

}
