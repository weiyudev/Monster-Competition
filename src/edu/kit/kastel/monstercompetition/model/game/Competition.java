package edu.kit.kastel.monstercompetition.model.game;

import edu.kit.kastel.monstercompetition.model.monster.Monster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a competition between monsters in the battle system.
 * Manages the list of participating monsters and tracks the competition state.
 * A competition is typically created using the "competition monsterA monsterB ..." command.
 *
 * @author uozqc
 */
public class Competition {

    /** List of monsters participating in this competition. */
    private final List<Monster> monsters;

    /** Flag indicating whether the competition has ended. */
    private boolean ended;

    /**
     * Creates a new empty competition with no monsters and not ended.
     */
    public Competition() {
        this.monsters = new ArrayList<>();
        this.ended = false;
    }

    /**
     * Resets the competition to its initial state.
     * Clears all monsters and sets the ended state to false.
     */
    public void reset() {
        this.monsters.clear();
        this.ended = false;
    }

    /**
     * Adds a monster to the competition.
     *
     * @param m The monster to add
     */
    public void addMonster(Monster m) {
        this.monsters.add(m);
    }

    /**
     * Gets all monsters participating in the competition.
     *
     * @return The list of all monsters
     */
    public List<Monster> getAllMonsters() {
        return this.monsters;
    }

    /**
     * Gets a list of monsters that are still able to battle (not fainted).
     *
     * @return The list of monsters that haven't fainted
     */
    public List<Monster> getAliveMonsters() {
        List<Monster> alive = new ArrayList<>();
        for (Monster m : monsters) {
            if (!m.isFainted()) {
                alive.add(m);
            }
        }
        return alive;
    }

    /**
     * Checks if the competition has ended.
     *
     * @return true if the competition has ended, false otherwise
     */
    public boolean isEnded() {
        return ended;
    }

    /**
     * Sets the ended state of the competition.
     *
     * @param ended The new ended state
     */
    public void setEnded(boolean ended) {
        this.ended = ended;
    }


    /**
     * Gets monsters sorted by speed (descending).
     * @param competition The current competition
     * @return The sorted list of monsters
     */
    public List<Monster> getMonstersInSpeedOrder(Competition competition) {
        List<Monster> monsters = new ArrayList<>(competition.getAllMonsters());
        monsters.sort(new Comparator<Monster>() {
            @Override
            public int compare(Monster m1, Monster m2) {
                // Sort in descending order (equivalent to reversed())
                return Double.compare(m2.getEffectiveSpd(), m1.getEffectiveSpd());
            }
        });
        return monsters;
    }
}