package edu.kit.kastel.monstercompetition;

import edu.kit.kastel.monstercompetition.model.game.Competition;
import edu.kit.kastel.monstercompetition.model.monster.Monster;
import edu.kit.kastel.monstercompetition.util.RoundHandler;
import edu.kit.kastel.monstercompetition.util.paser.ConfigParser;
import edu.kit.kastel.monstercompetition.util.RandomNumberGenerator;
import edu.kit.kastel.monstercompetition.command.CommandHandler;
import edu.kit.kastel.monstercompetition.util.InvalidArgumentException;

import java.io.IOException;
import java.util.Scanner;

/**
 * Main entry point for the Monster Competition game.
 *
 * @author uozqc
 */
public final class Main {
    private static final String ERROR_USAGE = "Error, usage: java Main <path> [<seed>|debug]";
    private static final String ERROR_TOO_MANY_ARGS = "Error, too many arguments. Usage: java Main <path> [<seed>|debug]";
    private static final String ERROR_INVALID_SEED = "Error, invalid seed: ";
    private static final String ERROR_READING_CONFIG = "Error reading config file: ";
    private static final String MSG_LOADED_DATA = "Loaded %d actions, %d monsters.";
    private static final String CMD_DEBUG = "debug";
    private static final String CMD_QUIT = "quit";
    private static final int MIN_ARGS = 1;
    private static final int MAX_ARGS = 2;
    private static final int CONFIG_PATH_INDEX = 0;
    private static final int SECOND_ARG_INDEX = 1;
    private static final int EMPTY_LINE_LENGTH = 0;

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private Main() {
    }
    /**
     * Main method that starts the application.
     * @param args Command line arguments: path to config file and optional seed or "debug"
     */
    public static void main(String[] args) {
        if (args.length < MIN_ARGS) {
            System.err.println(ERROR_USAGE);
            return;
        }
        
        if (args.length > MAX_ARGS) {
            System.err.println(ERROR_TOO_MANY_ARGS);
            return;
        }

        String configPath = args[CONFIG_PATH_INDEX];
        // Parse optional second arg => seed or debug
        Long seed = null;
        boolean debugMode = false;

        if (args.length == MAX_ARGS) {
            String secondArg = args[SECOND_ARG_INDEX];
            if (CMD_DEBUG.equalsIgnoreCase(secondArg)) {
                debugMode = true;
            } else {
                try {
                    seed = Long.valueOf(secondArg);
                } catch (NumberFormatException e) {
                    System.err.println(ERROR_INVALID_SEED + secondArg);
                    return;
                }
            }
        }

        // Initialize game components
        RandomNumberGenerator rng = new RandomNumberGenerator(seed, debugMode);
        ConfigParser parser = initializeParser(configPath);
        if (parser == null) {
            return;
        }

        printLoadedData(parser);

        // Set up game components
        Competition competition = new Competition();
        for (Monster monster : parser.getParsedMonsters()) {
            competition.addMonster(monster);
        }
        CommandHandler cmdHandler = new CommandHandler(parser, competition);
        RoundHandler roundHandler = new RoundHandler(rng, cmdHandler);
        cmdHandler.setRoundHandler(roundHandler);
        
        // Set the CommandHandler in the RandomNumberGenerator for handling special commands during debug mode
        if (debugMode) {
            RandomNumberGenerator.setCommandHandler(cmdHandler);
        }

        // Run game loop
        runGameLoop(cmdHandler, rng);
    }

    private static ConfigParser initializeParser(String configPath) {
        ConfigParser parser = new ConfigParser();
        try {
            parser.parseConfigFile(configPath);
            return parser;
        } catch (IOException | InvalidArgumentException e) {
            System.err.println(ERROR_READING_CONFIG + e.getMessage());
            return null;
        }
    }

    private static void printLoadedData(ConfigParser parser) {
        int numActions = parser.getParsedActions().size();
        int numMonsters = parser.getParsedMonsters().size();
        System.out.println();
        System.out.println(String.format(MSG_LOADED_DATA, numActions, numMonsters));
    }

    private static void runGameLoop(CommandHandler cmdHandler, RandomNumberGenerator rng) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            if (!sc.hasNextLine()) {
                break;
            }
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            
            // Only exact "quit" command should exit immediately
            if (line.equals(CMD_QUIT)) {
                break;
            }

            // For all other commands, including "quit xyz", process through the command handler
            boolean result = cmdHandler.handleCommand(line);
            if (!result) {
                // Nothing
            }
        }
        sc.close();
        rng.close();
    }
}