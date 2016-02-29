/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gesis.reshaperdf.utils.jsonldparser;

import com.fasterxml.jackson.core.JsonParseException;
import com.github.jsonldjava.core.DocumentLoader;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.RemoteDocument;
import com.github.jsonldjava.utils.JsonUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 *
 * @author bensmafx
 */
public class FileDocumentLoader extends DocumentLoader {

    private Map<String,File> map = null;
    
    public FileDocumentLoader(Map<String, File> map) {
        super();
        this.map = map;
    }

//    @Override
//    public RemoteDocument loadDocument(String url) throws JsonLdError {
//        String disallowRemote = System.getProperty(DocumentLoader.DISALLOW_REMOTE_CONTEXT_LOADING);
//
//        if ("true".equalsIgnoreCase(disallowRemote)) {
//            throw new JsonLdError(JsonLdError.Error.LOADING_REMOTE_CONTEXT_FAILED, url);
//        }
//
//        final RemoteDocument doc = new RemoteDocument(url, null);
//        try {
//            doc.setDocument(fromURL(new URL(url)));
//        } catch (final Exception e) {
//            throw new JsonLdError(JsonLdError.Error.LOADING_REMOTE_CONTEXT_FAILED, url);
//        }
//        return doc;
//    }

    @Override
    public Object fromURL(java.net.URL url) throws JsonParseException, IOException {
        File file = map.get(url.toString());
        if(file == null){
            super.fromURL(url);
        }
        return JsonUtils.fromInputStream(new FileInputStream(file),"UTF-8");
    }
    
    @Override
    public InputStream openStreamFromURL(java.net.URL url) throws IOException {
        File file = map.get(url);
        if(file == null){
            super.openStreamFromURL(url);
        }
        return new FileInputStream(file);
    }
}
