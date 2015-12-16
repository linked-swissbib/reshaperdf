package org.gesis.reshaperdf.cmd.boundary;

/**
 * Interface for the different commands.
 */
public interface ICMD {
    
    public String getName();
    public String getExplanation();
    public String getHelptext();
    public CommandExecutionResult execute(String[] args) throws CommandExecutionException;
    public boolean finished = false;
    
}
