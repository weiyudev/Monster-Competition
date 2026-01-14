package edu.kit.kastel.monstercompetition.model.monster;

import edu.kit.kastel.monstercompetition.model.element.Element;
import edu.kit.kastel.monstercompetition.model.state.condition.StatusCondition;

/**
 * Represents a monster with its name, element, base attributes, and battle state.
 * This class handles the stat stage management and effective stat calculations.
 * @author uozqc
 */
public class Monster extends MonsterBattleState {
    // Stat stage constants
    private static final int INITIAL_STAGE = 0;
    private static final int MIN_STAGE = -5;
    private static final int MAX_STAGE = 5;
    private static final double MIN_STAT_VALUE = 1.0;
    private static final double BURN_ATK_MULTIPLIER = 0.75;
    private static final double WET_DEF_MULTIPLIER = 0.75;
    private static final double QUICKSAND_SPD_MULTIPLIER = 0.75;
    private static final int ATK_DEF_SPD_BASE_FACTOR = 2;
    private static final int PRC_AGL_BASE_FACTOR = 3;
    private static final String MSG_PROTECTED_UNAFFECTED = "%s is protected and is unaffected!";
    private static final String MSG_STAT_RISES = "%s's %s rises!";
    private static final String MSG_STAT_DECREASES = "%s's %s decreases...";
    private static final String STAT_ATK = "ATK";
    private static final String STAT_DEF = "DEF";
    private static final String STAT_SPD = "SPD";
    private static final String STAT_PRC = "PRC";
    private static final String STAT_AGL = "AGL";
    private static final int NEUTRAL_STAT_CHANGE = 0;
    private static final int NEUTRAL_STAGE = 0;

    // Stat stage management
    private int atkStage;
    private int defStage;
    private int spdStage;
    private int prcStage;
    private int aglStage;

    /**
     * Constructor with StatsConfig.
     * @param name Monster name
     * @param element Monster element
     * @param stats The stats configuration
     */
    public Monster(String name, Element element, StatsConfig stats) {
        super(name, element, stats);
        initializeStages();
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
    public Monster(String name, Element element,
                   int baseHp, int baseAtk, int baseDef, int baseSpd) {
        super(name, element, baseHp, baseAtk, baseDef, baseSpd);
        initializeStages();
    }

    /**
     * Initializes all stat stages to 0.
     */
    private void initializeStages() {
        this.atkStage = INITIAL_STAGE;
        this.defStage = INITIAL_STAGE;
        this.spdStage = INITIAL_STAGE;
        this.prcStage = INITIAL_STAGE;
        this.aglStage = INITIAL_STAGE;
    }

    /**
     * Gets the monster's current attack stage.
     * @return The attack stage value
     */
    public int getAtkStage() {
        return atkStage;
    }

    /**
     * Gets the monster's current defense stage.
     * @return The defense stage value
     */
    public int getDefStage() {
        return defStage;
    }

    /**
     * Gets the monster's current speed stage.
     * @return The speed stage value
     */
    public int getSpdStage() {
        return spdStage;
    }

    /**
     * Gets the monster's current precision stage.
     * @return The precision stage value
     */
    public int getPrcStage() {
        return prcStage;
    }

    /**
     * Gets the monster's current agility stage.
     * @return The agility stage value
     */
    public int getAglStage() {
        return aglStage;
    }

    /**
     * Changes the monster's attack stage by the specified amount.
     * @param delta The amount to change the attack stage by
     */
    public void changeAtkStage(int delta) {
        // If protected against stat changes and delta is negative, don't apply
        if (isProtectedAgainstStatChanges() && delta < NEUTRAL_STAT_CHANGE) {
            System.out.println(String.format(MSG_PROTECTED_UNAFFECTED, name));
            return;
        }
        int oldStage = this.atkStage;
        this.atkStage = limitStage(this.atkStage + delta);

        if (this.atkStage > oldStage) {
            System.out.println(String.format(MSG_STAT_RISES, name, STAT_ATK));
        } else if (this.atkStage < oldStage) {
            System.out.println(String.format(MSG_STAT_DECREASES, name, STAT_ATK));
        }
    }

    /**
     * Changes the monster's defense stage by the specified amount.
     * @param delta The amount to change the defense stage by
     */
    public void changeDefStage(int delta) {
        if (isProtectedAgainstStatChanges() && delta < NEUTRAL_STAT_CHANGE) {
            System.out.println(String.format(MSG_PROTECTED_UNAFFECTED, name));
            return;
        }
        int oldStage = this.defStage;
        this.defStage = limitStage(this.defStage + delta);

        if (this.defStage > oldStage) {
            System.out.println(String.format(MSG_STAT_RISES, name, STAT_DEF));
        } else if (this.defStage < oldStage) {
            System.out.println(String.format(MSG_STAT_DECREASES, name, STAT_DEF));
        }
    }

    /**
     * Changes the monster's speed stage by the specified amount.
     * @param delta The amount to change the speed stage by
     */
    public void changeSpdStage(int delta) {
        if (isProtectedAgainstStatChanges() && delta < NEUTRAL_STAT_CHANGE) {
            System.out.println(String.format(MSG_PROTECTED_UNAFFECTED, name));
            return;
        }
        int oldStage = this.spdStage;
        this.spdStage = limitStage(this.spdStage + delta);

        if (this.spdStage > oldStage) {
            System.out.println(String.format(MSG_STAT_RISES, name, STAT_SPD));
        } else if (this.spdStage < oldStage) {
            System.out.println(String.format(MSG_STAT_DECREASES, name, STAT_SPD));
        }
    }

    /**
     * Changes the monster's precision stage by the specified amount.
     * @param delta The amount to change the precision stage by
     */
    public void changePrcStage(int delta) {
        if (isProtectedAgainstStatChanges() && delta < NEUTRAL_STAT_CHANGE) {
            System.out.println(String.format(MSG_PROTECTED_UNAFFECTED, name));
            return;
        }
        int oldStage = this.prcStage;
        this.prcStage = limitStage(this.prcStage + delta);

        if (this.prcStage > oldStage) {
            System.out.println(String.format(MSG_STAT_RISES, name, STAT_PRC));
        } else if (this.prcStage < oldStage) {
            System.out.println(String.format(MSG_STAT_DECREASES, name, STAT_PRC));
        }
    }

    /**
     * Changes the monster's agility stage by the specified amount.
     * @param delta The amount to change the agility stage by
     */
    public void changeAglStage(int delta) {
        if (isProtectedAgainstStatChanges() && delta < NEUTRAL_STAT_CHANGE) {
            System.out.println(String.format(MSG_PROTECTED_UNAFFECTED, name));
            return;
        }
        int oldStage = this.aglStage;
        this.aglStage = limitStage(this.aglStage + delta);

        if (this.aglStage > oldStage) {
            System.out.println(String.format(MSG_STAT_RISES, name, STAT_AGL));
        } else if (this.aglStage < oldStage) {
            System.out.println(String.format(MSG_STAT_DECREASES, name, STAT_AGL));
        }
    }

    /**
     * Limits the stage value to be between -5 and 5.
     * @param stage The stage value to limit
     * @return The limited stage value
     */
    private int limitStage(int stage) {
        return Math.max(MIN_STAGE, Math.min(MAX_STAGE, stage));
    }

    /**
     * Calculates the monster's effective attack stat.
     * @return The effective attack value after applying stage and status effects
     */
    public double getEffectiveAtk() {
        double factor = calculateStatFactor(ATK_DEF_SPD_BASE_FACTOR, atkStage);
        double result = this.baseAtk * factor;

        // Apply status condition effects
        if (statusCondition == StatusCondition.BURN) {
            result *= BURN_ATK_MULTIPLIER; // BURN reduces ATK by 25%
        }

        return Math.max(MIN_STAT_VALUE, result);
    }

    /**
     * Calculates the monster's effective defense stat.
     * @return The effective defense value after applying stage and status effects
     */
    public double getEffectiveDef() {
        double factor = calculateStatFactor(ATK_DEF_SPD_BASE_FACTOR, defStage);
        double result = this.baseDef * factor;

        // Apply status condition effects
        if (statusCondition == StatusCondition.WET) {
            result *= WET_DEF_MULTIPLIER; // WET reduces DEF by 25%
        }

        return Math.max(MIN_STAT_VALUE, result);
    }

    /**
     * Calculates the monster's effective speed stat.
     * @return The effective speed value after applying stage and status effects
     */
    public double getEffectiveSpd() {
        double factor = calculateStatFactor(ATK_DEF_SPD_BASE_FACTOR, spdStage);
        double result = this.baseSpd * factor;

        // Apply status condition effects
        if (statusCondition == StatusCondition.QUICKSAND) {
            result *= QUICKSAND_SPD_MULTIPLIER; // QUICKSAND reduces SPD by 25%
        }

        return Math.max(MIN_STAT_VALUE, result);
    }

    /**
     * Calculates the monster's effective precision stat.
     * @return The effective precision value after applying stage effects
     */
    public double getEffectivePrc() {
        double factor = calculateStatFactor(PRC_AGL_BASE_FACTOR, prcStage);
        double result = this.basePrc * factor;
        return Math.max(MIN_STAT_VALUE, result);
    }

    /**
     * Calculates the monster's effective agility stat.
     * @return The effective agility value after applying stage effects
     */
    public double getEffectiveAgl() {
        double factor = calculateStatFactor(PRC_AGL_BASE_FACTOR, aglStage);
        double result = this.baseAgl * factor;
        return Math.max(MIN_STAT_VALUE, result);
    }

    /**
     * Calculates the stat factor according to the formula in A.2.3.
     *
     * @param baseValue Base value (2 for ATK/DEF/SPD, 3 for PRC/AGL)
     * @param stageChange Stage change (-5 to +5)
     * @return The multiplicative factor for the stat
     */
    private double calculateStatFactor(int baseValue, int stageChange) {
        if (stageChange >= NEUTRAL_STAGE) {
            return (double) (baseValue + stageChange) / baseValue;
        } else {
            return (double) baseValue / (baseValue - stageChange);
        }
    }
}