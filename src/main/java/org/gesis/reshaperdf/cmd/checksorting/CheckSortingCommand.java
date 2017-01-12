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
package org.gesis.reshaperdf.cmd.checksorting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.PullReader;
import org.gesis.reshaperdf.utils.StatementsComparatorSPO;
import org.openrdf.model.Statement;

/**
 * @author Felix Bensmann
 * Checks the sorting of an NTriple file.
 */
public class CheckSortingCommand implements ICMD {

    public String NAME = "checksorting";
    public String EXPLANATION = "Checks the input file for proper sorting. This sorting differs from plain line sorting in the fact that it ignores the control characters.";
    public String HELPTEXT = "Usage: " + NAME + " <infile> \n" + EXPLANATION;

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
     * Checks the sorting of an NTriple file.
     *
     * @param args
     * @return
     * @throws CommandExecutionException
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length != 2) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }
        File inFile = new File(args[1]);
        if (!inFile.exists() || !inFile.isFile()) {
            return new CommandExecutionResult(false, "Input file is not a valid file.");
        }

        //use pull reader
        PullReader pullReader = new PullReader(inFile);
        pullReader.load();
        Statement last = pullReader.peek();
        pullReader.removeHead();

        //compare each statement with its successor
        Statement curr = pullReader.peek();
        pullReader.removeHead();
        Comparator<Statement> comparator = new StatementsComparatorSPO();
        while (curr != null) {
            if (comparator.compare(last, curr) > 0) {
                System.out.println("Not sorted");
                System.out.println("Last line=" + last);
                System.out.println("Cur. line=" + curr);
                return new CommandExecutionResult(true);
            }
            last = curr;
            curr = pullReader.peek();
            pullReader.removeHead();
        }

        System.out.println("Sorted");
        return new CommandExecutionResult(true);
    }
}
