package org.gesis.reshaperdf.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.NTriplesParserSettings;
import org.openrdf.rio.ntriples.NTriplesParser;

/**
 * Reader that uses NTriplesParster to parse sorted NT files. Collects
 * statements that belong to the same resource. These resources can then be
 * handled by a resource handler to be set beforehand.
 */
public class ResourceReader {

    private IResourceHandler resHandler = null;
    private RDFParser ntParser = null;
    private Queue<Statement> resBuffer = null;
    private String currentResource = null; //subject string of current resource

    public ResourceReader() {
        ntParser = new NTriplesParser();
        ntParser.getParserConfig().addNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);
        resBuffer = new LinkedList<Statement>();
        ntParser.setRDFHandler(new RDFHandler() {

            @Override
            public void startRDF() throws RDFHandlerException {
                resBuffer.clear();
                resHandler.onStart();
            }

            @Override
            public void endRDF() throws RDFHandlerException {
                if (!resBuffer.isEmpty()) {
                    resHandler.handleResource(resBuffer.toArray(new Statement[resBuffer.size()]));
                    resBuffer.clear();
                }
                resHandler.onStop();
            }

            @Override
            public void handleNamespace(String prefix, String uri) throws RDFHandlerException {

            }

            @Override
            public void handleStatement(Statement st) throws RDFHandlerException {
                if (currentResource == null) { //statement is the very first one
                    currentResource = st.getSubject().stringValue();
                    resBuffer.add(st);
                } else { //statement is not the first
                    if (st.getSubject().stringValue().equals(currentResource)) {//stmt belongs to the same resource
                        resBuffer.add(st);
                    } else { //stmt belongs to a new resource
                        resHandler.handleResource(resBuffer.toArray(new Statement[resBuffer.size()]));
                        currentResource = st.getSubject().stringValue();
                        resBuffer.clear();
                        resBuffer.add(st);
                    }
                }
            }

            @Override
            public void handleComment(String comment) throws RDFHandlerException {

            }
        }
        );
    }

    public void setResourceHandler(IResourceHandler resHandler) {
        this.resHandler = resHandler;
    }

    public void parse(InputStream is, String baseURI) throws RDFParseException, RDFHandlerException, IOException {
        ntParser.parse(is, baseURI);
    }

}
