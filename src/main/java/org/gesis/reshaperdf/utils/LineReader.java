package org.gesis.reshaperdf.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * A reader for line based data. Buffers one line to provide a isEmpty-method.
 * @author bensmafx
 */
public class LineReader {
    
    private static final String UTF8 = "UTF-8";
    
    private File file  =null;
    private BufferedReader br = null;
    private String buf = null;
    
    /**
     * Ctor calls value ctor, with UTF-8 charset.
     * @param file
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public LineReader(File file) throws FileNotFoundException, IOException{
        this(file,Charset.forName(UTF8));
    }
    
    /**
     * Ctor
     * @param file
     * @param cs
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public LineReader(File file, Charset cs) throws FileNotFoundException, IOException{
        this.file=file;
        br = new BufferedReader(new InputStreamReader(new FileInputStream(file), cs));
        buf = br.readLine();
    }
    
    
    
    public boolean isEmpty(){
        if(buf == null)
            return true;
        return false;
    }
    
    public String readLine() throws IOException{
        String retVal = buf;
        buf = br.readLine();
        return retVal;
    }
    
    
    public void close() throws IOException{
        br.close();
    }
    
    
    
}
