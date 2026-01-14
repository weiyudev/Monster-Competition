package edu.kit.kastel.monstercompetition.util.paser;

import edu.kit.kastel.monstercompetition.model.monster.Monster;
import edu.kit.kastel.monstercompetition.model.effect.Action;
import edu.kit.kastel.monstercompetition.util.InvalidArgumentException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Main parser for monster configuration files.
 * This class coordinates the parsing process and stores the parsed results.
 * @author uozqc
 */
public class ConfigParser {
    
    // Error message constants
    private static final String ERROR_DUPLICATE_ACTION = "Error, exact duplicate action found: %s";
    private static final String ERROR_ACTIONS_BEFORE_MONSTERS = "Error on line %d: actions should be declared before monsters!";
    private static final String ERROR_UNRECOGNIZED_LINE = "Error on line %d: Unrecognized line: %s";

    // Array index constants
    private static final int INDEX_IN_ACTION_BLOCK = 0;
    private static final int INDEX_MOVED_TO_MONSTERS = 1;
    
    // Line number adjustment constant
    private static final int LINE_NUMBER_OFFSET = 1;
    private static final int LOOP_BEGAIN = 0;

    // Parsed results
    private List<String> lines;
    private final List<Action> parsedActions = new ArrayList<>();
    private final List<Monster> monsters = new ArrayList<>();

    // Helper parsers
    private final EffectParser effectParser;
    private final ActionParser actionParser;
    private final MonsterParser monsterParser;
    
    /**
     * Creates a new ConfigParser with initialized sub-parsers.
     */
    public ConfigParser() {
        this.effectParser = new EffectParser();
        this.actionParser = new ActionParser(effectParser);
        this.monsterParser = new MonsterParser();
    }
    
    /**
     * Parsing phases for tracking the current section of the config file.
     */
    private enum ParsingPhase {
        ACTIONS,
        MONSTERS,
        FINISHED
    }

    /**
     * Parses a configuration file from the specified path.
     * Always prints the entire file content first, then reports any errors.
     * 
     * @param filePath The path to the configuration file
     * @throws InvalidArgumentException if the configuration contains errors
     * @throws IOException if the file cannot be read
     */
    public void parseConfigFile(String filePath) throws InvalidArgumentException, IOException {
        // Read all lines from the file
        lines = Files.readAllLines(Path.of(filePath));
        
        // First, print the entire file content
        for (String line : lines) {
            System.out.println(line);
        }
        
        // Then perform the actual parsing
        parseLines();
    }
    
    /**
     * Parses the loaded lines from the configuration file.
     * 
     * @throws InvalidArgumentException if the configuration contains errors
     */
    private void parseLines() throws InvalidArgumentException {
        ParsingPhase phase = ParsingPhase.ACTIONS;
        boolean inActionBlock = false;

        for (int i = LOOP_BEGAIN; i < lines.size(); i++) {
            if (effectParser.queryProcessedLines(i)) {
                continue;
            }

            String originalLine = lines.get(i);
            // No need to print here as we already printed all lines

            // Trim and skip blank lines
            String line = originalLine.strip();
            if (line.isEmpty()) {
                continue;
            }

            // Handle action blocks or top-level lines
            if (inActionBlock) {
                inActionBlock = handleActionBlock(line, i, inActionBlock);
            } else {
                // Handle top-level lines
                boolean[] result = handleTopLevelLine(line, i, phase);
                inActionBlock = result[INDEX_IN_ACTION_BLOCK];
                if (result[INDEX_MOVED_TO_MONSTERS]) {
                    phase = ParsingPhase.MONSTERS;
                }
            }
        }
    }

    /**
     * Handles parsing of content inside an action block.
     * 
     * @param line The current line
     * @param lineNumber The line number
     * @param inActionBlock Whether we're in an action block
     * @return Whether we're still in an action block after processing
     * @throws InvalidArgumentException if parsing errors occur
     */
    private boolean handleActionBlock(String line, int lineNumber, boolean inActionBlock) 
            throws InvalidArgumentException {
        if (actionParser.isEndActionLine(line)) {
            Action action = actionParser.finalizeCurrentAction();
            if (isDuplicateAction(action)) {
                throw new InvalidArgumentException(ERROR_DUPLICATE_ACTION.formatted(action.getName()));
            }
            parsedActions.add(action);
            return false; // No longer in action block
        }
        
        // Parse effect inside action block
        actionParser.parseEffectLine(line, lineNumber, lines);
        return true; // Still in action block
    }

    /**
     * Checks if an action is a duplicate (same name, element, and effects).
     * 
     * @param action The action to check
     * @return Whether this action is an exact duplicate
     */
    private boolean isDuplicateAction(Action action) {
        for (Action existingAction : parsedActions) {
            if (existingAction.getName().equals(action.getName())
                    && existingAction.getElement().equals(action.getElement())
                    && existingAction.getEffects().equals(action.getEffects())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles parsing of top-level lines (action or monster definitions).
     * 
     * @param line The current line
     * @param lineNumber The line number
     * @param phase The current parsing phase
     * @return Array where [0] = inActionBlock, [1] = moved to monsters phase
     * @throws InvalidArgumentException if parsing errors occur
     */
    private boolean[] handleTopLevelLine(String line, int lineNumber, ParsingPhase phase) 
            throws InvalidArgumentException {
        boolean inActionBlock = false;
        boolean movedToMonstersPhase = false;
        
        if (actionParser.isActionLine(line)) {
            // Check phase transition
            if (phase == ParsingPhase.MONSTERS) {
                throw new InvalidArgumentException(
                    ERROR_ACTIONS_BEFORE_MONSTERS.formatted(lineNumber + LINE_NUMBER_OFFSET));
            }
            // Start new action block
            actionParser.startNewAction(line, lineNumber);
            inActionBlock = true;
        } else if (monsterParser.isMonsterLine(line)) {
            // Move to MONSTERS phase
            movedToMonstersPhase = true;
            
            // Parse the monster
            Monster monster = monsterParser.parseMonsterLine(line, lineNumber, parsedActions);
            monsters.add(monster);
        } else {
            // Unrecognized line
            throw new InvalidArgumentException(
                ERROR_UNRECOGNIZED_LINE.formatted(lineNumber + LINE_NUMBER_OFFSET, line));
        }
        
        return new boolean[] {inActionBlock, movedToMonstersPhase};
    }

    /**
     * Gets the list of parsed actions.
     * 
     * @return The list of parsed Action objects
     */
    public List<Action> getParsedActions() {
        return parsedActions;
    }

    /**
     * Gets the list of parsed monsters.
     * 
     * @return The list of parsed Monster objects
     */
    public List<Monster> getParsedMonsters() {
        return monsters;
    }

    /**
     * Replaces the current parsed configuration with the contents of another parser.
     * Used when reloading a configuration file.
     * 
     * @param newParser The new ConfigParser whose data will replace the current parser's data
     */
    public void replaceWith(ConfigParser newParser) {
        // Replace the configuration lines
        this.lines = newParser.lines;

        // Clear and add all actions from the new parser
        this.parsedActions.clear();
        this.parsedActions.addAll(newParser.getParsedActions());

        // Clear and add all monsters from the new parser
        this.monsters.clear();
        this.monsters.addAll(newParser.getParsedMonsters());
    }
}


