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
package org.gesis.reshaperdf.cmd.sort;

import java.io.File;
import java.io.IOException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.sort.Sort;
import org.openrdf.rio.RDFHandlerException;

/**
 * @author Felix Bensmann Sorts an N-Triple file alphabetically. Uses the sort
 * utilities from the ...utils.sort-package.
 */
public class SortCommand implements ICMD, Thread.UncaughtExceptionHandler {

    public String NAME = "sort";
    public String EXPLANATION = "Sorts an N-Triple file in ascending order of codepoints.";
    public String HELPTEXT = "Usage: "+NAME+" <input file> <output file>\n"+EXPLANATION;

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
        if (args.length != 3) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }
        File inFile = new File(args[1]);
        if (!inFile.exists() || !inFile.isFile()) {
            return new CommandExecutionResult(false, "Input file is not a valid file.");
        }

        File outFile = new File(args[2]);

        try {
            //Sort.sort(inFile,outFile,this);
            Sort.sort(inFile, outFile);
        } catch (InterruptedException ex) {
            throw new CommandExecutionException(ex);
        } catch (IOException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException(ex);
        }

        return new CommandExecutionResult(true);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.err.println("Error in thread " + t.getName() + ": " + e.getMessage());
        System.exit(-2);
    }

}
