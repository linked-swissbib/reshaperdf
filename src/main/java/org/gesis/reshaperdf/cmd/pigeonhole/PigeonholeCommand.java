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
package org.gesis.reshaperdf.cmd.pigeonhole;

import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gesis.reshaperdf.cmd.analyzetype.CombinationGenerator;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.IResourceHandler;
import org.gesis.reshaperdf.utils.ResourceReader;
import org.gesis.reshaperdf.utils.ResourceWriter;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

/**
 * Categorizes the resources within a file according to the frequency of 
 * their attributes.
 * @author Felix Bensmann
 */
public class PigeonholeCommand implements ICMD, IResourceHandler {

    private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    private String NAME = "pigeonhole";
    private String EXPLANATION = "Pigeonholes the resources within a file according to the frequency of their attributes.";
    private String HELPTEXT = "Usage: " + NAME + " <input file> <output file A> <output file B> <output file C> <CSV> <total threshold>\n" + EXPLANATION;

    private String type = null;
    private String[] predicateArr = null;
    private int totalThreshold = -1; //threshold for the total column in the CSV

    private ResourceWriter writerA = null;
    private ResourceWriter writerB = null;
    private ResourceWriter writerC = null;

    private ArrayList<String> list = null;

    private int resCnt = 0;

    public PigeonholeCommand() {
        list = new ArrayList<String>();
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
     *
     *
     * @param args cli args. args[0] is the name of this command.
     * @return
     * @throws CommandExecutionException
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length != 7) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }

        File inFile = new File(args[1]);
        if (!inFile.exists() && !inFile.isFile()) {
            return new CommandExecutionResult(false, "Invalid file: " + inFile.getAbsolutePath());
        }

        File outFileA = new File(args[2]);
        File outFileB = new File(args[3]);
        File outFileC = new File(args[4]);

        File csvFile = new File(args[5]);
        if (!csvFile.exists() && !csvFile.isFile()) {
            return new CommandExecutionResult(false, "Invalid file: " + csvFile.getAbsolutePath());
        }

        this.totalThreshold = Integer.parseInt(args[6]);

        try {
            readCSV(csvFile);
        } catch (IOException ex) {
            throw new CommandExecutionException(ex);
        }
        Collections.sort(list);

        ResourceReader rReader = new ResourceReader();
        try {
            this.writerA = new ResourceWriter(outFileA, null);
            this.writerB = new ResourceWriter(outFileB, null);
            this.writerC = new ResourceWriter(outFileC, null);
        } catch (FileNotFoundException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException(ex);
        }
        rReader.setResourceHandler(this);
        try {
            FileInputStream fis = new FileInputStream(inFile);
            rReader.parse(fis, "");
            fis.close();
            writerA.close();
            writerB.close();
            writerC.close();
        } catch (FileNotFoundException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFParseException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException(ex);
        } catch (IOException ex) {
            throw new CommandExecutionException(ex);
        }

        return new CommandExecutionResult(true);
    }

    private void readCSV(File csvFile) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(csvFile);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        CSVReader reader = new CSVReader(isr, ';', '"', '\\');

        this.type = reader.readNext()[0];
        this.predicateArr = reader.readNext();
        int totalIndex = predicateArr.length;

        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine.length == this.predicateArr.length + 2) {
                int total = Integer.parseInt(nextLine[totalIndex]);
                if (total > totalThreshold) {
                    String concatLiterals = concat(nextLine, 0, totalIndex - 1);
                    list.add(concatLiterals);
                }
            }
        }

    }

    private static String concat(String[] arr, int startIndex, int endIndex) {
        String retStr = "";
        for (int i = startIndex; i <= endIndex; i++) {
            retStr += arr[i];
        }
        return retStr;
    }

    @Override
    public void onStart() {
    }

    @Override
    public void handleResource(Statement[] res) {
        resCnt++;
        if (resCnt % 1000 == 0) {
            System.out.println("Handling resource " + resCnt);
        }
        if (containsType(res, type)) {  //type was read from CSV file
            String[][] values = new String[predicateArr.length][];
            for (int i = 0; i < predicateArr.length; i++) {//for every requested predicate, as read from the CSV file...
                values[i] = getAllDistinctObjects(res, predicateArr[i]); //get the objects from the SNT infile
            }
            if (containsOneEmpty(values)) { //when a resource does not contain all the requested properties
                try {
                    writerC.writeResource(res);
                } catch (RDFHandlerException ex) {
                    System.err.println(ex);
                }
            } else {

                CombinationGenerator cGen = new CombinationGenerator(values);
                for (int i = 0; i < cGen.count(); i++) {
                    String[] combi = cGen.getCombination(); //reconstruct the combined entries from the single ones
                    String concat = concat(combi, 0, combi.length - 1);
                    if (Collections.binarySearch(list, concat) >= 0) { //entry exists
                        try {
                            writerA.writeResource(res);
                        } catch (RDFHandlerException ex) {
                            System.err.println(ex);
                        }
                    } else { //entry does not exist
                        try {
                            writerB.writeResource(res);
                        } catch (RDFHandlerException ex) {
                            System.err.println(ex);
                        }
                    }
                    cGen.next();
                }
            }
        }
    }

    @Override
    public void onStop() {

    }

    private static boolean containsType(Statement[] res, String type) {
        for (Statement stmt : res) {
            if (stmt.getPredicate().stringValue().equals(RDF_TYPE)) {
                if (stmt.getObject().stringValue().equals(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String[] getAllDistinctObjects(Statement[] res, String predicate) {
        ArrayList<String> objectList = new ArrayList<String>();
        for (Statement stmt : res) {
            if (stmt.getPredicate().stringValue().equals(predicate)) {
                if (!objectList.contains(stmt.getObject().stringValue())) {
                    objectList.add(stmt.getObject().stringValue());
                }
            }
        }
        return objectList.toArray(new String[objectList.size()]);
    }

    private static boolean containsOneEmpty(String[][] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].length == 0) {
                return true;
            }
        }
        return false;
    }

}
