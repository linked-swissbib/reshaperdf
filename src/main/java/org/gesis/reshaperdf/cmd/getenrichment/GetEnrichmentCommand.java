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
package org.gesis.reshaperdf.cmd.getenrichment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.CheckedNTriplesWriter;
import org.gesis.reshaperdf.utils.ResourcePullReader;
import org.gesis.reshaperdf.utils.StatementsComparatorO;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.NTriplesParserSettings;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * @author Felix Bensmann
 * Gets a link file and a resource file as input. Extracts all resources
 * specified by the object of the links.
 */
public class GetEnrichmentCommand implements ICMD {

    private static final String NAME = "getenrichment";
    private static final String EXPLANATION = "Extracts resources from an SNT file, that are adressed by the object of an SNT link file. "
            + "Missing resources in the resources file are ignored. "
            + "The subjects of the extracted statements are altered to the subject of the link.";
    private static final String HELPTEXT = "Usage: getenrichment <linkfile> <resource file> <outfile> \n" + EXPLANATION;

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
     * Sort the link file by its object. An compares them against the resources
     * file.
     *
     * @param args
     * @return
     * @throws CommandExecutionException
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length != 4) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }
        File linkFile = new File(args[1]);
        if (!linkFile.exists() || !linkFile.isFile()) {
            return new CommandExecutionResult(false, "Input file is not a valid file.");
        }

        File resourceFile = new File(args[2]);
        if (!resourceFile.exists() || !resourceFile.isFile()) {
            return new CommandExecutionResult(false, "Input file is not a valid file.");
        }

        File outFile = new File(args[3]);
        ArrayList<Statement> linkList = null;

        //Sorting linkfile by objects
        try {
            RDFParser rdfParser = Rio.createParser(RDFFormat.NTRIPLES);
            linkList = new ArrayList<Statement>();
            rdfParser.setRDFHandler(new StatementCollector(linkList));
            rdfParser.getParserConfig().addNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);
            rdfParser.parse(new FileInputStream(linkFile), "");

            //sort the linkList by !!objects!!
            Collections.sort(linkList, new StatementsComparatorO());
        } catch (FileNotFoundException ex) {
            throw new CommandExecutionException("Sorting links failed. " + ex);
        } catch (IOException ex) {
            throw new CommandExecutionException("Sorting links failed. " + ex);
        } catch (RDFParseException ex) {
            throw new CommandExecutionException("Sorting links failed. " + ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException("Sorting links failed. " + ex);
        }

        //prepare datasets
        ResourcePullReader rpReader = new ResourcePullReader(resourceFile);
        rpReader.load();

        try {
            CheckedNTriplesWriter writer = new CheckedNTriplesWriter(new FileOutputStream(outFile), null);
            writer.startRDF();

            int cnt = 0;
            
            //get initial element of linklist
            int max = linkList.size();
            Statement link = linkList.get(0);
            String obj = link.getObject().stringValue();
            linkList.remove(0);
            cnt++;

            //get initial element of resource file
            Statement[] res = rpReader.peek();
            rpReader.removeHead();
            String subj = res[0].getSubject().stringValue();
      

            //use data sets as queues, compare head at head. 
            //Use alphabetical order to determine if resources are not present
            //while no list is empty ...
            while (!linkList.isEmpty() && rpReader.peek() != null) {

                //comparison
                int result = obj.compareTo(subj);
                //System.out.println(obj+"|"+subj+"|"+result);

                //obj < subj
                if (result < 0) {
                    //ressource could not be found, another alphanum. greater one is already present
                    System.out.println("Resource #"+cnt+" not found: " + obj);
                    link = linkList.get(0);
                    obj = link.getObject().stringValue();
                    linkList.remove(0);
                    cnt++;
                    continue;
                } //obj == subj
                else if (result == 0) {
                    //ressource found -> extract
                    System.out.println("Found resource #" + cnt + " of " + max);
                    writeAndMerge(writer, res, link.getSubject().stringValue());
                    link = linkList.get(0);
                    obj = link.getObject().stringValue();
                    linkList.remove(0);
                    cnt++;
                    continue;
                } //obj > subj
                else if (result > 0) {
                    //continue searching...
                    res = rpReader.peek();
                    rpReader.removeHead();
                    subj = res[0].getSubject().stringValue();
                    continue;
                }
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
     * Renames the subject of an extracted resource after the subject of the link.
     * @param writer
     * @param res
     * @param newSubj
     * @throws RDFHandlerException 
     */
    private void writeAndMerge(RDFWriter writer, Statement[] res, String newSubj) throws RDFHandlerException {
        for (int i = 0; i < res.length; i++) {
            Statement st = new StatementImpl(new URIImpl(newSubj), res[i].getPredicate(), res[i].getObject());
            writer.handleStatement(st);
        }

    }

}
