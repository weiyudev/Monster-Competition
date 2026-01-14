package edu.kit.kastel.monstercompetition.command;

/**
 * Base interface for user commands.
 *
 * @author uozqc
 */
public interface Command {

    /**
     * Executes command logic.
     * @param args the arguments after command word
     * @return true if success, false otherwise
     */
    boolean execute(String[] args);
}
