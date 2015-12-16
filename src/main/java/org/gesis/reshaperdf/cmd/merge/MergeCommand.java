package org.gesis.reshaperdf.cmd.merge;

import java.io.File;
import java.io.FileNotFoundException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.MergeUtils;
import org.openrdf.rio.RDFHandlerException;

/**
 * Merges a couple of sorted NTriple files.
 */
public class MergeCommand implements ICMD {

    private static final String NAME = "merge";
    private static final String EXPLANATION = "Merges a couple of sorted N-Triple files.";
    private static final String HELPTEXT = "Usage: merge <outfile> <infile1> <infile2> [<infile3>...]\nMerges a couple of sorted NTriple files.";

    public MergeCommand() {

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
     * Executes this command. Uses the merge utils for actual processing.
     * @param args
     * @return
     * @throws CommandExecutionException 
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length < 4) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }

        File outFile = new File(args[1]);

        File[] fileArr = new File[args.length - 2];
        for (int i = 0; i < args.length - 2; i++) {
            fileArr[i] = new File(args[i + 2]);
        }
        for (File f : fileArr) {
            if (!f.isFile() || !f.exists()) {
                return new CommandExecutionResult(false, "Invalid file: " + f.getAbsolutePath());
            }
        }

        try {
            //use method from merge utils
            MergeUtils.merge(fileArr, outFile, null);
        } catch (FileNotFoundException ex) {
            return new CommandExecutionResult(false, ex.toString());
        } catch (RDFHandlerException ex) {
            return new CommandExecutionResult(false, ex.toString());
        } catch(IllegalArgumentException ex){
            return new CommandExecutionResult(false, ex.toString());
        }
        return new CommandExecutionResult(true);
        
        
    }

}
