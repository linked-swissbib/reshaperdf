/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gesis.reshaperdf.cmd.analyzeproperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.IResourceHandler;
import org.gesis.reshaperdf.utils.ResourceReader;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

/**
 *
 * @author bensmafx
 */
public class AnalyzeTypeCommand implements ICMD, IResourceHandler {

    private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    private String NAME = "analyzetype";
    private String EXPLANATION = "Analyzes literal object variations for given propterties.";
    private String HELPTEXT = "Usage: " + NAME + " <infile> <type-object> <predicate1> [<predicate2> ...] \n" + EXPLANATION;

    private String type = null;
    private String[] predicateArr = null;
    private OccurrenceTable[] tables = null;
    private OccurrenceTable combiTable = null;

    private int resCnt = 0;

    public AnalyzeTypeCommand() {

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
        if (args.length < 4) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }

        File inFile = new File(args[1]);
        if (!inFile.exists() && !inFile.isFile()) {
            return new CommandExecutionResult(false, "Invalid file: " + inFile.getAbsolutePath());
        }

        this.type = args[2];

        this.predicateArr = new String[args.length - 3];
        for (int i = 0; (i + 3) < args.length; i++) {
            this.predicateArr[i] = args[i + 3];
        }

        this.tables = new OccurrenceTable[predicateArr.length];
        for (int i = 0; i < predicateArr.length; i++) {
            tables[i] = new OccurrenceTable(new String[]{predicateArr[i]},"table_"+i);
        }
        this.combiTable = new OccurrenceTable(predicateArr,"combi_table");

        ResourceReader rReader = new ResourceReader();
        rReader.setResourceHandler(this);
        try {
            FileInputStream fis = new FileInputStream(inFile);
            rReader.parse(fis, "");
        } catch (FileNotFoundException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFParseException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException(ex);
        } catch (IOException ex) {
            throw new CommandExecutionException(ex);
        }

        try {
            for (int i = 0; i < tables.length; i++) {
                File file = new File(tables[i].getName()+".csv");
                System.out.println("Writing table " + i +" to "+file.getAbsolutePath());
                tables[i].write2File(file);
            }
            List<OccurenceRow> sortedList = combiTable.toSortedList(false);
            File file = new File(combiTable.getName()+".csv");
            System.out.println("Writing combination table to "+file.getAbsolutePath());
            combiTable.write2File(file, sortedList);
        } catch (IOException ex) {
            throw new CommandExecutionException(ex);
        }

        return new CommandExecutionResult(true);
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
        if (containsType(res, type)) {
            String[][] values = new String[predicateArr.length][];
            for (int i = 0; i < predicateArr.length; i++) {//for every requested predicate
                values[i] = getAllDistinctObjects(res, predicateArr[i]); //get the objects
            }
            for (int i = 0; i < predicateArr.length; i++) {//for every requested predicate
                for (int j = 0; j < values[i].length; j++) { //insert the individual occurences
                    tables[i].addOccurence(new String[]{values[i][j]}, values[i].length - 1);
                }
            }
            if (predicateArr.length > 1) {
                CombinationGenerator cGen = new CombinationGenerator(values);
                for (int i = 0; i < cGen.count(); i++) {
                    combiTable.addOccurence(cGen.getCombination(), cGen.count() - 1);
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

}
