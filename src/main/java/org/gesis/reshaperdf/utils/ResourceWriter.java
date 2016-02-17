/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author bensmafx
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
