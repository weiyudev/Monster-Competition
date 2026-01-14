package edu.kit.kastel.monstercompetition.command;

import edu.kit.kastel.monstercompetition.model.game.Competition;
import edu.kit.kastel.monstercompetition.model.game.Phase;
import edu.kit.kastel.monstercompetition.util.paser.ConfigParser;
import edu.kit.kastel.monstercompetition.util.InvalidArgumentException;
import edu.kit.kastel.monstercompetition.model.monster.Monster;

import java.io.IOException;
import java.util.List;

/**
 * Replaces current configuration. The old competition is reset.
 *
 * @author uozqc
 */
public class LoadCommand implements Command {

    // Error message constants
    private static final String ERROR_USAGE = "Error, usage: load <path> [<seed>|debug]";
    private static final String ERROR_SECOND_ARG =
            "Error, the second argument must be either 'debug' or a numeric seed value.";
    private static final String ERROR_LOAD_FAILED = "Error, failed to load file %s: %s";
    
    // Other message constants
    private static final String MESSAGE_SEED_NOTE =
            "Note: The seed/debug parameter is accepted but not applied in the current implementation.";
    private static final String MESSAGE_LOADED = "Loaded %d actions, %d monsters.";
    
    // Parameter constants
    private static final String DEBUG_PARAMETER = "debug";
    
    // Numeric constants
    private static final int MIN_ARGS = 1;
    private static final int MAX_ARGS = 2;
    private static final int PATH_INDEX = 0;
    private static final int SECOND_ARG_INDEX = 1;
    
    // Formatting constants
    private static final String NEWLINE = "";

    private final ConfigParser parser;
    private final Competition competition;
    private final CommandHandler commandHandler;

    /**
     * Constructor.
     * @param parser the current ConfigParser
     * @param competition the competition to manage
     * @param commandHandler the command handler to reset state
     */
    public LoadCommand(ConfigParser parser, Competition competition, CommandHandler commandHandler) {
        this.parser = parser;
        this.competition = competition;
        this.commandHandler = commandHandler;
    }

    @Override
    public boolean execute(String[] args) {
        if (args.length < MIN_ARGS || args.length > MAX_ARGS) {
            System.out.println(ERROR_USAGE);
            return false;
        }

        String newPath = args[PATH_INDEX];
        
        // Validate optional seed or debug parameter format
        if (args.length == MAX_ARGS) {
            String arg2 = args[SECOND_ARG_INDEX];
            if (!DEBUG_PARAMETER.equalsIgnoreCase(arg2)) {
                // Try to parse as a seed (number)
                try {
                    Long.parseLong(arg2);
                } catch (NumberFormatException e) {
                    System.out.println(ERROR_SECOND_ARG);
                    return false;
                }
            }
            
            // Note: Currently the seed/debug values are not used in this implementation,
            // as they would require restarting the game with a new RandomNumberGenerator.
            // They are only validated for format correctness.
            System.out.println(MESSAGE_SEED_NOTE);
        }
        
        ConfigParser newParser = new ConfigParser();
        try {
            newParser.parseConfigFile(newPath);
        } catch (IOException | InvalidArgumentException e) {
            System.out.println(String.format(ERROR_LOAD_FAILED, newPath, e.getMessage()));
            return false;
        }

        // If success => apply new config
        parser.replaceWith(newParser);
        competition.reset();
        
        // Reset the command handler's state completely - no prompts
        commandHandler.resetMonsterIndex();
        commandHandler.setCurrentActionMonster(null);
        
        // Ensure we don't have any leftover RoundHandler state
        if (commandHandler.getRoundHandler() != null) {
            // Reset all action choices
            commandHandler.getRoundHandler().setCurrentPhase(Phase.PHASE0);
        }

        List<Monster> monsters = newParser.getParsedMonsters();
        for (Monster monster : monsters) {
            competition.addMonster(monster);
        }

        int numActions = newParser.getParsedActions().size();
        int numMonsters = newParser.getParsedMonsters().size();
        System.out.println();
        System.out.println(String.format(MESSAGE_LOADED, numActions, numMonsters));
        return true;
    }
}
