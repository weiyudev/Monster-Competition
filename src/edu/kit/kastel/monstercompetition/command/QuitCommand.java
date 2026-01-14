package edu.kit.kastel.monstercompetition.command;

import edu.kit.kastel.monstercompetition.model.monster.Monster;

/**
 * Exit the program. This is handled directly in Main.java, so this class is just a placeholder.
 *
 * @author uozqc
 */
public class QuitCommand implements Command {
    // Error message constants
    private static final String ERROR_EXTRA_ARGS = "Error, the 'quit' command does not accept any arguments.";
    
    // Format constants
    private static final String ASK_WHAT_SHOULD_DO = "What should %s do?";
    
    // Numeric constants
    private static final int MAX_ARGS = 0;
    
    // Formatting constants
    private static final String NEWLINE = "";

    private final CommandHandler commandHandler;
    
    /**
     * Creates a new QuitCommand.
     *
     * @param commandHandler commandHandler
     */
    public QuitCommand(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public boolean execute(String[] args) {
        // Check if additional arguments were provided
        if (args.length > MAX_ARGS) {
            System.out.println(ERROR_EXTRA_ARGS);
            
            // Display the action prompt for the current monster if applicable
            Monster currentMonster = commandHandler.getCurrentActionMonster();
            if (currentMonster != null) {
                System.out.println();
                System.out.println(String.format(ASK_WHAT_SHOULD_DO, currentMonster.getName()));
            }
            
            return false;
        }
        
        // This is handled directly in Main.java
        return true;
    }
}