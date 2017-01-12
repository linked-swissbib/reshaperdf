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
package org.gesis.reshaperdf.cmd.split;


import java.io.File;
import java.io.FileNotFoundException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.Splitter;
import org.openrdf.rio.RDFHandlerException;

/**
 * @author Felix Bensmann
 * Splits a given sorted N-Triples into equal parts.
 */
public class SplitCommand implements ICMD {

    public String NAME = "split";
    public String EXPLANATION = "Splits a sorted N-Triple file into several smaller files, with a given number of resources.";
    public String HELPTEXT = "Usage: "+NAME+" <input file> <output file prefix> <resources per file>\n"+EXPLANATION;
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
     * Executes this command. Uses Splitter from Utils package.
     * @param args
     * @return
     * @throws CommandExecutionException 
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length != 4) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }
        File inFile = new File(args[1]);
        if (!inFile.exists() || !inFile.isFile()) {
            return new CommandExecutionResult(false, "Input file is not a valid file.");
        }
        File outFilePrefix = new File(args[2]);
        int resourcesPerFile = Integer.valueOf(args[3]);

        try {
            //use Splitter class
            Splitter.split(inFile, outFilePrefix.getAbsolutePath(), resourcesPerFile);
        } catch (FileNotFoundException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException(ex);
        }

        System.out.println("Done");
        return new CommandExecutionResult(true);
    }
    

    
}
