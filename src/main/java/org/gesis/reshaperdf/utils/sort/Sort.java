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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import org.gesis.reshaperdf.utils.CheckedNTriplesWriter;
import org.gesis.reshaperdf.utils.LineCounter;
import org.gesis.reshaperdf.utils.PullReader;
import org.gesis.reshaperdf.utils.StatementsComparatorSPO;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.NTriplesParserSettings;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * Provieds the functionality for sorting an N-Triples file. Uses build in Java
 * functionality to sort small files in-memory and a MergeSort approach for
 * larger files.
 *
 * For files with more than 100 000 Statements: This approach uses multithreaded
 * MergeSort with a threadpool of 10 and a workspace directory for intermediary
 * results. A thread reads the N-Triples in a streamline fashion, collecting 100
 * 000 statements each time, sorts the statements and writes them into the
 * workspace directory. Subsequent merge steps are executed by the threadpool.
 *
 * @author bensmafx
 */
public class Sort {

    private static final int MAX_PERMIT_FILE_SIZE = 100000;

    /**
     * Sorts the file.
     * @param inFile N-Triples file to sort
     * @param outFile File to store results in
     * @throws IOException
     * @throws RDFHandlerException
     * @throws InterruptedException 
     */
    public static void sort(File inFile, File outFile) throws IOException, RDFHandlerException, InterruptedException {

        //Get length of file
        System.out.println("Counting statements");
        long fLength = LineCounter.countLines(inFile.getAbsolutePath());
        System.out.println(fLength);
        if (fLength < MAX_PERMIT_FILE_SIZE) {
            System.out.println("Use in-memory sorting approach.");
            //quick in-memory sort
            quickInMemorySort(inFile, outFile, new StatementsComparatorSPO());
        } else {
            System.out.println("Use extensive sorting approach.");
            //extensive sort
            //create a workspace folder in user.home
            String workspaceName = String.valueOf(System.currentTimeMillis());
            File workspace = new File(".", workspaceName);
            workspace.mkdir();
            System.out.println("Using " + workspace.getAbsolutePath() + " as workspace.");

            //calculate number of files needed
            int fileCount = 1;
            while (fLength / fileCount > MAX_PERMIT_FILE_SIZE) {
                fileCount *= 2;
            }
            System.out.println("Starting with " + fileCount + " level 0 files.");

            MultithreadMerger merger = new MultithreadMerger(fileCount, outFile, workspace);
            readSplitSort(inFile, fLength, workspace, merger);
            System.out.println("All level 0 files habe been processed.");
            merger.waitForIt();

            deleteFlatDir(workspace);
        }
        System.out.println("Complete");

    }

    /**
     * Reads all statments from the readers and writes them sorted into
     * .nt-Files.
     *
     * @param pReaderArr Array of readers
     * @param nrOfStmts Number of statments in inFile
     * @param workspace Directory where to store intermediate results.
     * @throws RDFHandlerException
     * @throws IOException
     */
    private static void readSplitSort(File inFile, long nrOfStmts, File workspace, MultithreadMerger merger) throws RDFHandlerException, IOException {

        //calculate nr of files needed to be under permissable file size; nr needs to be power of two 
        int fileCount = 1;
        while (nrOfStmts / fileCount > MAX_PERMIT_FILE_SIZE) {
            fileCount *= 2;
        }

        //calculate number of statement in first fileCount-1 files
        int effectiveStmtsPerFile = (int) (nrOfStmts / fileCount);

        final Statement[] buffer = new Statement[effectiveStmtsPerFile + fileCount]; //Stores read statements

        //help vars
        int fileNr = 0;

        //read for first n files
        PullReader pReader = new PullReader(inFile);
        pReader.load();
        for (int i = 0; i < fileCount - 1; i++) {
            for (int j = 0; j < effectiveStmtsPerFile; j++) {
                Statement stmt = pReader.peek();
                pReader.removeHead();
                buffer[j] = stmt;
            }
            Arrays.sort(buffer, 0, effectiveStmtsPerFile, new StatementsComparatorSPO());
            File file = new File(workspace, "lv0_" + ++fileNr);
            try {
                writeBuffer(buffer, 0, effectiveStmtsPerFile, file);
                merger.registerFile(file, 0);
            } catch (RDFHandlerException ex) {
                throw new RDFHandlerException("When writing file " + file.getName(), ex);
            } catch (IOException ex) {
                throw new IOException("When writing file " + file.getName(), ex);
            }

        }

        //read for last file
        int idx = 0;
        while (!pReader.isEmpty()) {
            Statement stmt = pReader.peek();
            pReader.removeHead();
            buffer[idx] = stmt;
            idx++;
        }

        Arrays.sort(buffer, 0, idx, new StatementsComparatorSPO());
        File file = new File(workspace, "lv0_" + ++fileNr);
        try {
            writeBuffer(buffer, 0, idx - 1, file);
            merger.registerFile(file, 0);
        } catch (RDFHandlerException ex) {
            throw new RDFHandlerException("When writing file " + file.getName(), ex);
        } catch (IOException ex) {
            throw new IOException("When writing file " + file.getName(), ex);
        }
        pReader.close();
    }

    private static void writeBuffer(Statement[] arr, int off, int len, File file) throws RDFHandlerException, IOException {
        FileOutputStream fos = new FileOutputStream(file);
        CheckedNTriplesWriter writer = new CheckedNTriplesWriter(fos, null);
        writer.startRDF();
        for (int i = off; i < len; i++) {
            writer.handleStatement(arr[i]);
        }
        writer.endRDF();
        fos.close();
        System.out.println("Wrote level 0 file: " + file.getName());
    }

    /**
     * Clean the workspace afterwards.
     *
     * @param dir
     */
    private static void deleteFlatDir(File dir) {
        for (File f : dir.listFiles()) {
            f.delete();
        }
        dir.delete();
    }

    /**
     * In-memory sort.
     *
     * @param inFile File to sort.
     * @param comparator Implementation for a statements comparator
     */
    private static void quickInMemorySort(File inFile, File outFile, Comparator<Statement> comparator) {

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

}
