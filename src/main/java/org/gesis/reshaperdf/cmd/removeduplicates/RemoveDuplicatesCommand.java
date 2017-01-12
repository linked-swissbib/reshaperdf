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
package org.gesis.reshaperdf.cmd.removeduplicates;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;

/**
 * @author Felix Bensmann
 * Removes duplicates in a sorted N-Triples file.
 */
public class RemoveDuplicatesCommand implements ICMD {

    public String NAME = "removeduplicates";
    public String EXPLANATION = "Removes duplicate statements from a sorted N-Triples file.";
    public String HELPTEXT = "Usage: " + NAME + " <input file> <output file>\n" + EXPLANATION;

    public RemoveDuplicatesCommand() {

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
     * Executes this command. Is line based. Reads the N-Triples as lines and
     * compares it to its predecessor. If the lines do not equal the current
     * line is written to the target file.
     *
     * @param args
     * @return
     * @throws CommandExecutionException
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length < 3) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }

        File inFile = new File(args[1]);
        if (!inFile.exists() && !inFile.isFile()) {
            return new CommandExecutionResult(false, "Invalid file: " + inFile.getAbsolutePath());
        }

        File outFile = new File(args[2]);

        try {
            //prepare reader
            BufferedReader br = new BufferedReader(new FileReader(inFile));
            String line = br.readLine();
            BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));

            String lastLine = "";
            while (line != null) {
                if (!line.equals(lastLine)) { //compare, write if file does not equal its predecessor
                    bw.write(line + "\n");
                }
                lastLine = line;
                line = br.readLine();
            }
            bw.close();
            br.close();

        } catch (FileNotFoundException ex) {
            throw new CommandExecutionException(ex);
        } catch (IOException ex) {
            throw new CommandExecutionException(ex);
        }
        return new CommandExecutionResult(true);
    }
}
