package edu.kit.kastel.monstercompetition.model.effect;

import edu.kit.kastel.monstercompetition.model.element.Element;
import edu.kit.kastel.monstercompetition.model.monster.Monster;
import edu.kit.kastel.monstercompetition.util.RoundHandler;

/**
 * Implements the Protection effect which can protect a monster against damage or stat changes.
 * Protection can last for a fixed or random number of rounds.
 *
 * @author uozqc
 */
public class ProtectEffect implements Effect {

    // Constants for messages and contexts
    private static final String PROTECTION_EFFECT_CONTEXT = "protection effect";
    private static final String PROTECT_ROUNDS_CONTEXT = "protect rounds";
    
    // Constants for string formatting in toString method
    private static final String TO_STRING_FORMAT_START = "ProtectEffect(target=%s";
    private static final String TO_STRING_FORMAT_RANDOM = ", rounds=random[%d-%d]";
    private static final String TO_STRING_FORMAT_FIXED = ", rounds=%d";
    private static final String TO_STRING_FORMAT_END = ", hitRate=%d%%)";
    private static final int DEFAUT_COUNT = 0;

    private final String protectTarget; // "health" or "stats"

    // If randomCount is false, then use fixedCount, otherwise use minCount and maxCount.
    private final boolean randomCount;
    private final int fixedCount;
    private final int minCount;
    private final int maxCount;

    private final int hitRate;

    private Element actionElement;

    /**
     * Constructor for a protection effect with a fixed number of rounds.
     *
     * @param protectTarget Type of protection ("health" or "stats")
     * @param fixedCount Fixed number of rounds the protection will last
     * @param hitRate Hit rate percentage (0-100)
     */
    public ProtectEffect(String protectTarget, int fixedCount, int hitRate) {
        this.protectTarget = protectTarget;
        this.fixedCount = fixedCount;
        this.hitRate = hitRate;
        this.randomCount = false;
        this.minCount = DEFAUT_COUNT;
        this.maxCount = DEFAUT_COUNT;
    }

    /**
     * Constructor for a protection effect with a random number of rounds.
     *
     * @param protectTarget Type of protection ("health" or "stats")
     * @param minCount Minimum number of rounds the protection can last
     * @param maxCount Maximum number of rounds the protection can last
     * @param hitRate Hit rate percentage (0-100)
     */
    public ProtectEffect(String protectTarget, int minCount, int maxCount, int hitRate) {
        this.protectTarget = protectTarget;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.hitRate = hitRate;
        this.randomCount = true;
        this.fixedCount = DEFAUT_COUNT;
    }

    @Override
    public boolean apply(Monster user, Monster target, RoundHandler handler) {
        // 1. Skip if either monster is fainted
        if (user.isFainted() || target.isFainted()) {
            return false;
        }

        // 2. Check hit rate
        boolean hits = handler.getRandomGenerator().checkProbability(PROTECTION_EFFECT_CONTEXT, hitRate);
        if (!hits) {
            return false;
        }

        // 3. Determine protection duration (number of rounds)
        int rounds;
        if (randomCount) {
            rounds = handler.getRandomGenerator().getIntInRange(PROTECT_ROUNDS_CONTEXT, minCount, maxCount);
        } else {
            rounds = fixedCount;
        }

        // 4. Apply protection to the user (protection is always applied to the acting monster)
        // Note: This will automatically end any existing protection as per the requirements
        user.setProtection(protectTarget, rounds);

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(TO_STRING_FORMAT_START, protectTarget));

        if (randomCount) {
            sb.append(String.format(TO_STRING_FORMAT_RANDOM, minCount, maxCount));
        } else {
            sb.append(String.format(TO_STRING_FORMAT_FIXED, fixedCount));
        }

        sb.append(String.format(TO_STRING_FORMAT_END, hitRate));
        return sb.toString();
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
