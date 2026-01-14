package edu.kit.kastel.monstercompetition.command;

import edu.kit.kastel.monstercompetition.model.monster.Monster;
import edu.kit.kastel.monstercompetition.model.effect.Action;
import edu.kit.kastel.monstercompetition.model.game.Competition;
import edu.kit.kastel.monstercompetition.model.monster.StatsConfig;
import edu.kit.kastel.monstercompetition.util.paser.ConfigParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parse competition command.
 * start new competition with given monster names
 *
 * @author uozqc
 */
public class CompetitionCommand implements Command {

    // Error message constants
    private static final String ERROR_USAGE = "Error, usage: competition <monster1> <monster2> ...";
    private static final String ERROR_UNKNOWN_MONSTER = "Error, unknown monster: %s";
    private static final String MESSAGE_MONSTERS_ENTER = "The %d monsters enter the competition!";
    private static final String MESSAGE_WHAT_SHOULD_DO = "What should %s do?";
    
    // Format constants
    private static final String NAME_COUNTER_FORMAT = "%s#%d";
    private static final String NEWLINE = "";
    
    // Numeric constants
    private static final int MIN_MONSTERS = 2;
    private static final int FIRST_MONSTER_INDEX = 0;
    private static final int INITIAL_COUNT = 0;
    private static final int COUNT_INCREMENT = 1;

    private final Competition competition;
    private final ConfigParser parser;
    private final CommandHandler commandHandler;

    /**
     * Creates a new CompetitionCommand.
     *
     * @param parser Command handler for parsing
     * @param comp Competition to manage
     * @param commandHandler Command handler
     */
    public CompetitionCommand(ConfigParser parser, Competition comp, CommandHandler commandHandler) {
        this.parser = parser;
        this.competition = comp;
        this.commandHandler = commandHandler;
    }

    @Override
    public boolean execute(String[] args) {
        if (args.length < MIN_MONSTERS) {
            System.out.println(ERROR_USAGE);
            return false;
        }
        // Reset old competition
        competition.reset();
        List<Monster> allMonsters = parser.getParsedMonsters();
        List<Monster> selectedMonsters = new ArrayList<>();

        // Handle duplicate monster names with counters
        Map<String, Integer> nameCountMap = new HashMap<>();
        for (String name : args) {
            nameCountMap.put(name, nameCountMap.getOrDefault(name, INITIAL_COUNT) + COUNT_INCREMENT);
        }
        Map<String, Integer> nameCurrentCountMap = new HashMap<>();

        for (String name : args) { // Find monsters by name
            Monster found = findMonsterByName(name, allMonsters);
            if (found == null) {
                System.out.println(String.format(ERROR_UNKNOWN_MONSTER, name));
                return false;
            }
            nameCurrentCountMap.put(name, nameCurrentCountMap.getOrDefault(name, INITIAL_COUNT) + COUNT_INCREMENT);
            int currentCount = nameCurrentCountMap.get(name);
            String displayName = name;

            if (nameCountMap.get(name) > COUNT_INCREMENT) {
                displayName = String.format(NAME_COUNTER_FORMAT, name, currentCount);
            }

            // Clone the monster to avoid sharing state
            StatsConfig statsConfig = new StatsConfig(found.getBaseHp(), found.getBaseAtk(),
                    found.getBaseDef(), found.getBaseSpd(), found.getBasePrc(), found.getBaseAgl());

            Monster clone = new Monster(displayName, found.getElement(), statsConfig);
            // Add all actions from the original monster
            for (Action action : found.getKnownActions()) {
                clone.addAction(action);
            }
            selectedMonsters.add(clone);
        }
        // Add selected monsters to competition
        for (Monster mon : selectedMonsters) {
            competition.addMonster(mon);
        }
        System.out.println(String.format(MESSAGE_MONSTERS_ENTER, args.length));

        // Set the first monster as the one currently selecting an action
        if (!competition.getAllMonsters().isEmpty()) {
            Monster firstMonster = competition.getAllMonsters().get(FIRST_MONSTER_INDEX);
            commandHandler.setCurrentActionMonster(firstMonster);
            System.out.println();
            System.out.println(String.format(MESSAGE_WHAT_SHOULD_DO, firstMonster.getName()));
        }
        return true;
    }

    /**
     * Finds a monster by name in the list of available monsters.
     *
     * @param name The name of the monster to find
     * @param monsters The list of available monsters
     * @return The found monster, or null if not found
     */
    private Monster findMonsterByName(String name, List<Monster> monsters) {
        for (Monster monster : monsters) {
            if (monster.getName().equals(name)) {
                return monster;
            }
        }
        return null;
    }
}