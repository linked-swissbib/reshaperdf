package org.gesis.reshaperdf.utils;

import org.openrdf.model.Statement;

/**
 * An inteface for resource handler. These handler can be use by the
 * ResourceReader class.
 */
public interface IResourceHandler {

    /**
     * To be called when the processing starts.
     */
    public void onStart();

    /**
     * Is called to handle a resource.
     * @param res 
     */
    public void handleResource(Statement[] res);

    /**
     * To be called when the processing stops.
     */
    public void onStop();

}
