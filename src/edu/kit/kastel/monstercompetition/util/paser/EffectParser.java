package edu.kit.kastel.monstercompetition.util.paser;

import edu.kit.kastel.monstercompetition.model.effect.Effect;
import edu.kit.kastel.monstercompetition.model.effect.ContinueEffect;
import edu.kit.kastel.monstercompetition.model.effect.DamageEffect;
import edu.kit.kastel.monstercompetition.model.effect.HealEffect;
import edu.kit.kastel.monstercompetition.model.effect.InflictStatChangeEffect;
import edu.kit.kastel.monstercompetition.model.effect.InflictStatusConditionEffect;
import edu.kit.kastel.monstercompetition.model.effect.RepeatEffect;
import edu.kit.kastel.monstercompetition.model.effect.RepeatCount;
import edu.kit.kastel.monstercompetition.model.effect.ProtectEffect;

import edu.kit.kastel.monstercompetition.model.state.condition.StatusCondition;
import edu.kit.kastel.monstercompetition.util.InvalidArgumentException;
import edu.kit.kastel.monstercompetition.util.paser.ParserUtils.StrengthInfo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parser for different effect types in monster configuration files.
 * This class handles parsing individual effect lines into Effect objects.
 * @author uozqc
 */
public class EffectParser {

    // Command name constants
    private static final String CONTINUE_COMMAND_NAME = "continue";
    private static final String DAMAGE_COMMAND_NAME = "damage";
    private static final String INFLICT_STATUS_CONDITION_COMMAND_NAME = "inflictStatusCondition";
    private static final String INFLICT_STAT_CHANGE_COMMAND_NAME = "inflictStatChange";
    private static final String PROTECT_STAT_COMMAND_NAME = "protectStat";
    private static final String HEAL_COMMAND_NAME = "heal";
    private static final String REPEAT_COMMAND_NAME = "repeat";
    private static final String END_REPEAT_COMMAND_NAME = "end repeat";
    
    // Target type constants
    private static final String TARGET_USER = "user";
    private static final String TARGET_TARGET = "target";
    
    // Protection target constants
    private static final String PROTECT_HEALTH = "health";
    private static final String PROTECT_STATS = "stats";
    
    // Stat name constants
    private static final String STAT_ATK = "ATK";
    private static final String STAT_DEF = "DEF";
    private static final String STAT_SPD = "SPD";
    private static final String STAT_PRC = "PRC";
    private static final String STAT_AGL = "AGL";
    
    // Token splitting constant
    private static final String TOKEN_DELIMITER = "\\s+";
    
    // Index constants
    private static final int INDEX_COMMAND = 0;
    private static final int INDEX_TARGET = 1;
    private static final int INDEX_STRENGTH_TYPE = 2;
    private static final int INDEX_STRENGTH_VALUE = 3;
    private static final int INDEX_HIT_RATE = 4;
    private static final int INDEX_STAT = 2;
    private static final int INDEX_STAT_CHANGE = 3;
    private static final int INDEX_STATUS_CONDITION = 2;
    private static final int INDEX_STATUS_HIT_RATE = 3;
    private static final int INDEX_PROTECT_TARGET = 1;
    private static final int INDEX_PROTECT_COUNT_START = 2;
    
    // Token count constants
    private static final int CONTINUE_TOKEN_COUNT = 2;
    private static final int DAMAGE_TOKEN_COUNT = 5;
    private static final int INFLICT_STATUS_TOKEN_COUNT = 4;
    private static final int INFLICT_STAT_CHANGE_TOKEN_COUNT = 5;
    private static final int PROTECT_STAT_MIN_TOKEN_COUNT = 4;
    private static final int HEAL_TOKEN_COUNT = 5;
    private static final int REPEAT_MIN_TOKEN_COUNT = 2;
    
    // Hit rate range constants
    private static final int MIN_HIT_RATE = 0;
    private static final int MAX_HIT_RATE = 100;
    
    // Line number adjustment constant
    private static final int LINE_NUMBER_OFFSET = 1;
    
    // Error message constants
    private static final String ERROR_DAMAGE_PARAMS =
            "Error, 'damage' on line %d requires 4 parameters: damage <target> <strength_type> <strength_val> <hitRate>";
    private static final String ERROR_INFLICT_STATUS_PARAMS =
            "Error, 'inflictStatusCondition' on line %d requires 3 parameters: inflictStatusCondition <target> <STATUS> <hitRate>";
    private static final String ERROR_INFLICT_STAT_CHANGE =
            "Error, 'inflictStatChange' on line %d requires 4 parameters: inflictStatChange <target> <STAT> <change> <hitRate>";
    private static final String ERROR_INVALID_STATUS =
            "Error, invalid status condition on line %d: %s";
    private static final String ERROR_PROTECT_STAT_PARAMS =
            "Error, 'protectStat' on line %d requires at least 3 parameters: protectStat <target> <count> <hitRate>";
    private static final String ERROR_PROTECT_STAT_HITRATE =
            "Error, not enough tokens after parsing 'protectStat' count on line %d. Need a hitRate.";
    private static final String ERROR_HEAL_PARAMS =
            "Error, 'heal' on line %d requires 4 parameters: heal <target> <strength_type> <strength_val> <hitRate>";
    private static final String ERROR_REPEAT_PARAMS =
            "Error, 'repeat' on line %d requires at least one parameter: repeat <count>";
    private static final String ERROR_REPEAT_NO_END =
            "Error, 'repeat' block at line %d has no 'end repeat'";
    private static final String ERROR_NESTED_REPEATS =
            "Error, nested repeats are not allowed on line %d";
    private static final String ERROR_INVALID_STAT_TYPE =
            "Error, invalid stat type on line %d: %s (must be 'ATK', 'DEF', 'SPD', 'PRC', or 'AGL')";
    private static final String ERROR_INVALID_STAT_CHANGE =
            "Error, invalid stat change on line %d: %s";
    private static final String ERROR_CONTINUE_PARAMS =
            "Error, 'continue' on line %d requires exactly 1 parameter: continue <hitRate>";
    private static final String ERROR_UNKNOWN_EFFECT =
            "Error, unknown effect type on line %d: %s";

    private final Set<Integer> processedLines = new HashSet<>();

    /**
     * It will be used to check whether the line of text has been parsed.
     * @param num the line number
     * @return true, if the line of text has been parsed
     */
    public boolean queryProcessedLines(int num) {
        return processedLines.contains(num);
    }

    /**
     * Parses an effect from a line in the configuration file.
     *
     * @param line The line to parse
     * @param lineNumber The line number for error reporting
     * @param lines The complete list of lines from the config file (for repeat blocks)
     * @return The parsed Effect object
     * @throws InvalidArgumentException if the effect specification is invalid
     */
    public Effect parseEffect(String line, int lineNumber, List<String> lines) throws InvalidArgumentException {
        // Check if it's a repeat block
        if (line.startsWith(REPEAT_COMMAND_NAME)) {
            return parseRepeatBlock(line, lineNumber, lines);
        } else if (line.startsWith(CONTINUE_COMMAND_NAME)) {
            return parseContinueEffect(line, lineNumber);
        }

        // Otherwise, parse individual effect types
        String[] tokens = line.split(TOKEN_DELIMITER);
        String actionCommand = tokens[INDEX_COMMAND];

        return switch (actionCommand) {
            case DAMAGE_COMMAND_NAME -> parseDamageEffect(tokens, lineNumber);
            case INFLICT_STATUS_CONDITION_COMMAND_NAME -> parseInflictStatusConditionEffect(tokens, lineNumber);
            case INFLICT_STAT_CHANGE_COMMAND_NAME -> parseInflictStatChangeEffect(tokens, lineNumber);
            case PROTECT_STAT_COMMAND_NAME -> parseProtectStatEffect(tokens, lineNumber);
            case HEAL_COMMAND_NAME -> parseHealEffect(tokens, lineNumber);
            default -> throw new InvalidArgumentException(
                String.format(ERROR_UNKNOWN_EFFECT, lineNumber + LINE_NUMBER_OFFSET, actionCommand));
        };
    }

    /**
     * Parse "continue" effect with hit rate.
     *
     * @param line The config line
     * @param lineNumber The line number for error reporting
     * @return The parsed ContinueEffect
     * @throws InvalidArgumentException if the syntax is invalid
     */
    private Effect parseContinueEffect(String line, int lineNumber) throws InvalidArgumentException {
        String[] tokens = line.split(TOKEN_DELIMITER);

        if (tokens.length != CONTINUE_TOKEN_COUNT) {
            throw new InvalidArgumentException(
                String.format(ERROR_CONTINUE_PARAMS, lineNumber + LINE_NUMBER_OFFSET));
        }

        int hitRate = ParserUtils.parseIntInRange(tokens[INDEX_HIT_RATE - INDEX_STRENGTH_VALUE], 
                                                MIN_HIT_RATE, MAX_HIT_RATE, lineNumber);
        return new ContinueEffect(hitRate);
    }

    /**
     * Parse a "damage" effect.
     *
     * @param tokens The tokens from the config line
     * @param lineNumber The line number for error reporting
     * @return The parsed DamageEffect
     * @throws InvalidArgumentException if the syntax is invalid
     */
    private Effect parseDamageEffect(String[] tokens, int lineNumber) throws InvalidArgumentException {
        if (tokens.length != DAMAGE_TOKEN_COUNT) {
            throw new InvalidArgumentException(String.format(ERROR_DAMAGE_PARAMS, lineNumber + LINE_NUMBER_OFFSET));
        }

        String targetMonster = tokens[INDEX_TARGET];
        StrengthInfo strength = ParserUtils.parseStrength(tokens[INDEX_STRENGTH_TYPE], 
                                                        tokens[INDEX_STRENGTH_VALUE], lineNumber);
        int hitRate = ParserUtils.parseIntInRange(tokens[INDEX_HIT_RATE], MIN_HIT_RATE, MAX_HIT_RATE, lineNumber);

        return new DamageEffect(targetMonster, strength.getType(),
                strength.getValue(), hitRate);
    }

    /**
     * Parse "inflictStatusCondition" effect.
     *
     * @param tokens The tokens from the config line
     * @param lineNumber The line number for error reporting
     * @return The parsed InflictStatusConditionEffect
     * @throws InvalidArgumentException if the syntax or status condition is invalid
     */
    private Effect parseInflictStatusConditionEffect(String[] tokens, int lineNumber) throws InvalidArgumentException {
        if (tokens.length != INFLICT_STATUS_TOKEN_COUNT) {
            throw new InvalidArgumentException(String.format(ERROR_INFLICT_STATUS_PARAMS, lineNumber + LINE_NUMBER_OFFSET));
        }

        String targetMonster = tokens[INDEX_TARGET];
        String statusName = tokens[INDEX_STATUS_CONDITION];
        if (!StatusCondition.isValidStatusCondition(statusName)) {
            throw new InvalidArgumentException(String.format(ERROR_INVALID_STATUS, 
                                                          lineNumber + LINE_NUMBER_OFFSET, statusName));
        }
        StatusCondition condition = StatusCondition.valueOf(statusName);
        int hitRate = ParserUtils.parseIntInRange(tokens[INDEX_STATUS_HIT_RATE], MIN_HIT_RATE, MAX_HIT_RATE, lineNumber);

        return new InflictStatusConditionEffect(targetMonster, condition, hitRate);
    }

    /**
     * Parse "inflictStatChange" effect.
     *
     * @param tokens The tokens from the config line
     * @param lineNumber The line number for error reporting
     * @return The parsed InflictStatChangeEffect
     * @throws InvalidArgumentException if the syntax or stat is invalid
     */
    private Effect parseInflictStatChangeEffect(String[] tokens, int lineNumber) throws InvalidArgumentException {
        if (tokens.length != INFLICT_STAT_CHANGE_TOKEN_COUNT) {
            throw new InvalidArgumentException(String.format(ERROR_INFLICT_STAT_CHANGE, lineNumber));
        }

        String targetMonster = tokens[INDEX_TARGET];
        String stat = tokens[INDEX_STAT];
        
        // Validate stat name
        if (!isValidStat(stat)) {
            throw new InvalidArgumentException(String.format(ERROR_INVALID_STAT_TYPE, 
                                                          lineNumber + LINE_NUMBER_OFFSET, stat));
        }
        
        int change;
        try {
            change = Integer.parseInt(tokens[INDEX_STAT_CHANGE]);
        } catch (NumberFormatException e) {
            throw new InvalidArgumentException(String.format(ERROR_INVALID_STAT_CHANGE, 
                                                          lineNumber + LINE_NUMBER_OFFSET, tokens[INDEX_STAT_CHANGE]));
        }
        int hitRate = ParserUtils.parseIntInRange(tokens[INDEX_HIT_RATE], MIN_HIT_RATE, MAX_HIT_RATE, lineNumber);

        return new InflictStatChangeEffect(targetMonster, stat, change, hitRate);
    }
    
    /**
     * Checks if a stat name is valid.
     * 
     * @param stat The stat name to check
     * @return true if the stat name is valid
     */
    private boolean isValidStat(String stat) {
        String upperStat = stat.toUpperCase();
        return upperStat.equals(STAT_ATK) || upperStat.equals(STAT_DEF) || upperStat.equals(STAT_SPD) 
               || upperStat.equals(STAT_PRC) || upperStat.equals(STAT_AGL);
    }

    /**
     * Parse "protectStat" effect.
     *
     * @param tokens The tokens from the config line
     * @param lineNumber The line number for error reporting
     * @return The parsed ProtectEffect
     * @throws InvalidArgumentException if the syntax or protection target is invalid
     */
    private Effect parseProtectStatEffect(String[] tokens, int lineNumber) throws InvalidArgumentException {
        if (tokens.length < PROTECT_STAT_MIN_TOKEN_COUNT) {
            throw new InvalidArgumentException(String.format(ERROR_PROTECT_STAT_PARAMS, lineNumber + LINE_NUMBER_OFFSET));
        }

        String protectTarget = tokens[INDEX_PROTECT_TARGET]; // "health" or "stats"

        // Parse the COUNT parameter
        RepeatCount countObj = ParserUtils.parseCount(tokens, INDEX_PROTECT_COUNT_START, lineNumber);

        // Determine the index for hitRate based on tokens consumed by parseCount
        int tokensConsumed = countObj.getTokensConsumed();
        int indexForHitRate = INDEX_PROTECT_COUNT_START + tokensConsumed;
        if (indexForHitRate >= tokens.length) {
            throw new InvalidArgumentException(String.format(ERROR_PROTECT_STAT_HITRATE, lineNumber + LINE_NUMBER_OFFSET));
        }
        int hitRate = ParserUtils.parseIntInRange(tokens[indexForHitRate], MIN_HIT_RATE, MAX_HIT_RATE, lineNumber);

        // Create the appropriate protectEffect based on count type
        if (countObj.isRandom()) {
            return new ProtectEffect(protectTarget, countObj.getMinCount(), countObj.getMaxCount(), hitRate);
        } else {
            return new ProtectEffect(protectTarget, countObj.getFixedCount(), hitRate);
        }
    }

    /**
     * Parse "heal" effect.
     *
     * @param tokens The tokens from the config line
     * @param lineNumber The line number for error reporting
     * @return The parsed HealEffect
     * @throws InvalidArgumentException if the syntax or heal parameters are invalid
     */
    private Effect parseHealEffect(String[] tokens, int lineNumber) throws InvalidArgumentException {
        if (tokens.length != HEAL_TOKEN_COUNT) {
            throw new InvalidArgumentException(String.format(ERROR_HEAL_PARAMS, lineNumber + LINE_NUMBER_OFFSET));
        }

        String targetMonster = tokens[INDEX_TARGET];
        StrengthInfo strength = ParserUtils.parseStrength(tokens[INDEX_STRENGTH_TYPE], 
                                                        tokens[INDEX_STRENGTH_VALUE], lineNumber);
        int hitRate = ParserUtils.parseIntInRange(tokens[INDEX_HIT_RATE], MIN_HIT_RATE, MAX_HIT_RATE, lineNumber);

        return new HealEffect(targetMonster, strength.getType(), strength.getValue(), hitRate);
    }

    /**
     * Parse a "repeat" block of effects.
     *
     * @param firstLineOfRepeat The first line of the repeat block
     * @param lineNumber The line number for error reporting
     * @param lines The complete list of lines from the config file
     * @return The parsed RepeatEffect
     * @throws InvalidArgumentException if the syntax or repeat parameters are invalid
     */
    private Effect parseRepeatBlock(String firstLineOfRepeat, int lineNumber, List<String> lines)
            throws InvalidArgumentException {
        String[] tokens = firstLineOfRepeat.split(TOKEN_DELIMITER);
        if (tokens.length < REPEAT_MIN_TOKEN_COUNT) {
            throw new InvalidArgumentException(String.format(ERROR_REPEAT_PARAMS, lineNumber + LINE_NUMBER_OFFSET));
        }

        RepeatCount repeatCount = ParserUtils.parseCount(tokens, INDEX_TARGET, lineNumber);
        List<Effect> subEffects = new ArrayList<>();

        int currentLine = lineNumber;
        while (true) {
            currentLine++;
            processedLines.add(currentLine);
            if (currentLine >= lines.size()) {
                throw new InvalidArgumentException(String.format(ERROR_REPEAT_NO_END, lineNumber + LINE_NUMBER_OFFSET));
            }

            String originalLine = lines.get(currentLine);
            String line = originalLine.strip();

            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith(END_REPEAT_COMMAND_NAME)) {
                break;
            }

            if (line.startsWith(REPEAT_COMMAND_NAME)) {
                throw new InvalidArgumentException(String.format(ERROR_NESTED_REPEATS, currentLine + LINE_NUMBER_OFFSET));
            }

            // Parse each effect inside the repeat block
            Effect subEffect = parseEffect(line, currentLine, lines);
            subEffects.add(subEffect);
        }
        return new RepeatEffect(repeatCount, subEffects);
    }
}