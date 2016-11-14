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
package org.gesis.reshaperdf.utils.jsonldparser;

import com.fasterxml.jackson.core.JsonParseException;
import com.github.jsonldjava.core.DocumentLoader;
import com.github.jsonldjava.utils.JsonUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Extension of DocumentLoader needed to use a JSON-LD context from an external
 * document.
 *
 * @author Felix Bensmann
 */
public class FileDocumentLoader extends DocumentLoader {

    private Map<String, File> map = null;
    private static Map<java.net.URL, Object>cache = null;

    public FileDocumentLoader(Map<String, File> map) {
        super();
        this.map = map;
        if(cache==null){
            cache = new HashMap<java.net.URL, Object>();
        }
    }

    @Override
    public Object fromURL(java.net.URL url) throws JsonParseException, IOException {
        //respond with cache
        if(cache.containsKey(url)){
            return cache.get(url);
        }
        //retrieve
        Object ctx = null;
        boolean exception = false;
        try {
            ctx = super.fromURL(url);
        } catch (IOException ioe) {
            exception = true;
        }
        if (ctx == null || exception) {
            File file = map.get(url.toString());
            ctx = JsonUtils.fromInputStream(new FileInputStream(file), "UTF-8");
            if (file == null) {
                throw new IOException("Unable to obtain a context.");
            }
        }
        
        //cache context
        if(!cache.containsKey(url)){
            cache.put(url, ctx);
        }
        return ctx;
    }
    

    @Override
    public InputStream openStreamFromURL(java.net.URL url) throws IOException {
        File file = map.get(url);
        if (file == null) {
            super.openStreamFromURL(url);
        }
        return new FileInputStream(file);
    }
}
