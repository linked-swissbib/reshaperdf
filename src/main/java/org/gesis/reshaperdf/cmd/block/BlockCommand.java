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
package org.gesis.reshaperdf.cmd.block;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.CheckedNTriplesWriter;
import org.gesis.reshaperdf.utils.LineCounter;
import org.gesis.reshaperdf.utils.ResourcePullReader;
import org.gesis.reshaperdf.utils.Splitter;
import org.gesis.reshaperdf.utils.StrictStatementFilter;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

/**
 * @author Felix Bensmann
 * Extracts resources and groups them in a new file. Group criterion is the
 * first letter of a given object.
 */
public class BlockCommand implements ICMD {

    private String NAME = "block";
    private String EXPLANATION = "Assigns the resources of the input file to blocks according to a given character sequence of a given property's value. "
            + "One block is one file. Files that exceed a statement count of 100 000 are further split into files of 100 000.";
    private String HELPTEXT = "Usage: " + NAME + " <input file> <output dir> <predicate> <char offset> <char length>\n" + EXPLANATION;

    private Map<String, File> map = null;

    public BlockCommand() {
        map = new HashMap<String, File>();
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
     * Extracts resources and groups them in a new file. Group criterion is the
     * first letter of a given object.
     *
     * @param args cli args. args[0] is the name of this command.
     * @return
     * @throws CommandExecutionException
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length < 6) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }

        File inFile = new File(args[1]);
        if (!inFile.exists() && !inFile.isFile()) {
            return new CommandExecutionResult(false, "Invalid file: " + inFile.getAbsolutePath());
        }

        File outputDir = new File(args[2]);

        //this predicate marks the object to examine
        String predicate = args[3];

        //Index of first character in object to use for blocking
        final int offset;
        try{
            offset = Integer.valueOf(args[4]);
        }catch(NumberFormatException ex){
            return new CommandExecutionResult(false, "Invalid offset parameter: " + args[4]);
        }
        if(offset <0){
            return new CommandExecutionResult(false, "Invalid offset parameter: " + args[4]);
        }
        
        //Length of substring in object to use for blocking
        final int length;
        try{
            length = Integer.valueOf(args[5]);
        }catch(NumberFormatException ex){
            return new CommandExecutionResult(false, "Invalid length parameter: " + args[5]);
        }
        if(length < 1){
            return new CommandExecutionResult(false, "Invalid length parameter: " + args[5]);
        }
        
        //Iterate over all !resources! from inFile...
        ResourcePullReader rpReader = new ResourcePullReader(inFile);
        rpReader.load();
        while (!rpReader.isEmpty()) {
            Statement[] res = rpReader.peek();
            rpReader.removeHead();

            for (int i = 0; i < res.length; i++) {//iterate over all statements in a resource...
                if (res[i].getPredicate().stringValue().equals(predicate)) {//if is the wanted property....
                    String obj = res[i].getObject().stringValue();
                    if (obj.length() > 0) { //...process its object
                        String seq = null;
                        try{
                            seq = obj.substring(offset, offset+length);
                        }catch(IndexOutOfBoundsException ex){
                            seq = obj.substring(0, obj.length()-1); //If index and length invalid, then use the whole word.
                        }
                       
                        //create a writer for the first letters file or use an existing one
                        File file = map.get(seq);
                        if (file == null) { 
                            file = new File(outputDir, deriveFileName(seq));
                            map.put(seq, file);
                        }
                        //write the whole resource each time a propterty was found
                        try {
                            writeResource(res, file);
                        } catch (RDFHandlerException ex) {
                            throw new CommandExecutionException(ex);
                        } catch (IOException ex) {
                            throw new CommandExecutionException(ex);
                        }

                    }

                }
            }

        }
        System.out.println("Blocking done. Splitting...");
        //In case the size of individual exceeds a certain amount these files are further subdivided.
        try {
            furtherSplit(outputDir);
        } catch (IOException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException(ex);
        }

        System.out.println("Done");
        return new CommandExecutionResult(true);

    }
    
    
    /**
     * Convert the character of the string into their codepoints and concatenates them. E.g. abc -> u61u62u63.nt
     * @param seq
     * @return 
     */
    private static String deriveFileName(String seq){
        StringBuffer fileName = new StringBuffer(seq.length()*4);
        for(int i=0; i<seq.length();i++){
            int asInt = (int) seq.charAt(i);
            fileName.append('u').append(Integer.toHexString(asInt).toUpperCase());
        }
        fileName.append(".nt");
        return fileName.toString();
    }

    /**
     * Writes a given resource with the given writer.
     * @param res
     * @param writer
     * @throws RDFHandlerException 
     */
    private static void writeResource(Statement[] res, File file) throws RDFHandlerException, FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(file, true);
        CheckedNTriplesWriter writer = new CheckedNTriplesWriter(fos, new StrictStatementFilter());
        writer.startRDF();
        for (int i = 0; i < res.length; i++) {
            writer.handleStatement(res[i]);
        }
        writer.endRDF();
        fos.close();
    }

   
    /**
     * Subdivides the files in a given directory if they exceed a certain count of statements
     * @param inDir
     * @throws IOException
     * @throws FileNotFoundException
     * @throws RDFHandlerException 
     */
    private static void furtherSplit(File inDir) throws IOException, FileNotFoundException, RDFHandlerException {
        long stmtsPerFile = 200000;
        long resourcesPerFile = 50000;

        ArrayList<File> fileList = findLongFiles(inDir, stmtsPerFile);

        for (File f : fileList) {
            int idx = f.getAbsolutePath().lastIndexOf(".");
            String prefix = f.getAbsolutePath().substring(0, idx);
            prefix = prefix + "_";
            Splitter.split(f, prefix, resourcesPerFile);
            f.delete();
        }

    }

    /**
     * Finds files in a dir whose line count exceeds the given threshold.
     * @param inDir Directory to search through
     * @param longerThan threshold
     * @return
     * @throws IOException 
     */
    private static ArrayList<File> findLongFiles(File inDir, long longerThan) throws IOException {
        ArrayList<File> fileList = new ArrayList<File>();
        for (File f : inDir.listFiles()) {
            long cnt = LineCounter.countLines(f.getAbsolutePath());
            if (cnt > longerThan) {
                fileList.add(f);
            }
        }
        return fileList;
    }
    
    
   
    

}
