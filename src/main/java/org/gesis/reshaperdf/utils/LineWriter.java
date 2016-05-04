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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * @author Felix Bensmann
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
