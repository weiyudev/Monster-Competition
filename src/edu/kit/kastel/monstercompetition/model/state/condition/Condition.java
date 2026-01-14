package edu.kit.kastel.monstercompetition.model.state.condition;

import edu.kit.kastel.monstercompetition.model.monster.Monster;

/**
 * The base interface for all status conditions (BURN, WET, QUICKSAND, SLEEP).
 * Provide hooks to modify effective stats or block actions,
 * and optionally do something at the end of an action (e.g. burn damage).
 *
 * @author uozqc
 */
public interface Condition {

    /**
     * Constant representing the Attack stat name.
     */
    String ATK_NAME = "ATK";

    /**
     * Constant representing the Defense stat name.
     */
    String DEF_NAME = "DEF";

    /**
     * Constant representing the Speed stat name.
     */
    String SPD_NAME = "SPD";

    /**
     * Get the name of this condition.
     * @return the condition name
     */
    String getName();

    /**
     * Apply the condition's effect to the monster's stats.
     * @param statName the stat name (ATK, DEF, SPD, etc.)
     * @param statValue the original stat value
     * @return the modified stat value
     */
    default double applyToStat(String statName, double statValue) {
        // Default implementation: no change
        return statValue;
    }

    /**
     * Modify the attack stat.
     * @param currentAtk the current ATK value
     * @return the modified ATK value
     */
    default double modifyAtk(double currentAtk) {
        return applyToStat(ATK_NAME, currentAtk);
    }

    /**
     * Modify the defense stat.
     * @param currentDef the current DEF value
     * @return the modified DEF value
     */
    default double modifyDef(double currentDef) {
        return applyToStat(DEF_NAME, currentDef);
    }

    /**
     * Modify the speed stat.
     * @param currentSpd the current SPD value
     * @return the modified SPD value
     */
    default double modifySpd(double currentSpd) {
        return applyToStat(SPD_NAME, currentSpd);
    }

    /**
     * Determine if the monster can act (e.g., not asleep).
     * @return true if the monster can act, false otherwise
     */
    default boolean canAct() {
        return true;
    }

    /**
     * Apply end-of-action effects for this condition (e.g., burn damage).
     * @param monster the monster with this condition
     */
    default void onActionEnd(Monster monster) {
        // Default implementation: no effect
    }

    /**
     * Get the message to display when a monster gets this condition.
     * @param monsterName the name of the monster
     * @return the start message
     */
    String getStartMessage(String monsterName);

    /**
     * Get the message to display when a monster already has this condition.
     * @param monsterName the name of the monster
     * @return the ongoing message
     */
    String getOngoingMessage(String monsterName);

    /**
     * Get the message to display when a monster's condition ends.
     * @param monsterName the name of the monster
     * @return the end message
     */
    String getEndMessage(String monsterName);
}