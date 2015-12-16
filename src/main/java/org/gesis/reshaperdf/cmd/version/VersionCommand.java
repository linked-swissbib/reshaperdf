package org.gesis.reshaperdf.cmd.version;

import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;

/**
 * Prints the version to screen.
 */
public class VersionCommand implements ICMD{

    public String NAME = "version";
    public String EXPLANATION = "Prints the version to screen.";
    public String HELPTEXT = "Prints the version to screen, e.g. v0.1";
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
