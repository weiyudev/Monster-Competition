package edu.kit.kastel.monstercompetition.model.monster;

import edu.kit.kastel.monstercompetition.model.effect.Action;
import edu.kit.kastel.monstercompetition.model.element.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class representing a monster with its name, element, and base attributes.
 * @author uozqc
 */
public class MonsterBase {
    // Default values for precision and agility
    private static final int DEFAULT_PRECISION = 100;
    private static final int DEFAULT_AGILITY = 100;
    
    protected final String name;
    protected final Element element;

    protected final int baseHp;
    protected final int baseAtk;
    protected final int baseDef;
    protected final int baseSpd;
    protected final int basePrc;
    protected final int baseAgl;
    protected List<Action> knownActions;

    /**
     * Constructor with base stats using StatsConfig.
     * @param name Monster name
     * @param element Monster element
     * @param stats The stats configuration
     */
    public MonsterBase(String name, Element element, StatsConfig stats) {
        this.name = name;
        this.element = element;
        this.baseHp = stats.getHp();
        this.baseAtk = stats.getAtk();
        this.baseDef = stats.getDef();
        this.baseSpd = stats.getSpd();
        this.basePrc = stats.getPrc();
        this.baseAgl = stats.getAgl();
        this.knownActions = new ArrayList<>();
    }

    /**
     * Constructor with individual base stats.
     * @param name Monster name
     * @param element Monster element
     * @param baseHp Base HP value
     * @param baseAtk Base ATK value
     * @param baseDef Base DEF value
     * @param baseSpd Base SPD value
     */
    public MonsterBase(String name, Element element,
                   int baseHp, int baseAtk, int baseDef, int baseSpd) {
        this(name, element, new StatsConfig(baseHp, baseAtk, baseDef, baseSpd));
    }

    /**
     * Gets the monster's name.
     * @return The monster's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the monster's elemental type.
     * @return The monster's element
     */
    public Element getElement() {
        return element;
    }

    /**
     * Gets the monster's base HP stat.
     * @return The monster's base HP
     */
    public int getBaseHp() {
        return baseHp;
    }

    /**
     * Gets the monster's base Attack stat.
     * @return The monster's base Attack
     */
    public int getBaseAtk() {
        return baseAtk;
    }

    /**
     * Gets the monster's base Defense stat.
     * @return The monster's base Defense
     */
    public int getBaseDef() {
        return baseDef;
    }

    /**
     * Gets the monster's base Speed stat.
     * @return The monster's base Speed
     */
    public int getBaseSpd() {
        return baseSpd;
    }

    /**
     * Gets the monster's base Precision stat.
     * @return The monster's base Precision
     */
    public int getBasePrc() {
        return basePrc;
    }

    /**
     * Gets the monster's base Agility stat.
     * @return The monster's base Agility
     */
    public int getBaseAgl() {
        return baseAgl;
    }

    /**
     * Gets the list of actions this monster knows.
     * @return List of known actions
     */
    public List<Action> getKnownActions() {
        return knownActions;
    }

    /**
     * Adds a new action to the monster's known actions.
     * @param action The action to add
     */
    public void addAction(Action action) {
        this.knownActions.add(action);
    }
} 