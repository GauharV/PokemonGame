package com.pokemon.data;

import com.pokemon.model.Pokemon;
import com.pokemon.model.PokemonType;

import java.util.HashMap;
import java.util.Map;

import static com.pokemon.model.PokemonType.*;

public class PokemonDatabase {

    // Template registry: id -> base stat template
    private static final Map<Integer, int[]> baseStats = new HashMap<>(); // [hp,atk,def,spatk,spdef,spd]
    private static final Map<Integer, String> names = new HashMap<>();
    private static final Map<Integer, PokemonType[]> types = new HashMap<>();
    private static final Map<Integer, int[]> evolution = new HashMap<>(); // [evolvesTo, level]
    private static final Map<Integer, String[]> learnset = new HashMap<>();

    static {
        // Format: id, name, type1, type2, hp, atk, def, spAtk, spDef, spd, evolvesTo, evolveLevel
        // Evolution: -1 = no evolution
        register(1,  "Bulbasaur",   GRASS,    POISON,   45, 49, 49, 65, 65, 45,  2, 16,
                 "Tackle","Growl","Vine Whip","Poison Powder");
        register(2,  "Ivysaur",     GRASS,    POISON,   60, 62, 63, 80, 80, 60,  3, 32,
                 "Vine Whip","Poison Powder","Razor Leaf","Sleep Powder");
        register(3,  "Venusaur",    GRASS,    POISON,   80, 82, 83,100,100, 80, -1, -1,
                 "Razor Leaf","Solar Beam","Sludge","Stun Spore");
        register(4,  "Charmander",  FIRE,     NONE,     39, 52, 43, 60, 50, 65,  5, 16,
                 "Scratch","Growl","Ember","Slash");
        register(5,  "Charmeleon",  FIRE,     NONE,     58, 64, 58, 80, 65, 80,  6, 36,
                 "Ember","Slash","Flamethrower","Slash");
        register(6,  "Charizard",   FIRE,     FLYING,   78, 84, 78,109, 85,100, -1, -1,
                 "Flamethrower","Fly","Slash","Fire Blast");
        register(7,  "Squirtle",    WATER,    NONE,     44, 48, 65, 50, 64, 43,  8, 16,
                 "Tackle","Tail Whip","Water Gun","Bubble");
        register(8,  "Wartortle",   WATER,    NONE,     59, 63, 80, 65, 80, 58,  9, 36,
                 "Water Gun","Bubble Beam","Bite","Surf");
        register(9,  "Blastoise",   WATER,    NONE,     79, 83,100, 85,105, 78, -1, -1,
                 "Surf","Hydro Pump","Body Slam","Bite");
        register(10, "Caterpie",    BUG,      NONE,     45, 30, 35, 20, 20, 45, 11,  7,
                 "Tackle","String Shot","Tackle","String Shot");
        register(11, "Metapod",     BUG,      NONE,     50, 20, 55, 25, 25, 30, 12, 10,
                 "Tackle","Tackle","Tackle","Tackle");
        register(12, "Butterfree",  BUG,      FLYING,   60, 45, 50, 90, 80, 70, -1, -1,
                 "Confusion","Sleep Powder","Stun Spore","Gust");
        register(13, "Weedle",      BUG,      POISON,   40, 35, 30, 20, 20, 50, 14,  7,
                 "Poison Sting","String Shot","Poison Sting","String Shot");
        register(14, "Kakuna",      BUG,      POISON,   45, 25, 50, 25, 25, 35, 15, 10,
                 "Poison Sting","String Shot","Poison Sting","String Shot");
        register(15, "Beedrill",    BUG,      POISON,   65, 90, 40, 45, 80, 75, -1, -1,
                 "Pin Missile","Poison Sting","Swords Dance","Leech Life");
        register(16, "Pidgey",      NORMAL,   FLYING,   40, 45, 40, 35, 35, 56, 17, 18,
                 "Tackle","Gust","Sand Attack","Quick Attack");
        register(17, "Pidgeotto",   NORMAL,   FLYING,   63, 60, 55, 50, 50, 71, 18, 36,
                 "Wing Attack","Gust","Quick Attack","Fly");
        register(18, "Pidgeot",     NORMAL,   FLYING,   83, 80, 75, 70, 70,101, -1, -1,
                 "Wing Attack","Fly","Quick Attack","Sky Attack");
        register(19, "Rattata",     NORMAL,   NONE,     30, 56, 35, 25, 35, 72, 20, 20,
                 "Tackle","Tail Whip","Quick Attack","Bite");
        register(20, "Raticate",    NORMAL,   NONE,     55, 81, 60, 50, 70, 97, -1, -1,
                 "Quick Attack","Bite","Body Slam","Slash");
        register(21, "Spearow",     NORMAL,   FLYING,   40, 60, 30, 31, 31, 70, 22, 20,
                 "Peck","Growl","Leer","Fury Attack");
        register(22, "Fearow",      NORMAL,   FLYING,   65, 90, 65, 61, 61,100, -1, -1,
                 "Drill Peck","Fly","Slash","Sky Attack");
        register(23, "Ekans",       POISON,   NONE,     35, 60, 44, 40, 54, 55, 24, 22,
                 "Wrap","Leer","Poison Sting","Acid");
        register(24, "Arbok",       POISON,   NONE,     60, 95, 69, 65, 79, 80, -1, -1,
                 "Crunch","Acid","Toxic","Body Slam");
        register(25, "Pikachu",     ELECTRIC, NONE,     35, 55, 40, 50, 50, 90, 26, 28,
                 "Thunder Shock","Tail Whip","Quick Attack","Thunderbolt");
        register(26, "Raichu",      ELECTRIC, NONE,     60, 90, 55, 90, 80,110, -1, -1,
                 "Thunderbolt","Thunder","Quick Attack","Iron Tail");
        register(27, "Sandshrew",   GROUND,   NONE,     50, 75, 85, 20, 30, 40, 28, 22,
                 "Scratch","Sand Attack","Slash","Dig");
        register(28, "Sandslash",   GROUND,   NONE,     75,100,110, 45, 55, 65, -1, -1,
                 "Slash","Dig","Earthquake","Scratch");
        register(29, "Nidoran-F",   POISON,   NONE,     55, 47, 52, 40, 40, 41, 30, 16,
                 "Scratch","Growl","Poison Sting","Bite");
        register(30, "Nidorina",    POISON,   NONE,     70, 62, 67, 55, 55, 56, 31, 36,
                 "Bite","Poison Sting","Toxic","Crunch");
        register(31, "Nidoqueen",   POISON,   GROUND,   90, 92, 87, 75, 85, 76, -1, -1,
                 "Earthquake","Crunch","Body Slam","Toxic");
        register(32, "Nidoran-M",   POISON,   NONE,     46, 57, 40, 40, 40, 50, 33, 16,
                 "Leer","Peck","Poison Sting","Bite");
        register(33, "Nidorino",    POISON,   NONE,     61, 72, 57, 55, 55, 65, 34, 36,
                 "Bite","Poison Sting","Toxic","Horn Attack");
        register(34, "Nidoking",    POISON,   GROUND,   81,102, 77, 85, 75, 85, -1, -1,
                 "Earthquake","Crunch","Thrash","Toxic");
        register(35, "Clefairy",    NORMAL,   NONE,     70, 45, 48, 60, 65, 35, 36, 36,
                 "Pound","Sing","Confusion","Body Slam");
        register(36, "Clefable",    NORMAL,   NONE,     95, 70, 73, 95, 90, 60, -1, -1,
                 "Sing","Body Slam","Psychic","Minimize");
        register(37, "Vulpix",      FIRE,     NONE,     38, 41, 40, 50, 65, 65, 38, 30,
                 "Ember","Tail Whip","Quick Attack","Flamethrower");
        register(38, "Ninetales",   FIRE,     NONE,     73, 76, 75, 81,100,100, -1, -1,
                 "Flamethrower","Fire Blast","Confuse Ray","Body Slam");
        register(39, "Jigglypuff",  NORMAL,   NONE,    115, 45, 20, 45, 25, 20, 40, 36,
                 "Sing","Pound","Body Slam","Headbutt");
        register(40, "Wigglytuff",  NORMAL,   NONE,    140, 70, 45, 85, 50, 45, -1, -1,
                 "Sing","Body Slam","Headbutt","Minimize");
        register(41, "Zubat",       POISON,   FLYING,   40, 45, 35, 30, 40, 55, 42, 22,
                 "Leech Life","Supersonic","Bite","Confuse Ray");
        register(42, "Golbat",      POISON,   FLYING,   75, 80, 70, 65, 75, 90, -1, -1,
                 "Leech Life","Crunch","Wing Attack","Confuse Ray");
        register(43, "Oddish",      GRASS,    POISON,   45, 50, 55, 75, 65, 30, 44, 21,
                 "Absorb","Acid","Sleep Powder","Stun Spore");
        register(44, "Gloom",       GRASS,    POISON,   60, 65, 70, 85, 75, 40, 45, 36,
                 "Acid","Mega Drain","Sleep Powder","Petal Dance");
        register(45, "Vileplume",   GRASS,    POISON,   75, 80, 85,110, 90, 50, -1, -1,
                 "Solar Beam","Sludge","Sleep Powder","Stun Spore");
        register(46, "Paras",       BUG,      GRASS,    35, 70, 55, 45, 55, 25, 47, 24,
                 "Scratch","Stun Spore","Leech Life","Slash");
        register(47, "Parasect",    BUG,      GRASS,    60, 95, 80, 60, 80, 30, -1, -1,
                 "Slash","Leech Life","Spore","Stun Spore");
        register(48, "Venonat",     BUG,      POISON,   60, 55, 50, 40, 55, 45, 49, 31,
                 "Tackle","Poison Powder","Confusion","Stun Spore");
        register(49, "Venomoth",    BUG,      POISON,   70, 65, 60, 90, 75, 90, -1, -1,
                 "Confusion","Psychic","Sleep Powder","Stun Spore");
        register(50, "Diglett",     GROUND,   NONE,     10, 55, 25, 35, 45, 95, 51, 26,
                 "Scratch","Mud Slap","Sand Attack","Dig");
        register(51, "Dugtrio",     GROUND,   NONE,     35,100, 50, 50, 70,120, -1, -1,
                 "Earthquake","Dig","Slash","Sand Attack");
        register(52, "Meowth",      NORMAL,   NONE,     40, 45, 35, 40, 40, 90, 53, 28,
                 "Scratch","Growl","Bite","Slash");
        register(53, "Persian",     NORMAL,   NONE,     65, 70, 60, 65, 65,115, -1, -1,
                 "Slash","Bite","Quick Attack","Body Slam");
        register(54, "Psyduck",     WATER,    NONE,     50, 52, 48, 65, 50, 55, 55, 33,
                 "Scratch","Tail Whip","Water Gun","Confusion");
        register(55, "Golduck",     WATER,    NONE,     80, 82, 78, 95, 80, 85, -1, -1,
                 "Surf","Psychic","Confusion","Hydro Pump");
        register(56, "Mankey",      FIGHTING, NONE,     40, 80, 35, 35, 45, 70, 57, 28,
                 "Scratch","Leer","Karate Chop","Low Kick");
        register(57, "Primeape",    FIGHTING, NONE,     65,105, 60, 60, 70, 95, -1, -1,
                 "Karate Chop","Low Kick","Submission","Mega Punch");
        register(58, "Growlithe",   FIRE,     NONE,     55, 70, 45, 70, 50, 60, 59, 36,
                 "Bite","Ember","Flame Wheel","Flamethrower");
        register(59, "Arcanine",    FIRE,     NONE,     90,110, 80,100, 80, 95, -1, -1,
                 "Flamethrower","Bite","Extreme Speed","Fire Blast");
        register(60, "Poliwag",     WATER,    NONE,     40, 50, 40, 40, 40, 90, 61, 25,
                 "Water Gun","Bubble","Hypnosis","Body Slam");
        register(61, "Poliwhirl",   WATER,    NONE,     65, 65, 65, 50, 50, 90, 62, 38,
                 "Surf","Body Slam","Hypnosis","Bubble Beam");
        register(62, "Poliwrath",   WATER,    FIGHTING, 90, 95, 95, 70, 90, 70, -1, -1,
                 "Surf","Submission","Body Slam","Hydro Pump");
        register(63, "Abra",        PSYCHIC,  NONE,     25, 20, 15,105, 55, 90, 64, 16,
                 "Teleport","Confusion","Confusion","Confusion");
        register(64, "Kadabra",     PSYCHIC,  NONE,     40, 35, 30,120, 70,105, 65, 38,
                 "Confusion","Psybeam","Psychic","Future Sight");
        register(65, "Alakazam",    PSYCHIC,  NONE,     55, 50, 45,135, 95,120, -1, -1,
                 "Psychic","Future Sight","Psybeam","Meditate");
        register(66, "Machop",      FIGHTING, NONE,     70, 80, 50, 35, 35, 35, 67, 28,
                 "Low Kick","Karate Chop","Focus Energy","Mega Punch");
        register(67, "Machoke",     FIGHTING, NONE,     80,100, 70, 50, 60, 45, 68, 38,
                 "Mega Punch","Karate Chop","Submission","Focus Energy");
        register(68, "Machamp",     FIGHTING, NONE,     90,130, 80, 65, 85, 55, -1, -1,
                 "Mega Punch","Submission","Karate Chop","Body Slam");
        register(69, "Bellsprout",  GRASS,    POISON,   50, 75, 35, 70, 30, 40, 70, 21,
                 "Vine Whip","Acid","Sleep Powder","Poison Powder");
        register(70, "Weepinbell",  GRASS,    POISON,   65, 90, 50, 85, 45, 55, 71, 36,
                 "Vine Whip","Acid","Razor Leaf","Sludge");
        register(71, "Victreebel", GRASS,    POISON,    80,105, 65,100, 60, 70, -1, -1,
                 "Razor Leaf","Solar Beam","Sludge","Toxic");
        register(72, "Tentacool",   WATER,    POISON,   40, 40, 35, 50,100, 70, 73, 30,
                 "Acid","Poison Sting","Bubble Beam","Clamp");
        register(73, "Tentacruel",  WATER,    POISON,   80, 70, 65, 80,120,100, -1, -1,
                 "Surf","Acid","Hydro Pump","Toxic");
        register(74, "Geodude",     ROCK,     GROUND,   40, 80,100, 30, 30, 20, 75, 25,
                 "Tackle","Rock Throw","Magnitude","Rock Slide");
        register(75, "Graveler",    ROCK,     GROUND,   55, 95,115, 45, 45, 35, 76, 38,
                 "Rock Slide","Magnitude","Earthquake","Rock Blast");
        register(76, "Golem",       ROCK,     GROUND,   80,120,130, 55, 65, 45, -1, -1,
                 "Earthquake","Rock Slide","Strength","Rock Blast");
        register(77, "Ponyta",      FIRE,     NONE,     50, 85, 55, 65, 65, 90, 78, 40,
                 "Tackle","Ember","Flame Wheel","Flamethrower");
        register(78, "Rapidash",    FIRE,     NONE,     65,100, 70, 80, 80,105, -1, -1,
                 "Flamethrower","Flame Wheel","Quick Attack","Fire Blast");
        register(79, "Slowpoke",    WATER,    PSYCHIC,  90, 65, 65, 40, 40, 15, 80, 37,
                 "Water Gun","Confusion","Headbutt","Surf");
        register(80, "Slowbro",     WATER,    PSYCHIC,  95, 75,110,100, 80, 30, -1, -1,
                 "Surf","Psychic","Amnesia","Headbutt");
        register(81, "Magnemite",   ELECTRIC, STEEL,    25, 35, 70, 95, 55, 45, 82, 30,
                 "Thunder Shock","Tackle","Thunderbolt","Thunder Wave");
        register(82, "Magneton",    ELECTRIC, STEEL,    50, 60, 95,120, 70, 70, -1, -1,
                 "Thunderbolt","Thunder","Thunder Wave","Iron Tail");
        register(83, "Farfetch'd",  NORMAL,   FLYING,   52, 90, 55, 58, 62, 60, -1, -1,
                 "Slash","Wing Attack","Fly","Cut");
        register(84, "Doduo",       NORMAL,   FLYING,   35, 85, 45, 35, 35, 75, 85, 31,
                 "Peck","Growl","Fury Attack","Drill Peck");
        register(85, "Dodrio",      NORMAL,   FLYING,   60,110, 70, 60, 60,110, -1, -1,
                 "Drill Peck","Fly","Tri Attack","Slash");
        register(86, "Seel",        WATER,    NONE,     65, 45, 55, 45, 70, 45, 87, 34,
                 "Headbutt","Growl","Ice Beam","Bubble Beam");
        register(87, "Dewgong",     WATER,    ICE,      90, 70, 80, 70, 95, 70, -1, -1,
                 "Ice Beam","Surf","Headbutt","Blizzard");
        register(88, "Grimer",      POISON,   NONE,     80, 80, 50, 40, 50, 25, 89, 38,
                 "Pound","Acid","Sludge","Toxic");
        register(89, "Muk",         POISON,   NONE,    105,105, 75, 65,100, 50, -1, -1,
                 "Sludge","Crunch","Toxic","Body Slam");
        register(90, "Shellder",    WATER,    NONE,     30, 65,100, 45, 25, 40, 91, 36,
                 "Tackle","Clamp","Bubble Beam","Ice Beam");
        register(91, "Cloyster",    WATER,    ICE,      50, 95,180, 85, 45, 70, -1, -1,
                 "Blizzard","Clamp","Ice Beam","Surf");
        register(92, "Gastly",      GHOST,    POISON,   30, 35, 30,100, 35, 80, 93, 25,
                 "Lick","Confuse Ray","Night Shade","Hypnosis");
        register(93, "Haunter",     GHOST,    POISON,   45, 50, 45,115, 55, 95, 94, 36,
                 "Shadow Ball","Confuse Ray","Night Shade","Lick");
        register(94, "Gengar",      GHOST,    POISON,   60, 65, 60,130, 75,110, -1, -1,
                 "Shadow Ball","Psychic","Confuse Ray","Sludge");
        register(95, "Onix",        ROCK,     GROUND,   35, 45,160, 30, 45, 70, -1, -1,
                 "Rock Throw","Tackle","Slam","Earthquake");
        register(96, "Drowzee",     PSYCHIC,  NONE,     60, 48, 45, 43, 90, 42, 97, 26,
                 "Pound","Hypnosis","Confusion","Psychic");
        register(97, "Hypno",       PSYCHIC,  NONE,     85, 73, 70, 73,115, 67, -1, -1,
                 "Psychic","Hypnosis","Future Sight","Confuse Ray");
        register(98, "Krabby",      WATER,    NONE,     30,105, 90, 25, 25, 50, 99, 28,
                 "Vicegrip","Leer","Clamp","Bubble Beam");
        register(99, "Kingler",     WATER,    NONE,     55,130,115, 50, 50, 75, -1, -1,
                 "Crabhammer","Clamp","Body Slam","Surf");
        register(100,"Voltorb",     ELECTRIC, NONE,     40, 30, 50, 55, 55,100, 101, 30,
                 "Tackle","Sonicboom","Thunder Wave","Thunderbolt");
        register(101,"Electrode",   ELECTRIC, NONE,     60, 50, 70, 80, 80,150, -1, -1,
                 "Thunderbolt","Thunder","Thunder Wave","Body Slam");
        register(102,"Exeggcute",   GRASS,    PSYCHIC,  60, 40, 80, 60, 45, 40, 103, 36,
                 "Absorb","Confusion","Sleep Powder","Stun Spore");
        register(103,"Exeggutor",   GRASS,    PSYCHIC,  95, 95, 85,125, 75, 55, -1, -1,
                 "Solar Beam","Psychic","Sleep Powder","Confusion");
        register(104,"Cubone",      GROUND,   NONE,     50, 50, 95, 40, 50, 35, 105, 28,
                 "Growl","Bone Club","Leer","Bonemerang");
        register(105,"Marowak",     GROUND,   NONE,     60, 80,110, 50, 80, 45, -1, -1,
                 "Earthquake","Bonemerang","Slash","Headbutt");
        register(106,"Hitmonlee",   FIGHTING, NONE,     50,120, 53, 35,110, 87, -1, -1,
                 "Low Kick","Submission","Karate Chop","Mega Punch");
        register(107,"Hitmonchan",  FIGHTING, NONE,     50,105, 79, 35,110, 76, -1, -1,
                 "Mega Punch","Karate Chop","Submission","Focus Energy");
        register(108,"Lickitung",   NORMAL,   NONE,     90, 55, 75, 60, 75, 30, -1, -1,
                 "Lick","Slam","Body Slam","Strength");
        register(109,"Koffing",     POISON,   NONE,     40, 65, 95, 60, 45, 35, 110, 35,
                 "Tackle","Smog","Sludge","Toxic");
        register(110,"Weezing",     POISON,   NONE,     65, 90,120, 85, 70, 60, -1, -1,
                 "Sludge","Toxic","Crunch","Shadow Ball");
        register(111,"Rhyhorn",     GROUND,   ROCK,     80, 85, 95, 30, 30, 25, 112, 42,
                 "Horn Attack","Stomp","Rock Slide","Tail Whip");
        register(112,"Rhydon",      GROUND,   ROCK,    105,130,120, 45, 45, 40, -1, -1,
                 "Earthquake","Rock Slide","Stomp","Horn Attack");
        register(113,"Chansey",     NORMAL,   NONE,    250,  5,  5, 35,105, 50, -1, -1,
                 "Sing","Pound","Body Slam","Headbutt");
        register(114,"Tangela",     GRASS,    NONE,     65, 55,115,100, 40, 60, -1, -1,
                 "Vine Whip","Absorb","Stun Spore","Solar Beam");
        register(115,"Kangaskhan",  NORMAL,   NONE,    105, 95, 80, 40, 80, 90, -1, -1,
                 "Scratch","Stomp","Body Slam","Headbutt");
        register(116,"Horsea",      WATER,    NONE,     30, 40, 70, 70, 25, 60, 117, 32,
                 "Bubble","Leer","Water Gun","Bubble Beam");
        register(117,"Seadra",      WATER,    NONE,     55, 65, 95, 95, 45, 85, -1, -1,
                 "Water Gun","Hydro Pump","Bubble Beam","Surf");
        register(118,"Goldeen",     WATER,    NONE,     45, 67, 60, 35, 50, 63, 119, 33,
                 "Peck","Tail Whip","Water Gun","Horn Attack");
        register(119,"Seaking",     WATER,    NONE,     80, 92, 65, 65, 80, 68, -1, -1,
                 "Horn Attack","Surf","Hydro Pump","Waterfall");
        register(120,"Staryu",      WATER,    NONE,     30, 45, 55, 70, 55, 85, 121, 36,
                 "Tackle","Water Gun","Swift","Bubble Beam");
        register(121,"Starmie",     WATER,    PSYCHIC,  60, 75, 85,100, 85,115, -1, -1,
                 "Surf","Psychic","Thunderbolt","Ice Beam");
        register(122,"Mr. Mime",    PSYCHIC,  NONE,     40, 45, 65,100,120, 90, -1, -1,
                 "Confusion","Psychic","Meditate","Confuse Ray");
        register(123,"Scyther",     BUG,      FLYING,   70,110, 80, 55, 80,105, -1, -1,
                 "Slash","Wing Attack","Swords Dance","Quick Attack");
        register(124,"Jynx",        ICE,      PSYCHIC,  65, 50, 35,115, 95, 95, -1, -1,
                 "Ice Punch","Psychic","Blizzard","Confuse Ray");
        register(125,"Electabuzz",  ELECTRIC, NONE,     65, 83, 57, 95, 85,105, -1, -1,
                 "Thunderbolt","Thunder Wave","Quick Attack","Ice Punch");
        register(126,"Magmar",      FIRE,     NONE,     65, 95, 57,100, 85, 93, -1, -1,
                 "Flamethrower","Fire Blast","Smokescreen","Lick");
        register(127,"Pinsir",      BUG,      NONE,     65,125,100, 55, 70, 85, -1, -1,
                 "Slash","Swords Dance","Leech Life","Submission");
        register(128,"Tauros",      NORMAL,   NONE,     75,100, 95, 40, 70,110, -1, -1,
                 "Body Slam","Strength","Slam","Headbutt");
        register(129,"Magikarp",    WATER,    NONE,     20, 10, 55, 15, 20, 80, 130, 20,
                 "Splash","Tackle","Splash","Tackle");
        register(130,"Gyarados",    WATER,    FLYING,   95,125, 79, 60,100, 81, -1, -1,
                 "Surf","Bite","Hyper Beam","Dragon Rage");
        register(131,"Lapras",      WATER,    ICE,     130, 85, 80, 85, 95, 60, -1, -1,
                 "Ice Beam","Surf","Body Slam","Blizzard");
        register(132,"Ditto",       NORMAL,   NONE,     48, 48, 48, 48, 48, 48, -1, -1,
                 "Tackle","Tackle","Tackle","Tackle");
        register(133,"Eevee",       NORMAL,   NONE,     55, 55, 50, 45, 65, 55, 136, 30,
                 "Tackle","Growl","Quick Attack","Bite");
        register(134,"Vaporeon",    WATER,    NONE,    130, 65, 60,110, 95, 65, -1, -1,
                 "Surf","Hydro Pump","Quick Attack","Body Slam");
        register(135,"Jolteon",     ELECTRIC, NONE,     65, 65, 60,110, 95,130, -1, -1,
                 "Thunderbolt","Thunder Wave","Quick Attack","Pin Missile");
        register(136,"Flareon",     FIRE,     NONE,     65,130, 60, 95,110, 65, -1, -1,
                 "Flamethrower","Fire Blast","Quick Attack","Body Slam");
        register(137,"Porygon",     NORMAL,   NONE,     65, 60, 70, 85, 75, 40, -1, -1,
                 "Tackle","Psybeam","Thunderbolt","Ice Beam");
        register(138,"Omanyte",     ROCK,     WATER,    35, 40,100, 90, 55, 35, 139, 40,
                 "Tackle","Water Gun","Ancient Power","Rock Slide");
        register(139,"Omastar",     ROCK,     WATER,    70, 60,125,115, 70, 55, -1, -1,
                 "Ancient Power","Hydro Pump","Rock Slide","Surf");
        register(140,"Kabuto",      ROCK,     WATER,    30, 80, 90, 55, 45, 55, 141, 40,
                 "Scratch","Harden","Absorb","Scratch");
        register(141,"Kabutops",    ROCK,     WATER,    60,115,105, 65, 70, 80, -1, -1,
                 "Slash","Rock Slide","Absorb","Surf");
        register(142,"Aerodactyl",  ROCK,     FLYING,   80,105, 65, 60, 75,130, -1, -1,
                 "Wing Attack","Rock Slide","Hyper Beam","Ancient Power");
        register(143,"Snorlax",     NORMAL,   NONE,    160,110, 65, 65,110, 30, -1, -1,
                 "Body Slam","Headbutt","Hyper Beam","Amnesia");
        register(144,"Articuno",    ICE,      FLYING,   90, 85,100, 95,125, 85, -1, -1,
                 "Ice Beam","Blizzard","Fly","Sky Attack");
        register(145,"Zapdos",      ELECTRIC, FLYING,   90, 90, 85,125, 90,100, -1, -1,
                 "Thunderbolt","Thunder","Fly","Drill Peck");
        register(146,"Moltres",     FIRE,     FLYING,   90,100, 90,125, 85, 90, -1, -1,
                 "Flamethrower","Fire Blast","Fly","Sky Attack");
        register(147,"Dratini",     DRAGON,   NONE,     41, 64, 45, 50, 50, 50, 148, 30,
                 "Leer","Wrap","Dragon Rage","Dragonbreath");
        register(148,"Dragonair",   DRAGON,   NONE,     61, 84, 65, 70, 70, 70, 149, 55,
                 "Dragonbreath","Dragon Rage","Wrap","Slam");
        register(149,"Dragonite",   DRAGON,   FLYING,   91,134, 95,100,100, 80, -1, -1,
                 "Dragon Claw","Hyper Beam","Thunderbolt","Fly");
        register(150,"Mewtwo",      PSYCHIC,  NONE,    106,110, 90,154, 90,130, -1, -1,
                 "Psychic","Future Sight","Ice Beam","Thunderbolt");
        register(151,"Mew",         PSYCHIC,  NONE,    100,100,100,100,100,100, -1, -1,
                 "Psychic","Flamethrower","Thunderbolt","Ice Beam");
    }

    private static void register(int id, String name,
                                  PokemonType t1, PokemonType t2,
                                  int hp, int atk, int def, int spa, int spd, int spe,
                                  int evoId, int evoLvl, String... moves) {
        names.put(id, name);
        types.put(id, new PokemonType[]{t1, t2});
        baseStats.put(id, new int[]{hp, atk, def, spa, spd, spe});
        evolution.put(id, new int[]{evoId, evoLvl});
        learnset.put(id, moves);
    }

    /** Create a Pokemon by ID at a given level with appropriate moves */
    public static Pokemon create(int id, int level) {
        if (!names.containsKey(id)) {
            throw new IllegalArgumentException("Unknown Pokemon ID: " + id);
        }
        int[] stats = baseStats.get(id);
        PokemonType[] t = types.get(id);
        int[] evo = evolution.get(id);

        Pokemon p = new Pokemon(id, names.get(id), t[0], t[1],
                stats[0], stats[1], stats[2], stats[3], stats[4], stats[5],
                level, evo[0], evo[1]);

        for (String moveName : learnset.get(id)) {
            try {
                p.addMove(MoveDatabase.get(moveName));
            } catch (Exception e) {
                // Fallback to Tackle
                p.addMove(MoveDatabase.get("Tackle"));
            }
        }
        p.calculateStats();
        p.heal(p.getMaxHp()); // full health
        return p;
    }

    public static String getName(int id) {
        return names.getOrDefault(id, "Unknown");
    }

    public static boolean exists(int id) {
        return names.containsKey(id);
    }

    public static int getCount() { return names.size(); }
}
