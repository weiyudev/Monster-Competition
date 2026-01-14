package edu.kit.kastel.monstercompetition.command;

import edu.kit.kastel.monstercompetition.model.monster.Monster;
import edu.kit.kastel.monstercompetition.model.game.Competition;
import edu.kit.kastel.monstercompetition.util.RoundHandler;
import edu.kit.kastel.monstercompetition.util.paser.ConfigParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dispatches user commands to specific command classes.
 * Acts as the single source of truth for which monster is currently selecting an action.
 *
 * @author uozqc
 */
public class CommandHandler {

    // Command names as constants
    private static final String LOAD_COMMAND = "load";
    private static final String COMPETITION_COMMAND = "competition";
    private static final String SHOW_COMMAND = "show";
    private static final String ACTION_COMMAND = "action";
    private static final String PASS_COMMAND = "pass";
    private static final String QUIT_COMMAND = "quit";
    
    // Prompts
    private static final String WHAT_SHOULD_DO = "What should %s do?";
    private static final String NEW_LINE = "";
    /**
     * Represents a space to separate the input
     */
    private static final String BLANK_SPACE = "\\s+";
    private static final int COMMAND_WORD_INDEX = 0;
    private static final int COMMAND_NAME_COUNT = 1;
    private static final int COMMAND_ARGS_START_INDEX = 1;
    private static final int ARGS_ARRAY_START_INDEX = 0;
    private static final int INITIAL_MONSTER_INDEX = 0;
    private static final int MIN_MONSTERS_FOR_COMPETITION = 2;

    private final ConfigParser parser;
    private final Competition competition;
    private RoundHandler roundHandler; // Not final to allow setting it later
    private final Map<String, Command> commands;

    // Monster management fields
    private Monster currentActionMonster;
    private int currentMonsterIndex = INITIAL_MONSTER_INDEX;

    /**
     * Creates a new CommandHandler.
     *
     * @param parser The configuration parser
     * @param competition The current competition
     */
    public CommandHandler(ConfigParser parser, Competition competition) {
        this.parser = parser;
        this.competition = competition;
        this.roundHandler = null; // Will be set later in Main
        this.commands = new HashMap<>();
        this.currentActionMonster = null;

        registerCommands();
    }

    /**
     * Sets the RoundHandler for this CommandHandler.
     *
     * @param roundHandler The round handler to set
     */
    public void setRoundHandler(RoundHandler roundHandler) {
        this.roundHandler = roundHandler;
    }

    /**
     * Registers all available commands.
     */
    private void registerCommands() {
        commands.put(LOAD_COMMAND, new LoadCommand(parser, competition, this));
        commands.put(COMPETITION_COMMAND, new CompetitionCommand(parser, competition, this));
        commands.put(SHOW_COMMAND, new ShowCommand(competition, this));
        commands.put(ACTION_COMMAND, new ActionCommand(competition, this));
        commands.put(PASS_COMMAND, new PassCommand(this));
        commands.put(QUIT_COMMAND, new QuitCommand(this));
    }

    /**
     * Handles user input commands.
     *
     * @param line The command line input
     * @return Whether the command execution was successful
     */
    public boolean handleCommand(String line) {
        String[] tokens = line.split(BLANK_SPACE);
        String cmdWord = tokens[COMMAND_WORD_INDEX].toLowerCase();
        String[] cmdArgs = new String[tokens.length - COMMAND_NAME_COUNT];
        System.arraycopy(tokens, COMMAND_ARGS_START_INDEX, cmdArgs, ARGS_ARRAY_START_INDEX, cmdArgs.length);

        Command cmd = commands.get(cmdWord);
        if (cmd == null) {
            // System.out.println(UNKNOWN_COMMAND_ERROR.formatted(cmdWord));
            return false;
        }

        return cmd.execute(cmdArgs);
    }

    /**
     * Resets the current monster index for a new round.
     */
    public void resetMonsterIndex() {
        currentMonsterIndex = INITIAL_MONSTER_INDEX;
        currentActionMonster = null;
    }

    /**
     * Sets the monster that is currently selecting an action.
     *
     * @param monster The current monster
     */
    public void setCurrentActionMonster(Monster monster) {
        this.currentActionMonster = monster;
    }

    /**
     * Gets the current monster that is selecting an action.
     *
     * @return The current action monster, or null if none
     */
    public Monster getCurrentActionMonster() {
        return currentActionMonster;
    }

    /**
     * Gets the round handler.
     *
     * @return The round handler
     */
    public RoundHandler getRoundHandler() {
        return roundHandler;
    }

    /**
     * Gets the configuration parser.
     *
     * @return The configuration parser
     */
    public ConfigParser getParser() {
        return parser;
    }

    /**
     * Gets the current competition.
     *
     * @return The competition
     */
    public Competition getCompetition() {
        return competition;
    }

    /**
     * Moves to the next monster that needs to select an action.
     * Skips fainted monsters.
     *
     * @return true if there is a next monster, false if all monsters have chosen
     */
    public boolean moveToNextMonster() {
        List<Monster> monsters = competition.getAllMonsters();
        if (monsters.isEmpty()) {
            currentActionMonster = null;
            return false;
        }

        // Increment the current monster index and find the next non-fainted monster
        currentMonsterIndex++;
        
        while (currentMonsterIndex < monsters.size()) {
            Monster nextMonster = monsters.get(currentMonsterIndex);
            if (!nextMonster.isFainted()) {
                // Found a non-fainted monster
                currentActionMonster = nextMonster;
                return true;
            }
            // This monster is fainted, try the next one
            currentMonsterIndex++;
        }
        
        // Reached the end of the list or all remaining monsters are fainted
        currentMonsterIndex = INITIAL_MONSTER_INDEX;
        currentActionMonster = null;
        return false;
    }

    /**
     * Starts a new round of action selection after Phase II.
     * This ensures we only prompt for monster actions in one place.
     * Skips fainted monsters.
     */
    public void startNewActionSelection() {
        // First check if the competition has already ended
        if (competition.isEnded()) {
            return;
        }

        resetMonsterIndex();
        List<Monster> monsters = competition.getAllMonsters();
        if (monsters.size() < MIN_MONSTERS_FOR_COMPETITION) {
            // Competition should be ended, not starting new selection
            return;
        }

        // Find the first non-fainted monster
        Monster firstNonFaintedMonster = null;
        for (Monster monster : monsters) {
            if (!monster.isFainted()) {
                firstNonFaintedMonster = monster;
                currentMonsterIndex = monsters.indexOf(monster);
                break;
            }
        }
        
        // If all monsters are fainted (shouldn't happen as competition should have ended)
        // or we found a non-fainted monster
        if (firstNonFaintedMonster != null) {
            setCurrentActionMonster(firstNonFaintedMonster);
            // This is the only place where we should prompt for the first monster action
            System.out.println();
            System.out.println(WHAT_SHOULD_DO.formatted(firstNonFaintedMonster.getName()));
        }
    }
}