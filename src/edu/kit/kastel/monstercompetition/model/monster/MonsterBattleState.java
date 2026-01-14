package edu.kit.kastel.monstercompetition.model.monster;

import edu.kit.kastel.monstercompetition.model.element.Element;
import edu.kit.kastel.monstercompetition.model.state.condition.StatusCondition;

/**
 * Handles the battle state of a monster including current HP, status conditions, and protections.
 * @author uozqc
 */
public class MonsterBattleState extends MonsterBase {
    // Constants for protection types and messages
    private static final String PROTECTION_TYPE_HEALTH = "health";
    private static final String PROTECTION_TYPE_STATS = "stats";
    private static final String MSG_PROTECTED_NO_DAMAGE = "%s is protected and takes no damage!";
    private static final String MSG_PROTECTED_HEALTH = "%s is now protected against damage!";
    private static final String MSG_PROTECTED_STATS = "%s is now protected against status changes!";
    private static final String MSG_PROTECTION_FADES = "%s's protection fades away...";
    private static final String MSG_HEAL = "%s gains back %d health!";
    private static final int INITIAL_PROTECTION_ROUNDS = 0;
    private static final int MIN_HP = 0;
    private static final int NO_PROTECTION_REMAINING = 0;

    /**
     * The current status condition affecting the monster (WET, BURN, QUICKSAND, SLEEP)
     */
    protected StatusCondition statusCondition;

    /**
     * The current health points of the monster.
     * This value is initialized to the monster's base HP (max health) and decreases when the monster
     * takes damage.
     */
    private int currentHp;


    /**
     * Flag indicating whether the monster is protected against damage.
     */
    private boolean protectedAgainstDamage;

    /**
     * Flag indicating whether the monster is protected against negative stat changes.
     */
    private boolean protectedAgainstStatChanges;

    /**
     * The number of rounds remaining for the monster's active protection.
     */
    private int protectionRoundsRemaining;

    /**
     * Constructor with stats config.
     * @param name Monster name
     * @param element Monster element
     * @param stats The stats configuration
     */
    public MonsterBattleState(String name, Element element, StatsConfig stats) {
        super(name, element, stats);
        initializeBattleState();
    }

    /**
     * Constructor with basic stats.
     * @param name Monster name
     * @param element Monster element
     * @param baseHp Base HP value
     * @param baseAtk Base ATK value
     * @param baseDef Base DEF value
     * @param baseSpd Base SPD value
     */
    public MonsterBattleState(String name, Element element,
                         int baseHp, int baseAtk, int baseDef, int baseSpd) {
        super(name, element, baseHp, baseAtk, baseDef, baseSpd);
        initializeBattleState();
    }

    /**
     * Initializes the battle state of the monster.
     */
    private void initializeBattleState() {
        this.currentHp = baseHp; // Start at max HP
        this.statusCondition = null; // no condition at start
        this.protectedAgainstDamage = false;
        this.protectedAgainstStatChanges = false;
        this.protectionRoundsRemaining = INITIAL_PROTECTION_ROUNDS;
    }

    /**
     * Gets the monster's current HP.
     * @return The monster's current HP
     */
    public int getCurrentHp() {
        return currentHp;
    }

    /**
     * Apply damage to the monster.
     * Returns true if the monster just fainted from this damage.
     *
     * @param damage The amount of damage to apply
     * @return true if the monster fainted from this damage
     */
    public boolean takeDamage(int damage) {
        // If protected against damage, don't take damage
        if (protectedAgainstDamage) {
            System.out.println(String.format(MSG_PROTECTED_NO_DAMAGE, name));
            return false;
        }

        // Calculate new HP (no output here)
        boolean wasFainted = this.currentHp <= MIN_HP;
        this.currentHp = Math.max(MIN_HP, this.currentHp - damage);
        boolean isFaintedNow = this.currentHp <= MIN_HP;

        // Return true if monster just fainted (wasn't fainted before but is now)
        return !wasFainted && isFaintedNow;
    }

    /**
     * Sets a status condition on the monster.
     * @param condition The status condition to apply
     */
    public void setStatusCondition(StatusCondition condition) {
        // Only print messages when appropriate - the actual messages should be handled
        // by the code that calls this method
        this.statusCondition = condition;
    }

    /**
     * Heal the monster by a specified amount.
     * If this would exceed max HP, HP is capped at the max.
     *
     * @param plusHP The amount of HP to heal
     */
    public void heal(int plusHP) {
        this.currentHp = Math.min(this.baseHp, this.currentHp + plusHP);
        // Output the original healing amount, not the actual amount healed
        System.out.println(String.format(MSG_HEAL, name, plusHP));
    }

    /**
     * Checks if the monster has fainted (HP <= 0).
     * @return true if the monster has fainted
     */
    public boolean isFainted() {
        return this.currentHp <= MIN_HP;
    }

    /**
     * Gets the monster's current status condition.
     * @return The monster's status condition, or null if none
     */
    public StatusCondition getStatusCondition() {
        return this.statusCondition;
    }

    /**
     * Sets protection for the monster against damage or stat changes.
     * @param type Type of protection ("health" or "stats")
     * @param rounds Number of rounds the protection lasts
     */
    public void setProtection(String type, int rounds) {
        // End any existing protection
        protectedAgainstDamage = false;
        protectedAgainstStatChanges = false;

        // Set new protection
        this.protectionRoundsRemaining = rounds;
        if (PROTECTION_TYPE_HEALTH.equals(type)) {
            this.protectedAgainstDamage = true;
            System.out.println(String.format(MSG_PROTECTED_HEALTH, name));
        } else if (PROTECTION_TYPE_STATS.equals(type)) {
            this.protectedAgainstStatChanges = true;
            System.out.println(String.format(MSG_PROTECTED_STATS, name));
        }
    }

    /**
     * Decrements the protection round counter and removes protection if it expires.
     */
    public void decrementProtection() {
        if (protectionRoundsRemaining > NO_PROTECTION_REMAINING) {
            protectionRoundsRemaining--;
            if (protectionRoundsRemaining == NO_PROTECTION_REMAINING) {
                if (protectedAgainstDamage || protectedAgainstStatChanges) {
                    System.out.println(String.format(MSG_PROTECTION_FADES, name));
                }
                protectedAgainstDamage = false;
                protectedAgainstStatChanges = false;
            }
        }
    }

    /**
     * Checks if the monster is protected against damage.
     * @return true if the monster is protected against damage
     */
    public boolean isProtectedAgainstDamage() {
        return protectedAgainstDamage;
    }

    /**
     * Checks if the monster is protected against stat changes.
     * @return true if the monster is protected against stat changes
     */
    public boolean isProtectedAgainstStatChanges() {
        return protectedAgainstStatChanges;
    }
} 