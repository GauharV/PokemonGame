package com.pokemon.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Pokemon implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status { NONE, BURN, POISON, PARALYSIS, SLEEP, FREEZE }

    private int id;
    private String name;
    private PokemonType type1;
    private PokemonType type2;
    private int level;
    private int experience;

    // Base stats (used for stat calculation)
    private int baseHp, baseAtk, baseDef, baseSpAtk, baseSpDef, baseSpd;

    // Calculated stats
    private int maxHp, currentHp;
    private int attack, defense, spAttack, spDefense, speed;

    // Temporary battle stat modifiers (-6 to +6)
    private int atkMod, defMod, spAtkMod, spDefMod, spdMod, accMod;

    private List<Move> moves;
    private Status status;
    private int sleepTurns;
    private boolean isFainted;

    // Evolution data
    private int evolutionId;     // -1 = no evolution
    private int evolutionLevel;  // level at which it evolves

    public Pokemon(int id, String name, PokemonType type1, PokemonType type2,
                   int baseHp, int baseAtk, int baseDef,
                   int baseSpAtk, int baseSpDef, int baseSpd,
                   int level, int evolutionId, int evolutionLevel) {
        this.id = id;
        this.name = name;
        this.type1 = type1;
        this.type2 = type2;
        this.baseHp = baseHp;
        this.baseAtk = baseAtk;
        this.baseDef = baseDef;
        this.baseSpAtk = baseSpAtk;
        this.baseSpDef = baseSpDef;
        this.baseSpd = baseSpd;
        this.level = level;
        this.evolutionId = evolutionId;
        this.evolutionLevel = evolutionLevel;
        this.moves = new ArrayList<>();
        this.status = Status.NONE;
        this.isFainted = false;
        calculateStats();
        this.experience = xpForLevel(level);
    }

    // Calculate stats from base stats and level (simplified formula)
    public void calculateStats() {
        maxHp = (int)((2.0 * baseHp * level) / 100.0) + level + 10;
        attack    = calcStat(baseAtk);
        defense   = calcStat(baseDef);
        spAttack  = calcStat(baseSpAtk);
        spDefense = calcStat(baseSpDef);
        speed     = calcStat(baseSpd);
        if (currentHp <= 0) currentHp = maxHp; // init HP
    }

    private int calcStat(int base) {
        return (int)((2.0 * base * level) / 100.0) + 5;
    }

    // XP needed for a given level (medium-slow curve)
    public static int xpForLevel(int lvl) {
        return (int)(Math.pow(lvl, 3));
    }

    // Add XP and return true if leveled up
    public boolean gainXP(int amount) {
        experience += amount;
        boolean leveled = false;
        while (level < 100 && experience >= xpForLevel(level + 1)) {
            levelUp();
            leveled = true;
        }
        return leveled;
    }

    private void levelUp() {
        int oldMaxHp = maxHp;
        level++;
        calculateStats();
        // Restore HP proportional to the HP gained
        currentHp = Math.min(currentHp + (maxHp - oldMaxHp), maxHp);
        // Restore PP on level up
        for (Move m : moves) {
            if (m.getCurrentPP() < m.getMaxPP()) {
                // partial restore
            }
        }
    }

    // Should this pokemon evolve right now?
    public boolean shouldEvolve() {
        return evolutionId != -1 && level >= evolutionLevel;
    }

    // Reset battle modifiers (used when switching out or battle ends)
    public void resetBattleModifiers() {
        atkMod = defMod = spAtkMod = spDefMod = spdMod = accMod = 0;
    }

    // Get effective stat including battle modifier
    public int getEffectiveAttack() { return applyMod(attack, atkMod); }
    public int getEffectiveDefense() { return applyMod(defense, defMod); }
    public int getEffectiveSpAttack() { return applyMod(spAttack, spAtkMod); }
    public int getEffectiveSpDefense() { return applyMod(spDefense, spDefMod); }
    public int getEffectiveSpeed() {
        int spd = applyMod(speed, spdMod);
        if (status == Status.PARALYSIS) spd /= 2;
        return spd;
    }

    private int applyMod(int stat, int mod) {
        double[] stages = {0.25, 0.28, 0.33, 0.40, 0.50, 0.66, 1.0,
                           1.5, 2.0, 2.5, 3.0, 3.5, 4.0};
        return (int)(stat * stages[mod + 6]);
    }

    public void modifyAtk(int delta) { atkMod = Math.max(-6, Math.min(6, atkMod + delta)); }
    public void modifyDef(int delta) { defMod = Math.max(-6, Math.min(6, defMod + delta)); }
    public void modifySpAtk(int delta) { spAtkMod = Math.max(-6, Math.min(6, spAtkMod + delta)); }
    public void modifySpd(int delta) { spdMod = Math.max(-6, Math.min(6, spdMod + delta)); }

    public void takeDamage(int amount) {
        currentHp = Math.max(0, currentHp - amount);
        if (currentHp == 0) isFainted = true;
    }

    public void heal(int amount) {
        currentHp = Math.min(maxHp, currentHp + amount);
        isFainted = false;
    }

    public void fullHeal() {
        currentHp = maxHp;
        status = Status.NONE;
        sleepTurns = 0;
        isFainted = false;
        resetBattleModifiers();
        for (Move m : moves) m.restorePP();
    }

    public double getHpPercent() { return maxHp > 0 ? (double) currentHp / maxHp : 0; }
    public boolean isFainted() { return isFainted || currentHp <= 0; }

    // Status effect handling
    public void applyStatus(Status newStatus) {
        if (this.status == Status.NONE) {
            this.status = newStatus;
            if (newStatus == Status.SLEEP) sleepTurns = (int)(Math.random() * 3) + 1;
        }
    }

    public boolean canAct() {
        if (status == Status.SLEEP) {
            sleepTurns--;
            if (sleepTurns <= 0) { status = Status.NONE; return true; }
            return false;
        }
        if (status == Status.PARALYSIS) {
            return Math.random() > 0.25; // 25% chance to be fully paralyzed
        }
        if (status == Status.FREEZE) {
            if (Math.random() < 0.20) { status = Status.NONE; return true; } // 20% thaw
            return false;
        }
        return true;
    }

    public void applyEndOfTurnStatus() {
        if (status == Status.BURN) takeDamage(Math.max(1, maxHp / 16));
        if (status == Status.POISON) takeDamage(Math.max(1, maxHp / 8));
    }

    // Getters / Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PokemonType getType1() { return type1; }
    public void setType1(PokemonType t) { this.type1 = t; }
    public PokemonType getType2() { return type2; }
    public void setType2(PokemonType t) { this.type2 = t; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpAttack() { return spAttack; }
    public int getSpDefense() { return spDefense; }
    public int getSpeed() { return speed; }
    public List<Move> getMoves() { return moves; }
    public void addMove(Move m) { if (moves.size() < 4) moves.add(m); }
    public Status getStatus() { return status; }
    public int getEvolutionId() { return evolutionId; }
    public int getEvolutionLevel() { return evolutionLevel; }
    public int getBaseHp() { return baseHp; }
    public int getBaseAtk() { return baseAtk; }
    public int getBaseDef() { return baseDef; }
    public int getBaseSpAtk() { return baseSpAtk; }
    public int getBaseSpDef() { return baseSpDef; }
    public int getBaseSpd() { return baseSpd; }

    @Override
    public String toString() {
        return name + " Lv." + level + " [" + currentHp + "/" + maxHp + " HP]";
    }
}
