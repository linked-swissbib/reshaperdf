package org.gesis.reshaperdf.utils;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import org.openrdf.model.Statement;

/**
 * Uses a PullReader to summarize statements in a sorted N-Triples file and
 * provide the together as resource.
 */
public class ResourcePullReader {

    private File inFile = null;
    private PullReader pullReader = null;
    private Queue<Statement> queue = null;
    private String currentResource = null;

    /**
     * Ctor
     * @param file 
     */
    public ResourcePullReader(File file) {
        this.inFile = file;
        pullReader = new PullReader(file);
        queue = new LinkedList<Statement>();
    }

    public File getFile() {
        return inFile;
    }

    public void load() {
        pullReader.load();
    }

    public boolean isEmpty() {
        return pullReader.isEmpty() && queue.isEmpty();
    }

    /**
     * Delivers the first resource but does not remove it.
     * @return 
     */
    public Statement[] peek() {
        if (this.isEmpty()) {
            return null;
        }
        if (currentResource == null) {
            currentResource = pullReader.peek().getSubject().stringValue();
        }
        if (queue.isEmpty() && !pullReader.isEmpty()) {
            //read whole resource if available
            while (!pullReader.isEmpty() && currentResource.equals(pullReader.peek().getSubject().stringValue())) {
                queue.add(pullReader.peek());
                pullReader.removeHead();
            }
            if (!pullReader.isEmpty()) {
                currentResource = pullReader.peek().getSubject().stringValue();
            }
        }
        return queue.toArray(new Statement[queue.size()]);

    }

    /**
     * Removes the first resource from the queue.
     */
    public void removeHead() {
        queue.clear();
    }

}
