/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gesis.reshaperdf.cmd.outline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.CheckedNTriplesWriter;
import org.gesis.reshaperdf.utils.ResourcePullReader;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;

/**
 *
 * @author bensmafx
 */
public class OutlineCommand implements ICMD {

    private static final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
    
    
    private static final String[] PROPERTIES = new String[]{RDFS_LABEL};
    
    
    private String NAME = "outlineresources";
    private String EXPLANATION = "Outlines a resource into one statement with a literal.";
    private String HELPTEXT = "Usage: " + NAME + "<infile> <outfile> <target property>\n" + EXPLANATION;

    public OutlineCommand() {

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
        if (args.length != 4) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }

        File inFile = new File(args[1]);
        if (!inFile.exists() && !inFile.isFile()) {
            return new CommandExecutionResult(false, "Invalid file: " + inFile.getAbsolutePath());
        }
        File outFile = new File(args[2]);
        String targetProperty = args[3];

        try {
            ResourcePullReader reader = new ResourcePullReader(inFile);
            reader.load();

            FileOutputStream fos = new FileOutputStream(outFile);
            CheckedNTriplesWriter writer = new CheckedNTriplesWriter(fos, null);
            writer.startRDF();
            while (reader.peek() != null) {
                Statement[] res = reader.peek();
                reader.removeHead();
                Statement st = outlineResource(res,PROPERTIES,targetProperty);
                writer.handleStatement(st);
            }
            writer.endRDF();
        } catch (IOException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException(ex);
        }

        return new CommandExecutionResult(true);
    }

    /**
     * Summarizes a resource into one literal.
     *
     * @param res
     * @return
     */
    private static Statement outlineResource(Statement[] res, String[] properties, String targetProperty) {
        //Search for given properties, if literals were found for one properties, the remains are not searched.
        ArrayList<String> literalList = new ArrayList<String>();
        for (int i = 0; i < properties.length; i++) {
            addFindings(res, properties[i], literalList);
            if (!literalList.isEmpty()) {
                break;
            }
        }
        //if no literal could be found, use subject uri
        if(literalList.isEmpty()){
            literalList.add(res[0].getSubject().stringValue());
        }
        //Remove duplicates
        Collections.sort(literalList);
        for (int i = literalList.size() - 1; i > 0; i--) {
            if (literalList.get(i).equals(literalList.get(i - 1))) {
                literalList.remove(i);
            }
        }
        //concat findings to a single string
        String retVal = "";
        for (int i = 0; i < literalList.size(); i++) {
            retVal += literalList.get(i);
            if (i < literalList.size() - 1) {
                retVal += "/";
            }
        }
        //return new Statement
        return new StatementImpl(res[0].getSubject(), new URIImpl(targetProperty), new LiteralImpl(retVal));
    }

    /**
     * Searches a resource for statements with the given property and adds the
     * corresponding object to the given list.
     *
     * @param res
     * @param property
     * @param list
     */
    private static void addFindings(Statement[] res, String property, ArrayList<String> list) {
        for (int i = 0; i < res.length; i++) {
            if (res[i].getPredicate().equals(property)) {
                list.add(res[i].getObject().stringValue());
            }
        }
    }

}
