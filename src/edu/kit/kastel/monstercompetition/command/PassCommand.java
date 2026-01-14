package edu.kit.kastel.monstercompetition.command;

import edu.kit.kastel.monstercompetition.model.monster.Monster;
import edu.kit.kastel.monstercompetition.model.game.Competition;
import edu.kit.kastel.monstercompetition.util.RoundHandler;

/**
 * "pass"
 * Command that allows a monster to skip its turn.
 *
 * @author uozqc
 */
public class PassCommand implements Command {

    // Error message constants
    private static final String ERROR_NO_ARGS = "Error, the 'pass' command does not accept any arguments.";
    private static final String ERROR_NO_MONSTER = "Error, no monster is currently selecting an action.";
    
    // Message constants
    private static final String MESSAGE_WHAT_SHOULD_DO = "What should %s do?";
    
    // Numeric constants
    private static final int MAX_ARGS = 0;
    
    // Formatting constants
    private static final String NEWLINE = "";

    private final CommandHandler commandHandler;

    /**
     * Creates a new PassCommand.
     *
     * @param commandHandler The command handler
     */
    public PassCommand(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public boolean execute(String[] args) {
        // Check if additional arguments were provided
        if (args.length > MAX_ARGS) {
            System.out.println(ERROR_NO_ARGS);
            Monster currentMonster = commandHandler.getCurrentActionMonster();
            if (currentMonster != null) {
                System.out.println();
                System.out.println(String.format(MESSAGE_WHAT_SHOULD_DO, currentMonster.getName()));
            }
            return false;
        }

        Monster currentMonster = commandHandler.getCurrentActionMonster();
        if (currentMonster == null) {
            System.out.println(ERROR_NO_MONSTER);
            return false;
        }

        RoundHandler roundHandler = commandHandler.getRoundHandler();
        roundHandler.setMonsterPass(currentMonster);

        if (commandHandler.moveToNextMonster()) {
            Monster nextMonster = commandHandler.getCurrentActionMonster();
            System.out.println();
            System.out.println(String.format(MESSAGE_WHAT_SHOULD_DO, nextMonster.getName()));
        } else {
            Competition competition = commandHandler.getCompetition();
            roundHandler.executePhaseII(competition);

            if (!competition.isEnded()) {
                roundHandler.startNewRound(competition);
            }
        }

        return true;
    }
}