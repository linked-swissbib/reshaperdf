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
package org.gesis.reshaperdf.utils.sort;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Semaphore;
import org.gesis.reshaperdf.utils.CheckedNTriplesWriter;
import org.gesis.reshaperdf.utils.LineReader;
import org.gesis.reshaperdf.utils.LineWriter;
import org.gesis.reshaperdf.utils.PullReader;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.NTriplesParserSettings;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.ntriples.NTriplesWriter;

/**
 * @author Felix Bensmann
 * Asynchronous merge sort implementation. Extends Thread, use start() to
 * execute.
 */
public class AsyncSplitMerge extends Thread {

    private static final long LINES_PER_FILE = 2000000;
    private static Semaphore semaphore = new Semaphore(10);
    
    private File workspace = null;
    private File inFile = null;
    private File outFile = null;
    private Comparator<Statement> comparator = null;
    private long fLength = -1;
    private boolean topLevel = false;

    /**
     * Ctor
     *
     * @param inFile N-Triples inFile to sort.
     * @param outFile File to store the output to.
     * @param workspace A directory to use for temporary files.
     * @param comparator A comparator implementation for statements.
     * @param fLength The line count of the file to sort.
     */
    public AsyncSplitMerge(File inFile, File outFile, File workspace, Comparator<Statement> comparator, long fLength, boolean topLevel, UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.inFile = inFile;
        this.outFile = outFile;
        this.workspace = workspace;
        this.comparator = comparator;
        this.fLength = fLength;
        this.topLevel = topLevel;
        this.setName("SplitMerge thread for " + inFile.getName());
        this.setUncaughtExceptionHandler(uncaughtExceptionHandler);
    }

    /**
     * Implements merge sort. Splits the inFile in further smaller files. Since
     * writing a inFile that contains a single element isn't suitable, whenever
     * a inFile becomes smaller than 2 000 000 statements is sorted in memory.
     */
    @Override
    public void run() {

        try {
            System.out.println("Thread 4 " + inFile.getName() + " started.");
            //do normal merge sort, as long as the inFile is larger than 2 000 000 stmts.
            if (fLength > LINES_PER_FILE) {
                File fileA = new File(workspace, inFile.getName() + "_A");
                File fileB = new File(workspace, inFile.getName() + "_B");
                System.out.println("Thread 4 " + inFile.getName() + " started splitting.");
                //split
                split(inFile, fileA, fileB);
                //remove file except the original
                if (!topLevel) {
                    inFile.delete();
                }
                System.out.println("Thread 4 " + inFile.getName() + " finished splitting.");
                //recursive call for inFile A
                AsyncSplitMerge tA = new AsyncSplitMerge(fileA, fileA, workspace, comparator, fLength / 2, false, this.getUncaughtExceptionHandler());
                tA.start();
                //recursive call for inFile B
                AsyncSplitMerge tB = new AsyncSplitMerge(fileB, fileB, workspace, comparator, fLength / 2, false, this.getUncaughtExceptionHandler());
                tB.start();
                //wait for both
                tA.join();
                tB.join();
                System.out.println("Thread 4 " + inFile.getName() + " started merging.");
                //merge files
                merge(outFile, fileA, fileB, comparator);
                fileA.delete();
                fileB.delete();
                System.out.println("Thread 4 " + inFile.getName() + " finished merging.");
            } else {
                //sort when the inFile is <= 2 000 000 statements
                //reduce access to sorting in order to save memory
                semaphore.acquire();
                System.out.println("Thread 4 " + inFile.getName() + " started sorting.");
                sort(inFile, outFile, comparator);
                System.out.println("Thread 4 " + inFile.getName() + " finished sorting.");
                semaphore.release();
            }
            System.out.println("Thread 4 " + inFile.getName() + " exited.");
        } catch (InterruptedException ex) {
            System.err.println(ex);
        }

    }

    /**
     * Splits inFile in two. Is done line based.
     *
     * @param inFile The inFile to split.
     * @param fileA The inFile for the first part.
     * @param fileB The inFile for the second part.
     */
    private void split(File inFile, File fileA, File fileB) {
        try {
            LineReader lReader = new LineReader(inFile);
            LineWriter aWriter = new LineWriter(fileA);
            LineWriter bWriter = new LineWriter(fileB);

            String line = lReader.readLine();
            boolean readerSwitch = true;
            while (line != null) {
                if (readerSwitch) {
                    aWriter.writeLine(line);
                } else {
                    bWriter.writeLine(line);
                }
                readerSwitch = !readerSwitch;
                line = lReader.readLine();
            }
            lReader.close();
            aWriter.close();
            bWriter.close();

        } catch (IOException ex) {
            System.err.println(ex);
        }

    }

    /**
     * In-memory sort.
     *
     * @param inFile File to sort.
     * @param comparator Implementation for a statements comparator
     */
    private void sort(File inFile, File outFile, Comparator<Statement> comparator) {
        ArrayList<Statement> list = null;

        try {
            RDFParser rdfParser = Rio.createParser(RDFFormat.NTRIPLES);
            list = new ArrayList<Statement>();
            rdfParser.setRDFHandler(new StatementCollector(list));
            rdfParser.getParserConfig().addNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);
            FileInputStream fis = new FileInputStream(inFile);
            rdfParser.parse(fis, "");
            fis.close();
            Collections.sort(list, comparator);

            OutputStream out = new FileOutputStream(outFile);
            CheckedNTriplesWriter writer = new CheckedNTriplesWriter(out, null);
            writer.startRDF();
            for (int i = 0; i < list.size(); i++) {
                writer.handleStatement(list.get(i));
            }
            writer.endRDF();
            out.close();
        } catch (IOException ex) {
            System.err.println(ex);
        } catch (RDFHandlerException ex) {
            System.err.println(ex);
        } catch (RDFParseException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Merges two alphabetically sorted files.
     *
     * @param file Merged inFile.
     * @param fileA First inFile to merge.
     * @param fileB Second inFile to merge.
     * @param comparator Implementation of statements comparator to use.
     */
    public void merge(File file, File fileA, File fileB, Comparator<Statement> comparator) {
        PullReader readerA = new PullReader(fileA);
        readerA.load();
        PullReader readerB = new PullReader(fileB);
        readerB.load();

        NTriplesWriter writer;
        try {
            //use a special writer that only writes valid triples.
            FileOutputStream fos = new FileOutputStream(file);
            writer = new CheckedNTriplesWriter(fos, null);
            writer.startRDF();

            Statement a = readerA.peek();
            readerA.removeHead();
            Statement b = readerB.peek();
            readerB.removeHead();

            while (a != null && b != null) {

                //compare
                int result = comparator.compare(a, b);

                //a<b
                if (result < 0) {
                    writer.handleStatement(a);
                    a = readerA.peek();
                    readerA.removeHead();
                } //a>b
                else if (result > 0) {
                    writer.handleStatement(b);
                    b = readerB.peek();
                    readerB.removeHead();
                } //a==b
                else {
                    writer.handleStatement(a);
                    writer.handleStatement(b);
                    a = readerA.peek();
                    readerA.removeHead();
                    b = readerB.peek();
                    readerB.removeHead();
                }
            }
            //transfer remaining statements
            while (a != null) {
                writer.handleStatement(a);
                a = readerA.peek();
                readerA.removeHead();
            }
            while (b != null) {
                writer.handleStatement(b);
                b = readerB.peek();
                readerB.removeHead();
            }
            writer.endRDF();
            fos.close();
        } catch (IOException ex) {
            System.err.println(ex);
        } catch (RDFHandlerException ex) {
            System.err.println(ex);
        }

    }

}
