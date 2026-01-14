package edu.kit.kastel.monstercompetition.model.element;

/**
 * This enum represents the elements of the monsters in the competition.
 *
 * @author uozqc
 *
 */

public enum Element {
    /**
     * The element of Water.
     */
    WATER,
    /**
     * The element of Fire.
     */
    FIRE,
    /**
     * The element of Earth.
     */
    EARTH,
    /**
     * The element of Normal.
     */
    NORMAL;

    /**
     * Check if it is a valid element.
     * @param name input
     * @return true, if it is a valid element
     */
    public static boolean isValidElement(String name) {
        for (Element element : Element.values()) {
            if (element.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
}

