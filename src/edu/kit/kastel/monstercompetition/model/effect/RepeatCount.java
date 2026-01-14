package edu.kit.kastel.monstercompetition.model.effect;

import edu.kit.kastel.monstercompetition.model.element.Element;

/**
 * Represents the count configuration for repeating effects in the monster battle system.
 * Can represent either a fixed count or a random count within a specified range.
 * This class is used primarily by RepeatEffect to determine how many times to repeat its sub-effects.
 *
 * @author uozqc
 */
public class RepeatCount {
    // Error message constants
    private static final String ERROR_FIXED_COUNT_RANDOM = "Cannot get fixed count for a random RepeatCount.";

    private final boolean isRandom;

    private final int minVal;

    private final int maxVal;

    /** The number of tokens consumed during parsing in the configuration file. */
    private final int tokensConsumed;

    private Element actionElement;

    /**
     * Creates a new RepeatCount with the specified parameters.
     *
     * @param isRandom Whether this count is random (true) or fixed (false)
     * @param minVal The minimum value or fixed count value
     * @param maxVal The maximum value (only relevant if isRandom is true)
     * @param tokensConsumed The number of tokens consumed in the configuration parsing
     */
    public RepeatCount(boolean isRandom, int minVal, int maxVal, int tokensConsumed) {
        this.isRandom = isRandom;
        this.minVal = minVal;
        this.maxVal = maxVal;
        this.tokensConsumed = tokensConsumed;
    }

    /**
     * Gets the number of tokens that were consumed when parsing this count from the configuration.
     * This is used by the parser to determine where to continue parsing.
     *
     * @return The number of tokens consumed
     */
    public int getTokensConsumed() {
        return tokensConsumed;
    }

    /**
     * Checks if this count is random or fixed.
     *
     * @return true if this count is random, false if it's fixed
     */
    public boolean isRandom() {
        return isRandom;
    }

    /**
     * Gets the minimum count value.
     * If this is a fixed count, minVal and maxVal are the same.
     *
     * @return The minimum count value
     */
    public int getMinCount() {
        return minVal;
    }

    /**
     * Gets the maximum count value.
     * If this is a fixed count, minVal and maxVal are the same.
     *
     * @return The maximum count value
     */
    public int getMaxCount() {
        return maxVal;
    }

    /**
     * When RepeatCount is in fixed mode, returns the fixed count value (minVal is equal to maxVal).
     * If the current mode is random, an exception is thrown.
     *
     * @return Fixed count value
     * @throws IllegalStateException If the current mode is random
     */
    public int getFixedCount() {
        if (!isRandom) {
            return minVal;
        }
        throw new IllegalStateException(ERROR_FIXED_COUNT_RANDOM);
    }

    /**
     * Sets the element of the action this effect belongs to.
     *
     * @param element The action's element
     */
    public void setActionElement(Element element) {
        this.actionElement = element;
    }
}