package org.gesis.reshaperdf.cmd.removeduplicates;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;

/**
 * Removes duplicates in a sorted N-Triples file.
 */
public class RemoveDuplicatesCommand implements ICMD {

    public String NAME = "removeduplicates";
    public String EXPLANATION = "Removes duplicate lines from an NTriples file.";
    public String HELPTEXT = "Usage: " + NAME + " <infile> <outfile>\n" + EXPLANATION;

    public RemoveDuplicatesCommand() {

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
     * Executes this command. Is line based. Reads the N-Triples as lines and
     * compares it to its predecessor. If the lines do not equal the current
     * line is written to the target file.
     *
     * @param args
     * @return
     * @throws CommandExecutionException
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length < 3) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }

        File inFile = new File(args[1]);
        if (!inFile.exists() && !inFile.isFile()) {
            return new CommandExecutionResult(false, "Invalid file: " + inFile.getAbsolutePath());
        }

        File outFile = new File(args[2]);

        try {
            //prepare reader
            BufferedReader br = new BufferedReader(new FileReader(inFile));
            String line = br.readLine();
            BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));

            String lastLine = "";
            while (line != null) {
                if (!line.equals(lastLine)) { //compare, write if file does not equal its predecessor
                    bw.write(line + "\n");
                }
                lastLine = line;
                line = br.readLine();
            }
            bw.close();
            br.close();

        } catch (FileNotFoundException ex) {
            throw new CommandExecutionException(ex);
        } catch (IOException ex) {
            throw new CommandExecutionException(ex);
        }
        return new CommandExecutionResult(true);
    }
}
