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

import org.openrdf.model.Statement;

/**
 * @author Felix Bensmann
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
