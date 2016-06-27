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
package org.gesis.reshaperdf.cmd.filter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.BlackListPropertyFilter;
import org.gesis.reshaperdf.utils.CheckedNTriplesWriter;
import org.gesis.reshaperdf.utils.LineReader;
import org.gesis.reshaperdf.utils.PullReader;
import org.gesis.reshaperdf.utils.WhiteListPropertyFilter;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;

/**
 * @author Felix Bensmann
 * Removes statements from an N-Triple file according to a filter.
 */
public class FilterCommand implements ICMD {

    private static final String NAME = "filter";
    private static final String EXPLANATION = "Removes statments from an N-Triple file.";
    private static final String HELPTEXT = "Usage: " + NAME + " <whitelist|blacklist> <source file> <filter file> <outfile> \n" + EXPLANATION;

    private static final String FILTER_TYPE_WHITELIST = "whitelist";
    private static final String FILTER_TYPE_BLACKLIST = "blacklist";

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
     * Executes the command. Removes statements from an N-Triple file according
     * to a filter.
     *
     * @param args
     * @return
     * @throws CommandExecutionException
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length != 5) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }

        String filterType = args[1];
        if (!filterType.equalsIgnoreCase(FILTER_TYPE_WHITELIST) && !filterType.equalsIgnoreCase(FILTER_TYPE_BLACKLIST)) {
            return new CommandExecutionResult(false, "Filter type must be either " + FILTER_TYPE_WHITELIST + " or " + FILTER_TYPE_BLACKLIST);
        }

        File inFile = new File(args[2]);
        if (!inFile.exists() || !inFile.isFile()) {
            return new CommandExecutionResult(false, "Source file is not a valid file.");
        }

        File filterFile = new File(args[3]);
        if (!filterFile.exists() || !filterFile.isFile()) {
            return new CommandExecutionResult(false, "Filter file "+filterFile.getName()+" is not a valid file.");
        }
        String[] proptertyArr = null;
        try {
            LineReader lR = new LineReader(filterFile);
            String line = lR.readLine();
            ArrayList<String> propertyList = new ArrayList<String>();
            while (line != null) {
                propertyList.add(line);
                line = lR.readLine();
            }
            lR.close();
            proptertyArr = propertyList.toArray(new String[propertyList.size()]);
        } catch (IOException ex) {
            throw new CommandExecutionException("Error reading filter file. " + ex);
        }

        File outFile = new File(args[4]);

        //open reader and writer
        PullReader reader = new PullReader(inFile);
        reader.load();

        CheckedNTriplesWriter writer = null;
        try {
            if (filterType.equalsIgnoreCase(FILTER_TYPE_WHITELIST)) {
                writer = new CheckedNTriplesWriter(new FileOutputStream(outFile), new WhiteListPropertyFilter(proptertyArr));

            } else {
                writer = new CheckedNTriplesWriter(new FileOutputStream(outFile), new BlackListPropertyFilter(proptertyArr));

            }
        } catch (FileNotFoundException ex) {
            throw new CommandExecutionException("Error when creating writer. " + ex);
        }

        //do filtering
        Statement stmt = reader.peek();
        reader.removeHead();
        try {
            writer.startRDF();
            while (stmt != null) {
                writer.handleStatement(stmt);
                stmt = reader.peek();
                reader.removeHead();
            }
            writer.endRDF();
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException("Error when writing statement. " + ex);
        }

        return new CommandExecutionResult(true);
    }
}
