/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gesis.reshaperdf.utils.jsonldparser;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 *
 * @author bensmafx
 */
public class JSONLDParser {

    private RDFHandler rdfHandler = null;

    public JSONLDParser() {
    }

    public RDFHandler getRdfHandler() {
        return rdfHandler;
    }

    public void setRdfHandler(RDFHandler rdfHandler) {
        this.rdfHandler = rdfHandler;
    }

    public void parse(InputStream inputStream, Map<String,File>map) throws RDFHandlerException, IOException, JsonLdError {
        if(rdfHandler!= null){
            rdfHandler.startRDF();
            
            //read file and parse
            Object jsonObject = JsonUtils.fromInputStream(inputStream);
      
            // Create an instance of JsonLdOptions with customized options
            JsonLdOptions options = new JsonLdOptions();
            options.useNamespaces=true;
            options.outputForm="N-Triples";
            //apply dirty redirect FileDocumentLoader
            FileDocumentLoader fLoader = new FileDocumentLoader(map);
            options.setDocumentLoader(fLoader);
            
            //convert to RDF
            MyJsonLDTripleCallback cb = new MyJsonLDTripleCallback(rdfHandler);
            Object obj = JsonLdProcessor.toRDF(jsonObject,cb,options);
            
            
            rdfHandler.endRDF();
        }
        
        
        
    
        
    }

}
