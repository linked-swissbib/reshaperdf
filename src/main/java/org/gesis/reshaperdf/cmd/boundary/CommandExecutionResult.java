package org.gesis.reshaperdf.cmd.boundary;

/**
 * Stores the result of an execution.
 */
public class CommandExecutionResult {
    
    private boolean successful = false;
    private String errorText = "no error";
    
    public CommandExecutionResult(boolean successful){
        this.successful = successful;
    }
    
    public CommandExecutionResult(boolean successful, String errorMessage){
        this.successful = successful;
        this.errorText = errorMessage;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getErrorText() {
        return errorText;
    }
    
    
    
}
