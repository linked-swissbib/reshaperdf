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
package org.gesis.reshaperdf.cmd.merge;

import java.io.File;
import java.io.FileNotFoundException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.MergeUtils;
import org.openrdf.rio.RDFHandlerException;

/**
 * @author Felix Bensmann
 * Merges a couple of sorted NTriple files.
 */
public class MergeCommand implements ICMD {

    private static final String NAME = "merge";
    private static final String EXPLANATION = "Merges multiple sorted N-Triple files.";
    private static final String HELPTEXT = "Usage: "+NAME+" <outfile> <infile1> <infile2> [<infile3>...]\n"+EXPLANATION;

    public MergeCommand() {

    }

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
     * Executes this command. Uses the merge utils for actual processing.
     * @param args
     * @return
     * @throws CommandExecutionException 
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length < 4) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }

        File outFile = new File(args[1]);

        File[] fileArr = new File[args.length - 2];
        for (int i = 0; i < args.length - 2; i++) {
            fileArr[i] = new File(args[i + 2]);
        }
        for (File f : fileArr) {
            if (!f.isFile() || !f.exists()) {
                return new CommandExecutionResult(false, "Invalid file: " + f.getAbsolutePath());
            }
        }

        try {
            //use method from merge utils
            MergeUtils.merge(fileArr, outFile, null);
        } catch (FileNotFoundException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException(ex);
        } catch(IllegalArgumentException ex){
            throw new CommandExecutionException(ex);
        }
        return new CommandExecutionResult(true);
        
        
    }

}
