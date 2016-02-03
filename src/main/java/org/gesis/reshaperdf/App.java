package org.gesis.reshaperdf;

import org.gesis.reshaperdf.cmd.analyzeproperty.AnalyzeTypeCommand;
import org.gesis.reshaperdf.cmd.block.BlockCommand;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionException;
import org.gesis.reshaperdf.cmd.boundary.CommandExecutionResult;
import org.gesis.reshaperdf.cmd.boundary.ICMD;
import org.gesis.reshaperdf.cmd.checksorting.CheckSortingCommand;
import org.gesis.reshaperdf.cmd.correct.CorrectCommand;
import org.gesis.reshaperdf.cmd.extractresources.ExtractResourcesCommand;
import org.gesis.reshaperdf.cmd.filter.FilterCommand;
import org.gesis.reshaperdf.cmd.getenrichment.GetEnrichmentCommand;
import org.gesis.reshaperdf.cmd.merge.MergeCommand;
import org.gesis.reshaperdf.cmd.mergedir.MergeDirCommand;
import org.gesis.reshaperdf.cmd.ntriplify.NTriplifyCommand;
import org.gesis.reshaperdf.cmd.pumpup.PumpupCommand;
import org.gesis.reshaperdf.cmd.removeduplicates.RemoveDuplicatesCommand;
import org.gesis.reshaperdf.cmd.renameproperty.RenamePropertyCommand;
import org.gesis.reshaperdf.cmd.sort.SortCommand;
import org.gesis.reshaperdf.cmd.split.SplitCommand;
import org.gesis.reshaperdf.cmd.substract.SubtractCommand;
import org.gesis.reshaperdf.cmd.version.VersionCommand;
import org.gesis.reshaperdf.cmd.separatelinks.ExtractDuplicateLinksCommand;

/**
 * reshaperdf is a tool collection to work with RDF data based on sorted
 * NTriples. The main idea of this tool is to organize RDF data as NTriples. In
 * an alphabetically sorted NTriples file all statements describing a resource
 * are kept together due to their common subject.
 *
 * With sorted NTriples it is possible to extract resources that share certain
 * propertys e.g. a type. Extracted resources can be stored as sorted NTriples
 * as well and therefor be processed just like the original dataset. Apart from
 * extraction merging is possible, reshaping literal data is possible, removing
 * duplicates is possible and so on.
 * 
 * The general syntax is:
 * 
 * Get a list of available commands
 * java -jar reshaperdf.jar
 * 
 * Get a short description for available commands
 * java -jar reshaperdf.jar help
 * 
 * Get the help text for a command:
 * java -jar reshaperdf.jar help <command>
 * 
 * Issue a command
 * java -jar reshaperdf.jar <command> [<args>]
 */
public class App {

    //Version
    private static final String VERSION = "v0.1";
    
    //build in cmds
    private static final String CMD_HELP = "help";
    private static final String CMD_HELP_EXPLANATION = "Displays a helpt text.";
    private static final String CMD_HELP_TEXT = "Usage: help <cmd>\nDisplays informaton about a specific command.";

    /**
     * Main routine. Reads the command line args and determines what commands
     * are to be invoked.
     *
     * @param args CLI input
     */
    public static void main(String[] args) {

        //instanciate the command repository, the help command is extra because 
        //it depends on the other commands
        CommandRepository repo = new CommandRepository();
        repo.add(new VersionCommand(VERSION));
        repo.add(new NTriplifyCommand());
        repo.add(new SortCommand());
        repo.add(new MergeCommand());
        repo.add(new ExtractResourcesCommand());
        repo.add(new CorrectCommand());
        repo.add(new SplitCommand());
        repo.add(new CheckSortingCommand());
        repo.add(new MergeDirCommand());
        repo.add(new GetEnrichmentCommand());
        repo.add(new RemoveDuplicatesCommand());
        repo.add(new PumpupCommand());
        repo.add(new BlockCommand());
        repo.add(new ExtractDuplicateLinksCommand());
        repo.add(new SubtractCommand());
        repo.add(new RenamePropertyCommand());
        repo.add(new FilterCommand());
        repo.add(new AnalyzeTypeCommand());
        
        if (args.length == 0) {//check input
            printCommandList(repo); //print command list
        } else {
            if (args[0].equalsIgnoreCase(CMD_HELP)) { //handle help command
                handleHelp(args, repo);
            } else { //chose a suitable command
                ICMD cmd = repo.getCommand(args[0]);
                if (cmd == null) {
                    printUsageError("No such command.");
                    System.exit(-1);
                }
                try { //execute the command
                    CommandExecutionResult result = cmd.execute(args);
                    if (result.isSuccessful()) {
                        /* freu */
                        //successful command
                        System.exit(0);
                    } else {
                        printUsageError(result.getErrorText());
                        System.exit(-1);
                    }
                } catch (CommandExecutionException ex) {
                    printSystemError(ex.getMessage());
                    System.exit(-1);
                }
            }
        }
        System.exit(0);
    }

    /**
     * Handles the help command.
     * @param args
     * @param repo 
     */
    private static void handleHelp(String[] args, CommandRepository repo) {
        if (args.length == 1) { 
            printHelp(repo); //print short description of every command
        } else if (args.length == 2) {
            printHelp(repo, args[1]); //print full help text of a specific command
        }
    }

    /**
     * Prints a short description of every command in the repository.
     * @param repo 
     */
    private static void printHelp(CommandRepository repo) {
        for (int i = 0; i < repo.size(); i++) {
            System.out.print(repo.get(i).getName());
            System.out.print("\t\t");
            System.out.println(repo.get(i).getExplanation());
        }
        //Add description of the help command
        System.out.print(CMD_HELP);
        System.out.print("\t\t");
        System.out.println(CMD_HELP_EXPLANATION);

    }

    /**
     * Prints the help text for a specific command.
     * @param repo
     * @param cmdName 
     */
    private static void printHelp(CommandRepository repo, String cmdName) {
        if (cmdName.equalsIgnoreCase(CMD_HELP)) { //if help for help is wanted
            System.out.println(CMD_HELP_TEXT);
        } else { //if help for any other cmd is wanted
            ICMD cmd = repo.getCommand(cmdName);
            if (cmd == null) {
                printUsageError("Command \"" + cmdName + "\" not found");
                return;
            } else {
                System.out.println(cmd.getHelptext());
            }
        }
    }

    /**
     * Prints a list of all commands in repo.
     * @param repo 
     */
    private static void printCommandList(CommandRepository repo) {
        String[] arr = repo.getCommandList();
        for (int i = 0; i < arr.length; i++) {
            System.out.println(arr[i]);
        }
        System.out.println(CMD_HELP);
    }

    /**
     * Prints and formats a usage error message.
     * @param message 
     */
    private static void printUsageError(String message) {
        System.out.println("Usage error: " + message);
    }

    /**
     * Prints and formats a system error message.
     * @param message 
     */
    private static void printSystemError(String message) {
        System.out.println("Application error: " + message);
    }

}
