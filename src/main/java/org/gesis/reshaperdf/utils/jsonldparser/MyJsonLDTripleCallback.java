/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gesis.reshaperdf.utils.jsonldparser;

import com.github.jsonldjava.core.JsonLdTripleCallback;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.core.RDFDataset.Node;
import com.github.jsonldjava.core.RDFDataset.Quad;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 *
 * @author bensmafx
 */
public class MyJsonLDTripleCallback implements JsonLdTripleCallback {

    private static final String GRAPH = "@default";
    private RDFHandler rdfHandler = null;

    public MyJsonLDTripleCallback(RDFHandler aRdfHandler) {
        this.rdfHandler = aRdfHandler;
    }

    @Override
    public Object call(RDFDataset dataset) {
        if (rdfHandler != null) {
            List<Quad> list = dataset.getQuads(GRAPH);
            for (int i = 0; i < list.size(); i++) {
                Quad quad = list.get(i);
                Resource s = null;
                URI p = null;
                Value o = null;

                Node subject = quad.getSubject();
                if (subject.isIRI()) {
                    s = new URIImpl(subject.getValue());
                } else {
                    s = new BNodeImpl(subject.getValue());
                }

                Node predicate = quad.getPredicate();
                if (predicate.isIRI()) {
                    p = new URIImpl(predicate.getValue());
                }

                Node object = quad.getObject();
                if (object.isIRI()) {
                    o = new URIImpl(object.getValue());
                } else if (object.isBlankNode()) {
                    o = new BNodeImpl(object.getValue());
                } else {
                    String lang = object.getLanguage();
                    String tmp_dtype = object.getDatatype();
                    URI dtype = null;
                    if(tmp_dtype != null){
                        dtype = new URIImpl(tmp_dtype);
                    }
                    String label = object.getValue();
                    if (lang == null && dtype != null) {
                        o = new LiteralImpl(label, dtype);
                    } else if (lang != null && dtype == null) {
                        o = new LiteralImpl(label, lang);
                    } else {
                        o = new LiteralImpl(label);
                    }
                }
                Statement st = new StatementImpl(s, p, o);

                try {
                    rdfHandler.handleStatement(st);
                } catch (RDFHandlerException ex) {
                    System.err.println(ex);
                }
            }
        }

        return dataset;
    }

}
