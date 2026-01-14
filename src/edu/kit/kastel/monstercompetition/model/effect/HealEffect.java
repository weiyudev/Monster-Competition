package edu.kit.kastel.monstercompetition.model.effect;

import edu.kit.kastel.monstercompetition.model.element.Element;
import edu.kit.kastel.monstercompetition.model.monster.Monster;
import edu.kit.kastel.monstercompetition.util.BattleCalculations;
import edu.kit.kastel.monstercompetition.util.RoundHandler;

/**
 * Implements a healing effect that can restore health to a monster.
 * Healing can be applied to either the user or the target monster.
 *
 * @author uozqc
 */
public class HealEffect implements Effect {
    private static final String HEAL_EFFECT_NAME = "heal effect";
    private static final String USER_NAME = "user";
    private static final String STRENGTHTTPE_BASE_NAME = "base";
    private static final String STRENGTHTTPE_REL_NAME = "rel";
    private static final String STRENGTHTTPE_ABS_NAME = "abs";
    private static final double PERCENT_TO_DECIMAL_DIVISOR = 100.0;

    private final String targetType;    // "user" or "target"
    private final String strengthType;  // "base", "rel", or "abs"
    private final int strengthValue;    // Healing amount or base value
    private final int hitRate;          // Probability of effect hitting (0-100)
    private Element actionElement;

    /**
     * Creates a new healing effect with the specified parameters.
     *
     * @param targetType The target of the healing ("user" or "target")
     * @param strengthType The type of healing strength ("base", "rel", or "abs")
     * @param strengthValue The value for the healing strength
     * @param hitRate The hit rate percentage (0-100)
     */
    public HealEffect(String targetType, String strengthType, int strengthValue, int hitRate) {
        this.targetType = targetType;
        this.strengthType = strengthType;
        this.strengthValue = strengthValue;
        this.hitRate = hitRate;
    }

    @Override
    public void setActionElement(Element element) {
        this.actionElement = element;
    }

    @Override
    public boolean apply(Monster user, Monster target, RoundHandler handler) {
        // 1. If user or target is fainted, the effect fails
        if (user.isFainted() || target.isFainted()) {
            return false;
        }

        // 2. Perform hit rate check
        boolean success = handler.getRandomGenerator().checkProbability(HEAL_EFFECT_NAME, hitRate);
        if (!success) {
            // Effect misses - no output needed per requirements
            return false;
        }

        // 3. Determine the actual target for healing
        Monster realTarget = targetType.equals(USER_NAME) ? user : target;

        // 4. Calculate healing amount based on strength type
        int healAmount;
        if (STRENGTHTTPE_BASE_NAME.equals(strengthType)) {
            // For base healing, we would need a complex formula similar to damage calculation
            // This is a simplified implementation
            healAmount = BattleCalculations.computeValue(user, target, actionElement, strengthValue, strengthType, handler);
        } else if (STRENGTHTTPE_REL_NAME.equals(strengthType)) {
            // Relative healing: percentage of max HP
            healAmount = (int) Math.ceil(realTarget.getBaseHp() * (strengthValue / PERCENT_TO_DECIMAL_DIVISOR));
        } else { // "abs"
            // Absolute healing: fixed value
            healAmount = strengthValue;
        }

        // 5. Apply healing - the Monster.heal() method will output the message
        realTarget.heal(healAmount);

        return true;
    }

    /**
     * Get the hit rate of this effect.
     *
     * @return The hit rate
     */
    public int getHitRate() {
        return hitRate;
    }
}