package edu.kit.kastel.monstercompetition.util.paser;

import edu.kit.kastel.monstercompetition.model.monster.Monster;
import edu.kit.kastel.monstercompetition.model.effect.Action;
import edu.kit.kastel.monstercompetition.model.element.Element;
import edu.kit.kastel.monstercompetition.util.InvalidArgumentException;

import java.util.List;

/**
 * Parser for monster definitions in configuration files.
 * This class handles parsing monster lines into Monster objects.
 * @author uozqc
 */
public class MonsterParser {

    private static final String MONSTER_COMMAND_NAME = "monster";
    private static final String ERROR_INVALID_LINE =
            "Error on line %d: Monster line must have at least %d tokens (including 1..4 action names).";
    private static final String ERROR_INVALID_ELEMENT = "Error on line %d: Invalid element type for monster: %s";
    private static final String ERROR_TOO_MANY_ACTIONS = "Error on line %d: Monster must list 1..4 action names, found: %d";
    private static final String ERROR_UNKNOWN_ACTION = "Error, monster references unknown action: %s";
    private static final String TOKEN_DELIMITER = "\\s+";
    private static final int MIN_TOKENS = 8;
    private static final int MAX_ACTIONS = 4;
    private static final int NAME_INDEX = 1;
    private static final int ELEMENT_INDEX = 2;
    private static final int HP_INDEX = 3;
    private static final int ATK_INDEX = 4;
    private static final int DEF_INDEX = 5;
    private static final int SPD_INDEX = 6;
    private static final int FIRST_ACTION_INDEX = 7;
    private static final int LINE_NUMBER_OFFSET = 1;
    private static final int LOOP_BEGIN = 0;

    /**
     * Checks if a line is a monster definition.
     * 
     * @param line The line to check
     * @return true if this line defines a monster
     */
    public boolean isMonsterLine(String line) {
        return line.startsWith(MONSTER_COMMAND_NAME);
    }
    
    /**
     * Parses a monster definition line.
     * 
     * @param line The monster line to parse
     * @param lineNumber The line number for error reporting
     * @param availableActions The list of previously parsed actions
     * @return The parsed Monster object
     * @throws InvalidArgumentException if the monster definition is invalid
     */
    public Monster parseMonsterLine(String line, int lineNumber, List<Action> availableActions) 
            throws InvalidArgumentException {
        String[] tokens = line.split(TOKEN_DELIMITER);
        if (tokens.length < MIN_TOKENS) {
            throw new InvalidArgumentException(
                String.format(ERROR_INVALID_LINE, lineNumber + LINE_NUMBER_OFFSET, MIN_TOKENS));
        }
        
        // Parse basic monster properties
        String monsterName = tokens[NAME_INDEX];
        String elementStr = tokens[ELEMENT_INDEX];

        int maxHp = ParserUtils.parseInt(tokens[HP_INDEX], lineNumber);
        int baseAtk = ParserUtils.parseInt(tokens[ATK_INDEX], lineNumber);
        int baseDef = ParserUtils.parseInt(tokens[DEF_INDEX], lineNumber);
        int baseSpd = ParserUtils.parseInt(tokens[SPD_INDEX], lineNumber);

        Element element;
        if (Element.isValidElement(elementStr)) {
            element = Element.valueOf(elementStr);
        } else {
            throw new InvalidArgumentException(
                String.format(ERROR_INVALID_ELEMENT, lineNumber + LINE_NUMBER_OFFSET, elementStr));
        }

        // Parse action names
        int numActions = tokens.length - FIRST_ACTION_INDEX;
        if (numActions > MAX_ACTIONS) {
            throw new InvalidArgumentException(
                String.format(ERROR_TOO_MANY_ACTIONS, lineNumber + LINE_NUMBER_OFFSET, numActions));
        }
        
        // Construct the monster
        Monster monster = new Monster(monsterName, element, maxHp, baseAtk, baseDef, baseSpd);

        // Add actions to the monster
        for (int j = LOOP_BEGIN; j < numActions; j++) {
            String actionName = tokens[FIRST_ACTION_INDEX + j];
            try {
                Action found = ActionParser.findActionByName(actionName, availableActions);
                if (found == null) {
                    throw new InvalidArgumentException(String.format(ERROR_UNKNOWN_ACTION, actionName));
                }
                monster.addAction(found);
            } catch (InvalidArgumentException e) {
                // Rethrow the exception for duplicate action names or unknown actions
                throw new InvalidArgumentException(e.getMessage());
            }
        }
        
        return monster;
    }
} 