package edu.kit.kastel.monstercompetition.model.state.condition;

import edu.kit.kastel.monstercompetition.model.monster.Monster;

/**
 * Represents the standard status conditions monsters can have.
 * Assignment section A.2.5 defines these conditions and their effects.
 *
 * @author uozqc
 */
public enum StatusCondition implements Condition {
    /**
     * WET condition: reduces DEF by 25%.
     */
    WET {
        @Override
        public double applyToStat(String statName, double statValue) {
            if (STAT_DEF.equals(statName)) {
                return statValue * STAT_REDUCTION_FACTOR; // 25% reduction
            }
            return statValue;
        }

        @Override
        public double modifyDef(double currentDef) {
            return currentDef * STAT_REDUCTION_FACTOR;
        }

        @Override
        public String getName() {
            return CONDITION_WET;
        }

        @Override
        public String getStartMessage(String monsterName) {
            return String.format(MSG_WET_START, monsterName);
        }

        @Override
        public String getOngoingMessage(String monsterName) {
            return String.format(MSG_WET_ONGOING, monsterName);
        }

        @Override
        public String getEndMessage(String monsterName) {
            return String.format(MSG_WET_END, monsterName);
        }
    },

    /**
     * BURN condition: reduces ATK by 25% and causes 10% max HP damage per turn.
     */
    BURN {
        @Override
        public double applyToStat(String statName, double statValue) {
            if (STAT_ATK.equals(statName)) {
                return statValue * STAT_REDUCTION_FACTOR; // 25% reduction
            }
            return statValue;
        }

        @Override
        public double modifyAtk(double currentAtk) {
            return currentAtk * STAT_REDUCTION_FACTOR;
        }

        @Override
        public void onActionEnd(Monster monster) {
            int burnDamage = (int) Math.ceil(monster.getBaseHp() * BURN_DAMAGE_PERCENT);
            monster.takeDamage(burnDamage);
        }

        @Override
        public String getName() {
            return CONDITION_BURN;
        }

        @Override
        public String getStartMessage(String monsterName) {
            return String.format(MSG_BURN_START, monsterName);
        }

        @Override
        public String getOngoingMessage(String monsterName) {
            return String.format(MSG_BURN_ONGOING, monsterName);
        }

        @Override
        public String getEndMessage(String monsterName) {
            return String.format(MSG_BURN_END, monsterName);
        }
    },

    /**
     * QUICKSAND condition: reduces SPD by 25%.
     */
    QUICKSAND {
        @Override
        public double applyToStat(String statName, double statValue) {
            if (STAT_SPD.equals(statName)) {
                return statValue * STAT_REDUCTION_FACTOR; // 25% reduction
            }
            return statValue;
        }

        @Override
        public double modifySpd(double currentSpd) {
            return currentSpd * STAT_REDUCTION_FACTOR;
        }

        @Override
        public String getName() {
            return CONDITION_QUICKSAND;
        }

        @Override
        public String getStartMessage(String monsterName) {
            return String.format(MSG_QUICKSAND_START, monsterName);
        }

        @Override
        public String getOngoingMessage(String monsterName) {
            return String.format(MSG_QUICKSAND_ONGOING, monsterName);
        }

        @Override
        public String getEndMessage(String monsterName) {
            return String.format(MSG_QUICKSAND_END, monsterName);
        }
    },

    /**
     * SLEEP condition: prevents the monster from using actions.
     */
    SLEEP {
        @Override
        public double applyToStat(String statName, double statValue) {
            // SLEEP doesn't directly affect stats
            return statValue;
        }

        @Override
        public boolean canAct() {
            // Monster is sleeping => cannot perform actions
            return false;
        }

        @Override
        public String getName() {
            return CONDITION_SLEEP;
        }

        @Override
        public String getStartMessage(String monsterName) {
            return String.format(MSG_SLEEP_START, monsterName);
        }

        @Override
        public String getOngoingMessage(String monsterName) {
            return String.format(MSG_SLEEP_ONGOING, monsterName);
        }

        @Override
        public String getEndMessage(String monsterName) {
            return String.format(MSG_SLEEP_END, monsterName);
        }
    };

    // Stat name constants
    private static final String STAT_DEF = "DEF";
    private static final String STAT_ATK = "ATK";
    private static final String STAT_SPD = "SPD";

    // Condition name constants
    private static final String CONDITION_WET = "WET";
    private static final String CONDITION_BURN = "BURN";
    private static final String CONDITION_QUICKSAND = "QUICKSAND";
    private static final String CONDITION_SLEEP = "SLEEP";

    // Stat reduction factor constants
    private static final double STAT_REDUCTION_FACTOR = 0.75; // 25% reduction

    // Burn damage constant
    private static final double BURN_DAMAGE_PERCENT = 0.1; // 10% of max HP

    // Message constants
    private static final String MSG_WET_START = "%s becomes soaking wet!";
    private static final String MSG_WET_ONGOING = "%s is soaking wet!";
    private static final String MSG_WET_END = "%s dried up!";

    private static final String MSG_BURN_START = "%s caught on fire!";
    private static final String MSG_BURN_ONGOING = "%s is burning!";
    private static final String MSG_BURN_END = "%s's burning has faded!";

    private static final String MSG_QUICKSAND_START = "%s gets caught by quicksand!";
    private static final String MSG_QUICKSAND_ONGOING = "%s is caught in quicksand!";
    private static final String MSG_QUICKSAND_END = "%s escaped the quicksand!";

    private static final String MSG_SLEEP_START = "%s falls asleep!";
    private static final String MSG_SLEEP_ONGOING = "%s is asleep!";
    private static final String MSG_SLEEP_END = "%s woke up!";


    /**
     * Used to determine whether the element type is valid.
     * @param name The given name
     * @return Tells true or false
     */
    public static boolean isValidStatusCondition(String name) {
        for (StatusCondition condition : StatusCondition.values()) {
            if (condition.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
}