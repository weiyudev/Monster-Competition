package edu.kit.kastel.monstercompetition.util.paser;

import edu.kit.kastel.monstercompetition.model.effect.Action;
import edu.kit.kastel.monstercompetition.model.effect.Effect;
import edu.kit.kastel.monstercompetition.model.element.Element;
import edu.kit.kastel.monstercompetition.util.InvalidArgumentException;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for actions in monster configuration files.
 * This class handles parsing actions blocks and their effects.
 * @author uozqc
 */
public class ActionParser {
    
    private static final String ACTION_COMMAND_NAME = "action";
    private static final String END_ACTION_COMMAND_NAME = "end action";

    private static final String ERROR_INVALID_ELEMENT_PARAMETER_FORMAT = 
            "'%s' in line '%s' is not a valid format for a element.";
    private static final String ERROR_ARGUMENT_PARAMETER_FORMAT = 
            "'%s' in line '%s' is not a valid format for a argument.";
    private static final String ERROR_EMPTY_ACTION =
            "action '%s' has no effects";
    private static final String ERROR_NO_ACTION_BLOCK =
            "finalizeCurrentAction() called but no valid action block was in progress.";
    private static final String ERROR_DUPLICATE_ACTION = "Error, duplicate action name: %s";
    
    // Token constants
    private static final String TOKEN_DELIMITER = "\\s+";
    private static final int EXPECTED_TOKEN_COUNT = 3;
    private static final int ACTION_NAME_INDEX = 1;
    private static final int ELEMENT_INDEX = 2;
    
    private final EffectParser effectParser;
    
    // Current action being parsed
    private String currentActionName;
    private Element currentActionElement;
    private List<Effect> currentActionEffects;
    
    /**
     * Creates a new ActionParser with the specified effect parser.
     * 
     * @param effectParser The effect parser to use for parsing effects within actions
     */
    public ActionParser(EffectParser effectParser) {
        this.effectParser = effectParser;
        this.currentActionEffects = new ArrayList<>();
    }
    
    /**
     * Starts a new action definition.
     * 
     * @param line The action definition line
     * @param lineNumber The line number for error reporting
     * @throws InvalidArgumentException if the action specification is invalid
     */
    public void startNewAction(String line, int lineNumber) throws InvalidArgumentException {
        String[] tokens = line.split(TOKEN_DELIMITER);
        if (tokens.length != EXPECTED_TOKEN_COUNT) {
            throw new InvalidArgumentException(
                    ERROR_ARGUMENT_PARAMETER_FORMAT.formatted(line, lineNumber));
        }

        currentActionName = tokens[ACTION_NAME_INDEX];
        String elementStr = tokens[ELEMENT_INDEX];

        if (Element.isValidElement(elementStr)) {
            currentActionElement = Element.valueOf(elementStr);
        } else {
            throw new InvalidArgumentException(
                    ERROR_INVALID_ELEMENT_PARAMETER_FORMAT.formatted(elementStr, lineNumber));
        }
        currentActionEffects = new ArrayList<>();
    }

    /**
     * Parses an effect line within an action block.
     * 
     * @param line The effect line to parse
     * @param lineNumber The line number for error reporting
     * @param lines The complete list of lines from the config file
     * @throws InvalidArgumentException if the effect specification is invalid
     */
    public void parseEffectLine(String line, int lineNumber, List<String> lines)
            throws InvalidArgumentException {
        Effect effect = effectParser.parseEffect(line, lineNumber, lines);
        addEffect(effect);
    }
    
    /**
     * Adds an effect to the current action being parsed.
     * 
     * @param effect The Effect object to add
     */
    private void addEffect(Effect effect) {
        currentActionEffects.add(effect);
    }
    
    /**
     * Finalizes the current action and returns the complete Action object.
     * 
     * @return The completed Action object
     * @throws InvalidArgumentException if the action has no effects
     * @throws RuntimeException if called when no valid action block is in progress
     */
    public Action finalizeCurrentAction() throws InvalidArgumentException {
        if (currentActionName == null || currentActionElement == null || currentActionEffects == null) {
            throw new InvalidArgumentException(ERROR_NO_ACTION_BLOCK);
        }
        
        // Check if the action has at least one effect
        if (currentActionEffects.isEmpty()) {
            throw new InvalidArgumentException(String.format(ERROR_EMPTY_ACTION, currentActionName));
        }
        
        Action action = new Action(currentActionName, currentActionElement, currentActionEffects);
        
        // Reset for next action
        // String name = currentActionName;
        currentActionName = null;
        currentActionElement = null;
        currentActionEffects = new ArrayList<>();
        
        return action;
    }
    
    /**
     * Checks if a line indicates the end of an action block.
     * 
     * @param line The line to check
     * @return true if this line marks the end of an action block
     */
    public boolean isEndActionLine(String line) {
        return line.startsWith(END_ACTION_COMMAND_NAME);
    }
    
    /**
     * Checks if a line indicates the start of an action block.
     * 
     * @param line The line to check
     * @return true if this line marks the start of an action block
     */
    public boolean isActionLine(String line) {
        return line.startsWith(ACTION_COMMAND_NAME);
    }


    /**
     * Finds an action by name in a list of actions.
     * 
     * @param name The name to search for
     * @param actions The list of actions to search
     * @return The matching Action or throws an exception if multiple actions found with same name
     * @throws InvalidArgumentException if multiple actions with the same name exist
     */
    public static Action findActionByName(String name, List<Action> actions) throws InvalidArgumentException {
        Action foundAction = null;
        
        for (Action action : actions) {
            if (action.getName().equals(name)) {
                if (foundAction != null) {
                    // Found a second action with the same name
                    throw new InvalidArgumentException(String.format(ERROR_DUPLICATE_ACTION, name));
                }
                foundAction = action;
            }
        }
        
        return foundAction; // Will be null if no matching action found
    }
}

