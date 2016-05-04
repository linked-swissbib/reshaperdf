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
package org.gesis.reshaperdf.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.ntriples.NTriplesWriter;

/**
 * @author Felix Bensmann
 * Provides utilities for merging a couple of sorted files.
 * The result is also sorted.
 */
public class MergeUtils {

    private MergeUtils() {
    }

    /**
     * Merges a couple of sorted NTriple files.
     * @param fileArr An array of the file to merge
     * @param outFile The file to write the result to
     * @throws FileNotFoundException
     * @throws RDFHandlerException
     * @throws IllegalArgumentException 
     */
    public static void merge(File[] fileArr, File outFile, IStatementFilter filter) throws FileNotFoundException, RDFHandlerException, IllegalArgumentException {

        //instanciate a reader for every given file.
        //uses a special reader that can be polled.
        ArrayList<PullReader> readerList = new ArrayList<PullReader>();
        for (int i = 0; i < fileArr.length; i++) {
            PullReader reader = new PullReader(fileArr[i]);
            reader.load();
            readerList.add(reader);
        }

        NTriplesWriter writer;
        //use a special writer that only writes valid triples.
        writer = new CheckedNTriplesWriter(new FileOutputStream(outFile), filter);
        writer.startRDF();

        //actual merge routine
        //iterates over all readers und takes the alphabetically first statement and writes to the output.
        //a reader is removed when all its statements are processed.
        //repeat until all readers are gone.
        StatementsComparatorSPO comparator = new StatementsComparatorSPO();
        writeOpenList(readerList);
        while (!readerList.isEmpty()) {
            int candidateIdx = 0;
            for (int i = 1; i < readerList.size(); i++) {
                if (comparator.compare(readerList.get(i).peek(), readerList.get(candidateIdx).peek()) < 0) {
                    candidateIdx = i;
                }
            }
            Statement stmt = readerList.get(candidateIdx).peek();
            readerList.get(candidateIdx).removeHead();
            if (readerList.get(candidateIdx).isEmpty()) {
                readerList.remove(candidateIdx);
                writeOpenList(readerList);
            }
            if(stmt.getSubject() instanceof BNode)
                throw new IllegalArgumentException("Source file "+readerList.get(candidateIdx).getFile().getAbsolutePath()+" contains blank node "+stmt.getSubject());
            
            writer.handleStatement(stmt);
        }
        System.out.println("All readers finished.");
        writer.endRDF();
    }

    
    private static void writeOpenList(ArrayList<PullReader> list){
        System.out.print("Open: ");
        for(int i=0; i<list.size();i++){
            if(!list.get(i).isEmpty()){
                System.out.print(list.get(i).getFile().getName()+" ");
            }
        }
        System.out.println();
    }
    
    
    
}
