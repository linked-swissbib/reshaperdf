package org.gesis.reshaperdf.utils.sort;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.gesis.reshaperdf.utils.CheckedNTriplesWriter;
import org.gesis.reshaperdf.utils.LineCounter;
import org.gesis.reshaperdf.utils.LineReader;
import org.gesis.reshaperdf.utils.LineWriter;
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
import org.openrdf.rio.ntriples.NTriplesWriter;

/**
 * Asynchronous merge sort implementation. Extends Thread, use start() to execute.
 */
public class AsyncSplitMerge extends Thread {

    private static final long LINES_PER_FILE = 2000000;
    private File workspace = null;
    private File file = null;
    private Comparator<Statement> comparator = null;

    /**
     * Ctor. 
     * @param file N-Triples file to sort.
     * @param workspace A directory to use for temporary files.
     * @param comparator A comparator implementation for statements.
     */
    public AsyncSplitMerge(File file, File workspace, Comparator<Statement> comparator ) {
        this.file = file;
        this.workspace = workspace;
        this.comparator = comparator;
        this.setName("SplitMerge thread for "+file.getName());
    }

    /**
     * Implements merge sort. Splits the file in further smaller files. Since writing a file that contains a single element isn't suitable, whenever a file becomes smaller than 2 000 000 statements is sorted in memory. 
     */
    @Override
    public void run() {

        try {
            System.out.println("Thread 4 "+file.getName()+" started.");
            long fLength = LineCounter.countLines(file.getAbsolutePath());
            //do normal merge sort, as long as the file is larger than 2 000 000 stmts.
            if (fLength > LINES_PER_FILE) {
                File fileA = new File(workspace, file.getName()+"_A");
                File fileB = new File(workspace, file.getName()+"_B");
                System.out.println("Thread 4 "+file.getName()+" started splitting.");
                //split
                split(file, fileA, fileB);
                System.out.println("Thread 4 "+file.getName()+" finished splitting.");
                //recursive call for file A
                AsyncSplitMerge tA = new AsyncSplitMerge(fileA, workspace, comparator);
                tA.start();
                //recursive call for file B
                AsyncSplitMerge tB = new AsyncSplitMerge(fileB, workspace, comparator);
                tB.start();
                //wait for both
                tA.join();
                tB.join();
                System.out.println("Thread 4 "+file.getName()+" started merging.");
                //merge files
                merge(file, fileA, fileB, new StatementsComparatorSPO());
                System.out.println("Thread 4 "+file.getName()+" finished merging.");
            } else {
                //sort when the file is <= 2 000 000 statements
                System.out.println("Thread 4 "+file.getName()+" started sorting.");
                sort(file, new StatementsComparatorSPO());
                System.out.println("Thread 4 "+file.getName()+" finished sorting.");
            }
            System.out.println("Thread 4 "+file.getName()+" exited.");
        } catch (IOException ex) {
            System.err.println(ex);
        } catch (InterruptedException ex) {
            System.err.println(ex);
        }

    }

    /**
     * Splits file in two. Is done line based.
     * @param inFile The file to split.
     * @param fileA The file for the first part.
     * @param fileB  The file for the second part.
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
     * @param file File to sort.
     * @param comparator Implementation for a statements comparator
     */
    private void sort(File file, Comparator<Statement> comparator) {
        ArrayList<Statement> list = null;

        try {
            RDFParser rdfParser = Rio.createParser(RDFFormat.NTRIPLES);
            list = new ArrayList<Statement>();
            rdfParser.setRDFHandler(new StatementCollector(list));
            rdfParser.getParserConfig().addNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);
            rdfParser.parse(new FileInputStream(file), "");
            Collections.sort(list, comparator);

            OutputStream out = new FileOutputStream(file);
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
     * @param file Merged file.
     * @param fileA First file to merge.
     * @param fileB Second file to merge.
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
            writer = new CheckedNTriplesWriter(new FileOutputStream(file), null);
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

            fileA.delete();
            fileB.delete();
        } catch (IOException ex) {
            System.err.println(ex);
        } catch (RDFHandlerException ex) {
            System.err.println(ex);
        }

    }

}
