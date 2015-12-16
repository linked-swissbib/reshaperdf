package org.gesis.reshaperdf.cmd.boundary;

/**
 * An exception to be thrown in case of an error in a command.
 */
public class CommandExecutionException extends Exception{
    
    
    public CommandExecutionException(Exception ex){
        super(ex);
    }
    
    public CommandExecutionException(String str){
        super(str);
    }
    
}
