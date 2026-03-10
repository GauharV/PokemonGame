package com.pokemon.model;

public enum PokemonType {
    NORMAL, FIRE, WATER, GRASS, ELECTRIC, ICE, FIGHTING, POISON,
    GROUND, FLYING, PSYCHIC, BUG, ROCK, GHOST, DRAGON, DARK, STEEL, NONE;

    // Returns multiplier when THIS type attacks the DEFENDER type
    public static double getEffectiveness(PokemonType attacker, PokemonType defender) {
        if (attacker == NONE || defender == NONE) return 1.0;
        return switch (attacker) {
            case NORMAL   -> switch (defender) { case ROCK, STEEL -> 0.5; case GHOST -> 0.0; default -> 1.0; };
            case FIRE     -> switch (defender) { case FIRE,WATER,ROCK,DRAGON -> 0.5; case GRASS,ICE,BUG,STEEL -> 2.0; default -> 1.0; };
            case WATER    -> switch (defender) { case WATER,GRASS,DRAGON -> 0.5; case FIRE,GROUND,ROCK -> 2.0; default -> 1.0; };
            case GRASS    -> switch (defender) { case FIRE,GRASS,POISON,FLYING,BUG,DRAGON,STEEL -> 0.5; case WATER,GROUND,ROCK -> 2.0; default -> 1.0; };
            case ELECTRIC -> switch (defender) { case GRASS,ELECTRIC,DRAGON -> 0.5; case GROUND -> 0.0; case WATER,FLYING -> 2.0; default -> 1.0; };
            case ICE      -> switch (defender) { case WATER,ICE -> 0.5; case STEEL -> 0.5; case GRASS,GROUND,FLYING,DRAGON -> 2.0; default -> 1.0; };
            case FIGHTING -> switch (defender) { case POISON,FLYING,PSYCHIC,BUG -> 0.5; case GHOST -> 0.0; case NORMAL,ICE,ROCK,DARK,STEEL -> 2.0; default -> 1.0; };
            case POISON   -> switch (defender) { case POISON,GROUND,ROCK,GHOST -> 0.5; case STEEL -> 0.0; case GRASS -> 2.0; default -> 1.0; };
            case GROUND   -> switch (defender) { case GRASS,BUG -> 0.5; case FLYING -> 0.0; case FIRE,ELECTRIC,POISON,ROCK,STEEL -> 2.0; default -> 1.0; };
            case FLYING   -> switch (defender) { case ELECTRIC,ROCK,STEEL -> 0.5; case GRASS,FIGHTING,BUG -> 2.0; default -> 1.0; };
            case PSYCHIC  -> switch (defender) { case PSYCHIC,STEEL -> 0.5; case DARK -> 0.0; case FIGHTING,POISON -> 2.0; default -> 1.0; };
            case BUG      -> switch (defender) { case FIRE,FIGHTING,FLYING,GHOST,STEEL -> 0.5; case GRASS,PSYCHIC,DARK -> 2.0; default -> 1.0; };
            case ROCK     -> switch (defender) { case FIGHTING,GROUND,STEEL -> 0.5; case FIRE,ICE,FLYING,BUG -> 2.0; default -> 1.0; };
            case GHOST    -> switch (defender) { case NORMAL -> 0.0; case DARK -> 0.5; case GHOST,PSYCHIC -> 2.0; default -> 1.0; };
            case DRAGON   -> switch (defender) { case STEEL -> 0.5; case DRAGON -> 2.0; default -> 1.0; };
            case DARK     -> switch (defender) { case FIGHTING,DARK,STEEL -> 0.5; case PSYCHIC,GHOST -> 2.0; default -> 1.0; };
            case STEEL    -> switch (defender) { case FIRE,WATER,ELECTRIC,STEEL -> 0.5; case ICE,ROCK -> 2.0; default -> 1.0; };
            default -> 1.0;
        };
    }

    public String getColor() {
        return switch (this) {
            case NORMAL   -> "#A8A878";
            case FIRE     -> "#F08030";
            case WATER    -> "#6890F0";
            case GRASS    -> "#78C850";
            case ELECTRIC -> "#F8D030";
            case ICE      -> "#98D8D8";
            case FIGHTING -> "#C03028";
            case POISON   -> "#A040A0";
            case GROUND   -> "#E0C068";
            case FLYING   -> "#A890F0";
            case PSYCHIC  -> "#F85888";
            case BUG      -> "#A8B820";
            case ROCK     -> "#B8A038";
            case GHOST    -> "#705898";
            case DRAGON   -> "#7038F8";
            case DARK     -> "#705848";
            case STEEL    -> "#B8B8D0";
            default       -> "#888888";
        };
    }

    public String getDisplayName() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
