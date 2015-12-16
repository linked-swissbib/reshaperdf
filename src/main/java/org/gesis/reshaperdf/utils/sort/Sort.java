package org.gesis.reshaperdf.utils.sort;

import java.io.File;
import java.util.Comparator;
import org.gesis.reshaperdf.utils.StatementsComparatorSPO;
import org.openrdf.model.Statement;

/**
 * Class to use async sort with presettings.
 */
public class Sort {
    
    
    /**
     * Sorts an NTriples file using merge sort.
     * @param file The file to sort. This file is Overwritten by the sorted file.
     * @throws InterruptedException 
     */
    public static void sort(File file) throws InterruptedException{
    
        //chose workspace
        String userHome = System.getProperty("user.home");
        File userHomeDir = new File(userHome);
        String tmpDirName = String.valueOf(System.currentTimeMillis());
        File workspace = new File(userHomeDir, tmpDirName);
        workspace.mkdir();
    
        //start sorting
        Comparator<Statement> comparator = new StatementsComparatorSPO();
        AsyncSplitMerge asm = new AsyncSplitMerge(file, workspace, comparator);
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
