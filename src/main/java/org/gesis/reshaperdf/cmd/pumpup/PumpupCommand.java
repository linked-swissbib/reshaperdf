package org.gesis.reshaperdf.cmd.pumpup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.CheckedNTriplesWriter;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.NTriplesParserSettings;
import org.openrdf.rio.ntriples.NTriplesParser;

/**
 * Extends namespace from short form to long form. Long forms are kept. This
 * class consults a text file to find mappings between short and long forms.
 */
public class PumpupCommand implements ICMD {

    public String NAME = "pumpup";
    public String EXPLANATION = "Extends the namespaces in an NTriple file to thier long forms.";
    public String HELPTEXT = "Usage: pumpup <infile> <outfile>\n" + EXPLANATION;
    public int linesPerFile = 1000000;

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

    /**
     * Executes this command. Reads input files and writes them using the
     * CheckedNTriplesWriter. This writer does the namespace extension by
     * default.
     *
     * @param args
     * @return
     * @throws CommandExecutionException
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length != 3) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }
        File inFile = new File(args[1]);
        if (!inFile.exists() || !inFile.isFile()) {
            return new CommandExecutionResult(false, "Input file is not a valid file.");
        }
        File outFile = new File(args[2]);

        //prepare parster
        RDFParser parser = new NTriplesParser();
        parser.getParserConfig().addNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);

        try {
            //use CheckedNTriplesWriter as RDFHandler
            parser.setRDFHandler(new CheckedNTriplesWriter(new FileOutputStream(outFile), null));
        } catch (FileNotFoundException ex) {
            throw new CommandExecutionException(ex);
        }
        try {
            //read input file stmt by stmt
            parser.parse(new FileInputStream(inFile), "");
        } catch (IOException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFParseException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException(ex);
        }
        return new CommandExecutionResult(true);
    }
}
