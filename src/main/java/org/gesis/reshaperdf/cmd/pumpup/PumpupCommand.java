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
 * @author Felix Bensmann
 * Extends namespace from short form to long form. Long forms are kept. This
 * class consults a text file to find mappings between short and long forms.
 */
public class PumpupCommand implements ICMD {

    public String NAME = "pumpup";
    public String EXPLANATION = "Extends the namespaces in an N-Triples file to thier long forms. Uses the namespaces as stated below. "
            + "The file \"namespaces.txt\" specifying these namespaces comes along with the binaries and can be adapted to custom needs."
            + "Often commands already include this functionality.";
    public String HELPTEXT = "Usage: pumpup <input file> <output file>\n" + EXPLANATION;

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
