package edu.kit.kastel.monstercompetition.model.effect;

import edu.kit.kastel.monstercompetition.model.element.Element;
import edu.kit.kastel.monstercompetition.model.monster.Monster;
import edu.kit.kastel.monstercompetition.util.RoundHandler;

/**
 * Implements a status change effect that modifies a monster's stat stages.
 * This effect can increase or decrease ATK, DEF, SPD, PRC, or AGL stats
 * by a specified number of stages.
 *
 * @author uozqc
 */
public class InflictStatChangeEffect implements Effect {
    // Constants for target types
    private static final String TARGET_TYPE_USER = "user";
    
    // Constants for stat types
    private static final String STAT_ATK = "ATK";
    private static final String STAT_DEF = "DEF";
    private static final String STAT_SPD = "SPD";
    private static final String STAT_PRC = "PRC";
    private static final String STAT_AGL = "AGL";
    
    // Constants for messages
    private static final String STAT_CHANGE_CONTEXT = "stat change";
    private static final String ERROR_UNKNOWN_STAT = "Error, unknown stat: %s";

    private final String targetType;  // "user" or "target"
    private final String stat;        // e.g. "ATK","DEF","SPD","PRC","AGL"
    private final int delta;          // e.g. +2, -1 (stage change)
    private final int hitRate;        // Probability to hit (0-100)
    private Element actionElement;

    /**
     * Creates a new stat change effect.
     *
     * @param targetType Which monster to affect ("user" or "target")
     * @param stat The stat to modify ("ATK", "DEF", "SPD", "PRC", "AGL")
     * @param delta The amount to change the stat's stage by
     * @param hitRate The hit rate percentage (0-100)
     */
    public InflictStatChangeEffect(String targetType,
                                   String stat,
                                   int delta,
                                   int hitRate) {
        this.targetType = targetType;
        this.stat = stat;
        this.delta = delta;
        this.hitRate = hitRate;
    }

    @Override
    public boolean apply(Monster user, Monster target, RoundHandler handler) {
        // Fail if either monster is fainted
        if (user.isFainted() || target.isFainted()) {
            return false;
        }

        // Check if the effect hits based on hit rate
        boolean success = handler.getRandomGenerator()
                .checkProbability(STAT_CHANGE_CONTEXT, hitRate);
        if (!success) {
            // Effect missed, return false without a message
            return false;
        }

        // Determine which monster will be affected
        Monster realTarget = targetType.equals(TARGET_TYPE_USER) ? user : target;

        // Apply stage change to the appropriate stat
        // The Monster class methods handle protection checks and output messages
        switch (stat.toUpperCase()) {
            case STAT_ATK:
                realTarget.changeAtkStage(delta);
                break;
            case STAT_DEF:
                realTarget.changeDefStage(delta);
                break;
            case STAT_SPD:
                realTarget.changeSpdStage(delta);
                break;
            case STAT_PRC:
                realTarget.changePrcStage(delta);
                break;
            case STAT_AGL:
                realTarget.changeAglStage(delta);
                break;
            default:
                System.out.println(String.format(ERROR_UNKNOWN_STAT, stat));
                break;
        }

        return true;
    }

    /**
     * Get hit rate.
     * @return the current hit rate
     */
    public int getHitRate() {
        return hitRate;
    }

    @Override
    public void setActionElement(Element element) {
        this.actionElement = element;
    }
}