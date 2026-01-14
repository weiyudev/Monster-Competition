package edu.kit.kastel.monstercompetition.model.effect;

import edu.kit.kastel.monstercompetition.model.element.Element;
import edu.kit.kastel.monstercompetition.model.monster.Monster;
import edu.kit.kastel.monstercompetition.util.RoundHandler;
import edu.kit.kastel.monstercompetition.model.state.condition.StatusCondition;

/**
 * Implements an effect that applies a status condition to a monster.
 * Per the assignment requirements, this only applies if:
 * 1. The hit rate check succeeds
 * 2. The target does not already have a status condition
 *
 * @author uozqc
 */
public class InflictStatusConditionEffect implements Effect {
    // Constants for target types
    private static final String TARGET_TYPE_USER = "user";
    
    // Constants for status effect messages
    private static final String STATUS_CONTEXT = "inflict status";
    private static final String MSG_BURN = "%s caught on fire!";
    private static final String MSG_WET = "%s becomes soaking wet!";
    private static final String MSG_QUICKSAND = "%s gets caught by quicksand!";
    private static final String MSG_SLEEP = "%s falls asleep!";
    private static final String MSG_DEFAULT = "%s is now under %s condition!";

    private final String targetType;         // "user" or "target"
    private final StatusCondition condition; // WET, BURN, QUICKSAND, SLEEP
    private final int hitRate;               // 0-100 percentage

    private Element actionElement;

    /**
     * Creates a new status condition effect.
     *
     * @param targetType Which monster to affect ("user" or "target")
     * @param condition The status condition to apply
     * @param hitRate The hit rate percentage (0-100)
     */
    public InflictStatusConditionEffect(String targetType,
                                        StatusCondition condition,
                                        int hitRate) {
        this.targetType = targetType;
        this.condition = condition;
        this.hitRate = hitRate;
    }

    @Override
    public boolean apply(Monster user, Monster target, RoundHandler handler) {
        // 1) If user or target fainted => fail
        if (user.isFainted() || target.isFainted()) {
            return false;
        }

        // 2) Probability check
        boolean success = handler.getRandomGenerator()
                .checkProbability(STATUS_CONTEXT, hitRate);
        if (!success) {
            // Effect missed, return false (no message needed here)
            return false;
        }

        // 3) Determine real target
        Monster realTarget = targetType.equals(TARGET_TYPE_USER) ? user : target;

        // 4) If realTarget already has a condition, skip applying a new one
        if (realTarget.getStatusCondition() != null) {
            // Target already has a condition - effect succeeds but doesn't change anything
            return true;
        }

        // 5) Apply the new condition
        realTarget.setStatusCondition(condition);

        // Output appropriate message based on the condition
        switch (condition) {
            case BURN:
                System.out.println(String.format(MSG_BURN, realTarget.getName()));
                break;
            case WET:
                System.out.println(String.format(MSG_WET, realTarget.getName()));
                break;
            case QUICKSAND:
                System.out.println(String.format(MSG_QUICKSAND, realTarget.getName()));
                break;
            case SLEEP:
                System.out.println(String.format(MSG_SLEEP, realTarget.getName()));
                break;
            default:
                System.out.println(String.format(MSG_DEFAULT, realTarget.getName(), condition.getName()));
                break;
        }

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

    @Override
    public void setActionElement(Element element) {
        this.actionElement = element;
    }
}