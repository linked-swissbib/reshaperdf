package org.gesis.reshaperdf.cmd.securelooseends;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.utils.CheckedNTriplesWriter;
import org.gesis.reshaperdf.utils.ObjectComparator;
import org.gesis.reshaperdf.utils.PullReader;
import org.gesis.reshaperdf.utils.ResourcePullReader;
import org.gesis.reshaperdf.utils.StatementsComparatorO;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;

/**
 *
 */
public class SecureLooseEndsCommand implements ICMD {

    private static final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
    private static final String DC_TITEL = "http://purl.org/dc/terms/title";
    private static final String FOAF_NAME = "http://xmlns.com/foaf/0.1/name";
    private static final String RDFS_COMMENT = "http://www.w3.org/2000/01/rdf-schema#comment";
    private static final String DC_DESCRIPTION = "http://purl.org/dc/elements/1.1/description";
    private static final String[] PROPERTIES = new String[]{RDFS_LABEL, DC_TITEL, FOAF_NAME, RDFS_COMMENT, DC_DESCRIPTION};

    private String NAME = "securelooseends";
    private String EXPLANATION = "Extracts resources from file B that are referenced in file A. "
            + "Then reduces this resource to a meaningful string and adds it to the original resource.";
    private String HELPTEXT = "Usage: " + NAME + " <file A> <file B> <outfile> <predicate1> <substitue1>[<predicate2> ...] \n" + EXPLANATION;

    public SecureLooseEndsCommand() {

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
     *
     *
     * @param args cli args. args[0] is the name of this command.
     * @return
     * @throws CommandExecutionException
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        //check args
        if (args.length < 6) {
            return new CommandExecutionResult(false, "Invalid parameter count.");
        }

        File inFileA = new File(args[1]);
        if (!inFileA.exists() && !inFileA.isFile()) {
            return new CommandExecutionResult(false, "Invalid file: " + inFileA.getAbsolutePath());
        }

        File inFileB = new File(args[2]);
        if (!inFileB.exists() && !inFileB.isFile()) {
            return new CommandExecutionResult(false, "Invalid file: " + inFileB.getAbsolutePath());
        }

        File outFile = new File(args[3]);

        //collects the remaining property-substitute pairs
        int off = 4;
        int argsRemaining = args.length - off;
        int pairs = argsRemaining/2;
        Map<String, String> map = new HashMap<String, String>();
        int iter=0;
        for (int i = 0; i < pairs; i++) {
            map.put(args[off+ iter ], args[off + iter + 1]);
            iter+=2;
        }

        //Step 1: get all resource URIs from file A that are specified by the property array
        System.out.println("Reading");
        PullReader reader = new PullReader(inFileA);
        reader.load();
        ArrayList<Statement> stmtList = new ArrayList<Statement>();
        while (reader.peek() != null) {
            Statement stmt = reader.peek();
            reader.removeHead();
            String predicate = stmt.getPredicate().stringValue();
            if (map.containsKey(predicate)) {
                stmtList.add(stmt);
            }
        }
        if (stmtList.isEmpty()) {
            System.out.println("No statements with the specified properties were found.");
            return new CommandExecutionResult(true);
        }

        //Step 2: sort these URIs by their objects
        System.out.println("Sorting");
        Collections.sort(stmtList, new StatementsComparatorO());

        //remove duplicates
        for (int i = stmtList.size() - 1; i > 0; i--) {
            if (stmtList.get(i).equals(stmtList.get(i - 1))) {
                System.out.println("Removing duplicate " + stmtList.get(i));
                stmtList.remove(i);
            }
        }

        //Step 3: use the sorted property URI array to extract the referenced resources
        System.out.println("Extracting");
        CheckedNTriplesWriter writer;
        try {
            writer = new CheckedNTriplesWriter(new FileOutputStream(outFile), null);
            writer.startRDF();

            ResourcePullReader rpReader = new ResourcePullReader(inFileB);
            rpReader.load();
            Statement[] res = null;
            res = rpReader.peek();
            rpReader.removeHead();
            String subj = res[0].getSubject().stringValue();
            Statement link = stmtList.get(0);
            while (!stmtList.isEmpty() && rpReader.peek() != null) {

                //comparison
                int result = link.getObject().stringValue().compareTo(subj);
                //System.out.println(obj+"|"+subj+"|"+result);
                                    
           
                //obj < subj
                if (result < 0) {
                    //ressource could not be found, another alphanum. greater one is already present
                    System.out.println("Resource not found: " + link.getObject().stringValue());
                    link = stmtList.get(0);
                    stmtList.remove(0);
                    continue;
                } //link.obj == subj
                else if (result == 0) {
                    //ressource found -> extract
                    System.out.println("Found resource " + link.getObject().stringValue());
                    Literal[] summarizations = outlineResource(res, PROPERTIES);
                    Resource subject = link.getSubject();
                    URI predicate = new URIImpl(map.get(link.getPredicate().stringValue()));
                    for (Literal l : summarizations) {
                        Statement st = new StatementImpl(subject, predicate, l);
                        writer.handleStatement(st);
                    }
                    link = stmtList.get(0);
                    stmtList.remove(0);
                    continue;
                } //obj > subj
                else if (result > 0) {
                    //continue searching...
                    res = rpReader.peek();
                    rpReader.removeHead();
                    subj = res[0].getSubject().stringValue();
                    continue;
                }
            }
            writer.endRDF();
        } catch (FileNotFoundException ex) {
            throw new CommandExecutionException(ex);
        } catch (RDFHandlerException ex) {
            throw new CommandExecutionException(ex);
        }
        return new CommandExecutionResult(true);

    }

    /**
     * Summarizes a resource into one literal.
     *
     * @param res
     * @return
     */
    private static Literal[] outlineResource(Statement[] res, String[] properties) {
        //Search for given properties, if literals were found for one properties, the remains are not searched.
        ArrayList<Literal> literalList = new ArrayList<Literal>();
        for (int i = 0; i < properties.length; i++) {
            addFindings(res, properties[i], literalList);
            if (!literalList.isEmpty()) {
                break;
            }
        }
        //if no literal could be found, use subject uri
        if (literalList.isEmpty()) {
            Resource r = res[0].getSubject();
            if (r instanceof URI) {
                URI uri = (URI) r;
                literalList.add(new LiteralImpl(uri.getLocalName()));
            } else {
                literalList.add(new LiteralImpl(r.stringValue()));
            }
        }
        //Remove duplicates
        Collections.sort(literalList, new ObjectComparator());
        for (int i = literalList.size() - 1; i > 0; i--) {
            if (literalList.get(i).equals(literalList.get(i - 1))) {
                literalList.remove(i);
            }
        }
        //concat findings to a single string
        return literalList.toArray(new Literal[literalList.size()]);
    }

    /**
     * Searches a resource for statements with the given property and adds the
     * corresponding object to the given list.
     *
     * @param res
     * @param property
     * @param list
     */
    private static void addFindings(Statement[] res, String property, ArrayList<Literal> list) {
        for (int i = 0; i < res.length; i++) {
            if (res[i].getPredicate().stringValue().equals(property)) {
                list.add((Literal) res[i].getObject());
            }
        }
    }

}
