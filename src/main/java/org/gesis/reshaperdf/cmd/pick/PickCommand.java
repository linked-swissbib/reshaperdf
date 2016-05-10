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
package org.gesis.reshaperdf.cmd.pick;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.LineWriter;
import org.gesis.reshaperdf.utils.PullReader;
import org.openrdf.model.Statement;

/**
 * Extracts either subject predicate or objects according to a pattern and
 * stores the result into a list.
 *
 * @author Felix Bensmann
 */
public class PickCommand implements ICMD {

    private static final String QUERYCHAR = "?";
    private static final String WILDCARD = "%";

    private String NAME = "pick";
    private String EXPLANATION = "Extracts either subject predicate or objects "
            + "according to a pattern and stores the result into a list. "
            + "Example: subject URI predicate URI " + QUERYCHAR + " "
            + "this returns all objects whose statments match subject URI and predicate URI. "
            + "A " + WILDCARD + " character can be used to indicate a wildcard";
    private String HELPTEXT = "Usage: " + NAME + " <infile> <outfile> <s> <p> <o>\n" + EXPLANATION;

    public PickCommand() {

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
     * Executes the command.
     *
     * @param args cli args. args[0] is the name of this command.
     * @return
     * @throws CommandExecutionException
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length != 6) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }
        //check infile
        File inFile = new File(args[1]);
        if (!inFile.exists() || !inFile.isFile()) {
            return new CommandExecutionResult(false, "Invalid file: " + inFile.getAbsolutePath());
        }
        //assign other values
        File outFile = new File(args[2]);
        String subject = args[3];
        String predicate = args[4];
        String object = args[5];

        //check and determine pattern
        boolean patternQXX = subject.equals(QUERYCHAR) && !predicate.equals(QUERYCHAR) && !object.equals(QUERYCHAR);
        boolean patternXQX = !subject.equals(QUERYCHAR) && predicate.equals(QUERYCHAR) && !object.equals(QUERYCHAR);
        boolean patternXXQ = !subject.equals(QUERYCHAR) && !predicate.equals(QUERYCHAR) && object.equals(QUERYCHAR);
        if (!(patternQXX || patternXQX || patternXXQ)) { //only one can be true, or all are false
            return new CommandExecutionResult(false, "Invalid pattern. " + getHelptext());
        }

        //process input
        //setup reader and writer
        PullReader pReader = new PullReader(inFile);
        pReader.load();
        LineWriter lWriter = null;
        try {
            lWriter = new LineWriter(outFile);
        } catch (FileNotFoundException ex) {
            return new CommandExecutionResult(false, "Invalid output file " + outFile.getAbsolutePath());
        }
        //actual work
        while (pReader.peek() != null) {
            Statement stmt = pReader.peek();
            pReader.removeHead();
            if (patternQXX) {
                if (wildcardEquals(stmt.getPredicate().stringValue(), predicate)
                        && wildcardEquals(stmt.getObject().stringValue(), object)) {
                    try {
                        lWriter.writeLine(stmt.getSubject().stringValue());
                    } catch (IOException ex) {
                        throw new CommandExecutionException("Error when write to outfile. " + ex);
                    }
                }
            } else if (patternXQX) {
                if (wildcardEquals(stmt.getSubject().stringValue(), subject) && wildcardEquals(stmt.getObject().stringValue(), object)) {
                    try {
                        lWriter.writeLine(stmt.getPredicate().stringValue());
                    } catch (IOException ex) {
                        throw new CommandExecutionException("Error when write to outfile. " + ex);
                    }
                }
            } else if (patternXXQ) {
                if (wildcardEquals(stmt.getSubject().stringValue(), subject) && wildcardEquals(stmt.getPredicate().stringValue(), predicate)) {
                    try {
                        lWriter.writeLine(stmt.getObject().stringValue());
                    } catch (IOException ex) {
                        throw new CommandExecutionException("Error when write to outfile. " + ex);
                    }
                }
            }
            //else... other statements are dropped
        }
        try {
            lWriter.close();
        } catch (IOException ex) {
            throw new CommandExecutionException("Error when closing outfile. " + ex);
        }

        return new CommandExecutionResult(true);
    }

    /**
     * Like String.equals(String) but considers a wildcard character in the
     * pattern.
     *
     * @param text Text to compare
     * @param pattern Pattern to match can be a text or whole pattern can be the
     * wildcard.
     * @return
     */
    private static boolean wildcardEquals(String text, String pattern) {
        if (pattern.equals(WILDCARD)) {
            return true;
        }
        return text.equals(pattern);
    }

}
