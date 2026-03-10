package com.pokemon.data;

import com.pokemon.model.Trainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainerDatabase {

    public record LocationTrainer(String location, Trainer trainer) {}

    private static final Map<String, List<Trainer>> trainersByLocation = new HashMap<>();

    static {
        // ---- Route 1 ----
        addTrainer("Route 1", makeTrainer("Bug Catcher", "Joey", "Bug", "Hey! You look like a trainer!",
                "You're strong for a new trainer!", 150,
                new int[][]{{10,5},{13,6}})); // Caterpie, Weedle

        // ---- Viridian City ----
        addTrainer("Viridian City", makeTrainer("Youngster", "Tim", "Normal", "I'll show you a real battle!",
                "You beat me fair and square...", 200,
                new int[][]{{19,8},{21,9}})); // Rattata, Spearow

        // ---- Mt. Moon ----
        addTrainer("Mt. Moon", makeTrainer("Team Rocket", "Grunt", "Rocket", "Hand over your Pokemon!",
                "Team Rocket will not forget this!", 400,
                new int[][]{{41,10},{88,12}})); // Zubat, Grimer
        addTrainer("Mt. Moon", makeTrainer("Lass", "Kira", "Normal", "Wanna battle? Don't hold back!",
                "You're really good...", 250,
                new int[][]{{35,12},{39,12}})); // Clefairy, Jigglypuff

        // ---- Cerulean City ---- (Misty - Gym Leader)
        Trainer misty = makeTrainer("Gym Leader", "Misty", "Gym", "Prepare to be overwhelmed by my Water-type Pokemon!",
                "You defeated me! You're worthy of the Cascade Badge!", 2400,
                new int[][]{{54,18},{121,21}}); // Psyduck, Starmie
        addTrainer("Cerulean City", misty);

        // ---- Route 24 ----
        addTrainer("Route 24", makeTrainer("Camper", "Ricky", "Normal", "Let's see what you've got!",
                "Back to training...", 300,
                new int[][]{{16,15},{27,16}})); // Pidgey, Sandshrew
        addTrainer("Route 24", makeTrainer("Team Rocket", "Admin", "Rocket", "I'll take your Pokemon by force!",
                "Impossible! Team Rocket doesn't lose!", 600,
                new int[][]{{23,18},{92,18}})); // Ekans, Gastly

        // ---- Lavender Town ----
        addTrainer("Lavender Town", makeTrainer("Channeler", "Sandra", "Ghost", "The spirits guide my Pokemon!",
                "The spirits... they told me you'd win...", 800,
                new int[][]{{92,22},{93,25}})); // Gastly, Haunter
        addTrainer("Lavender Town", makeTrainer("Team Rocket", "Jessie & James", "Rocket", "Prepare for trouble! And make it double!",
                "We will be back! Team Rocket, blasting off again!", 1000,
                new int[][]{{52,25},{41,25},{109,25}})); // Meowth, Zubat, Koffing

        // ---- Celadon City ---- (Erika - Gym Leader)
        Trainer erika = makeTrainer("Gym Leader", "Erika", "Gym", "Welcome to the Celadon Gym! My Grass-types are ready for you!",
                "Oh, you're such a strong trainer! The Rainbow Badge is yours!", 2800,
                new int[][]{{71,29},{114,32},{45,34}}); // Victreebel, Tangela, Vileplume
        addTrainer("Celadon City", erika);

        // ---- Fuchsia City ----
        addTrainer("Fuchsia City", makeTrainer("Biker", "Rick", "Normal", "Bikers are tough! Don't think you can handle us!",
                "My bike gang won't hear about this...", 700,
                new int[][]{{88,28},{109,28},{41,30}})); // Grimer, Koffing, Golbat

        // ---- Cinnabar Island ---- (Blaine - Gym Leader)
        Trainer blaine = makeTrainer("Gym Leader", "Blaine", "Gym", "My fiery Pokemon will incinerate you!",
                "Such a cool trainer! You've earned the Volcano Badge!", 3600,
                new int[][]{{77,42},{78,40},{59,47},{126,54}}); // Ponyta, Rapidash, Arcanine, Magmar
        addTrainer("Cinnabar Island", blaine);

        // ---- Viridian Gym ---- (Giovanni - Final Gym Leader)
        Trainer giovanni = makeTrainer("Gym Leader", "Giovanni", "Team Rocket", "Foolish child! You can't stop Team Rocket!",
                "Hmph! I'll acknowledge your skill. Here, take the Earth Badge.",5000,
                new int[][]{{111,45},{28,47},{95,45},{31,50}}); // Rhyhorn, Sandslash, Onix, Nidoqueen
        addTrainer("Viridian Gym", giovanni);

        // ---- Pokemon League ---- (Elite Four - Optional endgame)
        addTrainer("Pokemon League", makeTrainer("Elite Four", "Lorelei", "Elite", "There is no advantage in fighting me! Surrender now!",
                "I see you are a top-notch trainer. The others are no pushover!", 8000,
                new int[][]{{91,54},{87,54},{131,56},{80,58},{124,58}}));

        addTrainer("Pokemon League", makeTrainer("Champion", "Blue", "Champion", "Smell ya later! Oh wait, it's time to fight!",
                "You did it... You beat me. You truly are a Pokemon Master.",15000,
                new int[][]{{59,63},{103,63},{130,65},{143,65},{65,67},{6,68}}));
    }

    private static Trainer makeTrainer(String trainerClass, String name, String theme,
                                        String pre, String post, int money, int[][] pokemon) {
        Trainer t = new Trainer(name, trainerClass, pre, post, money);
        for (int[] p : pokemon) {
            t.addPokemon(PokemonDatabase.create(p[0], p[1]));
        }
        return t;
    }

    private static void addTrainer(String location, Trainer trainer) {
        trainersByLocation.computeIfAbsent(location, k -> new ArrayList<>()).add(trainer);
    }

    public static List<Trainer> getTrainersAt(String location) {
        return trainersByLocation.getOrDefault(location, new ArrayList<>());
    }

    public static Trainer getGymLeader(String location) {
        return getTrainersAt(location).stream()
                .filter(t -> t.getTrainerClass().equals("Gym Leader"))
                .findFirst().orElse(null);
    }
}
