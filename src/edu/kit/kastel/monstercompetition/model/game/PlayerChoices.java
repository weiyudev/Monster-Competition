package edu.kit.kastel.monstercompetition.model.game;

import edu.kit.kastel.monstercompetition.model.monster.Monster;
import edu.kit.kastel.monstercompetition.model.effect.Action;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores the actions chosen by each monster in Phase I.
 * For each monster, we store the chosen action and the "target" if needed.
 *
 * @author uozqc
 */
public class PlayerChoices {

    // For demonstration, store each monster's chosen effect
    // plus a reference to the "target monster name" if that effect needs a target.
    private final Map<Monster, ChosenAction> choices;

    /**
     * Creates a new PlayerChoices with an empty choices map.
     */
    public PlayerChoices() {
        this.choices = new HashMap<>();
    }

    /**
     * A small class to hold the action plus a reference to the chosen target monster.
     */
    public static class ChosenAction {
        /** The action chosen by the monster. */
        public Action action;

        /** The target monster of the action. */
        public Monster chosenTarget;

        /**
         * Creates a new ChosenAction with specified action and target.
         *
         * @param action The action chosen
         * @param chosenTarget The target of the action
         */
        public ChosenAction(Action action, Monster chosenTarget) {
            this.action = action;
            this.chosenTarget = chosenTarget;
        }
    }

    /**
     * Sets the choice for a given monster.
     *
     * @param monster The monster making the choice
     * @param action The action chosen
     * @param target The target monster for the action
     */
    public void setChoice(Monster monster, Action action, Monster target) {
        choices.put(monster, new ChosenAction(action, target));
    }

    /**
     * Gets the chosen action and target for a monster.
     *
     * @param monster The monster whose choice to retrieve
     * @return The monster's chosen action and target, or null if none exists
     */
    public ChosenAction getChoice(Monster monster) {
        return choices.get(monster);
    }

    /**
     * Clears all choices.
     */
    public void clear() {
        choices.clear();
    }

    /**
     * Removes the choice for a monster (e.g., when passing).
     *
     * @param monster The monster whose choice to remove
     */
    public void removeChoice(Monster monster) {
        choices.remove(monster);
    }
}