package org.gesis.reshaperdf.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * Wrapper for a BufferedReader, uses auto append and sets UTF-8 charset.
 */
public class LineWriter {
    
    private static final String UTF8 = "UTF-8";
    
    private File file = null;
    private BufferedWriter writer = null;
    
    public LineWriter(File file) throws FileNotFoundException{
        this(file, Charset.forName(UTF8));      
    }
    
    public LineWriter(File file, Charset cs) throws FileNotFoundException{
        this.file = file;
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true),cs));
    }
    
    public void writeLine(String line) throws IOException{
        writer.write(line);
        writer.write("\n");
    }
    
    public void close() throws IOException{
        writer.close();
    }
    
}
