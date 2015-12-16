package org.gesis.reshaperdf.cmd.substract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.LineReader;
import org.gesis.reshaperdf.utils.LineWriter;

/**
 * Removes all statements from file A that are also in file B.
 */
public class SubtractCommand implements ICMD {

    private static final String NAME = "subtract";
    private static final String EXPLANATION = "Removes all statements from file A that are also in file B.";
    private static final String HELPTEXT = "Usage: " + NAME + " <file A> <file B> <out file>\n" + EXPLANATION;

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
     * Executes this command.
     * @param args
     * @return
     * @throws CommandExecutionException 
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length != 4) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }
        File fileA = new File(args[1]);
        if (!fileA.exists() || !fileA.isFile()) {
            return new CommandExecutionResult(false, "File A is not a valid file.");
        }
        File fileB = new File(args[2]);
        if (!fileB.exists() || !fileB.isFile()) {
            return new CommandExecutionResult(false, "File B is not a valid file.");
        }

        File outFile = new File(args[3]);

        //prepare readers and writer
        LineReader readerA = null;
        LineReader readerB = null;
        LineWriter writer = null;
        try {
            readerA = new LineReader(fileA);
            readerB = new LineReader(fileB);
            writer = new LineWriter(outFile);

            //read initial lines
            String lineA = readerA.readLine();
            String lineB = readerB.readLine();
            while (lineA != null && lineB != null) {

                //comparison
                int result = lineA.compareTo(lineB);

                //lineA < lineB -> write A, read more As
                if (result < 0) {
                    writer.writeLine(lineA);
                    lineA = readerA.readLine();
                    continue;
                } //lineA > lineB -> read more Bs
                else if (result > 0) {
                    lineB = readerB.readLine();
                    continue;
                } //lineA = lineB -> read one A
                else {
                    lineA = readerA.readLine();
                    continue;
                }
            }

            //empty rest of reader A
            while (lineA!= null) {
                writer.writeLine(lineA);
                lineA = readerA.readLine();
            }

            readerA.close();
            readerB.close();
            writer.close();

        } catch (FileNotFoundException ex) {
            throw new CommandExecutionException(ex);
        } catch (IOException ex) {
            throw new CommandExecutionException(ex);
        }

        return new CommandExecutionResult(true);

    }
}
