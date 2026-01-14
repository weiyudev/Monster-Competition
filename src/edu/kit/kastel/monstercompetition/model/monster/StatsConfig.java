package edu.kit.kastel.monstercompetition.model.monster;

/**
 * Configuration class to encapsulate all monster stats.
 * This helps reduce the number of parameters in constructors.
 * @author uozqc
 */
public class StatsConfig {
    private static final int PRC_DEFAUT = 1;
    private static final int AGL_DEFAUT = 1;
    private final int hp;
    private final int atk;
    private final int def;
    private final int spd;
    private final int prc;
    private final int agl;

    /**
     * Creates a stats configuration with all stats specified.
     * 
     * @param hp Hit Points value
     * @param atk Attack value
     * @param def Defense value
     * @param spd Speed value
     * @param prc Precision value
     * @param agl Agility value
     */
    public StatsConfig(int hp, int atk, int def, int spd, int prc, int agl) {
        this.hp = hp;
        this.atk = atk;
        this.def = def;
        this.spd = spd;
        this.prc = prc;
        this.agl = agl;
    }

    /**
     * Creates a stats configuration with basic stats, setting PRC and AGL to 1.
     * 
     * @param hp Hit Points value
     * @param atk Attack value
     * @param def Defense value
     * @param spd Speed value
     */
    public StatsConfig(int hp, int atk, int def, int spd) {
        this(hp, atk, def, spd, PRC_DEFAUT, AGL_DEFAUT);
    }

    /**
     * Gets the HP value.
     * @return The HP value
     */
    public int getHp() {
        return hp;
    }

    /**
     * Gets the Attack value.
     * @return The Attack value
     */
    public int getAtk() {
        return atk;
    }

    /**
     * Gets the Defense value.
     * @return The Defense value
     */
    public int getDef() {
        return def;
    }

    /**
     * Gets the Speed value.
     * @return The Speed value
     */
    public int getSpd() {
        return spd;
    }

    /**
     * Gets the Precision value.
     * @return The Precision value
     */
    public int getPrc() {
        return prc;
    }

    /**
     * Gets the Agility value.
     * @return The Agility value
     */
    public int getAgl() {
        return agl;
    }
} 