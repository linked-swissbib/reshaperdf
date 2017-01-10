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
package org.gesis.reshaperdf.cmd.extractreferenced;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.PullReader;
import org.gesis.reshaperdf.utils.ResourcePullReader;
import org.gesis.reshaperdf.utils.ResourceWriter;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.rio.RDFHandlerException;

/**
 * Extracts resources from file B that are referenced in file A.
 * @author Felix Bensmann
 */
public class ExtractReferencedCommand implements ICMD {

    private String NAME = "extractreferenced";
    private String EXPLANATION = "Extracts resources from file B that are referenced in file A.";
    private String HELPTEXT = "Usage: " + NAME + " <file A> <file B> <outfile> <predicate1> [<predicate2> ...] \n" + EXPLANATION;

    public ExtractReferencedCommand() {

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
     * @param args cli args. args[0] is the name of this command.
     * @return
     * @throws CommandExecutionException
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length < 5) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }

        File inFileA = new File(args[1]);
        if (!inFileA.exists() && !inFileA.isFile()) {
            return new CommandExecutionResult(false, "Invalid file: " + inFileA.getAbsolutePath());
        }

        File inFileB = new File(args[2]);
        if (!inFileB.exists() && !inFileB.isFile()) {
            return new CommandExecutionResult(false, "Invalid file: " + inFileB.getAbsolutePath());
        }

        File outfile = new File(args[3]);

        int offset = 4;
        String[] predicates = new String[args.length - offset];
        for (int i = 0; i < args.length - offset; i++) {
            predicates[i] = args[i + offset];
        }

        //Step 1: get all resource URIs from file A that are specified by the property array
        System.out.println("Reading");
        PullReader reader = new PullReader(inFileA);
        reader.load();
        ArrayList<String> urlList = new ArrayList<String>();
        while (reader.peek() != null) {
            Statement stmt = reader.peek();
            reader.removeHead();
            String predicate = stmt.getPredicate().stringValue();
            for (int i = 0; i < predicates.length; i++) {
                if (predicates[i].equals(predicate)) {
                    urlList.add(stmt.getObject().stringValue());
                    break;
                } else {
                    break;
                }
            }
        }
        if (urlList.isEmpty()) {
            System.out.println("No statements with the specified properties were found.");
            return new CommandExecutionResult(true);
        }

        //Step 2: sort these URIs
        System.out.println("Sorting");
        Collections.sort(urlList);
        
        
        for(int i=urlList.size()-1; i>0 ;i--){
            if(urlList.get(i).equals(urlList.get(i-1))){
                System.out.println("Removing duplicate "+urlList.get(i));
                urlList.remove(i);
            }
        }

        //Step 3: use the sorted property URI array to extract the referenced resources
        System.out.println("Extracting");
        ResourceWriter writer;
        try {
            writer = new ResourceWriter(outfile, null);
        } catch (FileNotFoundException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException(ex);
        }
        ResourcePullReader rpReader = new ResourcePullReader(inFileB);
        rpReader.load();
        Statement[] res = null;
        res = rpReader.peek();
        rpReader.removeHead();
        String subj = res[0].getSubject().stringValue();
        String obj = urlList.get(0);
        while (!urlList.isEmpty() && rpReader.peek() != null) {

            //comparison
            int result = obj.compareTo(subj);
                //System.out.println(obj+"|"+subj+"|"+result);

            //obj < subj
            if (result < 0) {
                //ressource could not be found, another alphanum. greater one is already present
                System.out.println("Resource not found: " + obj);
                obj = urlList.get(0);
                urlList.remove(0);
                continue;
            } //obj == subj
            else if (result == 0) {
                //ressource found -> extract
                System.out.println("Found resource " + obj);
                try {
                    writer.writeResource(res);
                } catch (RDFHandlerException ex) {
                    throw new CommandExecutionException(ex);
                }
                obj = urlList.get(0);
                urlList.remove(0);
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
        
        return new CommandExecutionResult(true);
    }
     

}
