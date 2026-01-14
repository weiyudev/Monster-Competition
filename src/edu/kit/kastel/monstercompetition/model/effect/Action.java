package edu.kit.kastel.monstercompetition.model.effect;

import edu.kit.kastel.monstercompetition.model.element.Element;
import java.util.List;

/**
 * Represents an action that a monster can perform during battle.
 * An action has a name, an elemental type, and a list of effects that are applied when the action is used.
 * 
 * @author uozqc
 */
public class Action {
    // Constants for string formatting in toString method
    private static final String TO_STRING_FORMAT = "Action{name='%s', element=%s, effects=%s}";
    
    private final String name;
    private final Element element;
    private final List<Effect> effects;

    /**
     * Creates a new action with the specified name, element, and effects.
     * 
     * @param name The name of the action
     * @param element The elemental type of the action
     * @param effects The list of effects that this action applies when used
     */
    public Action(String name, Element element, List<Effect> effects) {
        this.name = name;
        this.element = element;
        this.effects = effects;
    }

    /**
     * Gets the name of this action.
     * 
     * @return The action's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the elemental type of this action.
     * The element affects damage calculations and type effectiveness.
     * 
     * @return The action's elemental type
     */
    public Element getElement() {
        return element;
    }

    /**
     * Gets the list of effects that this action applies when used.
     * Effects are applied in sequence, with the first effect being critical
     * for determining if the action succeeds.
     * 
     * @return The list of effects for this action
     */
    public List<Effect> getEffects() {
        return effects;
    }

    /**
     * Returns a string representation of this action.
     * Includes the action's name, element, and effects.
     * 
     * @return A string representation of the action
     */
    @Override
    public String toString() {
        return String.format(TO_STRING_FORMAT, name, element, effects);
    }
}
