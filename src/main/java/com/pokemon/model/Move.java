package com.pokemon.model;

import java.io.Serializable;

public class Move implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Category { PHYSICAL, SPECIAL, STATUS }
    public enum Effect { NONE, BURN, POISON, PARALYZE, SLEEP, FREEZE, CONFUSE,
                         LOWER_ATK, LOWER_DEF, LOWER_SPD, RAISE_ATK, RAISE_DEF,
                         RAISE_SP_ATK, FLINCH, DRAIN, RECOIL }

    private String name;
    private PokemonType type;
    private Category category;
    private int power;         // 0 for status moves
    private int accuracy;      // 0-100, 0 = never misses
    private int maxPP;
    private int currentPP;
    private Effect effect;
    private int effectChance;  // 0-100
    private String description;

    public Move(String name, PokemonType type, Category category,
                int power, int accuracy, int pp, String description) {
        this(name, type, category, power, accuracy, pp, Effect.NONE, 0, description);
    }

    public Move(String name, PokemonType type, Category category,
                int power, int accuracy, int pp,
                Effect effect, int effectChance, String description) {
        this.name = name;
        this.type = type;
        this.category = category;
        this.power = power;
        this.accuracy = accuracy;
        this.maxPP = pp;
        this.currentPP = pp;
        this.effect = effect;
        this.effectChance = effectChance;
        this.description = description;
    }

    // Copy constructor (for creating fresh copies)
    public Move(Move other) {
        this.name = other.name;
        this.type = other.type;
        this.category = other.category;
        this.power = other.power;
        this.accuracy = other.accuracy;
        this.maxPP = other.maxPP;
        this.currentPP = other.currentPP;
        this.effect = other.effect;
        this.effectChance = other.effectChance;
        this.description = other.description;
    }

    public boolean isUsable() { return currentPP > 0; }
    public void usePP() { if (currentPP > 0) currentPP--; }
    public void restorePP() { currentPP = maxPP; }

    // Getters
    public String getName() { return name; }
    public PokemonType getType() { return type; }
    public Category getCategory() { return category; }
    public int getPower() { return power; }
    public int getAccuracy() { return accuracy; }
    public int getMaxPP() { return maxPP; }
    public int getCurrentPP() { return currentPP; }
    public Effect getEffect() { return effect; }
    public int getEffectChance() { return effectChance; }
    public String getDescription() { return description; }

    @Override
    public String toString() { return name + " (" + currentPP + "/" + maxPP + " PP)"; }
}
