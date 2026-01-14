package edu.kit.kastel.monstercompetition.util;

import edu.kit.kastel.monstercompetition.model.element.Element;
import edu.kit.kastel.monstercompetition.model.monster.Monster;

/**
 * Utility class for battle-related calculations in the monster competition.
 * Contains methods for calculating damage and healing amounts.
 *
 * @author uozqc
 */
public final class BattleCalculations {

    // Strength type constants
    private static final String STRENGTHTYPE_REL_NAME = "rel";
    private static final String STRENGTHTYPE_ABS_NAME = "abs";
    
    // Element factor constants
    private static final double ELEMENT_ADVANTAGE_FACTOR = 2.0;
    private static final double ELEMENT_DISADVANTAGE_FACTOR = 0.5;
    private static final double ELEMENT_NEUTRAL_FACTOR = 1.0;
    
    // Message constants
    private static final String MSG_VERY_EFFECTIVE = "It is very effective!";
    private static final String MSG_NOT_VERY_EFFECTIVE = "It is not very effective...";
    private static final String MSG_CRITICAL_HIT = "Critical hit!";
    private static final String MSG_CRITICAL_HIT_KLEIN = "critical hit";
    
    // Critical hit constants
    private static final double CRITICAL_HIT_BASE = 100.0;
    private static final double CRITICAL_HIT_EXPONENT_BASE = 10.0;
    private static final double CRITICAL_HIT_FACTOR = 2.0;
    private static final double NORMAL_HIT_FACTOR = 1.0;
    
    // Same element bonus constant
    private static final double SAME_ELEMENT_BONUS = 1.5;
    
    // Random factor constants
    private static final String RANDOM_FACTOR_NAME = "damage random";
    private static final double RANDOM_FACTOR_MIN = 0.85;
    private static final double RANDOM_FACTOR_MAX = 1.0;
    private static final double RANDOM_FACTOR_DEFAULT = 0.925;
    
    // Normalization factor constant
    private static final double NORMALIZATION_FACTOR = 1.0 / 3.0;
    
    // Percentage calculation constant
    private static final double PERCENT_DIVISOR = 100.0;

    /**
     * Private constructor to prevent instantiation.
     */
    private BattleCalculations() {
        // Private constructor to prevent instantiation
    }

    /**
     * Compute damage.
     *
     * @param user The monster using the action
     * @param target The target monster
     * @param actionElement The element of the action
     * @param handler The round handler
     * @param strengthType int
     * @param strengthValue String
     * @return The calculated damage
     */
    public static int computeValue(Monster user, Monster target, Element actionElement,
                            int strengthValue, String strengthType, RoundHandler handler) {
        // Handle different damage types according to A.2.4
        if (STRENGTHTYPE_REL_NAME.equals(strengthType)) {
            // Relative damage is directly a percentage of target's max HP
            return (int) Math.ceil(target.getBaseHp() * (strengthValue / PERCENT_DIVISOR));
        } else if (STRENGTHTYPE_ABS_NAME.equals(strengthType)) {
            // Absolute damage is exactly the specified value
            return strengthValue;
        }
        // Only "base" damage type uses the complex damage formula
        int baseDamage = strengthValue;
        // 2. Element factor
        double elementFactor = ELEMENT_NEUTRAL_FACTOR;
        Element targetElement = target.getElement();

        // Element relationships
        if ((actionElement == Element.WATER && targetElement == Element.FIRE)
                || (actionElement == Element.FIRE && targetElement == Element.EARTH)
                || (actionElement == Element.EARTH && targetElement == Element.WATER)) {
            elementFactor = ELEMENT_ADVANTAGE_FACTOR;
            System.out.println(MSG_VERY_EFFECTIVE);
        } else if ((actionElement == Element.FIRE && targetElement == Element.WATER)
                || (actionElement == Element.EARTH && targetElement == Element.FIRE)
                || (actionElement == Element.WATER && targetElement == Element.EARTH)) {
            elementFactor = ELEMENT_DISADVANTAGE_FACTOR;
            System.out.println(MSG_NOT_VERY_EFFECTIVE);
        }
        // 3. Status factor (ATK/DEF ratio)
        double statusFactor = user.getEffectiveAtk() / target.getEffectiveDef();
        // 4. Critical hit factor
        double criticalFactor = NORMAL_HIT_FACTOR;
        // Critical hit chance formula
        double criticalChance = CRITICAL_HIT_BASE * Math.pow(CRITICAL_HIT_EXPONENT_BASE,
                -target.getEffectiveSpd() / user.getEffectiveSpd());
        boolean isCritical = handler.getRandomGenerator().checkProbability(MSG_CRITICAL_HIT_KLEIN, (int) criticalChance);
        if (isCritical) {
            criticalFactor = CRITICAL_HIT_FACTOR;
            System.out.println(MSG_CRITICAL_HIT);
        }
        // 5. Same element bonus
        double sameElementFactor = (user.getElement() == actionElement) ? SAME_ELEMENT_BONUS : NORMAL_HIT_FACTOR;
        // 6. Random factor between 0.85 and 1.0
        double randomFactor;
        try {
            randomFactor = handler.getRandomGenerator().getDoubleInRange(RANDOM_FACTOR_NAME, RANDOM_FACTOR_MIN, RANDOM_FACTOR_MAX);
        } catch (InvalidArgumentException e) {
            randomFactor = RANDOM_FACTOR_DEFAULT; // Default mid-point if random fails
        }
        // 7. Normalization factor (1/3)
        double normalizationFactor = NORMALIZATION_FACTOR;
        // 8. Calculate total damage
        double totalDamage = baseDamage
                * elementFactor
                * statusFactor
                * criticalFactor
                * sameElementFactor
                * randomFactor
                * normalizationFactor;
        return (int) Math.ceil(totalDamage); // 9. Round up to the nearest integer
    }
}
