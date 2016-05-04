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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;

/**
 * Writer that writes a collection of RDF statements.
 * @author Felix Bensmann
 */
public class ResourceWriter {
    
    private CheckedNTriplesWriter writer = null;
    private OutputStream out = null;
    
    public ResourceWriter(File file, IStatementFilter filter) throws FileNotFoundException, RDFHandlerException{
        out = new FileOutputStream(file);
        writer = new CheckedNTriplesWriter(out,filter);
        writer.startRDF();
    }
    
    public void writeResource(Statement[] resource) throws RDFHandlerException{
        for(int i=0; i<resource.length;i++){
            writer.handleStatement(resource[i]);
        }
        
    }
    
    
    public void close() throws IOException, RDFHandlerException{
        writer.endRDF();
        out.close();
    }
    
    
}
