package edu.kit.kastel.monstercompetition.model.effect;

import edu.kit.kastel.monstercompetition.model.monster.Monster;
import edu.kit.kastel.monstercompetition.model.element.Element;
import edu.kit.kastel.monstercompetition.util.RoundHandler;
import edu.kit.kastel.monstercompetition.util.BattleCalculations;
/**
 * Implements a damage-dealing effect that can be applied to monsters during battle.
 * Damage effects reduce the target monster's health points based on various factors
 * including elemental effectiveness, critical hits, and stat relationships.
 *
 * @author uozqc
 */
public class DamageEffect implements Effect {
    private static final String USER_NAME = "user";
    private static final String ATTACK_HIT_NAME = "attack hit";
    private static final String BLANKSPACE_FAINTS_NAME = " faints!";
    private static final String MSG_PROTECTED = "%s is protected and takes no damage!";
    private static final String MSG_TAKES_DAMAGE = "%s takes %d damage!";
    private static final String TARGET_TYPE_TARGET = "target";
    private static final int MAX_HITRATE = 100;
    private static final int MIN_HITRATE = 0;
    private static final int MIN_AGL_VALUE = 0;

    private final String targetType;
    private final String strengthType;
    private final int strengthValue;
    private final int hitRate;
    private Element actionElement;

    /**
     * Creates a new DamageEffect.
     *
     * @param targetType The target type ("user" or "target")
     * @param strengthType The strength type ("base", "rel", or "abs")
     * @param strengthValue The strength value
     * @param hitRate The hit rate
     */
    public DamageEffect(String targetType, String strengthType,
                        int strengthValue, int hitRate) {
        this.targetType = targetType;
        this.strengthType = strengthType;
        this.strengthValue = strengthValue;
        this.hitRate = hitRate;
    }

    /**
     * Sets the element of the action this effect belongs to.
     *
     * @param element The action's element
     */
    public void setActionElement(Element element) {
        this.actionElement = element;
    }

    @Override
    public boolean apply(Monster user, Monster target, RoundHandler handler) {

        // 1. If the user or target already dead
        if (user.isFainted() || target.isFainted()) {
            return false;
        }

        // 2. Hit determination using the proper formula from A.2.9
        double finalHitRate = calculateFinalHitRate(user, target);
        boolean hits = handler.getRandomGenerator().checkProbability(ATTACK_HIT_NAME, (int) finalHitRate);
        if (!hits) {
            return false;
        }

        // 3. Determine who will bear the damage ("user" or "target")
        Monster realTarget = targetType.equals(USER_NAME) ? user : target;

        // 4. If the target is protected, damage is prevented
        if (realTarget.isProtectedAgainstDamage()) {
            System.out.println(String.format(MSG_PROTECTED, realTarget.getName()));
            return true;
        }

        // 5. Calculate damage
        int damage = BattleCalculations.computeValue(user, target, actionElement, strengthValue, strengthType, handler);

        // 6. Apply the damage
        System.out.println(String.format(MSG_TAKES_DAMAGE, realTarget.getName(), damage));
        boolean fainted = realTarget.takeDamage(damage);

        // If monster fainted, print the message
        if (fainted) {
            System.out.println(realTarget.getName() + BLANKSPACE_FAINTS_NAME);
        }

        return true;
    }

    /**
     * Calculate final hit rate.
     *
     * @param user The monster using the action
     * @param target The target monster
     * @return The final hit rate
     */
    private double calculateFinalHitRate(Monster user, Monster target) {
        // Base rate is the hitRate from the effect declaration
        double baseRate = Math.min(MAX_HITRATE, Math.max(MIN_HITRATE, hitRate));

        // For effects targeting enemies, use PRC/AGL ratio
        if (TARGET_TYPE_TARGET.equals(targetType)) {
            double prcUser = user.getEffectivePrc();
            double aglTarget = target.getEffectiveAgl();
            double ratio = (aglTarget <= MIN_AGL_VALUE) ? prcUser : (prcUser / aglTarget);
            double finalRate = baseRate * ratio;

            // Clamp to [0..100]
            return Math.min(MAX_HITRATE, Math.max(MIN_HITRATE, finalRate));
        } else {
            // For self-targeting effects, use just PRC
            double prcUser = user.getEffectivePrc();
            double finalRate = baseRate * prcUser;

            // Clamp to [0..100]
            return Math.min(MAX_HITRATE, Math.max(MIN_HITRATE, finalRate));
        }
    }

    /**
     * Get the target type of this damage effect.
     *
     * @return The target type ("user" or "target")
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * Get the strength type of this damage effect.
     *
     * @return The strength type
     */
    public String getStrengthType() {
        return strengthType;
    }

    /**
     * Get the strength value of this damage effect.
     *
     * @return The strength value
     */
    public int getStrengthValue() {
        return strengthValue;
    }

    /**
     * Get the hit rate of this damage effect.
     *
     * @return The hit rate
     */
    public int getHitRate() {
        return hitRate;
    }
}