package org.gesis.reshaperdf.cmd.sort;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.OSUtils;
import org.gesis.reshaperdf.utils.sort.Sort;

/**
 * Sorts an NTriple file alphabetically. At first the input file is splitted to various smaller
 * files. These smaller files are sorted separately by a threads as soon as they
 * fully written. After each file is sorted, the files are remerged.
 */
public class SortCommand implements ICMD {

    public String NAME = "sort";
    public String EXPLANATION = "Sorts an NTriple file.";
    public String HELPTEXT = "Usage: sort <infile> <outfile>\nSorts an NTriple file.";
    public int linesPerFile = 1000000;

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

    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length != 2) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }
        File inFile = new File(args[1]);
        if (!inFile.exists() || !inFile.isFile()) {
            return new CommandExecutionResult(false, "Input file is not a valid file.");
        }
        
        try {
            Sort.sort(inFile);
        } catch (InterruptedException ex) {
            throw new CommandExecutionException(ex);
        }
          
        return new CommandExecutionResult(true);
    }

    
}
