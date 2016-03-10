package org.gesis.reshaperdf.cmd.extractresources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.IResourceHandler;
import org.gesis.reshaperdf.utils.ResourceReader;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

/**
 * Extracts resources with a given predicate-object combination.
 */
public class ExtractResourcesCommand implements ICMD {

    private static final String WILDCARD = "?";
    private static final String NAME = "extractresources";
    private static final String EXPLANATION = "Extracts resources with a given predicate-object combination. Wildcard=" + WILDCARD + ".";
    private static final String HELPTEXT = "Usage: "+NAME+" <infile> <outfile> <predicate> <object> <offset> <length>\n" + EXPLANATION;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getExplanation() {
        return EXPLANATION;
    }

    @Override
    public String getHelptext() {
        return HELPTEXT;
    }

    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length != 7) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }
        File inFile = new File(args[1]);
        if (!inFile.exists() || !inFile.isFile()) {
            return new CommandExecutionResult(false, "Input file is not a valid file.");
        }
        File outFile = new File(args[2]);

        //if a wildcard was set for the predicate, then it is assigned null
        String predicate = args[3];
        if (predicate.equals(WILDCARD)) {
            predicate = null;
        } else {
            try {
                new URL(predicate);
            } catch (MalformedURLException ex) {
                throw new CommandExecutionException("In predicate: " + ex);
            }
        }

        //if a wildcard was set for the object, then it is assigned null
        String object = args[4];
        if (object.equals(WILDCARD)) {
            object = null;
        } else {
            try {
                new URL(object);
            } catch (MalformedURLException ex) {
                throw new CommandExecutionException("In object: " + ex);
            }
        }

        int offset = -2;
        try {
            offset = Integer.valueOf(args[5]);
        } catch (NumberFormatException ex) {
            throw new CommandExecutionException("In offset: " + ex);
        }

        int length = -2;
        try {
            length = Integer.valueOf(args[6]);
        } catch (NumberFormatException ex) {
            throw new CommandExecutionException("In length: " + ex);
        }

        //start acutal processing
        ResourceReader resReader = new ResourceReader();
        //uses a special handler that evaluates the predicate-object combination
        //and writes matching resources into outfile.
        IResourceHandler resHandler = new ExtractResourcesHandler(predicate, object, offset, length, outFile);
        resReader.setResourceHandler(resHandler);
        try {
            resReader.parse(new FileInputStream(inFile), "");
        } catch (FileNotFoundException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFParseException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException(ex);
        } catch (IOException ex) {
            throw new CommandExecutionException(ex);
        }

        return new CommandExecutionResult(true);
    }

}
