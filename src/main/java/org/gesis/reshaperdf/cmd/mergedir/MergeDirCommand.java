package org.gesis.reshaperdf.cmd.mergedir;

import java.io.File;
import java.io.FileNotFoundException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.FileFinder;
import org.gesis.reshaperdf.utils.MergeUtils;
import org.openrdf.rio.RDFHandlerException;

/**
 * Merges all N-Triple files that are stored in one directory and its
 * subdirectories.
 */
public class MergeDirCommand implements ICMD {

    public String NAME = "mergedir";
    public String EXPLANATION = "Merges NTriple files that are in the same directory.";
    public String HELPTEXT = "Usage: mergedir <indir> <outfile>  \nMerges a NTriple files that are in the same directory.";

    public MergeDirCommand() {

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
     * Execute this command. Finds files in subdirectories. Uses MergeUtils
     * class to do the actual processing.
     *
     * @param args
     * @return
     * @throws CommandExecutionException
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length != 3) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }

        File inDir = new File(args[1]);
        if (!inDir.exists() || !inDir.isDirectory()) {
            return new CommandExecutionResult(false, "Invalid input directory.");
        }

        File outFile = new File(args[2]);

        try {
            //use methods from utils
            File[] fileArr = FileFinder.findFiles(inDir, new String[]{"nt"});
            MergeUtils.merge(fileArr, outFile, null);
        } catch (FileNotFoundException ex) {
            return new CommandExecutionResult(false, ex.toString());
        } catch (RDFHandlerException ex) {
            return new CommandExecutionResult(false, ex.toString());
        } catch (IllegalArgumentException ex) {
            return new CommandExecutionResult(false, ex.toString());
        }
        return new CommandExecutionResult(true);

    }

}
