package com.pokemon.data;

import com.pokemon.model.Move;
import com.pokemon.model.Move.Category;
import com.pokemon.model.Move.Effect;
import com.pokemon.model.PokemonType;

import java.util.HashMap;
import java.util.Map;

public class MoveDatabase {
    private static final Map<String, Move> moves = new HashMap<>();

    static {
        // ---- NORMAL ----
        add("Tackle",       PokemonType.NORMAL,   Category.PHYSICAL, 40,  100, 35, "A basic physical attack.");
        add("Scratch",      PokemonType.NORMAL,   Category.PHYSICAL, 40,  100, 35, "Scratches the foe.");
        add("Pound",        PokemonType.NORMAL,   Category.PHYSICAL, 40,  100, 35, "A basic pound attack.");
        add("Slam",         PokemonType.NORMAL,   Category.PHYSICAL, 80,  75,  20, "Slams foe hard.");
        add("Headbutt",  PokemonType.NORMAL, Category.PHYSICAL, 70, 100, 15, Effect.FLINCH,   30, "A strong headbutt. May cause flinching.");
        add("Body Slam", PokemonType.NORMAL, Category.PHYSICAL, 85, 100, 15, Effect.PARALYZE, 30, "May paralyze.");
        add("Body Slam",    PokemonType.NORMAL,   Category.PHYSICAL, 85,  100, 15, Effect.PARALYZE, 30, "May paralyze.");
        add("Quick Attack", PokemonType.NORMAL,   Category.PHYSICAL, 40,  100, 30, "A fast priority attack.");
        add("Hyper Beam",   PokemonType.NORMAL,   Category.SPECIAL,  150, 90,  5,  "Devastating but requires recharge.");
        add("Slash",        PokemonType.NORMAL,   Category.PHYSICAL, 70,  100, 20, "High critical hit ratio.");
        add("Double Slap",  PokemonType.NORMAL,   Category.PHYSICAL, 15,  85,  10, "Slaps 2-5 times.");
        add("Growl",        PokemonType.NORMAL,   Category.STATUS,   0,   100, 40, Effect.LOWER_ATK, 100, "Lowers foe's ATK.");
        add("Leer",         PokemonType.NORMAL,   Category.STATUS,   0,   100, 30, Effect.LOWER_DEF, 100, "Lowers foe's DEF.");
        add("Tail Whip",    PokemonType.NORMAL,   Category.STATUS,   0,   100, 30, Effect.LOWER_DEF, 100, "Lowers foe's DEF.");
        add("Swords Dance", PokemonType.NORMAL,   Category.STATUS,   0,   0,   30, Effect.RAISE_ATK, 100, "Sharply raises ATK.");
        add("Sing",         PokemonType.NORMAL,   Category.STATUS,   0,   55,  15, Effect.SLEEP, 100, "May put foe to sleep.");
        add("Minimize",     PokemonType.NORMAL,   Category.STATUS,   0,   0,   20, "Raises evasiveness.");
        add("Strength",     PokemonType.NORMAL,   Category.PHYSICAL, 80,  100, 15, "A powerful physical move.");
        add("Cut",          PokemonType.NORMAL,   Category.PHYSICAL, 50,  95,  30, "A slicing cut attack.");

        // ---- FIRE ----
        add("Ember",        PokemonType.FIRE,     Category.SPECIAL,  40,  100, 25, Effect.BURN, 10, "May burn.");
        add("Fire Spin",    PokemonType.FIRE,     Category.SPECIAL,  35,  85,  15, "Traps foe in flames.");
        add("Flamethrower", PokemonType.FIRE,     Category.SPECIAL,  90,  100, 15, Effect.BURN, 10, "May burn.");
        add("Fire Blast",   PokemonType.FIRE,     Category.SPECIAL,  110, 85,  5,  Effect.BURN, 10, "May burn.");
        add("Flame Wheel",  PokemonType.FIRE,     Category.PHYSICAL, 60,  100, 25, Effect.BURN, 10, "May burn.");

        // ---- WATER ----
        add("Water Gun",    PokemonType.WATER,    Category.SPECIAL,  40,  100, 25, "Shoots water at foe.");
        add("Bubble",       PokemonType.WATER,    Category.SPECIAL,  40,  100, 30, Effect.LOWER_SPD, 10, "May lower SPD.");
        add("Surf",         PokemonType.WATER,    Category.SPECIAL,  90,  100, 15, "A tidal wave attack.");
        add("Hydro Pump",   PokemonType.WATER,    Category.SPECIAL,  110, 80,  5,  "A powerful water blast.");
        add("Bubble Beam",  PokemonType.WATER,    Category.SPECIAL,  65,  100, 20, Effect.LOWER_SPD, 10, "May lower SPD.");
        add("Clamp",        PokemonType.WATER,    Category.PHYSICAL, 35,  85,  10, "Clamps foe.");

        // ---- GRASS ----
        add("Vine Whip",    PokemonType.GRASS,    Category.PHYSICAL, 45,  100, 25, "Lashes with vines.");
        add("Absorb",       PokemonType.GRASS,    Category.SPECIAL,  20,  100, 25, Effect.DRAIN, 50, "Absorbs half damage dealt.");
        add("Mega Drain",   PokemonType.GRASS,    Category.SPECIAL,  40,  100, 15, Effect.DRAIN, 50, "Absorbs half damage dealt.");
        add("Razor Leaf",   PokemonType.GRASS,    Category.PHYSICAL, 55,  95,  25, "High critical hit ratio.");
        add("Solar Beam",   PokemonType.GRASS,    Category.SPECIAL,  120, 100, 10, "Charges then fires.");
        add("Sleep Powder", PokemonType.GRASS,    Category.STATUS,   0,   75,  15, Effect.SLEEP, 100, "Puts foe to sleep.");
        add("Stun Spore",   PokemonType.GRASS,    Category.STATUS,   0,   75,  30, Effect.PARALYZE, 100, "Paralyzes foe.");
        add("Poison Powder",PokemonType.GRASS,    Category.STATUS,   0,   75,  35, Effect.POISON, 100, "Poisons foe.");
        add("Leech Seed",   PokemonType.GRASS,    Category.STATUS,   0,   90,  10, "Steals HP each turn.");

        // ---- ELECTRIC ----
        add("Thunder Shock",PokemonType.ELECTRIC, Category.SPECIAL,  40,  100, 30, Effect.PARALYZE, 10, "May paralyze.");
        add("Thunderbolt",  PokemonType.ELECTRIC, Category.SPECIAL,  90,  100, 15, Effect.PARALYZE, 10, "May paralyze.");
        add("Thunder",      PokemonType.ELECTRIC, Category.SPECIAL,  110, 70,  10, Effect.PARALYZE, 30, "May paralyze.");
        add("Thunder Wave", PokemonType.ELECTRIC, Category.STATUS,   0,   90,  20, Effect.PARALYZE, 100, "Paralyzes foe.");
        add("Spark",        PokemonType.ELECTRIC, Category.PHYSICAL, 65,  100, 20, Effect.PARALYZE, 30, "May paralyze.");

        // ---- ICE ----
        add("Ice Beam",     PokemonType.ICE,      Category.SPECIAL,  90,  100, 10, Effect.FREEZE, 10, "May freeze.");
        add("Blizzard",     PokemonType.ICE,      Category.SPECIAL,  110, 70,  5,  Effect.FREEZE, 10, "May freeze.");
        add("Powder Snow",  PokemonType.ICE,      Category.SPECIAL,  40,  100, 25, Effect.FREEZE, 10, "May freeze.");
        add("Ice Punch",    PokemonType.ICE,      Category.PHYSICAL, 75,  100, 15, Effect.FREEZE, 10, "May freeze.");

        // ---- FIGHTING ----
        add("Karate Chop",  PokemonType.FIGHTING, Category.PHYSICAL, 50,  100, 25, "High critical hit ratio.");
        add("Low Kick",     PokemonType.FIGHTING, Category.PHYSICAL, 65,  100, 20, "A tripping kick.");
        add("Mega Punch",   PokemonType.FIGHTING, Category.PHYSICAL, 80,  85,  20, "A powerful punch.");
        add("Submission",   PokemonType.FIGHTING, Category.PHYSICAL, 80,  80,  25, "Recoil damage.");
        add("Focus Energy", PokemonType.FIGHTING, Category.STATUS,   0,   0,   30, Effect.RAISE_ATK, 100, "Raises critical hit ratio.");

        // ---- POISON ----
        add("Poison Sting", PokemonType.POISON,   Category.PHYSICAL, 15,  100, 35, Effect.POISON, 30, "May poison.");
        add("Smog",         PokemonType.POISON,   Category.SPECIAL,  20,  70,  20, Effect.POISON, 40, "May poison.");
        add("Sludge",       PokemonType.POISON,   Category.SPECIAL,  65,  100, 20, Effect.POISON, 30, "May poison.");
        add("Acid",         PokemonType.POISON,   Category.SPECIAL,  40,  100, 30, Effect.LOWER_DEF, 10, "May lower DEF.");
        add("Toxic",        PokemonType.POISON,   Category.STATUS,   0,   90,  10, Effect.POISON, 100, "Badly poisons foe.");
        add("Poison Gas",   PokemonType.POISON,   Category.STATUS,   0,   55,  40, Effect.POISON, 100, "Poisons foe.");

        // ---- GROUND ----
        add("Dig",          PokemonType.GROUND,   Category.PHYSICAL, 80,  100, 10, "Digs underground then strikes.");
        add("Earthquake",   PokemonType.GROUND,   Category.PHYSICAL, 100, 100, 10, "A powerful earthquake.");
        add("Sand Attack",  PokemonType.GROUND,   Category.STATUS,   0,   100, 15, "Reduces foe's accuracy.");
        add("Magnitude",    PokemonType.GROUND,   Category.PHYSICAL, 70,  100, 30, "Variable power quake.");
        add("Mud Slap",     PokemonType.GROUND,   Category.SPECIAL,  20,  100, 10, Effect.LOWER_SPD, 100, "Lowers accuracy.");

        // ---- FLYING ----
        add("Gust",         PokemonType.FLYING,   Category.SPECIAL,  40,  100, 35, "A gust of wind attack.");
        add("Wing Attack",  PokemonType.FLYING,   Category.PHYSICAL, 60,  100, 35, "Strikes with wings.");
        add("Fly",          PokemonType.FLYING,   Category.PHYSICAL, 90,  95,  15, "Flies up then strikes.");
        add("Peck",         PokemonType.FLYING,   Category.PHYSICAL, 35,  100, 35, "Pecks with a beak.");
        add("Drill Peck",   PokemonType.FLYING,   Category.PHYSICAL, 80,  100, 20, "A spinning drill peck.");
        add("Sky Attack",   PokemonType.FLYING,   Category.PHYSICAL, 140, 90,  5,  "Charges then attacks.");

        // ---- PSYCHIC ----
        add("Confusion",    PokemonType.PSYCHIC,  Category.SPECIAL,  50,  100, 25, Effect.CONFUSE, 10, "May confuse.");
        add("Psychic",      PokemonType.PSYCHIC,  Category.SPECIAL,  90,  100, 10, Effect.LOWER_DEF, 10, "May lower Sp.DEF.");
        add("Hypnosis",     PokemonType.PSYCHIC,  Category.STATUS,   0,   60,  20, Effect.SLEEP, 100, "Puts foe to sleep.");
        add("Psybeam",      PokemonType.PSYCHIC,  Category.SPECIAL,  65,  100, 20, Effect.CONFUSE, 10, "May confuse.");
        add("Meditate",     PokemonType.PSYCHIC,  Category.STATUS,   0,   0,   40, Effect.RAISE_ATK, 100, "Raises ATK.");
        add("Future Sight", PokemonType.PSYCHIC,  Category.SPECIAL,  120, 100, 10, "Attack hits 2 turns later.");

        // ---- BUG ----
        add("String Shot",  PokemonType.BUG,      Category.STATUS,   0,   95,  40, Effect.LOWER_SPD, 100, "Lowers foe's speed.");
        add("Pin Missile",  PokemonType.BUG,      Category.PHYSICAL, 25,  95,  20, "Fires 2-5 pins.");
        add("Leech Life",   PokemonType.BUG,      Category.PHYSICAL, 80,  100, 10, Effect.DRAIN, 50, "Absorbs half damage dealt.");
        add("Bug Bite",     PokemonType.BUG,      Category.PHYSICAL, 60,  100, 20, "Bites the foe.");
        add("Fury Attack",  PokemonType.NORMAL,   Category.PHYSICAL, 15,  85,  20, "Attacks 2-5 times.");

        // ---- ROCK ----
        add("Rock Throw",   PokemonType.ROCK,     Category.PHYSICAL, 50,  90,  15, "Throws a rock at foe.");
        add("Rock Blast",   PokemonType.ROCK,     Category.PHYSICAL, 25,  90,  10, "Fires 2-5 rocks.");
        add("Ancient Power",PokemonType.ROCK,     Category.SPECIAL,  60,  100, 5, Effect.RAISE_ATK, 10, "May raise all stats.");
        add("Rock Slide",   PokemonType.ROCK,     Category.PHYSICAL, 75,  90,  10, Effect.FLINCH, 30, "May cause flinching.");

        // ---- GHOST ----
        add("Lick",         PokemonType.GHOST,    Category.PHYSICAL, 30,  100, 30, Effect.PARALYZE, 30, "May paralyze.");
        add("Night Shade",  PokemonType.GHOST,    Category.SPECIAL,  1,   100, 15, "Damage equal to user's level.");
        add("Shadow Ball",  PokemonType.GHOST,    Category.SPECIAL,  80,  100, 15, Effect.LOWER_DEF, 20, "May lower Sp.DEF.");
        add("Confuse Ray",  PokemonType.GHOST,    Category.STATUS,   0,   100, 10, Effect.CONFUSE, 100, "Confuses foe.");
        add("Curse",        PokemonType.GHOST,    Category.STATUS,   0,   0,   10, "Lays a curse on foe.");

        // ---- DRAGON ----
        add("Dragon Rage",  PokemonType.DRAGON,   Category.SPECIAL,  40,  100, 10, "Always does fixed damage.");
        add("Dragonbreath", PokemonType.DRAGON,   Category.SPECIAL,  60,  100, 20, Effect.PARALYZE, 30, "May paralyze.");
        add("Dragon Claw",  PokemonType.DRAGON,   Category.PHYSICAL, 80,  100, 15, "A slashing claw attack.");

        // ---- DARK ----
        add("Bite",         PokemonType.DARK,     Category.PHYSICAL, 60,  100, 25, Effect.FLINCH, 30, "May cause flinching.");
        add("Crunch",       PokemonType.DARK,     Category.PHYSICAL, 80,  100, 15, Effect.LOWER_DEF, 20, "May lower DEF.");
        add("Night Slash",  PokemonType.DARK,     Category.PHYSICAL, 70,  100, 15, "High critical hit ratio.");
        add("Thief",        PokemonType.DARK,     Category.PHYSICAL, 60,  100, 25, "May steal foe's item.");

        // ---- STEEL ----
        add("Iron Tail",    PokemonType.STEEL,    Category.PHYSICAL, 100, 75,  15, Effect.LOWER_DEF, 30, "May lower DEF.");
        add("Metal Claw",   PokemonType.STEEL,    Category.PHYSICAL, 50,  95,  35, Effect.RAISE_ATK, 10, "May raise ATK.");
        add("Steel Wing",   PokemonType.STEEL,    Category.PHYSICAL, 70,  90,  25, Effect.RAISE_DEF, 10, "May raise DEF.");
    }

    private static void add(String name, PokemonType type, Category cat,
                             int power, int acc, int pp, String desc) {
        moves.put(name, new Move(name, type, cat, power, acc, pp, desc));
    }

    private static void add(String name, PokemonType type, Category cat,
                             int power, int acc, int pp,
                             Effect effect, int effectChance, String desc) {
        moves.put(name, new Move(name, type, cat, power, acc, pp, effect, effectChance, desc));
    }

    public static Move get(String name) {
        Move m = moves.get(name);
        if (m == null) {
            System.err.println("WARNING: Move not found: " + name);
            return new Move("Struggle", PokemonType.NORMAL, Category.PHYSICAL, 50, 100, 1, "Desperation attack.");
        }
        return new Move(m); // return a copy so PP is independent
    }
}
