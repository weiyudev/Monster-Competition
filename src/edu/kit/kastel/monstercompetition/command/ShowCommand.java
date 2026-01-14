package edu.kit.kastel.monstercompetition.command;

import edu.kit.kastel.monstercompetition.model.effect.Action;
import edu.kit.kastel.monstercompetition.model.effect.Effect;
import edu.kit.kastel.monstercompetition.model.effect.DamageEffect;
import edu.kit.kastel.monstercompetition.model.effect.InflictStatusConditionEffect;
import edu.kit.kastel.monstercompetition.model.effect.InflictStatChangeEffect;
import edu.kit.kastel.monstercompetition.model.effect.ProtectEffect;
import edu.kit.kastel.monstercompetition.model.effect.HealEffect;
import edu.kit.kastel.monstercompetition.model.effect.ContinueEffect;
import edu.kit.kastel.monstercompetition.model.effect.RepeatEffect;

import edu.kit.kastel.monstercompetition.model.game.Competition;
import edu.kit.kastel.monstercompetition.model.monster.Monster;

import java.util.List;

/**
 * Command that shows various game information.
 * @author uozqc
 */
public class ShowCommand implements Command {
    // Command names
    private static final String ASK_WHAT_SHOULD_DO = "What should %s do?";
    private static final String MONSTERS_NAME = "monsters";
    private static final String ACTIONS_NAME = "actions";
    private static final String STATS_NAME = "stats";
    private static final String STATUS_NAME = "status";
    // Error messages
    private static final String NO_MONSTER_ERROR = "Error, no monster is currently selecting an action.";
    private static final String ERROR_UNKNOWN_COMMAND = "Error, unknown show command: %s";
    // Status constants
    private static final String STATUS_FAINTED = "FAINTED";
    private static final String STATUS_OK = "OK";
    // Display format constants
    private static final String MONSTER_INFO_FORMAT = "%s: ELEMENT %s, HP %d, ATK %d, DEF %d, SPD %d";
    private static final String ACTIONS_OF_FORMAT = "ACTIONS OF %s";
    private static final String ACTION_INFO_FORMAT = "%s: ELEMENT %s, Damage %s, HitRate %d";
    private static final String STATS_OF_FORMAT = "STATS OF %s";
    // Strength type constants
    private static final String STRENGTH_TYPE_BASE = "base";
    private static final String STRENGTH_TYPE_REL = "rel";
    private static final String STRENGTH_TYPE_ABS = "abs";
    private static final String DAMAGE_INFO_DEFAULT = "--";
    private static final String DAMAGE_PREFIX_BASE = "b";
    private static final String DAMAGE_PREFIX_REL = "r";
    private static final String DAMAGE_PREFIX_ABS = "a";
    // Health bar constants
    private static final int HEALTH_BAR_LENGTH = 20;
    private static final double HEALTH_BAR_LENGTH_DOUBLE = 20.0;
    private static final char HEALTH_BAR_FILLED = 'X';
    private static final char HEALTH_BAR_EMPTY = '_';
    private static final String HEALTH_BAR_START = "[";
    private static final String HEALTH_BAR_END = "]";
    // Stats display constants
    private static final String STATS_HP_FORMAT = "HP %d/%d";
    private static final String STATS_ATK_FORMAT = ", ATK %d";
    private static final String STATS_DEF_FORMAT = ", DEF %d";
    private static final String STATS_SPD_FORMAT = ", SPD %d";
    private static final String STATS_PRC_FORMAT = ", PRC 1";
    private static final String STATS_AGL_FORMAT = ", AGL 1";
    private static final String STATS_STAGE_FORMAT = "(%s%d)";
    private static final String STATS_STAGE_PLUS = "+";
    // Special characters and numeric constants
    private static final String SPACE = " ";
    private static final String EMPTY_STRING = "";
    private static final String STAR = "*";
    private static final String OPEN_PAREN = "(";
    private static final String CLOSE_PAREN = ")";
    private static final String NEWLINE = "";
    private static final int MIN_ARGS = 1;
    private static final int MAX_ARGS = 1;
    private static final int FIRST_ARG_INDEX = 0;
    private static final int FIRST_EFFECT_INDEX = 0;
    private static final int MONSTER_NUMBER_OFFSET = 1;
    private static final int DEFAULT_HIT_RATE = 0;
    private static final int INITIAL_STAGE = 0;
    private static final int LOOP_START = 0;

    private final Competition competition;
    private final CommandHandler commandHandler;

    /**
     * Creates a new ShowCommand.
     * @param comp The current competition
     * @param commandHandler The command handler
     */
    public ShowCommand(Competition comp, CommandHandler commandHandler) {
        this.competition = comp;
        this.commandHandler = commandHandler;
    }

    @Override
    public boolean execute(String[] args) {
        Monster currentActionMonster = commandHandler.getCurrentActionMonster();
        if (args.length < MIN_ARGS) { // Default to 'show competition status' if no arguments
            if (currentActionMonster != null) {
                showCompetitionStatus(); // If in a competition, show competition status
                // After showing monsters, repeat the action prompt if we're in action selection phase
                System.out.println();
                System.out.println(String.format(ASK_WHAT_SHOULD_DO, currentActionMonster.getName()));
                return true;
            } else {
                // No active competition or no monster selecting action
                System.out.println(NO_MONSTER_ERROR);
                return false;
            }
        } else if (args.length > MAX_ARGS) {
            System.out.println(String.format(ERROR_UNKNOWN_COMMAND, String.join(SPACE, args)));
            // Display action prompt if we're in action selection
            if (currentActionMonster != null) {
                System.out.println();
                System.out.println(String.format(ASK_WHAT_SHOULD_DO, currentActionMonster.getName()));
            }
            return false;
        }
        
        String subCommand = args[FIRST_ARG_INDEX].toLowerCase();
        return handleSubCommand(subCommand, currentActionMonster);
    }
    /**
     * Handles the specific show subcommand.
     * @param subCommand The subcommand (e.g., "monsters", "actions", "stats", "status")
     * @param currentActionMonster The current monster that's selecting an action, or null if none
     * @return true if the command was executed successfully, false otherwise
     */
    private boolean handleSubCommand(String subCommand, Monster currentActionMonster) {
        switch (subCommand) {
            case MONSTERS_NAME:
                // Always show all monsters in raw format when explicitly asked
                showAllMonsters();
                // After showing monsters, repeat the action prompt if we're in action selection phase
                if (currentActionMonster != null) {
                    System.out.println();
                    System.out.println(String.format(ASK_WHAT_SHOULD_DO, currentActionMonster.getName()));
                }
                return true;
            case ACTIONS_NAME:
                if (currentActionMonster == null) {
                    System.out.println(NO_MONSTER_ERROR);
                    return false;
                }
                showActions(currentActionMonster);
                System.out.println(); // After showing actions, repeat the action prompt
                System.out.println(String.format(ASK_WHAT_SHOULD_DO, currentActionMonster.getName()));
                return true;
            case STATS_NAME:
                if (currentActionMonster == null) {
                    System.out.println(NO_MONSTER_ERROR);
                    return false;
                }
                showStats(currentActionMonster);
                System.out.println();
                System.out.println(String.format(ASK_WHAT_SHOULD_DO, currentActionMonster.getName()));
                return true;
            case STATUS_NAME:
                if (currentActionMonster == null) {
                    System.out.println(NO_MONSTER_ERROR);
                    return false;
                }
                showCompetitionStatus();
                System.out.println();
                System.out.println(String.format(ASK_WHAT_SHOULD_DO, currentActionMonster.getName()));
                return true;
            default:
                System.out.println(String.format(ERROR_UNKNOWN_COMMAND, subCommand));
                if (currentActionMonster != null) {
                    System.out.println();
                    System.out.println(String.format(ASK_WHAT_SHOULD_DO, currentActionMonster.getName()));
                }
                return false;
        }
    }
    /**
     * Shows all available monsters from the configuration.
     */
    private void showAllMonsters() {
        // Get the original monsters from the config parser instead of the competition monsters
        List<Monster> originalMonsters = commandHandler.getParser().getParsedMonsters();
        for (Monster mon : originalMonsters) {
            String elementStr = mon.getElement().toString();
            System.out.println(String.format(MONSTER_INFO_FORMAT, 
                mon.getName(), elementStr, mon.getBaseHp(), mon.getBaseAtk(), mon.getBaseDef(), mon.getBaseSpd()));
        }
    }
    /**
     * Shows current status of all monsters in the competition.
     */
    private void showCompetitionStatus() {
        List<Monster> monsters = competition.getAllMonsters();
        Monster currentMonster = commandHandler.getCurrentActionMonster();

        for (int i = LOOP_START; i < monsters.size(); i++) {
            Monster m = monsters.get(i);
            StringBuilder healthBar = new StringBuilder(HEALTH_BAR_START);
            if (m.isFainted()) {
                // For fainted monsters, show empty health bar (all underscores)
                for (int j = LOOP_START; j < HEALTH_BAR_LENGTH; j++) {
                    healthBar.append(HEALTH_BAR_EMPTY);
                }
            } else {
                // For alive monsters, show health bar based on current HP ratio
                int healthRatio = (int) Math.ceil(HEALTH_BAR_LENGTH_DOUBLE * m.getCurrentHp() / m.getBaseHp());
                for (int j = LOOP_START; j < HEALTH_BAR_LENGTH; j++) {
                    healthBar.append(j < healthRatio ? HEALTH_BAR_FILLED : HEALTH_BAR_EMPTY);
                }
            }
            healthBar.append(HEALTH_BAR_END);

            // Monster number, name and condition
            StringBuilder monsterInfo = new StringBuilder();
            monsterInfo.append(SPACE).append(i + MONSTER_NUMBER_OFFSET).append(SPACE);

            // Add star if this is the current monster
            if (m == currentMonster) {
                monsterInfo.append(STAR);
            }

            monsterInfo.append(m.getName()).append(SPACE).append(OPEN_PAREN);
            // Condition display
            if (m.isFainted()) {
                monsterInfo.append(STATUS_FAINTED);
            } else if (m.getStatusCondition() == null) {
                monsterInfo.append(STATUS_OK);
            } else {
                monsterInfo.append(m.getStatusCondition().getName());
            }
            monsterInfo.append(CLOSE_PAREN);

            System.out.println(healthBar + monsterInfo.toString());
        }
    }

    /**
     * Shows all actions of the specified monster.
     *
     * @param monster The monster whose actions to show
     */
    private void showActions(Monster monster) {
        System.out.println(String.format(ACTIONS_OF_FORMAT, monster.getName()));

        for (Action action : monster.getKnownActions()) {
            String elementStr = action.getElement().toString();
            String damageInfo = DAMAGE_INFO_DEFAULT;
            int hitRate = DEFAULT_HIT_RATE;
            List<Effect> effects = action.getEffects();
            if (!effects.isEmpty()) {
                Effect firstEffect = effects.get(FIRST_EFFECT_INDEX);
                // If the first effect is a ContinueEffect, use its hit rate
                // This takes priority over any subsequent damage effect hit rates
                if (firstEffect instanceof ContinueEffect) {
                    hitRate = ((ContinueEffect) firstEffect).getHitRate();
                } else {
                    hitRate = getEffectHitRate(firstEffect);
                }
                // Look for the first damage effect to display, including inside RepeatEffect
                for (Effect effect : effects) {
                    // First check if this is a damage effect
                    if (effect instanceof DamageEffect dmgEffect) {
                        // Format based on strength type
                        String strengthType = dmgEffect.getStrengthType();
                        if (STRENGTH_TYPE_BASE.equals(strengthType)) {
                            damageInfo = DAMAGE_PREFIX_BASE + dmgEffect.getStrengthValue();
                        } else if (STRENGTH_TYPE_REL.equals(strengthType)) {
                            damageInfo = DAMAGE_PREFIX_REL + dmgEffect.getStrengthValue();
                        } else if (STRENGTH_TYPE_ABS.equals(strengthType)) {
                            damageInfo = DAMAGE_PREFIX_ABS + dmgEffect.getStrengthValue();
                        }
                        if (!(firstEffect instanceof ContinueEffect)) { // Only update hit rate if the first effect wasn't a ContinueEffect
                            hitRate = dmgEffect.getHitRate();
                        }
                        break;
                    } else if (effect instanceof RepeatEffect) { // Then check if it's a RepeatEffect with nested DamageEffect
                        // Need to get the first DamageEffect from the RepeatEffect's subEffects
                        DamageEffect nestedDamageEffect = findNestedDamageEffect(effect);
                        if (nestedDamageEffect != null) {
                            // Format based on strength type
                            String strengthType = nestedDamageEffect.getStrengthType();
                            if (STRENGTH_TYPE_BASE.equals(strengthType)) {
                                damageInfo = DAMAGE_PREFIX_BASE + nestedDamageEffect.getStrengthValue();
                            } else if (STRENGTH_TYPE_REL.equals(strengthType)) {
                                damageInfo = DAMAGE_PREFIX_REL + nestedDamageEffect.getStrengthValue();
                            } else if (STRENGTH_TYPE_ABS.equals(strengthType)) {
                                damageInfo = DAMAGE_PREFIX_ABS + nestedDamageEffect.getStrengthValue();
                            }
                            // Only update hit rate if the first effect wasn't a ContinueEffect
                            if (!(firstEffect instanceof ContinueEffect)) {
                                hitRate = nestedDamageEffect.getHitRate();
                            }
                            break;
                        }
                    }
                }
            }
            System.out.println(String.format(ACTION_INFO_FORMAT, 
                action.getName(), elementStr, damageInfo, hitRate));
        }
    }

    /**
     * Finds the first DamageEffect nested inside a RepeatEffect.
     *
     * @param effect The effect to check (expected to be a RepeatEffect)
     * @return The first DamageEffect found, or null if none
     */
    private DamageEffect findNestedDamageEffect(Effect effect) {
        if (effect instanceof RepeatEffect repeatEffect) {
            // Use the getter method to access subEffects
            List<Effect> subEffects = repeatEffect.getSubEffects();
            
            // Find the first DamageEffect in the subEffects
            for (Effect subEffect : subEffects) {
                if (subEffect instanceof DamageEffect) {
                    return (DamageEffect) subEffect;
                }
            }
        }
        return null;
    }

    /**
     * Gets the hit rate from an effect if it's a damage effect.
     *
     * @param effect The effect to check
     * @return The hit rate
     */
    private int getEffectHitRate(Effect effect) {
        if (effect instanceof DamageEffect) {
            return ((DamageEffect) effect).getHitRate();
        } else if (effect instanceof InflictStatChangeEffect) {
            return ((InflictStatChangeEffect) effect).getHitRate();
        } else if (effect instanceof ProtectEffect) {
            return ((ProtectEffect) effect).getHitRate();
        } else if (effect instanceof HealEffect) {
            return ((HealEffect) effect).getHitRate();
        } else if (effect instanceof InflictStatusConditionEffect) {
            return ((InflictStatusConditionEffect) effect).getHitRate();
        } else if (effect instanceof ContinueEffect) {
            return ((ContinueEffect) effect).getHitRate();
        } else if (effect instanceof RepeatEffect) {
            // For RepeatEffect, try to find a nested damage effect and get its hit rate
            DamageEffect nestedDamage = findNestedDamageEffect(effect);
            if (nestedDamage != null) {
                return nestedDamage.getHitRate();
            }
        }
        // Default fallback
        return DEFAULT_HIT_RATE;
    }

    /**
     * Shows current stats of the specified monster.
     *
     * @param monster The monster whose stats to show
     */
    private void showStats(Monster monster) {
        System.out.println(String.format(STATS_OF_FORMAT, monster.getName()));

        StringBuilder sb = new StringBuilder();
        // HP current/max
        sb.append(String.format(STATS_HP_FORMAT, monster.getCurrentHp(), monster.getBaseHp()));

        // ATK with change if any
        sb.append(String.format(STATS_ATK_FORMAT, monster.getBaseAtk()));
        if (monster.getAtkStage() != INITIAL_STAGE) {
            sb.append(String.format(STATS_STAGE_FORMAT, 
                monster.getAtkStage() > INITIAL_STAGE ? STATS_STAGE_PLUS : EMPTY_STRING, monster.getAtkStage()));
        }

        // DEF with change if any
        sb.append(String.format(STATS_DEF_FORMAT, monster.getBaseDef()));
        if (monster.getDefStage() != INITIAL_STAGE) {
            sb.append(String.format(STATS_STAGE_FORMAT, 
                monster.getDefStage() > INITIAL_STAGE ? STATS_STAGE_PLUS : EMPTY_STRING, monster.getDefStage()));
        }

        // SPD with change if any
        sb.append(String.format(STATS_SPD_FORMAT, monster.getBaseSpd()));
        if (monster.getSpdStage() != INITIAL_STAGE) {
            sb.append(String.format(STATS_STAGE_FORMAT, 
                monster.getSpdStage() > INITIAL_STAGE ? STATS_STAGE_PLUS : EMPTY_STRING, monster.getSpdStage()));
        }

        // PRC with change if any
        sb.append(STATS_PRC_FORMAT);
        if (monster.getPrcStage() != INITIAL_STAGE) {
            sb.append(String.format(STATS_STAGE_FORMAT, 
                monster.getPrcStage() > INITIAL_STAGE ? STATS_STAGE_PLUS : EMPTY_STRING, monster.getPrcStage()));
        }

        // AGL with change if any
        sb.append(STATS_AGL_FORMAT);
        if (monster.getAglStage() != INITIAL_STAGE) {
            sb.append(String.format(STATS_STAGE_FORMAT, 
                monster.getAglStage() > INITIAL_STAGE ? STATS_STAGE_PLUS : EMPTY_STRING, monster.getAglStage()));
        }

        System.out.println(sb);
    }
}