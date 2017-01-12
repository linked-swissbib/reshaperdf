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
package org.gesis.reshaperdf.cmd.version;

import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;

/**
 * @author Felix Bensmann
 * Prints the version to screen.
 */
public class VersionCommand implements ICMD{

    public String NAME = "version";
    public String EXPLANATION = "Prints the version to the screen, e.g. v0.1 .";
    public String HELPTEXT = "Usage: "+NAME+"\n"+EXPLANATION;
    public String version = "";
    
    public VersionCommand(String version){
        this.version = version;
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
     * Executes this command.
     * @param args
     * @return
     * @throws CommandExecutionException 
     */
    @Override
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException {
        if(args.length != 1)
            return new CommandExecutionResult(false,"Invalid parameter count.");
        System.out.println(version);
        return new CommandExecutionResult(true);
    }
    
}
