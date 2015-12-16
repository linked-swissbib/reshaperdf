package org.gesis.reshaperdf.cmd.extractresources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import org.gesis.reshaperdf.utils.CheckedNTriplesWriter;
import org.gesis.reshaperdf.utils.IResourceHandler;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;

/**
 * A handler for a ResourceRader. It implements a resource extranction process.
 * Resources are sent to it one by one. The handler compares them to its
 * matching criteria an copys matching resources to a specified file.
 */
public class ExtractResourcesHandler implements IResourceHandler {

    private static final int ALL = -1;
    private String pred = null;
    private String obj = null;
    private int off = -2;
    private int len = -2;
    private File outFile = null;
    private CheckedNTriplesWriter writer = null;
    private long resNr = 0;

    /**
     * Ctor for this extraction hander.
     *
     * @param pred The predicate to use. Use null for wildcard behavior.
     * @param obj The object to use. Use null for wildcard behavior.
     * @param off The resources in the source file to skip until extracting.
     * @param len The number of resources to extract. Use -1 to indicate to
     * extract all available matches.
     * @param outFile The file for the result.
     */
    public ExtractResourcesHandler(String pred, String obj, int off, int len, File outFile) {
        this.pred = pred;
        this.obj = obj;
        this.off = off;
        if (len == -1) {
            this.len = Integer.MAX_VALUE;
        } else {
            this.len = len;
        }
        this.outFile = outFile;
    }

    @Override
    public void onStart() {
        try {
            writer = new CheckedNTriplesWriter(new FileOutputStream(outFile), null);
            writer.startRDF();
        } catch (FileNotFoundException ex) {
            System.err.println("When starting writer: " + ex);
            System.exit(-1);
        } catch (RDFHandlerException ex) {
            System.err.println("When starting writer: " + ex);
            System.exit(-1);
        }

    }

    /**
     * Receives resources, checks matching and copies matching resources to the
     * specified file.
     *
     * @param res
     */
    @Override
    public void handleResource(Statement[] res) {
        resNr++;
        long hlp = off + len;
        if (resNr >= off && resNr <= hlp) { //only do sth. when within the range defined by offset and length
            for (Statement st : res) { //for each statement in the current resource ...
                boolean predOk = false; //set to true if predicate matches
                boolean objOk = false;  //set to true if object matches
                if (pred != null) {    //check predicate
                    if (st.getPredicate().stringValue().equals(pred)) {
                        predOk = true;
                    }
                } else {
                    predOk = true;
                }

                if (obj != null) {  //check object
                    if (st.getObject().stringValue().equals(obj)) {
                        objOk = true;
                    }
                } else {
                    objOk = true;
                }
                if (predOk && objOk) {   //extract resource
                    try {
                        writeResource(res);
                    } catch (RDFHandlerException ex) {
                        System.err.println("When writing: " + ex);
                        System.exit(-1);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onStop() {
        try {
            writer.endRDF();
        } catch (RDFHandlerException ex) {
            System.err.println("When stopping writer: " + ex);
            System.exit(-1);
        }
    }

    private void writeResource(Statement[] res) throws RDFHandlerException {
        for (int i = 0; i < res.length; i++) {
            writer.handleStatement(res[i]);
        }
    }

}
