package org.gesis.reshaperdf.cmd.correct;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;

/**
 * Removes invalid ntriples from a given file.
 */
public class CorrectCommand implements ICMD {

    public String NAME = "correct";
    public String EXPLANATION = "Removes invalid ntriples from a given file";
    public String HELPTEXT = "Usage: correct <infile> <outfile>\nRemoves invalid ntriples from a given file.";

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
        if (args.length != 3) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }
        File inFile = new File(args[1]);
        if (!inFile.exists() || !inFile.isFile()) {
            return new CommandExecutionResult(false, "Input file is not a valid file.");
        }
        File outFile = new File(args[2]);

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "UTF-8"));

            String line = br.readLine();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));

            String lastLine = "";
            while (line != null) {
                boolean drop = false;

                //replace invalid characters
                line = replaceBadSequence(line);
                String[] splitLine = line.split(" ");
                String obj = "";

                for (int i = 2; i < splitLine.length - 1; i++) {
                    obj += splitLine[i];
                    if(i!=splitLine.length-2){
                        obj+=" ";
                    }
                }

                if (obj.startsWith("<") && obj.endsWith(">")) {

                    if (containsInvalidBSSequence(obj)) {
                        drop = true;
                    }

                    if (containsSpecialChar(obj)) {
                        drop = true;
                    }

                } //write
                if (!drop) {
                    bw.write(line + "\n");
                } else {
                    System.out.println("Dropped statment " + line + " due to invalid formatting.");
                }
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

    /**
     * Searches in a String for surrogate sequences and replaces the surrogates
     * with '?' \uD800 - \uFFFF in plain text these are not valid in UTF-8 (only
     * in UTF-16) Source: https://de.wikipedia.org/wiki/UTF-8
     *
     * @param s The String to examine.
     * @return true if a surrogate character is present, false otherwise.
     */
    private static String replaceBadSequence(String s) {

        String str = s;
        int idx = 0;
        idx = str.indexOf("\\u", idx);
        while (idx != -1) {
            String subsequ = str.substring(idx + 2, idx + 6);
            int hex = 0;
            try {
                hex = Integer.valueOf(subsequ, 16);
            } catch (NumberFormatException nfe) {
                return null;
            }
            if (hex >= 0xD800 && hex <= 0xFFFF) {
                str = str.replace("\\u" + subsequ, "?");
                System.out.println("Changed statement "+s +" to "+str);
            }
            idx = str.indexOf("\\u", idx + 1);
        }
        return str;

    }

    /**
     * Examines a URL for sequences with \ but no following u.
     *
     * @param url
     * @return
     */
    private boolean containsInvalidBSSequence(String str) {

        int idx = str.indexOf("\\", 0);

        while (idx >= 0) {

            if (idx < str.length() - 1) {
                char c = str.charAt(idx + 1);
                if (c != 'u') {
                    return true;
                }
            } else {
                return true;
            }
            idx = str.indexOf("\\", idx + 1);
        }
        return false;
    }

    /**
     * Checks URL whether it contains spaces.
     *
     * @param url
     * @return True if it contains spaces, otherwise false.
     */
    private boolean containsSpecialChar(String str) {
        if (str.contains(" ")) {
            return true;
        }
        return false;
    }

}
