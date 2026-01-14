package edu.kit.kastel.monstercompetition.command;

import edu.kit.kastel.monstercompetition.model.monster.Monster;
import edu.kit.kastel.monstercompetition.model.effect.Action;
import edu.kit.kastel.monstercompetition.model.game.Competition;
import edu.kit.kastel.monstercompetition.util.RoundHandler;

/**
 * Command that allows a monster to select an action to perform.
 *
 * @author uozqc
 */
public class ActionCommand implements Command {

    private static final String NO_MONSTER_ERROR = "Error, no monster is currently selecting an action.";
    private static final String USAGE_ERROR = "Error, usage: action <actionName> [<targetMonsterName>]";
    private static final String ACTION_NOT_FOUND_ERROR = "Error, %s does not know the action %s.";
    private static final String UNKNOWN_TARGET_ERROR = "Error, unknown target monster: %s";
    private static final String NO_TARGET_ERROR = "Error, no valid target for the action.";
    private static final String WHAT_SHOULD_DO = "What should %s do?";
    
    // Numeric constants
    private static final int MIN_ARGS = 1;
    private static final int MAX_ARGS_WITH_TARGET = 2;
    private static final int ACTION_NAME_INDEX = 0;
    private static final int TARGET_NAME_INDEX = 1;
    private static final int MIN_MONSTERS_FOR_COMPETITION = 2;
    
    // Formatting constants
    private static final String NEWLINE = "";

    private final Competition competition;
    private final CommandHandler commandHandler;

    /**
     * Creates a new ActionCommand.
     *
     * @param competition The current competition
     * @param commandHandler The command handler
     */
    public ActionCommand(Competition competition, CommandHandler commandHandler) {
        this.competition = competition;
        this.commandHandler = commandHandler;
    }

    @Override
    public boolean execute(String[] args) {
        Monster currentMonster = commandHandler.getCurrentActionMonster();

        if (currentMonster == null) {
            System.out.println(NO_MONSTER_ERROR);
            return false;
        }

        String currentMonsterName = currentMonster.getName();

        if (args.length < MIN_ARGS) {
            showUsageError(currentMonsterName);
            return false;
        }

        String actionName = args[ACTION_NAME_INDEX];
        Action chosenAction = findMonsterAction(currentMonster, actionName);

        if (chosenAction == null) {
            showActionNotFoundError(currentMonsterName, actionName);
            return false;
        }

        Monster target = findTarget(currentMonster, args);
        if (target == null && competition.getAllMonsters().size() > MIN_MONSTERS_FOR_COMPETITION) {
            showNoTargetError(currentMonsterName);
            return false;
        }

        // Record chosen action and target
        processActionChoice(currentMonster, chosenAction, target);
        return true;
    }

    private void showUsageError(String monsterName) {
        System.out.println(USAGE_ERROR);
        System.out.println();
        System.out.println(WHAT_SHOULD_DO.formatted(monsterName));
    }

    private Action findMonsterAction(Monster monster, String actionName) {
        for (Action action : monster.getKnownActions()) {
            if (action.getName().equals(actionName)) {
                return action;
            }
        }
        return null;
    }

    private void showActionNotFoundError(String monsterName, String actionName) {
        System.out.println(ACTION_NOT_FOUND_ERROR.formatted(monsterName, actionName));
        System.out.println();
        System.out.println(WHAT_SHOULD_DO.formatted(monsterName));
    }

    private Monster findTarget(Monster currentMonster, String[] args) {
        // If target name provided
        if (args.length > MIN_ARGS) {
            return findNamedTarget(currentMonster, args[TARGET_NAME_INDEX]);
        } else {
            return findDefaultTarget(currentMonster);
        }
    }

    private Monster findNamedTarget(Monster currentMonster, String targetName) {
        for (Monster monster : competition.getAllMonsters()) {
            if (monster.getName().equals(targetName) && monster != currentMonster) {
                return monster;
            }
        }
        // Target not found
        System.out.println(UNKNOWN_TARGET_ERROR.formatted(targetName));
        System.out.println();
        System.out.println(WHAT_SHOULD_DO.formatted(currentMonster.getName()));
        return null;
    }

    private Monster findDefaultTarget(Monster currentMonster) {
        for (Monster m : competition.getAllMonsters()) {
            if (m != currentMonster && !m.isFainted()) {
                return m;
            }
        }
        return null;
    }

    private void showNoTargetError(String monsterName) {
        System.out.println(NO_TARGET_ERROR);
        System.out.println();
        System.out.println(WHAT_SHOULD_DO.formatted(monsterName));
    }

    private void processActionChoice(Monster monster, Action action, Monster target) {
        RoundHandler roundHandler = commandHandler.getRoundHandler();
        roundHandler.setMonsterAction(monster, action, target);

        if (commandHandler.moveToNextMonster()) {
            // Get the new current monster
            Monster nextMonster = commandHandler.getCurrentActionMonster();
            System.out.println();
            System.out.println(WHAT_SHOULD_DO.formatted(nextMonster.getName()));
        } else {
            // All monsters have chosen actions, execute Phase II
            roundHandler.executePhaseII(competition);
        }
    }
}