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
package org.gesis.reshaperdf.cmd.extractduplicatelinks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.CheckedNTriplesWriter;
import org.gesis.reshaperdf.utils.PullReader;
import org.gesis.reshaperdf.utils.StatementsComparatorO;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.NTriplesParserSettings;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * @author Felix Bensmann
 * Extracts statements with duplicate subjects and objects.
 */
public class ExtractDuplicateLinksCommand implements ICMD {

    public String NAME = "extractduplicates";
    public String EXPLANATION = "Extracts statements with duplicate subjects and objects.";
    public String HELPTEXT = "Usage: " + NAME + " <infile> \n" + EXPLANATION;

    private static String TMP = ".tmp";
    private static String UNIQUES = "uniques";
    private static String SUBS = "subs";
    private static String OBJS = "objs";

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
     * Executes this command. Uses two steps. First extracts links that share a
     * subject. Second extracts links that share an object.
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

        //prepare output files
        File subs = getMultipleSubjectFileName(inFile); //file with duplicate subs
        File objs = getMulitpleObjectFileName(inFile); //file with duplicate objs

        PullReader pReader = new PullReader(inFile);
        pReader.load();
        if (pReader.isEmpty()) {
            throw new CommandExecutionException("File is empty.");
        }

        Statement last = pReader.peek();
        pReader.removeHead();

        try {
            CheckedNTriplesWriter writer = new CheckedNTriplesWriter(new FileOutputStream(subs), null);
            writer.startRDF();

            //step 1: Since the triples are sorted, we can compare the subjects 
            //with its following. If they match both triples are writen to the subjects file.
            boolean serie = false;
            while (!pReader.isEmpty()) {
                Statement stmt = pReader.peek();
                pReader.removeHead();
                if (stmt.getSubject().stringValue().equals(last.getSubject().stringValue())) {
                    if (!serie) {
                        writer.handleStatement(last);
                        serie = true;
                    }
                    writer.handleStatement(stmt);
                } else {
                    serie = false;
                }
                last = stmt;
            }
            writer.endRDF();
        } catch (FileNotFoundException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException(ex);
        }

        //step 2 : Sorts the triples by their objects and does the same.
        Queue<Statement> q = (Queue<Statement>) readNSortObjects(inFile);

        if (q.isEmpty()) {
            return new CommandExecutionResult(true);
        }
        last = q.peek();
        q.remove();
        try {
            CheckedNTriplesWriter writer = new CheckedNTriplesWriter(new FileOutputStream(objs), null);
            writer.startRDF();
            boolean serie = false;
            while (!q.isEmpty()) {
                Statement stmt = q.peek();
                q.remove();
                if (stmt.getObject().stringValue().equals(last.getObject().stringValue())) {
                    if (!serie) {
                        writer.handleStatement(last);
                        serie = true;
                    }
                    writer.handleStatement(stmt);
                } else {
                    serie = false;
                }
                last = stmt;
            }

            writer.endRDF();
        } catch (FileNotFoundException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException(ex);
        }

        return new CommandExecutionResult(true);

    }

    /**
     * Attach a statement to a file.
     * @param st
     * @param file
     * @throws FileNotFoundException
     * @throws RDFHandlerException
     * @throws IOException 
     */
    private static void write(Statement st, File file) throws FileNotFoundException, RDFHandlerException, IOException {
        OutputStream out = new FileOutputStream(file, true);
        CheckedNTriplesWriter writer = new CheckedNTriplesWriter(out, null);
        writer.startRDF();
        writer.handleStatement(st);
        writer.endRDF();
        out.close();

    }

    /**
     * Composes the name for the subjects file.
     * @param inFile
     * @return 
     */
    private static File getMultipleSubjectFileName(File inFile) {
        String str = inFile.getName();
        int idx = str.lastIndexOf(".");
        str = str.substring(0, idx) + "_" + SUBS + ".nt";
        return new File(str);
    }

    /**
     * Composes the name for the objects file.
     * @param inFile
     * @return 
     */
    private static File getMulitpleObjectFileName(File inFile) {
        String str = inFile.getName();
        int idx = str.lastIndexOf(".");
        str = str.substring(0, idx) + "_" + OBJS + ".nt";
        return new File(str);
    }

    /**
     * Reads N-Triples from a file and sorts them in memory.
     * @param file
     * @return
     * @throws CommandExecutionException 
     */
    private static LinkedList<Statement> readNSortObjects(File file) throws CommandExecutionException {
        try {
            //read every statement in to a list
            RDFParser rdfParser = Rio.createParser(RDFFormat.NTRIPLES);
            LinkedList<Statement> list = new LinkedList<Statement>();
            rdfParser.setRDFHandler(new StatementCollector(list));
            rdfParser.getParserConfig().addNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);
            rdfParser.parse(new FileInputStream(file), "");
            //sort the list
            Collections.sort(list, new StatementsComparatorO());
            return list;
        } catch (FileNotFoundException ex) {
            throw new CommandExecutionException(ex);
        } catch (IOException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFParseException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException(ex);
        }
    }

}
