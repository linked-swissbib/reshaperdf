package org.gesis.reshaperdf.cmd.ntriplify;

import org.gesis.reshaperdf.utils.FileFinder;
import org.gesis.reshaperdf.utils.CheckedNTriplesWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.StrictStatementFilter;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.NTriplesParserSettings;
import org.openrdf.rio.ntriples.NTriplesParser;

/**
 * Takes an input directory and merges all RDF files into an (unsorted) NTriples
 * file.
 */
public class NTriplifyCommand implements ICMD {

    public String NAME = "ntriplify";
    public String EXPLANATION = "Takes an input directory and merges all RDF files into an NTriples file.";
    public String HELPTEXT = "Usage: ntriplify <input dir> <outfile>\nTakes an input directory and merges all RDF files into an NTriples file.";

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
     * Executes this command. Iterates over all files in the input dir and its
     * subdirectories, and adds them one by one to an N-Triples file.
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
        File inDir = new File(args[1]);
        if (!inDir.exists() || !inDir.isDirectory()) {
            return new CommandExecutionResult(false, "Input dir is not a valid directory.");
        }
        File outFile = new File(args[2]);

        //search in the given directory and its subdirectories for files with the extensions
        //xml, rdf, nt, jsonld
        File[] inputFiles = FileFinder.findFiles(inDir, new String[]{"xml", "rdf", "nt", "jsonld"});

        //use special N-Triples writer that only writes valid NTriples and drops invalid
        RDFWriter ntriplesWriter = null;
        try {
            ntriplesWriter = new CheckedNTriplesWriter(new FileOutputStream(outFile), new StrictStatementFilter());
        } catch (FileNotFoundException ex) {
            return new CommandExecutionResult(false, "Out file " + outFile.getAbsolutePath() + " was not found.");
        }

        for (int i = 0; i < inputFiles.length; i++) {
            System.out.println("#" + (i + 1) + " Processing " + inputFiles[i].getName());
            RDFFormat format = Rio.getParserFormatForFileName(inputFiles[i].getAbsolutePath());
            RDFParser rdfParser = Rio.createParser(format);
            // link our parser to our writer...
            rdfParser.setRDFHandler(ntriplesWriter);
            // set tollerance towards uncool ntriples
            rdfParser.getParserConfig().addNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);
            // ...and start the conversion!
            try {
                rdfParser.parse(new FileInputStream(inputFiles[i]), "");
            } catch (RDFHandlerException ex) {
                return new CommandExecutionResult(false, ex + " When processing file " + inputFiles[i].getAbsolutePath() + ".");
            } catch (FileNotFoundException ex) {
                return new CommandExecutionResult(false, ex + " When processing file " + inputFiles[i].getAbsolutePath() + ".");
            } catch (IOException ex) {
                return new CommandExecutionResult(false, ex + " When processing file " + inputFiles[i].getAbsolutePath() + ".");
            } catch (RDFParseException ex) {
                System.out.println("File: " + inputFiles[i] + " " + ex.getMessage());
                try {
                    ntriplesWriter.endRDF();
                } catch (RDFHandlerException ex1) {
                    return new CommandExecutionResult(false, ex1 + " When processing file " + inputFiles[i].getAbsolutePath() + ".");
                }
            }
        }
        System.out.println("Done");
        return new CommandExecutionResult(true);
    }

}
