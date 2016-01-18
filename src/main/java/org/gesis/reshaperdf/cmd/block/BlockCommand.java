package org.gesis.reshaperdf.cmd.block;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.CheckedNTriplesWriter;
import org.gesis.reshaperdf.utils.LineCounter;
import org.gesis.reshaperdf.utils.ResourcePullReader;
import org.gesis.reshaperdf.utils.Splitter;
import org.gesis.reshaperdf.utils.StrictStatementFilter;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

/**
 * Extracts resources and groups them in a new file. Group criterion is the
 * first letter of a given object.
 */
public class BlockCommand implements ICMD {

    private String NAME = "block";
    private String EXPLANATION = "Separates the input data according to its literals alphabetic order.";
    private String HELPTEXT = "Usage: " + NAME + " <infile> <outputdir> <predicate> \n" + EXPLANATION;

    private Map<Character, RDFWriter> map = null;

    public BlockCommand() {
        map = new HashMap<Character, RDFWriter>();
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
     * Extracts resources and groups them in a new file. Group criterion is the
     * first letter of a given object.
     *
     * @param args cli args. args[0] is the name of this command.
     * @return
     * @throws CommandExecutionException
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length < 4) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }

        File inFile = new File(args[1]);
        if (!inFile.exists() && !inFile.isFile()) {
            return new CommandExecutionResult(false, "Invalid file: " + inFile.getAbsolutePath());
        }

        File outputDir = new File(args[2]);

        //this predicate marks the object to examine
        String predicate = args[3];

        //Iterate over all !resources! from inFile...
        ResourcePullReader rpReader = new ResourcePullReader(inFile);
        rpReader.load();
        while (!rpReader.isEmpty()) {
            Statement[] res = rpReader.peek();
            rpReader.removeHead();

            for (int i = 0; i < res.length; i++) {//iterate over all statements in a resource...
                if (res[i].getPredicate().stringValue().equals(predicate)) {//if is right property....
                    String obj = res[i].getObject().stringValue();
                    if (obj.length() > 0) { //...process its object
                        char c = obj.charAt(0);
                        //create a writer for the first letters file or use an existing one
                        RDFWriter writer = map.get(c);
                        if (writer == null) { 
                            int asInt = (int) c;
                            String fileName = "u" + Integer.toHexString(asInt).toUpperCase() + ".nt";
                            File file = new File(outputDir, fileName);
                            try {
                                writer = new CheckedNTriplesWriter(new FileOutputStream(file, true), new StrictStatementFilter());
                                map.put(c, writer);
                            } catch (FileNotFoundException ex) {
                                throw new CommandExecutionException(ex);
                            }
                        }
                        //write the whole resource each time a propterty was found
                        try {
                            writeResource(res, writer);
                        } catch (RDFHandlerException ex) {
                            throw new CommandExecutionException(ex);
                        }

                    }

                }
            }

        }
        System.out.println("Blocking done. Splitting...");
        //In case the size of individual exceeds a certain amount these files are further subdivided.
        try {
            furtherSplit(outputDir);
        } catch (IOException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException(ex);
        }

        System.out.println("Done");
        return new CommandExecutionResult(true);

    }

    /**
     * Writes a given resource with the given writer.
     * @param res
     * @param writer
     * @throws RDFHandlerException 
     */
    private static void writeResource(Statement[] res, RDFWriter writer) throws RDFHandlerException {
        writer.startRDF();
        for (int i = 0; i < res.length; i++) {
            writer.handleStatement(res[i]);
        }
        writer.endRDF();
    }

   
    /**
     * Subdivides the files in a given directory if they exceed a certain count of statements
     * @param inDir
     * @throws IOException
     * @throws FileNotFoundException
     * @throws RDFHandlerException 
     */
    private static void furtherSplit(File inDir) throws IOException, FileNotFoundException, RDFHandlerException {
        long stmtsPerFile = 200000;
        long resourcesPerFile = 50000;

        ArrayList<File> fileList = findLongFiles(inDir, stmtsPerFile);

        for (File f : fileList) {
            int idx = f.getAbsolutePath().lastIndexOf(".");
            String prefix = f.getAbsolutePath().substring(0, idx);
            prefix = prefix + "_";
            Splitter.split(f, prefix, resourcesPerFile);
            f.delete();
        }

    }

    /**
     * Finds files in a dir whose line count exceeds the given threshold.
     * @param inDir Directory to search through
     * @param longerThan threshold
     * @return
     * @throws IOException 
     */
    private static ArrayList<File> findLongFiles(File inDir, long longerThan) throws IOException {
        ArrayList<File> fileList = new ArrayList<File>();
        for (File f : inDir.listFiles()) {
            long cnt = LineCounter.countLines(f.getAbsolutePath());
            if (cnt > longerThan) {
                fileList.add(f);
            }
        }
        return fileList;
    }
    
    
   
    

}
