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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.CheckedNTriplesWriter;
import org.gesis.reshaperdf.utils.LineReader;
import org.gesis.reshaperdf.utils.LineWriter;
import org.gesis.reshaperdf.utils.PullReader;
import org.gesis.reshaperdf.utils.ResourcePullReader;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

/**
 * Extracts either subject predicate or objects according to a pattern and
 * stores the vgl into a list.
 *
 * @author Felix Bensmann
 */
public class PickCommand implements ICMD {

    private static final String MODE_S = "s";
    private static final String MODE_P = "p";
    private static final String MODE_O = "o";
    private static final String MODE_STMT = "stmt";
    private static final String MODE_RES = "res";
    private static final String WILDCARD = "?";

    private static String NAME = "pick";
    private static String EXPLANATION = "Extracts either subject, predicate, object, "
            + "the whole statement or the whole resource "
            + "according to a pattern and stores the result into a list.\n"
            + "A " + WILDCARD + "-character can be used to indicate a wildcard."
            + "Example: infile.nt outfile.nt o subjectlist.txt predicatelist.txt" + WILDCARD + " ;"
            + "this returns all objects whose statments match any combination of subjectlist and predicatelist. ";
    private static String HELPTEXT = "Usage: " + NAME + " <input file> <output file> <s|p|o|stmt|res> <s|list|"+WILDCARD+"> <p|list|"+WILDCARD+"> <o|list|"+WILDCARD+">"
            + "\n" + EXPLANATION;

    private static final int ARG_CMD = 0;
    private static final int ARG_INFILE = 1;
    private static final int ARG_OUTFILE = 2;
    private static final int ARG_RESULT_MODE = 3;
    private static final int ARG_SUBJECT_LIST = 4;
    private static final int ARG_PREDICATE_LIST = 5;
    private static final int ARG_OBJECT_LIST = 6;

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
        if (args.length != 7) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }
        //check infile
        File inFile = new File(args[ARG_INFILE]);
        if (!inFile.exists() || !inFile.isFile()) {
            return new CommandExecutionResult(false, "Invalid file: " + inFile.getAbsolutePath());
        }
        //assign other values
        File outFile = new File(args[ARG_OUTFILE]);
        //determine vgl mode
        String resultMode = args[ARG_RESULT_MODE].toLowerCase();
        if (!(resultMode.equals(MODE_S)
                || resultMode.equals(MODE_S)
                || resultMode.equals(MODE_P)
                || resultMode.equals(MODE_O)
                || resultMode.equals(MODE_STMT)
                || resultMode.equals(MODE_RES))) {
            return new CommandExecutionResult(false, "Invalid result mode." + getHelptext());
        }

        ArrayList<Resource> subjectList = null;
        ArrayList<URI> predicateList = null;
        ArrayList<Value> objectList = null;
        try {
            //fill input lists
            subjectList = fillSubjectList(args[ARG_SUBJECT_LIST]);
            predicateList = fillPredicateList(args[ARG_PREDICATE_LIST]);
            objectList = fillObjectList(args[ARG_OBJECT_LIST]);
        } catch (IOException ex) {
            return new CommandExecutionResult(false, "Error parsing pattern." + getExplanation());
        }

        //actual processing
        if (resultMode.equals(MODE_S)
                || resultMode.equals(MODE_P)
                || resultMode.equals(MODE_O)) {
            return processSPO(resultMode, inFile, outFile, subjectList, predicateList, objectList);
        } else if (resultMode.equals(MODE_STMT)) {
            return processSTMT(resultMode, inFile, outFile, subjectList, predicateList, objectList);
        } else if (resultMode.equals(MODE_RES)) {
            return processRES(resultMode, inFile, outFile, subjectList, predicateList, objectList);
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

    public static ArrayList<Resource> fillSubjectList(String param) throws IOException {
        ArrayList<Resource> retList = new ArrayList<Resource>();
        File f = new File(param);
        if (f.exists() && f.isFile()) {
            //read in
            LineReader lR = new LineReader(f);
            while (!lR.isEmpty()) {
                retList.add(new URIImpl(lR.readLine()));
            }
            lR.close();
        } else if (param.equals(WILDCARD)) {
            //use empty list
        } else {
            retList.add(new URIImpl(param));
        }

        return retList;
    }

    public static ArrayList<URI> fillPredicateList(String param) throws IOException {
        ArrayList<URI> retList = new ArrayList<URI>();
        File f = new File(param);
        if (f.exists() && f.isFile()) {
            //read in
            LineReader lR = new LineReader(f);
            while (!lR.isEmpty()) {
                retList.add(new URIImpl(lR.readLine()));
            }
            lR.close();
        } else if (param.equals(WILDCARD)) {
            //use empty list
        } else {
            retList.add(new URIImpl(param));
        }

        return retList;
    }

    public static ArrayList<Value> fillObjectList(String param) throws IOException {
        ArrayList<Value> retList = new ArrayList<Value>();
        File f = new File(param);
        if (f.exists() && f.isFile()) {
            //read in
            LineReader lR = new LineReader(f);
            while (!lR.isEmpty()) {
                String str = lR.readLine();
                URI uri = getURI(str);
                if (uri != null) {
                    retList.add(uri);
                } else {
                    BNode bnode = getBNode(str);
                    if (bnode != null) {
                        retList.add(bnode);
                    } else {
                        Literal literal = getLiteral(str);
                        if (literal != null) {
                            retList.add(literal);
                        }
                    }
                }

            }
            lR.close();
        } else if (param.equals(WILDCARD)) {
            //use empty list
        } else {
            retList.add(new URIImpl(param));
        }

        return retList;
    }

    private static BNode getBNode(String str) {
        try {
            return new BNodeImpl(str);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static URI getURI(String str) {
        try {
            return new URIImpl(str);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static Literal getLiteral(String str) {
        return new LiteralImpl(str);
    }

    private static CommandExecutionResult processSPO(String resultMode, File inFile, File outFile,
            ArrayList<Resource> subjectList, ArrayList<URI> predicateList,
            ArrayList<Value> objectList) throws CommandExecutionException {
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
        Matcher matcher = new Matcher(subjectList, predicateList, objectList);
        Statement stmt = pReader.peek();
        pReader.removeHead();
        try {
            while (stmt != null) {
                int vgl = matcher.compareTo(stmt);
                if (vgl < 0) { //stmt is smaller
                    //pull next statement
                    stmt = pReader.peek();
                    pReader.removeHead();

                } else if (vgl == 0) {

                    if(resultMode.equals(MODE_S)){
                        lWriter.writeLine(stmt.getSubject().stringValue());
                    }
                    else if(resultMode.equals(MODE_P)){
                        lWriter.writeLine(stmt.getPredicate().stringValue());
                    }
                    else if(resultMode.equals(MODE_O)){
                        lWriter.writeLine(stmt.getObject().stringValue());
                    }
                    else{
                        throw new CommandExecutionException("This state must not exist.");
                    }
                    //pull next statement
                    stmt = pReader.peek();
                    pReader.removeHead();

                } else {
                    //shift index of matcher
                    matcher.incIndex();
                }
            }
        } catch (IOException ex) {
            throw new CommandExecutionException(ex);
        }

        try {
            lWriter.close();
        } catch (IOException ex) {
            throw new CommandExecutionException("Error when closing outfile. " + ex);
        }
        return new CommandExecutionResult(true);
    }

    private CommandExecutionResult processSTMT(String resultMode, File inFile,
            File outFile, ArrayList<Resource> subjectList,
            ArrayList<URI> predicateList, ArrayList<Value> objectList) throws CommandExecutionException {

        //setup reader and writer
        PullReader pReader = new PullReader(inFile);
        pReader.load();
        CheckedNTriplesWriter cnWriter;
        try {
            cnWriter = new CheckedNTriplesWriter(new FileOutputStream(outFile), null);
            cnWriter.startRDF();
        } catch (FileNotFoundException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException(ex);
        }

        //actual work
        Matcher matcher = new Matcher(subjectList, predicateList, objectList);
        Statement stmt = pReader.peek();
        pReader.removeHead();

        while (stmt != null) {
            int vgl = matcher.compareTo(stmt);
            if (vgl < 0) { //stmt is smaller
                //pull next statement
                stmt = pReader.peek();
                pReader.removeHead();

            } else if (vgl == 0) {
                try {
                    cnWriter.handleStatement(stmt);
                } catch (RDFHandlerException ex) {
                    throw new CommandExecutionException("Error when writing statement. " + ex);
                }
                //pull next statement
                stmt = pReader.peek();
                pReader.removeHead();

            } else {
                //shift index of matcher
                matcher.incIndex();
            }
        }

        try {
            cnWriter.endRDF();
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException("Error when closing outfile. " + ex);
        }
        return new CommandExecutionResult(true);
    }

    private CommandExecutionResult processRES(String resultMode, File inFile,
            File outFile, ArrayList<Resource> subjectList,
            ArrayList<URI> predicateList, ArrayList<Value> objectList) throws CommandExecutionException {

        //prepare datasets
        ResourcePullReader rpReader = new ResourcePullReader(inFile);
        rpReader.load();

        try {
            CheckedNTriplesWriter writer = new CheckedNTriplesWriter(new FileOutputStream(outFile), null);
            writer.startRDF();

            //get initial element of resource file
            Statement[] res = rpReader.peek();
            rpReader.removeHead();
            int idx = 0;
            Statement stmt = res[idx];

            Matcher matcher = new Matcher(subjectList, predicateList, objectList);

            //use data sets as queues, compare head at head. 
            //Use alphabetical order to determine if resources are not present
            //while no list is empty ...
            while (stmt != null) {

                //comparison
                int vgl = matcher.compareTo(stmt);

                if (vgl < 0) {
                    //ressource could not be found yet
                    if (idx < res.length - 1) {
                        idx++;
                        stmt = res[idx];
                    } else {
                        res = rpReader.peek();
                        if (res != null) {
                            rpReader.removeHead();
                            idx = 0;
                            stmt = res[idx];
                        }
                        else{
                            stmt=null;
                            idx=-1;
                        }
                    }
                }
                else if (vgl == 0){
                    //write the resource
                    writeResource(writer, res);
                    res = rpReader.peek();
                    if(res != null){
                        rpReader.removeHead();
                        idx=0;
                        stmt=res[idx];
                    }
                    else{
                        idx=-1;
                        stmt=null;
                    }  
                }
                else{
                    matcher.incIndex();
                }
            }
            writer.endRDF();
        } catch (RDFHandlerException ex) {
            Logger.getLogger(PickCommand.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PickCommand.class.getName()).log(Level.SEVERE, null, ex);
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
    private void writeResource(RDFWriter writer, Statement[] res) throws RDFHandlerException {
        for (int i = 0; i < res.length; i++) {
            writer.handleStatement(res[i]);
        }

    }
    
    
    
}
