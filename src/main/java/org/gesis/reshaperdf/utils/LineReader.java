/*
 * Copyright (C) 2016 GESIS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, see 
 * http://www.gnu.org/licenses/ .
 */
package org.gesis.reshaperdf.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * @author Felix Bensmann
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
    
    
    public ArrayList<String>read2List(ArrayList<String> list) throws IOException{
        while(!isEmpty()){
            list.add(readLine());
        }
        return list;
    }
    
    
    public void close() throws IOException{
        br.close();
    }
    
    
    
}
