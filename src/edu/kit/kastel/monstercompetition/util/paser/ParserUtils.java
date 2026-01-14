package edu.kit.kastel.monstercompetition.util.paser;

import edu.kit.kastel.monstercompetition.model.effect.RepeatCount;
import edu.kit.kastel.monstercompetition.util.InvalidArgumentException;

/**
 * Utility class providing common parsing functionality for the config file parsing system.
 * Contains helpers for parsing integers, ranges, and other common structures.
 * @author uozqc
 */
public final class ParserUtils {

    // Strength type constants
    private static final String STRENGTH_TYPE_BASE = "base";
    private static final String STRENGTH_TYPE_REL = "rel";
    private static final String STRENGTH_TYPE_ABS = "abs";
    
    // Count type constants
    private static final String COUNT_TYPE_RANDOM = "random";
    
    // Error message formats
    private static final String ERROR_INVALID_INTEGER = "Error, invalid integer on line %d: %s";
    private static final String ERROR_VALUE_OUT_OF_RANGE = "Error, value out of range on line %d: %d (must be between %d and %d)";
    private static final String ERROR_RANDOM_REQUIRES_PARAMS =
            "Error, 'random' on line %d requires two more integers: 'random <min> <max>'";
    private static final String ERROR_RANDOM_MIN_GREATER = "Error, 'random' minimum %d is greater than max %d on line %d";
    private static final String ERROR_INVALID_STRENGTH_TYPE = "Error, invalid strength type on line %d: %s (must be 'base','rel','abs')";
    private static final String ERROR_INVALID_STRENGTH_VALUE = "Error, invalid strength value on line %d: %s";
    private static final int LINE_NUMBER_OFFSET = 1;
    private static final int RANDOM_REQUIRED_EXTRA_PARAMS = 2;
    private static final int MIN_VALUE_OFFSET = 1;
    private static final int MAX_VALUE_OFFSET = 2;
    private static final int RANDOM_COUNT_TOKENS_CONSUMED = 3;
    private static final int FIXED_COUNT_TOKENS_CONSUMED = 1;

    private ParserUtils() {

    }

    /**
     * Parse an integer within a specified range.
     * 
     * @param str The string to parse as an integer
     * @param min The minimum allowed value (inclusive)
     * @param max The maximum allowed value (inclusive)
     * @param lineNumber The line number in the config file for error reporting
     * @return The parsed integer value
     * @throws InvalidArgumentException if the value is not an integer or outside the specified range
     */
    public static int parseIntInRange(String str, int min, int max, int lineNumber) throws InvalidArgumentException {
        int val;
        try {
            val = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new InvalidArgumentException(String.format(ERROR_INVALID_INTEGER, lineNumber + LINE_NUMBER_OFFSET, str));
        }
        if (val < min || val > max) {
            throw new InvalidArgumentException(String.format(ERROR_VALUE_OUT_OF_RANGE, lineNumber + LINE_NUMBER_OFFSET, val, min, max));
        }
        return val;
    }

    /**
     * Parse a simple integer, with error reporting including line number.
     * 
     * @param line The string to parse as an integer
     * @param lineNumber The line number in the config file for error reporting
     * @return The parsed integer value
     * @throws InvalidArgumentException if the string is not a valid integer
     */
    public static int parseInt(String line, int lineNumber) throws InvalidArgumentException {
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            throw new InvalidArgumentException(String.format(ERROR_INVALID_INTEGER, lineNumber + LINE_NUMBER_OFFSET, line));
        }
    }

    /**
     * Parse a count specification which can be a fixed number or a random range.
     * 
     * @param tokens The array of tokens from the line
     * @param startIndex The index in tokens where the count specification starts
     * @param lineNumber The line number in the config file for error reporting
     * @return A RepeatCount object containing the parsed count information
     * @throws InvalidArgumentException if the count specification is invalid
     */
    public static RepeatCount parseCount(String[] tokens, int startIndex, int lineNumber) throws InvalidArgumentException {
        String first = tokens[startIndex];
        if (first.equals(COUNT_TYPE_RANDOM)) {
            if (startIndex + RANDOM_REQUIRED_EXTRA_PARAMS >= tokens.length) {
                throw new InvalidArgumentException(String.format(ERROR_RANDOM_REQUIRES_PARAMS, lineNumber + LINE_NUMBER_OFFSET));
            }
            int minVal = parseInt(tokens[startIndex + MIN_VALUE_OFFSET], lineNumber);
            int maxVal = parseInt(tokens[startIndex + MAX_VALUE_OFFSET], lineNumber);
            if (minVal > maxVal) {
                throw new InvalidArgumentException(String.format(ERROR_RANDOM_MIN_GREATER,
                        minVal, maxVal, lineNumber + LINE_NUMBER_OFFSET));
            }
            return new RepeatCount(true, minVal, maxVal, RANDOM_COUNT_TOKENS_CONSUMED);
            // "3" tokens consumed: ["random","<min>","<max>"]
        } else {
            // It's presumably a single integer
            int val = parseInt(first, lineNumber);
            return new RepeatCount(false, val, val, FIXED_COUNT_TOKENS_CONSUMED);
        }
    }

    /**
     * Helper structure to store the strength type (base, rel, abs) and numeric value.
     */
    public static class StrengthInfo {
        private final String type; // "base", "rel", or "abs"
        private final int value;

        /**
         * Creates a new StrengthInfo with the specified type and value.
         * 
         * @param type The strength type, one of "base", "rel", or "abs"
         * @param value The numeric value for the strength
         */
        public StrengthInfo(String type, int value) {
            this.type = type;
            this.value = value;
        }

        /**
         * Gets the strength type.
         * 
         * @return The strength type ("base", "rel", or "abs")
         */
        public String getType() {
            return type; }

        /**
         * Gets the strength value.
         * 
         * @return The numeric value for the strength
         */
        public int getValue() {
            return value; }
    }

    /**
     * Parse a strength specification in the format of "type value".
     * 
     * @param typeStr The strength type ("base", "rel", or "abs")
     * @param valStr The strength value as a string
     * @param lineNumber The line number in the config file for error reporting
     * @return A StrengthInfo object containing the parsed strength information
     * @throws InvalidArgumentException if the strength type is invalid or the value is not a number
     */
    public static StrengthInfo parseStrength(String typeStr, String valStr, int lineNumber) throws InvalidArgumentException {
        if (!typeStr.equals(STRENGTH_TYPE_BASE) && !typeStr.equals(STRENGTH_TYPE_REL) && !typeStr.equals(STRENGTH_TYPE_ABS)) {
            throw new InvalidArgumentException(String.format(ERROR_INVALID_STRENGTH_TYPE, lineNumber + LINE_NUMBER_OFFSET, typeStr));
        }
        int val;
        try {
            val = Integer.parseInt(valStr);
        } catch (NumberFormatException e) {
            throw new InvalidArgumentException(String.format(ERROR_INVALID_STRENGTH_VALUE, lineNumber + LINE_NUMBER_OFFSET, valStr));
        }
        return new StrengthInfo(typeStr, val);
    }
} 