package org.gesis.reshaperdf.cmd.renameproperty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.CheckedNTriplesWriter;
import org.gesis.reshaperdf.utils.PullReader;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;

/**
 * Renames properties in an N-Triples file. Long forms are required for
 * namespaces.
 */
public class RenamePropertyCommand implements ICMD {

    private String NAME = "renameproperty";
    private String EXPLANATION = "Replaces properties with other properties.";
    private String HELPTEXT = "Usage: " + NAME + " <infile> <outfile> <property> <substitute> [<property> <substitute>...] \n" + EXPLANATION;
    private Map<String, String> map = null;

    public RenamePropertyCommand() {
        map = new HashMap<String, String>();
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
     * Executes this command. Examines every statement for the properties to
     * find and replaces the property by its substitute.
     *
     * @param args
     * @return
     * @throws CommandExecutionException
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length < 5 || ((args.length) % 2 != 1)) { //odd parameter count
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }

        File inFile = new File(args[1]);
        if (!inFile.exists() && !inFile.isFile()) {
            return new CommandExecutionResult(false, "Invalid file: " + inFile.getAbsolutePath());
        }

        File outFile = new File(args[2]);

        for (int i = 3; i < args.length; i += 2) {
            String prop = args[i];
            String subst = args[i + 1];
            map.put(prop, subst);
        }

        try {
            PullReader reader = new PullReader(inFile);
            reader.load();
            CheckedNTriplesWriter writer = new CheckedNTriplesWriter(new FileOutputStream(outFile), null);
            writer.startRDF();
            while (!reader.isEmpty()) { // iterate over statements
                Statement st = reader.peek();
                reader.removeHead();
                String prop = st.getPredicate().stringValue();
                if (map.containsKey(prop)) { //replace if necessary
                    st = new StatementImpl(st.getSubject(), new URIImpl(map.get(prop)), st.getObject());
                }
                writer.handleStatement(st);
            }
            writer.endRDF();
        } catch (IOException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException(ex);
        }
        return new CommandExecutionResult(true);
    }
}
