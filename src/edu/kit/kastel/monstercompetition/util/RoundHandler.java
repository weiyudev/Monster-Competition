package edu.kit.kastel.monstercompetition.util;

import edu.kit.kastel.monstercompetition.model.effect.DamageEffect;
import edu.kit.kastel.monstercompetition.model.effect.RepeatEffect;
import edu.kit.kastel.monstercompetition.model.monster.Monster;
import edu.kit.kastel.monstercompetition.command.CommandHandler;
import edu.kit.kastel.monstercompetition.model.effect.Action;
import edu.kit.kastel.monstercompetition.model.effect.Effect;
import edu.kit.kastel.monstercompetition.model.game.Competition;
import edu.kit.kastel.monstercompetition.model.game.Phase;
import edu.kit.kastel.monstercompetition.model.state.condition.StatusCondition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the round-by-round logic of the competition.Handles all phases of monster battles and tracks game state.
 * @author uozqc
 */
public class RoundHandler {
    private static final String MSG_TURN_START = "It's %s's turn."; // Message constants
    private static final String MSG_USES_ACTION = "%s uses %s!";
    private static final String MSG_PASSES = "%s passes!";
    private static final String MSG_ACTION_FAILED = "The action failed...";
    private static final String MSG_BURN_DAMAGE = "%s takes %d damage from burning!";
    private static final String MSG_FAINTS = "%s faints!";
    private static final String MSG_NO_OPPONENTS = "%s has no opponents left and wins the competition!";
    private static final String MSG_ALL_FAINTED = "All monsters have fainted. The competition ends without a winner!";
    private static final String TARGET_TYPE_USER = "user"; // Target type constants
    private static final int INITIAL_ROUND = 1; // Numeric constants
    private static final int CONDITION_END_CHANCE = 33;
    private static final int FIRST_EFFECT_INDEX = 0;
    private static final int SECOND_EFFECT_INDEX = 1;
    private static final int MIN_MONSTERS_FOR_COMPETITION = 2;
    private static final int FIRST_MONSTER_INDEX = 0;
    private static final double BURN_DAMAGE_PERCENT = 0.1;
    private static final String CONDITION_END_NAME = "condition end";
    private static final int SINGLE_WINNER_COUNT = 1;
    private final RandomNumberGenerator rng;
    private final CommandHandler commandHandler;
    private Phase currentPhase;
    private int currentRound;
    private final Map<Monster, ActionChoice> monsterActions;
    /**
     * Class to store a monster's action choice for Phase II.
     */
    private static class ActionChoice {
        private final Action action;
        private final Monster target;
        /**
         * Creates a new action choice.
         * @param action The chosen action
         * @param target The target monster
         */
        ActionChoice(Action action, Monster target) {
            this.action = action;
            this.target = target;
        }
    }
    /**
     * Creates a new RoundHandler.
     * @param rng The random number generator
     * @param commandHandler The command handler
     */
    public RoundHandler(RandomNumberGenerator rng, CommandHandler commandHandler) {
        this.rng = rng;
        this.commandHandler = commandHandler;
        this.currentPhase = Phase.PHASE0;
        this.currentRound = INITIAL_ROUND;
        this.monsterActions = new HashMap<>();
    }
    /**
     * Gets the random number generator.
     * @return The random number generator
     */
    public RandomNumberGenerator getRandomGenerator() {
        return rng; }
    /**
     * Sets the current phase of the round.
     * @param phase The phase to set
     */
    public void setCurrentPhase(Phase phase) {
        this.currentPhase = phase;
        // Reset actions when changing phases
        if (phase == Phase.PHASE0) {
            monsterActions.clear();
        }
    }
    /**
     * Starts a new round.
     * @param competition The current competition
     */
    public void startNewRound(Competition competition) {
        currentRound++;
        currentPhase = Phase.PHASE0;
        monsterActions.clear();
        commandHandler.resetMonsterIndex();
        playRound(competition); }
    /**
     * Plays a round of the competition.
     * @param competition The current competition
     */
    private void playRound(Competition competition) {
        boolean ended = checkEndCondition(competition); // Phase 0: Check if competition has ended
        if (ended) {
            if (!competition.isEnded()) { // Only announce if competition just ended
                announceWinner(competition);
            }
            return;
        }
        currentPhase = Phase.PHASEI; // Phase I: Move to monster action selection
        List<Monster> monsters = competition.getAllMonsters();
        if (!monsters.isEmpty()) {
            Monster firstMonster = monsters.get(FIRST_MONSTER_INDEX);
            commandHandler.setCurrentActionMonster(firstMonster); // Let CommandHandler handle the prompt
        }
    }
    /**
     * Execute Phase II of the round, where monsters perform their actions.
     * @param competition The current competition
     */
    public void executePhaseII(Competition competition) {
        currentPhase = Phase.PHASEII;
        boolean competitionEnded = false;
        // Sort monsters by speed (descending)
        List<Monster> monstersInSpeedOrder = competition.getMonstersInSpeedOrder(competition);
        // Execute actions for each monster
        for (Monster monster : monstersInSpeedOrder) {
            if (monster.isFainted()) {
                continue; // Skip fainted monsters
            } // Begin monster's turn
            System.out.println();
            System.out.println(String.format(MSG_TURN_START, monster.getName()));
            StatusCondition condition = processStatusCondition(monster); // Process status conditions and handle turn actions
            ActionChoice choice = monsterActions.get(monster); // Get the monster's action choice
            // In debug mode, we need to handle repeat counts first, before announcing the action
            if (choice != null && choice.action != null) {
                // Pre-determine random repeat counts before the action is announced
                List<Effect> effects = choice.action.getEffects();
                for (Effect effect : effects) {
                    if (effect instanceof RepeatEffect repeatEffect) {
                        repeatEffect.predetermineRepeatCount(this);
                    }
                }
                System.out.println(String.format(MSG_USES_ACTION, monster.getName(), choice.action.getName()));
            } else {
                System.out.println(String.format(MSG_PASSES, monster.getName()));
            }
            if (condition == StatusCondition.SLEEP) { // If monster is asleep, it can't act
                continue;
            }
            handleMonsterAction(monster, condition, competition); // Handle the monster's action or pass
            // Check if competition has ended after this monster's turn, but don't exit yet
            if (checkEndCondition(competition)) {
                competitionEnded = true;
            }
        }
        if (competitionEnded) { // Announce the winner only at the end of Phase II
            announceWinner(competition);
        }
        processEndOfRound(competition); // Process end of round events
    }
    /**
     * Processes status conditions for a monster.
     * @param monster The monster to process
     * @return The current status condition
     */
    private StatusCondition processStatusCondition(Monster monster) {
        StatusCondition condition = monster.getStatusCondition();
        if (condition != null) { // Check if status condition ends (1/3 chance)
            boolean conditionEnds = rng.checkProbability(CONDITION_END_NAME, CONDITION_END_CHANCE);
            if (conditionEnds) {
                System.out.println(condition.getEndMessage(monster.getName()));
                monster.setStatusCondition(null);
                return null;
            } else {
                System.out.println(condition.getOngoingMessage(monster.getName()));
                return condition;
            }
        }
        return null;
    }
    /**
     * Handles a monster's action or pass.
     * @param monster The acting monster
     * @param condition The monster's status condition (perhaps null)
     * @param competition The current competition
     */
    private void handleMonsterAction(Monster monster, StatusCondition condition, Competition competition) {
        ActionChoice choice = monsterActions.get(monster);
        if (choice == null) {
            handleMonsterPass(monster, condition);
        } else {
            executeMonsterAction(monster, choice.action, choice.target, condition, competition);
        }
    }
    /**
     * Handles when a monster passes.
     * @param monster The monster passing
     * @param condition The monster's status condition
     */
    private void handleMonsterPass(Monster monster, StatusCondition condition) {
        // Apply burn damage if needed, just check end condition without announcing
        boolean fainted = applyBurnDamageIfNeeded(monster, condition);
        if (fainted) {
            // Only check the end condition, don't announce winner yet
            checkEndCondition(this.commandHandler.getCompetition());
        }
    }

    /**
     * Applies burn damage if the monster has the BURN condition.
     * @param monster The monster
     * @param condition The monster's status condition (perhaps null)
     * @return true if the monster fainted from burn damage, false otherwise
     */
    private boolean applyBurnDamageIfNeeded(Monster monster, StatusCondition condition) {
        if (condition == StatusCondition.BURN) {
            int burnDamage = (int) Math.ceil(monster.getBaseHp() * BURN_DAMAGE_PERCENT);
            System.out.println(String.format(MSG_BURN_DAMAGE, monster.getName(), burnDamage));
            boolean fainted = monster.takeDamage(burnDamage);
            if (fainted) {
                System.out.println(String.format(MSG_FAINTS, monster.getName()));
            }
            return fainted;
        }
        return false;
    }

    /**
     * Executes a monster's action against a target.
     * @param monster The acting monster
     * @param action The action to be executed
     * @param target The target monster
     * @param condition The monster's status condition (may be null)
     * @param competition The current competition state
     */
    private void executeMonsterAction(Monster monster, Action action, Monster target, StatusCondition condition, Competition competition) {
        List<Effect> effects = action.getEffects();
        if (effects.isEmpty()) {
            return;
        }
        prepareEffects(effects, action); // Set action element for effects
        boolean selfTargetingOnly = true; // Check if this is a self-targeting-only action
        for (Effect effect : effects) {
            if (effect instanceof DamageEffect) {
                DamageEffect damageEffect = (DamageEffect) effect;
                if (!TARGET_TYPE_USER.equals(damageEffect.getTargetType())) {
                    selfTargetingOnly = false;
                    break;
                }
            } else {
                selfTargetingOnly = false;
                break;
            }
        }
        // If this is a self-targeting action and the target has fainted or is null, apply directly to self
        if (selfTargetingOnly && (target == null || target.isFainted())) {
            for (Effect effect : effects) { // Apply all effects to the monster itself
                effect.apply(monster, monster, this);
                if (monster.isFainted()) {
                    break;
                }
            }
            applyBurnDamageIfNeeded(monster, condition);
            return;
        }
        Effect firstEffect = effects.get(FIRST_EFFECT_INDEX); // Apply the first effect
        boolean firstEffectSuccess = firstEffect.apply(monster, target, this);
        if (!firstEffectSuccess) { // If first effect fails, the entire action fails
            handleActionFailure(monster, condition);
            return;
        }
        if (monster.isFainted()) { // If the acting monster has fainted, no need to continue
            applyBurnDamageIfNeeded(monster, condition);
            return;
        }
        // First effect succeeded, apply remaining effects
        applyRemainingEffects(effects, monster, target);
        applyBurnDamageIfNeeded(monster, condition);
        
        // Reset all predetermined repeat counts after the action is completed
        for (Effect effect : effects) {
            if (effect instanceof RepeatEffect repeatEffect) {
                repeatEffect.resetPredeterminedRepeatCount();
            }
        }
    }

    private void applyRemainingEffects(List<Effect> effects, Monster monster, Monster target) {
        for (int i = SECOND_EFFECT_INDEX; i < effects.size(); i++) {
            Effect effect = effects.get(i);
            // If target has fainted, only apply user-targeting effects
            if (target != null && target.isFainted() && effect instanceof DamageEffect) {
                DamageEffect damageEffect = (DamageEffect) effect;
                if (TARGET_TYPE_USER.equals(damageEffect.getTargetType())) {
                    effect.apply(monster, monster, this); // Apply the effect to the user directly using the proper channel
                } // Skip target-targeting effects when target is fainted
                continue;
            }
            effect.apply(monster, target, this); // Normal effect application
            if (monster.isFainted()) { // Check if monster fainted from this effect
                break;
            }
        }
    }

    /**
     * Prepares effects by setting action element for effects.
     * @param effects The list of effects to prepare
     * @param action The action containing these effects
     */
    private void prepareEffects(List<Effect> effects, Action action) {
        for (Effect effect : effects) {
            effect.setActionElement(action.getElement());
        }
    }

    /**
     * Handles the failure of a monster's action.
     * @param monster The monster whose action failed
     * @param condition The monster's status condition
     */
    private void handleActionFailure(Monster monster, StatusCondition condition) {
        System.out.println(MSG_ACTION_FAILED);
        applyBurnDamageIfNeeded(monster, condition);
    }

    /**
     * Processes the end of a round.
     * @param competition The current competition state
     */
    private void processEndOfRound(Competition competition) {
        // End of round: update protections
        for (Monster monster : competition.getAllMonsters()) {
            if (!monster.isFainted()) {
                monster.decrementProtection();
            }
        }
        currentPhase = Phase.PHASE0; // Reset for next round
        monsterActions.clear();
        if (!competition.isEnded()) { // If competition is still ongoing, start a new round
            boolean ended = checkEndCondition(competition);
            if (ended) { // Only announce winner if not already announced
                announceWinner(competition);
                return; // Exit if the competition has ended
            }
            currentRound++; // Set up for a new round without recursive calls
            commandHandler.startNewActionSelection();
        }
    }

    /**
     * Sets a monster's action for the current round. This action will be executed when it's the monster's turn.
     * @param monster The monster performing the action
     * @param action The action to perform
     * @param target The target monster for the action
     */
    public void setMonsterAction(Monster monster, Action action, Monster target) {
        monsterActions.put(monster, new ActionChoice(action, target));
    }

    /**
     * Sets a monster to pass its turn for the current round. A passing monster will not perform any action during its turn.
     * @param monster The monster that will pass its turn
     */
    public void setMonsterPass(Monster monster) {
        monsterActions.remove(monster);
    }

    /**
     * Checks if the competition has reached an end condition.
     * @param competition The current competition state
     * @return true if the competition has ended, false otherwise
     */
    private boolean checkEndCondition(Competition competition) {
        List<Monster> alive = competition.getAliveMonsters();
        if (alive.size() < MIN_MONSTERS_FOR_COMPETITION) {
            competition.setEnded(true);
            return true;
        }
        return false;
    }

    /**
     * Announces the winner of the competition. This should only be called once at the end of Phase II.
     * @param competition The current competition state
     */
    private void announceWinner(Competition competition) {
        List<Monster> alive = competition.getAliveMonsters();
        System.out.println();
        if (alive.size() == SINGLE_WINNER_COUNT) {
            System.out.println(String.format(MSG_NO_OPPONENTS, alive.get(FIRST_MONSTER_INDEX).getName()));
        } else {
            System.out.println(MSG_ALL_FAINTED);
        }
    }
}