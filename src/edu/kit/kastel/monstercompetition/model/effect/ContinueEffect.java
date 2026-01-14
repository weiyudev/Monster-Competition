package edu.kit.kastel.monstercompetition.model.effect;


import edu.kit.kastel.monstercompetition.model.element.Element;
import edu.kit.kastel.monstercompetition.model.monster.Monster;
import edu.kit.kastel.monstercompetition.util.InvalidArgumentException;
import edu.kit.kastel.monstercompetition.util.RoundHandler;


/**
 * Represents a continue effect in the monster battle system.
 * This effect does nothing special except check a hit rate and return success,
 * allowing subsequent effects to execute. It acts as a conditional gate for
 * the remaining effects in an action.
 *
 * @author uozqc
 */

public class ContinueEffect implements Effect {
    private static final String ERROR_CONTINUE_ARGUMENT_PARAMETER_FORMAT =
            "Error, the 'hitRate' is not valid for a continue argument.";
    private static final String CONTINUE_NAME = "continue";
    private static final int MIN_HIT_RATE = 0;
    private static final int MAX_HIT_RATE = 100;
    private final int hitRate;
    private Element actionElement;

    /**
     * Creates a new continue effect with the specified hit rate.
     *
     * @param hitRate The hit rate (0-100) for this effect
     * @throws InvalidArgumentException if hit rate is outside valid range
     */
    public ContinueEffect(int hitRate) throws InvalidArgumentException {
        if (hitRate < MIN_HIT_RATE || hitRate > MAX_HIT_RATE) {
            throw new InvalidArgumentException(ERROR_CONTINUE_ARGUMENT_PARAMETER_FORMAT);
        }
        this.hitRate = hitRate;
    }


    /**
     * Applies this continue effect by checking if it hits based on the hit rate.
     * If successful, it allows subsequent effects to execute. Otherwise, it prevents
     * any further effects from being applied.
     *
     * @param user The monster performing the action
     * @param target The monster receiving the effect
     * @param handler The round handler managing the battle
     * @return true if the effect hits and subsequent effects should be applied,
     *         false if the effect misses and no further effects should be applied
     */
    @Override
    public boolean apply(Monster user, Monster target, RoundHandler handler) {
        //1. If the HP of target is 0, effect can't proceed.
        if (user.isFainted() || target.isFainted()) {
            return false;
        }

        //2. Perform a hit-rate check.
        //3. If success, do nothing special, just allow subsequent effects to execute.
        return handler.getRandomGenerator().checkProbability(CONTINUE_NAME, hitRate);
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
