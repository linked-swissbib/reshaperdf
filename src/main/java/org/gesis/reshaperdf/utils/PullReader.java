package org.gesis.reshaperdf.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.NTriplesParserSettings;

/**
 * A special kind of RDFReader that can be polled. This is achied by using a
 * producer consumer approach. A thread that uses a standard reader fills a
 * threadsafe queue. Data can be read from the queue by consumer. Both producer
 * and consumer wait on the list until they can finish their work.
 */
public class PullReader {

    private File file = null;
    private LinkedBlockingQueue<Statement> queue = null;
    private boolean fileFinished = false;
    private Statement current = null;
    private RDFParser parser = null;

    public PullReader(File file) {
        this.file = file;
        //queue is limited to 1000 elements
        queue = new LinkedBlockingQueue<Statement>(1000);

        parser = Rio.createParser(RDFFormat.NTRIPLES);
        parser.getParserConfig().addNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);
        parser.setRDFHandler(new RDFHandler() {

            @Override
            public void startRDF() throws RDFHandlerException {

            }

            @Override
            public void endRDF() throws RDFHandlerException {
                //System.out.println("Pull reader for " + PullReader.this.file.getAbsolutePath() + " finished");
                fileFinished = true;
            }

            @Override
            public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
            }

            /**
             * Handles a statement by appending it to the queue.
             * @param st
             * @throws RDFHandlerException 
             */
            @Override
            public void handleStatement(Statement st) throws RDFHandlerException {
                try {
                    queue.put(st);
                } catch (InterruptedException ex) {
                    System.err.println(ex);
                }
            }

            @Override
            public void handleComment(String comment) throws RDFHandlerException {

            }
        });
    }

    /**
     * Starts the producer thread
     */
    public void load() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    parser.parse(new FileInputStream(file), "");
                } catch (IOException ex) {
                    System.err.println(ex);
                } catch (RDFParseException ex) {
                    System.err.println(ex);
                } catch (RDFHandlerException ex) {
                    System.err.println(ex);
                }
            }
        });
        t.setName("PullReader thread for " + getFile().getAbsolutePath());
        t.setUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
        t.setDaemon(true);
        t.start();
        this.removeHead();
    }

    /**
     * Determines whether there are statements available from this reader.
     *
     * @return True if all statements from the file are read and the queue is
     * empty.
     */
    public boolean isEmpty() {
        return fileFinished && queue.isEmpty() && current == null;

    }

    /**
     * Returns the first element of the queue but does not remove it.
     *
     * @return
     */
    public Statement peek() {
        return current;
    }

    /**
     * Removes the first element.
     */
    public void removeHead() {
        //uses a workaround to compensate that there is no blocking peek method in the queue
        try {
            current = queue.poll(5, TimeUnit.SECONDS);
            if (current == null && !fileFinished) {
                System.err.println("Unable to retrieve statement from pull reader " + file.getAbsolutePath());
                System.exit(-1);
            }
        } catch (InterruptedException ex) {
            System.err.println(ex);
        }
    }

    public File getFile() {
        return file;
    }

}
