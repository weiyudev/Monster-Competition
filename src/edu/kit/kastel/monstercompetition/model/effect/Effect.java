package edu.kit.kastel.monstercompetition.model.effect;

import edu.kit.kastel.monstercompetition.model.element.Element;
import edu.kit.kastel.monstercompetition.model.monster.Monster;
import edu.kit.kastel.monstercompetition.util.RoundHandler;

/**
 * Represents a generic effect in the monster battle system.
 * @author uozqc
 */
public interface Effect {

    /**
     * Applies this effect, modifying user or target if needed.
     *
     * @param user    The monster performing the action
     * @param target  The monster receiving the effect
     * @param handler The handler that handles it
     * @return true if this effect succeeded (and allows subsequent effects),
     *         false if it failed (the action stops).
     */
    boolean apply(Monster user, Monster target, RoundHandler handler);

    /**
     * Set the effect's element the same with the action.
     *
     * @param element element
     */
    void setActionElement(Element element);
}
