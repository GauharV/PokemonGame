package com.pokemon.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LocationDatabase {

    public record WildEncounter(int pokemonId, int minLevel, int maxLevel, int weight) {}

    public record Location(String name, String description, String[] connections,
                           boolean hasWildPokemon, boolean hasTrainers, boolean hasPokecenter) {}

    private static final Map<String, Location> locations = new HashMap<>();
    private static final Map<String, List<WildEncounter>> encounters = new HashMap<>();
    private static final Random rand = new Random();

    static {
        // Define all locations
        addLocation("Pallet Town",    "Your hometown. A quiet little place.",
                new String[]{"Route 1"}, false, false, false);
        addLocation("Route 1",        "A pleasant path between Pallet and Viridian.",
                new String[]{"Pallet Town", "Viridian City"}, true, true, false);
        addLocation("Viridian City",  "A city with a mysterious Gym.",
                new String[]{"Route 1", "Mt. Moon"}, true, true, true);
        addLocation("Mt. Moon",       "A vast cave filled with wild Pokemon.",
                new String[]{"Viridian City", "Cerulean City"}, true, true, false);
        addLocation("Cerulean City",  "A city with a famous water Gym.",
                new String[]{"Mt. Moon", "Route 24"}, true, true, true);
        addLocation("Route 24",       "A grassy route north of Cerulean.",
                new String[]{"Cerulean City", "Lavender Town"}, true, true, false);
        addLocation("Lavender Town",  "A creepy town known for its Pokemon Tower.",
                new String[]{"Route 24", "Celadon City", "Fuchsia City"}, true, true, true);
        addLocation("Celadon City",   "A large city famous for its Game Corner.",
                new String[]{"Lavender Town", "Fuchsia City"}, true, true, true);
        addLocation("Fuchsia City",   "Home to the Safari Zone and a tough Gym.",
                new String[]{"Celadon City", "Cinnabar Island"}, true, true, true);
        addLocation("Cinnabar Island","A volcanic island with a fire Gym.",
                new String[]{"Fuchsia City", "Viridian Gym"}, true, true, true);
        addLocation("Viridian Gym",   "The mysterious final Gym.",
                new String[]{"Cinnabar Island", "Pokemon League"}, false, true, false);
        addLocation("Pokemon League", "The ultimate challenge awaits here.",
                new String[]{"Viridian Gym"}, false, true, true);

        // Wild encounter tables
        addEncounters("Route 1",
                new WildEncounter(16, 2, 5, 45),    // Pidgey
                new WildEncounter(19, 2, 5, 45),    // Rattata
                new WildEncounter(13, 3, 6, 10));   // Weedle

        addEncounters("Viridian City",
                new WildEncounter(19, 5, 8, 40),    // Rattata
                new WildEncounter(21, 5, 9, 30),    // Spearow
                new WildEncounter(16, 5, 8, 30));   // Pidgey

        addEncounters("Mt. Moon",
                new WildEncounter(41, 8, 12, 35),   // Zubat
                new WildEncounter(74, 9, 14, 30),   // Geodude
                new WildEncounter(35, 10, 13, 15),  // Clefairy
                new WildEncounter(23, 10, 13, 20)); // Ekans

        addEncounters("Cerulean City",
                new WildEncounter(54, 13, 17, 30),  // Psyduck
                new WildEncounter(60, 13, 17, 30),  // Poliwag
                new WildEncounter(72, 13, 17, 25),  // Tentacool
                new WildEncounter(7,  14, 18, 15)); // Squirtle (rare!)

        addEncounters("Route 24",
                new WildEncounter(16, 15, 20, 30),  // Pidgey
                new WildEncounter(43, 15, 20, 30),  // Oddish
                new WildEncounter(69, 15, 20, 25),  // Bellsprout
                new WildEncounter(48, 15, 20, 15)); // Venonat

        addEncounters("Lavender Town",
                new WildEncounter(92, 20, 26, 40),  // Gastly
                new WildEncounter(93, 22, 28, 25),  // Haunter
                new WildEncounter(41, 20, 25, 35)); // Zubat

        addEncounters("Celadon City",
                new WildEncounter(43, 25, 30, 30),  // Oddish
                new WildEncounter(52, 25, 30, 30),  // Meowth
                new WildEncounter(113,25, 30, 10),  // Chansey (rare!)
                new WildEncounter(39, 25, 30, 30)); // Jigglypuff

        addEncounters("Fuchsia City",
                new WildEncounter(111,30, 38, 30),  // Rhyhorn
                new WildEncounter(115,30, 38, 20),  // Kangaskhan
                new WildEncounter(128,30, 38, 25),  // Tauros
                new WildEncounter(123,32, 38, 15),  // Scyther
                new WildEncounter(127,32, 38, 10)); // Pinsir

        addEncounters("Cinnabar Island",
                new WildEncounter(77, 35, 43, 30),  // Ponyta
                new WildEncounter(58, 35, 43, 30),  // Growlithe
                new WildEncounter(126,38, 45, 20),  // Magmar
                new WildEncounter(138,38, 42, 10),  // Omanyte (fossil)
                new WildEncounter(140,38, 42, 10)); // Kabuto (fossil)
    }

    private static void addLocation(String name, String desc, String[] connections,
                                     boolean wild, boolean trainers, boolean center) {
        locations.put(name, new Location(name, desc, connections, wild, trainers, center));
    }

    @SafeVarargs
    private static void addEncounters(String location, WildEncounter... encs) {
        List<WildEncounter> list = new ArrayList<>();
        for (WildEncounter e : encs) list.add(e);
        encounters.put(location, list);
    }

    public static Location getLocation(String name) {
        return locations.get(name);
    }

    public static List<String> getAllLocationNames() {
        return new ArrayList<>(locations.keySet());
    }

    /** Roll for a random wild Pokemon at this location */
    public static int[] rollWildEncounter(String location) {
        List<WildEncounter> table = encounters.get(location);
        if (table == null || table.isEmpty()) return null;

        int totalWeight = table.stream().mapToInt(e -> e.weight()).sum();
        int roll = rand.nextInt(totalWeight);
        int cumulative = 0;

        for (WildEncounter e : table) {
            cumulative += e.weight();
            if (roll < cumulative) {
                int level = e.minLevel() + rand.nextInt(e.maxLevel() - e.minLevel() + 1);
                return new int[]{e.pokemonId(), level};
            }
        }
        return null;
    }

    public static List<WildEncounter> getEncounterTable(String location) {
        return encounters.getOrDefault(location, new ArrayList<>());
    }
}
