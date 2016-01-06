package org.gesis.reshaperdf.utils.sort;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import org.gesis.reshaperdf.utils.LineCounter;
import org.gesis.reshaperdf.utils.StatementsComparatorSPO;
import org.openrdf.model.Statement;

/**
 * Class to use async sort with presettings.
 */
public class Sort {
    
    
    /**
     * Sorts an NTriples inFile using merge sort.
     * @param inFile The inFile to sort. This inFile is Overwritten by the sorted inFile.
     * @param outFile The sorted file.
     * @throws InterruptedException 
     */
    public static void sort(File inFile, File outFile) throws InterruptedException, IOException{
    
        //create a workspace folder in user.home
        String userHome = System.getProperty("user.home");
        File userHomeDir = new File(userHome);
        String tmpDirName = String.valueOf(System.currentTimeMillis());
        File workspace = new File(userHomeDir, tmpDirName);
        workspace.mkdir();
    
        //start sorting
        Comparator<Statement> comparator = new StatementsComparatorSPO();
        long fLength = LineCounter.countLines(inFile.getAbsolutePath());
        AsyncSplitMerge asm = new AsyncSplitMerge(inFile, outFile, workspace, comparator, fLength, true);
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
