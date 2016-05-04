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
import java.io.IOException;
import java.util.Comparator;
import org.gesis.reshaperdf.utils.LineCounter;
import org.gesis.reshaperdf.utils.StatementsComparatorSPO;
import org.openrdf.model.Statement;

/**
 * @author Felix Bensmann
 * Class to use async sort with presettings.
 */
public class Sort {
    
    
    /**
     * Sorts an NTriples inFile using merge sort.
     * @param inFile The inFile to sort. This inFile is Overwritten by the sorted inFile.
     * @param outFile The sorted file.
     * @throws InterruptedException 
     */
    public static void sort(File inFile, File outFile, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) throws InterruptedException, IOException{
    
        //create a workspace folder in user.home
        String userHome = ".";//System.getProperty("user.home");
        File userHomeDir = new File(userHome);
        String tmpDirName = String.valueOf(System.currentTimeMillis());
        File workspace = new File(userHomeDir, tmpDirName);
        workspace.mkdir();
        System.out.println("Using "+workspace.getAbsolutePath()+ " as workspace.");
    
        //start sorting
        Comparator<Statement> comparator = new StatementsComparatorSPO();
        long fLength = LineCounter.countLines(inFile.getAbsolutePath());
        
        AsyncSplitMerge asm = new AsyncSplitMerge(inFile, outFile, workspace, comparator, fLength, true, uncaughtExceptionHandler);
        asm.start();
        asm.join();
       
        deleteFlatDir(workspace);
        
        System.out.println("Complete");
    }
    
    
    /**
     * Clean the workspace afterwards.
     * @param dir 
     */
    private static void deleteFlatDir(File dir) {
        for (File f : dir.listFiles()) {
            f.delete();
        }
        dir.delete();
    }

    
}
