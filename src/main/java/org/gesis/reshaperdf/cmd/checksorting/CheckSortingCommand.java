package org.gesis.reshaperdf.cmd.checksorting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.PullReader;
import org.gesis.reshaperdf.utils.StatementsComparatorSPO;
import org.openrdf.model.Statement;

/**
 * Checks the sorting of an NTriple file.
 */
public class CheckSortingCommand implements ICMD {

    public String NAME = "checksorting";
    public String EXPLANATION = "Checks the sorting of an NTriple file.";
    public String HELPTEXT = "Usage: " + NAME + " <infile> \n" + EXPLANATION;

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
     * Checks the sorting of an NTriple file.
     *
     * @param args
     * @return
     * @throws CommandExecutionException
     */
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

        //use pull reader
        PullReader pullReader = new PullReader(inFile);
        pullReader.load();
        Statement last = pullReader.peek();
        pullReader.removeHead();

        //compare each statement with its successor
        Statement curr = pullReader.peek();
        pullReader.removeHead();
        Comparator<Statement> comparator = new StatementsComparatorSPO();
        while (curr != null) {
            if (comparator.compare(last, curr) >= 0) {
                System.out.println("Not sorted");
                System.out.println("Last line=" + last);
                System.out.println("Cur. line=" + curr);
                return new CommandExecutionResult(true);
            }
            last = curr;
            curr = pullReader.peek();
            pullReader.removeHead();
        }

        System.out.println("Sorted");
        return new CommandExecutionResult(true);
    }
}
