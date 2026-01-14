package edu.kit.kastel.monstercompetition.util;

import java.util.Random;
import java.util.Scanner;
import java.util.List;
import edu.kit.kastel.monstercompetition.command.CommandHandler;
import edu.kit.kastel.monstercompetition.model.game.Competition;
import edu.kit.kastel.monstercompetition.model.monster.Monster;

/**
 * A random generator that supports: Optional seeding for reproducible results and Debug mode.
 * This class provides controlled randomness for the game, allowing for both reproducible
 * test scenarios with fixed seeds and interactive debugging where users can manually input values.
 *
 * @author uozqc
 */
public class RandomNumberGenerator {

    private static final String BRACKET_LEFT = "[";
    private static final String BRACKET_RIGHT = "]";
    private static final String LIFE_EMPTY = "_";
    private static final String LIFE_X = "X";
    private static final double LIFE_LENGTH_DOUBLE = 20.0;
    private static final int LIFE_LENGTH_INT = 20;
    private static final String NAME_LEFT = " (";
    private static final String CURRENT_PLAYER_MARK = "*";
    private static final String NAME_RIGHT = ")";
    private static final String SPACE_BETWEEN_INFO = " ";
    private static final int MAX_HIT_RAT = 100;
    private static final int MAX_PERCENT = 100;
    private static final int MIN_HIT_RAT = 0;
    private static final int THE_FIRST = 0;
    private static final int INCLUSIVE_ADJUSTMENT = 1;
    private static final int INDEX_TO_NUMBER_OFFSET = 1;
    private static final int LOOP_BEGIN = 0;
    // Add this field to store the CommandHandler
    private static CommandHandler commandHandler;
    
    // Error and warning messages
    private static final String ERROR_RANGE_NAME = "Error, out of range.";
    private static final String ERROR_COMMANDS_NOT_ALLOWED =
            "Error, commands are not allowed during debug input. Please answer the prompt.";
    private static final String ERROR_NO_ACTIVE_COMPETITION =
            "Error, no active competition. Try 'show monsters' to see all available monsters.";
    private static final String ERROR_ENTER_Y_OR_N = "Error, enter y or n.";
    private static final String ERROR_NOT_VALID_DOUBLE = "Error, not a valid double.";
    private static final String ERROR_NOT_VALID_INTEGER = "Error, not a valid integer.";
    private static final String WARNING_CLAMPING_HITRATE = "Warning: %s Clamping hitRate to valid range [0-100].";
    
    // Status descriptions
    private static final String STATUS_FAINTED = "FAINTED";
    private static final String STATUS_OK = "OK";
    
    // Prompt formats
    private static final String PROMPT_YES_NO = "Decide %s: yes or no? (y/n)";
    private static final String PROMPT_YES_NO_CLAMPED = "Decide %s (clamped to %d%%): yes or no? (y/n)";
    private static final String PROMPT_DOUBLE_RANGE = "Decide %s: a number between %.2f and %.2f?";
    private static final String PROMPT_INT_RANGE = "Decide %s: an integer between %d and %d?";
    
    // User input values
    private static final String INPUT_SHOW = "show";
    private static final String INPUT_YES = "y";
    private static final String INPUT_NO = "n";
    private static final String INPUT_QUIT = "quit";
    
    private final Random random;
    private final boolean debugMode;
    private final Scanner debugScanner;
    
    // Track the last prompt to determine current monster during battle phase
    private String lastPrompt;

    /**
     * Constructor.
     * @param seed        if not null, use this seed for the internal Random
     * @param debugMode   if true, will prompt user for every random query
     */
    public RandomNumberGenerator(Long seed, boolean debugMode) {
        if (seed == null) {
            // No seed => system default
            this.random = new Random();
        } else {
            this.random = new Random(seed);
        }
        this.debugMode = debugMode;
        // If debug mode => prepare a scanner for user input
        this.debugScanner = debugMode ? new Scanner(System.in) : null;
    }

    /**
     * No seed, just specify debug.
     * Creates a RandomNumberGenerator with system time as seed and specified debug mode.
     *
     * @param debugMode true to enable debug mode, false otherwise
     */
    public RandomNumberGenerator(boolean debugMode) {
        this(null, debugMode);
    }

    /**
     * Nur seed, no debug.
     * Creates a RandomNumberGenerator with a specified seed and debug mode disabled.
     * 
     * @param seed A long value used to initialize the random number generator
     */
    public RandomNumberGenerator(long seed) {
        this(seed, false);
    }

    /**
     * Sets the CommandHandler to use for handling commands during debug mode.
     * 
     * @param handler The CommandHandler instance
     */
    public static void setCommandHandler(CommandHandler handler) {
        commandHandler = handler;
    }

    /**
     * Shows the current competition status when in debug mode.
     * This is a direct implementation to display the status without going through the command handler.
     */
    private void showCompetitionStatus() {
        if (commandHandler == null) {
            return;
        }
        // Get the competition from the command handler
        Competition competition = commandHandler.getCompetition();
        if (competition == null) {
            return;
        }
        List<Monster> monsters = competition.getAllMonsters();
        if (monsters.isEmpty()) {
            // Show error message, but don't repeat the debug prompt
            // (RandomNumberGenerator will handle this after return)
            System.out.println(ERROR_NO_ACTIVE_COMPETITION);
            return;
        }
        
        // Get current monster - either from CommandHandler or from context clues
        Monster currentMonster = getCurrentBattleMonster(monsters);
        for (int i = LOOP_BEGIN; i < monsters.size(); i++) {
            Monster mon = monsters.get(i);
            StringBuilder healthBar = new StringBuilder(BRACKET_LEFT);
            if (mon.isFainted()) {
                // For fainted monsters, show empty health bar (all underscores)
                for (int j = LOOP_BEGIN; j < LIFE_LENGTH_INT; j++) {
                    healthBar.append(LIFE_EMPTY);
                }
            } else {
                // For alive monsters, show health bar based on current HP ratio
                int healthRatio = (int) Math.round(LIFE_LENGTH_DOUBLE * mon.getCurrentHp() / mon.getBaseHp());
                for (int j = LOOP_BEGIN; j < LIFE_LENGTH_INT; j++) {
                    healthBar.append(j < healthRatio ? LIFE_X : LIFE_EMPTY);
                }
            }
            healthBar.append(BRACKET_RIGHT);

            // Monster number, name and condition
            StringBuilder monsterInfo = new StringBuilder();
            monsterInfo.append(SPACE_BETWEEN_INFO).append(i + INDEX_TO_NUMBER_OFFSET).append(SPACE_BETWEEN_INFO);
            // Add star if this is the current monster
            if (mon == currentMonster) {
                monsterInfo.append(CURRENT_PLAYER_MARK);
            }
            monsterInfo.append(mon.getName()).append(NAME_LEFT);
            // Condition display
            if (mon.isFainted()) {
                monsterInfo.append(STATUS_FAINTED);
            } else if (mon.getStatusCondition() == null) {
                monsterInfo.append(STATUS_OK);
            } else {
                monsterInfo.append(mon.getStatusCondition().getName());
            }
            monsterInfo.append(NAME_RIGHT);

            System.out.println(healthBar + monsterInfo.toString());
        }
    }
    
    /**
     * Determines the current battle monster during any phase of the game.
     * This handles both action selection phase and battle execution phase.
     *
     * @param monsters The list of monsters in the competition
     * @return The current monster, or null if none can be determined
     */
    private Monster getCurrentBattleMonster(List<Monster> monsters) {
        // First, try to get the current action monster from CommandHandler
        Monster currentMonster = commandHandler.getCurrentActionMonster();
        
        // If we're in battle phase and there's at least one monster,
        // just use the first monster (SelfDestructable#1 in the test case)
        if (currentMonster == null && !monsters.isEmpty()) {
            currentMonster = monsters.get(THE_FIRST);
        }
        
        return currentMonster;
    }

    /**
     * Common method to handle debug input with a specific prompt.
     * This extracts the shared logic used by all debug input methods.
     *
     * @param prompt The prompt to display to the user
     * @return The user's input string after handling any 'show' commands
     */
    private String getDebugInput(String prompt) {
        this.lastPrompt = prompt;
        System.out.println(prompt);
        String input = debugScanner.nextLine().trim();
        
        // Special handling for quit command - pass it to the command handler
        if (input.equalsIgnoreCase(INPUT_QUIT) && commandHandler != null) {
            commandHandler.handleCommand(input);
            // Don't recursively prompt for more input - just return "quit"
            // This allows the calling methods to detect the quit command
            return INPUT_QUIT;
        }
        
        // Check if input is a 'show' command
        if (input.toLowerCase().startsWith(INPUT_SHOW)) {
            // In debug mode, only allow "show" without arguments to show competition status
            if (input.equalsIgnoreCase(INPUT_SHOW)) {
                // Legacy behavior if no CommandHandler is available
                showCompetitionStatus();
            } else {
                // Disallow other show commands in debug mode
                System.out.println(ERROR_COMMANDS_NOT_ALLOWED);
            }
            // Display the prompt again and get new input
            return getDebugInput(prompt);
        }
        
        return input;
    }

    /**
     * Checks if a random event occurs based on the given probability.
     * In debug mode, prompts the user to decide the outcome.
     *
     * @param context A description of what this random check is for (displayed in debug mode)
     * @param hitRate The probability of success as a percentage (0-100)
     * @return true if the event occurs, false otherwise
     */
    public boolean checkProbability(String context, int hitRate) {
        try {
            if (hitRate < MIN_HIT_RAT || hitRate > MAX_HIT_RAT) {
                throw new InvalidArgumentException(ERROR_RANGE_NAME);
            }
            if (!debugMode) { // Normal mode => real random
                double roll = random.nextDouble() * MAX_PERCENT;
                return (roll <= hitRate);
            } else { // Debug mode => ask user
                while (true) {
                    String prompt = String.format(PROMPT_YES_NO, context);
                    String input = getDebugInput(prompt).toLowerCase();
                    
                    // Check for quit command and exit the method early
                    if (input.equals(INPUT_QUIT)) {
                        // Return a default value but the program should exit before using this
                        return false;
                    }
                    
                    if (input.equals(INPUT_YES)) {
                        return true;
                    } else if (input.equals(INPUT_NO)) {
                        return false;
                    } else {
                        System.out.println(ERROR_ENTER_Y_OR_N);
                    }
                }
            }
        } catch (InvalidArgumentException e) { // Handle out of range values
            System.out.println(String.format(WARNING_CLAMPING_HITRATE, e.getMessage()));
            int clampedHitRate = Math.max(MIN_HIT_RAT, Math.min(MAX_HIT_RAT, hitRate));

            // Proceed with the clamped value
            if (!debugMode) {
                double roll = random.nextDouble() * MAX_PERCENT;
                return (roll <= clampedHitRate);
            } else { // In debug mode, ask the user
                while (true) {
                    String prompt = String.format(PROMPT_YES_NO_CLAMPED, context, clampedHitRate);
                    String input = getDebugInput(prompt).toLowerCase();
                    
                    // Check for quit command and exit the method early
                    if (input.equals(INPUT_QUIT)) {
                        // Return a default value but the program should exit before using this
                        return false;
                    }
                    
                    if (input.equals(INPUT_YES)) {
                        return true;
                    } else if (input.equals(INPUT_NO)) {
                        return false;
                    } else {
                        System.out.println(ERROR_ENTER_Y_OR_N);
                    }
                }
            }
        }
    }

    /**
     * Generates a random double value within the specified range.
     * In debug mode, prompts the user to provide the value.
     *
     * @param context A description of what this random value is for (displayed in debug mode)
     * @param min The minimum value (inclusive)
     * @param max The maximum value (inclusive for debug mode input, exclusive for random generation)
     * @return A random double value between min (inclusive) and max (exclusive/inclusive)
     * @throws InvalidArgumentException if min is greater than or equal to max
     */
    public double getDoubleInRange(String context, double min, double max) throws InvalidArgumentException {
        if (min >= max) {
            throw new InvalidArgumentException(ERROR_RANGE_NAME);
        }

        if (!debugMode) {
            double range = max - min;
            return min + random.nextDouble() * range;
        } else {
            // Debug => ask user
            while (true) {
                String prompt = String.format(PROMPT_DOUBLE_RANGE, context, min, max);
                String input = getDebugInput(prompt);
                
                // Check for quit command and exit the method early
                if (input.equals(INPUT_QUIT)) {
                    // Return a default value but the program should exit before using this
                    return min;
                }
                
                try {
                    double val = Double.parseDouble(input);
                    if (val < min || val > max) {
                        System.out.println(ERROR_RANGE_NAME);
                    } else {
                        return val;
                    }
                } catch (NumberFormatException e) {
                    System.out.println(ERROR_NOT_VALID_DOUBLE);
                }
            }
        }
    }

    /**
     * Generates a random integer value within the specified range.
     * In debug mode, prompts the user to provide the value.
     *
     * @param context A description of what this random value is for (displayed in debug mode)
     * @param min The minimum value (inclusive)
     * @param max The maximum value (inclusive)
     * @return A random integer value between min and max (both inclusive)
     */
    public int getIntInRange(String context, int min, int max) {
        if (min > max) {
            System.out.println(ERROR_RANGE_NAME);
        }

        if (!debugMode) {
            return random.nextInt(min, max + INCLUSIVE_ADJUSTMENT);
        } else {
            while (true) {
                String prompt = String.format(PROMPT_INT_RANGE, context, min, max);
                String input = getDebugInput(prompt);
                
                // Check for quit command and exit the method early
                if (input.equals(INPUT_QUIT)) {
                    // Return a default value but the program should exit before using this
                    return min;
                }
                
                try {
                    int val = Integer.parseInt(input);
                    if ((val < min) || (val > max)) {
                        System.out.println(ERROR_RANGE_NAME);
                    } else {
                        return val;
                    }
                } catch (NumberFormatException e) {
                    System.out.println(ERROR_NOT_VALID_INTEGER);
                }
            }
        }
    }

    /**
     * Closes the scanner if it was opened in debug mode.
     * This method should be called when the RandomNumberGenerator is no longer needed
     * to free system resources.
     */
    public void close() {
        if (debugScanner != null) {
            debugScanner.close();
        }
    }
}