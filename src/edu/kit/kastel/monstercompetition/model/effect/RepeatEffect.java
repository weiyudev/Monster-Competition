package edu.kit.kastel.monstercompetition.model.effect;

// Implements an action that repeats certain effects.

import edu.kit.kastel.monstercompetition.model.element.Element;
import edu.kit.kastel.monstercompetition.model.monster.Monster;
import edu.kit.kastel.monstercompetition.util.RoundHandler;
import edu.kit.kastel.monstercompetition.util.RandomNumberGenerator;

import java.util.List;

/**
 * An effect that repeats a sequence of sub-effects multiple times.
 * The number of repetitions can be either fixed or randomly determined within a range.
 * This effect is used to create actions that apply multiple instances of the same effect,
 * such as multi-hit attacks.
 *
 * @author uozqc
 */
public class RepeatEffect implements Effect {
    // Constants for contexts
    private static final String REPEAT_COUNT_CONTEXT = "repeat count";
    
    // Constants for predetermined repeat count
    private static final int PREDETERMINED_COUNT_UNSET = -1;
    private static final int DEFAUT_COUNT = 0;
    private static final int LOOP_BEGINN = 0;
    private static final int FIRST_ITERATION_INDE = 0;

    /**
     * Flag indicating whether the number of repetitions is random or fixed.
     */
    private final boolean isRandom;
    
    /**
     * The fixed number of repetitions when isRandom is false.
     */
    private final int fixedCount;  // used if !isRandom
    
    /**
     * The minimum number of repetitions when isRandom is true.
     */
    private final int minCount;    // used if isRandom
    
    /**
     * The maximum number of repetitions when isRandom is true.
     */
    private final int maxCount;    // used if isRandom

    /**
     * The list of effects to be repeated.
     */
    private final List<Effect> subEffects; // the effects inside the repeat block

    private Element actionElement;
    
    /**
     * The pre-determined repeat count (used when predetermining repeat counts).
     * A value of -1 indicates that the count has not been predetermined.
     */
    private int predeterminedRepeatCount = PREDETERMINED_COUNT_UNSET;

    /**
     * Creates a new RepeatEffect using the specified repeat count configuration.
     *
     * @param repeatCount Object containing repeat count information (fixed or random)
     * @param subEffects List of effects to be repeated
     */
    public RepeatEffect(RepeatCount repeatCount, List<Effect> subEffects) {
        this.isRandom = repeatCount.isRandom();
        if (this.isRandom) {
            this.minCount = repeatCount.getMinCount();
            this.maxCount = repeatCount.getMaxCount();
            this.fixedCount = DEFAUT_COUNT;
        } else {
            this.fixedCount = repeatCount.getFixedCount();
            this.minCount = DEFAUT_COUNT;
            this.maxCount = DEFAUT_COUNT;
        }
        this.subEffects = subEffects;
    }
    
    /**
     * Predetermines the repeat count that will be used when this effect is applied.
     * This method should be called before applying any effects in an action to ensure
     * consistent random behavior regardless of execution order.
     *
     * @param handler The round handler managing the battle
     */
    public void predetermineRepeatCount(RoundHandler handler) {
        if (predeterminedRepeatCount != PREDETERMINED_COUNT_UNSET) {
            // Already predetermined
            return;
        }
        
        // Get the random number generator from the handler
        RandomNumberGenerator rng = handler.getRandomGenerator();
        
        // Determine how many times to repeat and store it
        if (isRandom) {
            predeterminedRepeatCount = rng.getIntInRange(REPEAT_COUNT_CONTEXT, minCount, maxCount);
        } else {
            predeterminedRepeatCount = fixedCount;
        }
    }
    
    /**
     * Resets the predetermined repeat count.
     * This should be called after an action is completed to prepare for the next time.
     */
    public void resetPredeterminedRepeatCount() {
        predeterminedRepeatCount = PREDETERMINED_COUNT_UNSET;
    }

    /**
     * Applies this repeat effect by executing the sub-effects multiple times.
     * The number of repetitions is determined based on the configuration (fixed or random).
     * If any sub-effect fails during application, the entire repeat effect is considered to have failed.
     *
     * @param user The monster using the effect
     * @param target The target monster of the effect
     * @param handler The round handler managing the battle
     * @return true if all repetitions of all sub-effects were successfully applied, false otherwise
     */
    @Override
    public boolean apply(Monster user, Monster target, RoundHandler handler) {
        if (user.isFainted() || target.isFainted()) {
            return false;
        }
        // Use the predetermined repeat count if available, otherwise determine it now
        int repeatCount;
        if (predeterminedRepeatCount != PREDETERMINED_COUNT_UNSET) {
            repeatCount = predeterminedRepeatCount;
        } else {
            // Fallback if predetermineRepeatCount wasn't called
            RandomNumberGenerator rng = handler.getRandomGenerator();
            if (isRandom) {
                repeatCount = rng.getIntInRange(REPEAT_COUNT_CONTEXT, minCount, maxCount);
            } else {
                repeatCount = fixedCount;
            }
        }
        // Track if at least one iteration was successful overall
        boolean anyIterationSuccessful = false;
        
        // Apply the sub-effects the determined number of times
        for (int i = LOOP_BEGINN; i < repeatCount; i++) {
            // Check if either monster has fainted before each iteration
            if (user.isFainted() || target.isFainted()) {
                return anyIterationSuccessful; // Stop repeating, success depends on previous iterations
            }
            // Track if at least one sub-effect was successful in this iteration
            boolean anySuccess = false;
            boolean anyAttempted = false;
            
            for (Effect effect : subEffects) {
                anyAttempted = true;
                boolean success = effect.apply(user, target, handler);
                if (success) {
                    anySuccess = true;
                }
                // Even if this effect failed (e.g., a damage effect missed), 
                // we continue with other effects in this iteration
                
                // Check if either monster has fainted after each sub-effect
                if (user.isFainted() || target.isFainted()) {
                    return anyIterationSuccessful || anySuccess; // Success if this or previous iterations succeeded
                }
            }
            
            // If this iteration had effects attempted and at least one succeeded
            if (anyAttempted && anySuccess) {
                anyIterationSuccessful = true;
            }
            
            // If all effects in this iteration failed, and it's the first iteration,
            // then the entire repeat effect should fail
            if (anyAttempted && !anySuccess && i == FIRST_ITERATION_INDE) {
                return false; // First iteration failed completely, so the repeat effect fails
            }
        }
        
        // Success only if at least one iteration was successful
        return anyIterationSuccessful;
    }

    @Override
    public void setActionElement(Element element) {
        this.actionElement = element;
    }
    
    /**
     * Gets the list of sub-effects that are repeated.
     *
     * @return The list of sub-effects
     */
    public List<Effect> getSubEffects() {
        return subEffects;
    }
}

