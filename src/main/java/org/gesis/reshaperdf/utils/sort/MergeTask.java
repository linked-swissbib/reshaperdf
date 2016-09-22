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
package org.gesis.reshaperdf.utils.sort;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import org.gesis.reshaperdf.utils.CheckedNTriplesWriter;
import org.gesis.reshaperdf.utils.PullReader;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.ntriples.NTriplesWriter;

/**
 *
 * @author bensmafx
 */
public class MergeTask implements Runnable {

    private final File file;
    private final File fileA;
    private final File fileB;
    private final Comparator<Statement> comparator;
    private final MultithreadMerger merger;
    private final int level;
    
    public MergeTask(File file, File fileA, File fileB, Comparator<Statement> comparator, MultithreadMerger merger, int level){
        this.file=file;
        this.fileA=fileA;
        this.fileB=fileB;
        this.comparator=comparator;
        this.merger=merger;
        this.level=level;
    }
    
    
    @Override
    public void run() {
        merge(file, fileA, fileB, comparator);
        merger.registerFile(file,level+1);        
    }
    
    
    
    /**
     * Merges two alphabetically sorted files.
     *
     * @param file Merged inFile.
     * @param fileA First inFile to merge.
     * @param fileB Second inFile to merge.
     * @param comparator Implementation of statements comparator to use.
     */
    public void merge(File file, File fileA, File fileB, Comparator<Statement> comparator) {
        System.out.println(Thread.currentThread().getName()+": started merging files "+fileA.getName()+" and "+fileB.getName()+" to "+file.getName());
        
        PullReader readerA = new PullReader(fileA);
        readerA.load();
        PullReader readerB = new PullReader(fileB);
        readerB.load();

        NTriplesWriter writer;
        try {
            //use a special writer that only writes valid triples.
            FileOutputStream fos = new FileOutputStream(file);
            writer = new CheckedNTriplesWriter(fos, null);
            writer.startRDF();

            Statement a = readerA.peek();
            readerA.removeHead();
            Statement b = readerB.peek();
            readerB.removeHead();

            while (a != null && b != null) {

                //compare
                int result = comparator.compare(a, b);

                //a<b
                if (result < 0) {
                    writer.handleStatement(a);
                    a = readerA.peek();
                    readerA.removeHead();
                } //a>b
                else if (result > 0) {
                    writer.handleStatement(b);
                    b = readerB.peek();
                    readerB.removeHead();
                } //a==b
                else {
                    writer.handleStatement(a);
                    writer.handleStatement(b);
                    a = readerA.peek();
                    readerA.removeHead();
                    b = readerB.peek();
                    readerB.removeHead();
                }
            }
            //transfer remaining statements
            while (a != null) {
                writer.handleStatement(a);
                a = readerA.peek();
                readerA.removeHead();
            }
            while (b != null) {
                writer.handleStatement(b);
                b = readerB.peek();
                readerB.removeHead();
            }
            writer.endRDF();
            fos.close();
            readerA.close();
            fileA.delete();
            readerB.close();
            fileB.delete();
        } catch (IOException ex) {
            System.err.println(ex);
        } catch (RDFHandlerException ex) {
            System.err.println(ex);
        }
        System.out.println(Thread.currentThread().getName()+": ended merging files "+fileA.getName()+" and "+fileB.getName()+" to "+file.getName());
    }
    
    
    
}
