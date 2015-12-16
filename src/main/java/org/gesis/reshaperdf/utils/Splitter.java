package org.gesis.reshaperdf.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;

/**
 * Splits a sorted NTriples file into parts with equal count of resources. The
 * splitter regards resources bounds.
 */
public class Splitter {

    /**
     * Splits a sorted NTriples file into parts with equal count of resources.
     * The splitter regards resources bounds.
     *
     * @param inFile File to split
     * @param outFilePrefix Prefix used for the output file, can be used to
     * place the ouput files in a specified directory.
     * @param resourcesPerFile Number of resource per file. Last file can
     * contain less.
     * @throws FileNotFoundException
     * @throws RDFHandlerException
     */
    public static void split(File inFile, String outFilePrefix, long resourcesPerFile) throws FileNotFoundException, RDFHandlerException {

        //use resource pull reader
        ResourcePullReader rpReader = new ResourcePullReader(inFile);
        rpReader.load();

        CheckedNTriplesWriter writer = null;

        long cnt = 0;
        long fileCnt = 1;

        File outFile1 = new File(outFilePrefix + fileCnt + ".nt");
        System.out.println("Starting file " + outFile1.getAbsolutePath());

        writer = new CheckedNTriplesWriter(new FileOutputStream(outFile1), new StrictStatementFilter());
        writer.startRDF();
        while (!rpReader.isEmpty()) {
            Statement[] res = rpReader.peek();
            rpReader.removeHead();
            cnt++;
            if (cnt % resourcesPerFile == 0) {
                writer.endRDF();
                fileCnt++;
                File outFile = new File("" + outFilePrefix + fileCnt + ".nt");
                System.out.println("Starting file " + outFile.getAbsolutePath());
                writer = new CheckedNTriplesWriter(new FileOutputStream(outFile), new StrictStatementFilter());
                writer.startRDF();
            }
            writeStatements(writer, res);
        }
        writer.endRDF();

    }

    private static void writeStatements(CheckedNTriplesWriter writer, Statement[] res) throws RDFHandlerException {
        for (int i = 0; i < res.length; i++) {
            writer.handleStatement(res[i]);
        }

    }

}
