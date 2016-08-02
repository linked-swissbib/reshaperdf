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
package org.gesis.reshaperdf.cmd.ntriplify;

import com.fasterxml.jackson.core.JsonParseException;
import com.github.jsonldjava.core.JsonLdError;
import org.gesis.reshaperdf.utils.FileFinder;
import org.gesis.reshaperdf.utils.CheckedNTriplesWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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

/**
 * @author Felix Bensmann
 * Takes an input directory and merges all RDF files into an (unsorted) NTriples
 * file.
 */
public class NTriplifyCommand implements ICMD {

    public String NAME = "ntriplify";
    public String EXPLANATION = "Takes an input directory and merges all RDF files into an NTriples file.";
    public String HELPTEXT = "Usage: ntriplify <input dir> <outfile> [<JSON-LD context path> <JSON-LD context file>][...] \nTakes an input directory and merges all RDF files into an NTriples file.";

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
        if (args.length < 3 || args.length % 2 == 0) { //case there are less than 3 parameters or the cound of params is even
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }
        File inDir = new File(args[1]);
        if (!inDir.exists() || !inDir.isDirectory()) {
            return new CommandExecutionResult(false, "Input dir is not a valid directory.");
        }
        File outFile = new File(args[2]);

        int off = 3;
        int x = args.length - off;
        Map<String, File> map = new HashMap<String, File>();
        for (int i = 0; i < x / 2; i += 2) {
            File contextFile = new File(args[off + i + 1]);
            if (!contextFile.exists() || !contextFile.isFile()) {
                return new CommandExecutionResult(false, "Context file " + contextFile.getName() + " is not a valid file.");
            }
            map.put(args[off + i], contextFile);
        }

        //search in the given directory and its subdirectories for files with the extensions
        //xml, rdf, nt, jsonld
        File[] inputFiles = FileFinder.findFiles(inDir, new String[]{"xml", "rdf", "nt", "jsonld", "ttl"});

        //use special N-Triples writer that only writes valid N-Triples and drops invalid
        RDFWriter ntriplesWriter = null;
        try {
            ntriplesWriter = new CheckedNTriplesWriter(new FileOutputStream(outFile), new StrictStatementFilter(), true);

        } catch (FileNotFoundException ex) {
            return new CommandExecutionResult(false, "Out file " + outFile.getAbsolutePath() + " was not found.");
        }
        for (int i = 0; i < inputFiles.length; i++) {
            System.out.println("#" + (i + 1) + " Processing " + inputFiles[i].getName());
            RDFFormat format = Rio.getParserFormatForFileName(inputFiles[i].getAbsolutePath());

            try {
                if (format.equals(RDFFormat.RDFXML) || format.equals(RDFFormat.NTRIPLES) || format.equals(RDFFormat.TURTLE)) {
                    RDFParser rdfParser = Rio.createParser(format);
                    // link our parser to our writer...
                    rdfParser.setRDFHandler(ntriplesWriter);
                    // set tollerance towards certain malformed ntriples
                    rdfParser.getParserConfig().addNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);
                    // ...and start the conversion!
                    FileInputStream fis = new FileInputStream(inputFiles[i]);
                    rdfParser.parse(fis, "");
                    fis.close();
                } else if (format.equals(RDFFormat.JSONLD)) {
                    org.gesis.reshaperdf.utils.jsonldparser.JSONLDParser rdfParser = new org.gesis.reshaperdf.utils.jsonldparser.JSONLDParser();
                    rdfParser.setRdfHandler(ntriplesWriter);
                    FileInputStream fis = new FileInputStream(inputFiles[i]);
                    rdfParser.parse(fis, map);
                    fis.close();
                }
            } catch (JsonParseException ex) {
                System.err.println("File: " + inputFiles[i] + " " + ex.getMessage());
                closeWriter(ntriplesWriter);
            } catch (RDFHandlerException ex) {
                return new CommandExecutionResult(false, ex + " When processing file " + inputFiles[i].getAbsolutePath() + ".");
            } catch (FileNotFoundException ex) {
                return new CommandExecutionResult(false, ex + " When processing file " + inputFiles[i].getAbsolutePath() + ".");
            } catch (IOException ex) {
                return new CommandExecutionResult(false, ex + " When processing file " + inputFiles[i].getAbsolutePath() + ".");
            } catch (RDFParseException ex) {
                System.err.println("File: " + inputFiles[i] + " " + ex.getMessage());
                closeWriter(ntriplesWriter);
            } catch (JsonLdError ex) {
                System.err.println("File: " + inputFiles[i] + " " + ex.getMessage());
                closeWriter(ntriplesWriter);
            } 

        }
        System.out.println("Done");
        return new CommandExecutionResult(true);
    }

    private static void closeWriter(RDFWriter writer) {
        try {
            writer.endRDF();
        } catch (RDFHandlerException ex) {
            System.err.println("Error on closing writer: "+ex);
        }
    }

}
