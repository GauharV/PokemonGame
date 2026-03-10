package com.pokemon.engine;

import com.pokemon.data.PokemonDatabase;
import com.pokemon.model.Move;
import com.pokemon.model.Pokemon;

import java.util.ArrayList;
import java.util.List;

public class EvolutionEngine {

    public record EvolutionResult(Pokemon evolved, String message) {}

    /**
     * Check if a pokemon should evolve and perform evolution if needed.
     * Returns null if no evolution occurs.
     */
    public static EvolutionResult checkAndEvolve(Pokemon pokemon) {
        if (!pokemon.shouldEvolve()) return null;

        int evoId = pokemon.getEvolutionId();
        if (!PokemonDatabase.exists(evoId)) return null;

        String oldName = pokemon.getName();
        int level = pokemon.getLevel();

        // Create the evolved form with the same level
        Pokemon evolved = PokemonDatabase.create(evoId, level);
        evolved.setId(evoId);

        // Transfer XP and HP percentage
        double hpPercent = pokemon.getHpPercent();
        int newHp = (int)(evolved.getMaxHp() * hpPercent);
        evolved.heal(newHp);

        // Keep moves from previous form (merge with evolution's moves)
        List<Move> combinedMoves = new ArrayList<>();
        // Keep old pokemon's moves first (player may have customized)
        for (Move oldMove : pokemon.getMoves()) {
            boolean alreadyHas = evolved.getMoves().stream()
                    .anyMatch(m -> m.getName().equals(oldMove.getName()));
            if (!alreadyHas) combinedMoves.add(new Move(oldMove));
        }
        // Add new moves from evolution if slots remain
        for (Move newMove : evolved.getMoves()) {
            if (combinedMoves.size() < 4) combinedMoves.add(new Move(newMove));
        }
        // Apply combined moves
        evolved.getMoves().clear();
        combinedMoves.stream().limit(4).forEach(evolved::addMove);

        String message = "✨ What?! " + oldName + " is evolving!\n"
                + oldName + " evolved into " + evolved.getName() + "! ✨";

        return new EvolutionResult(evolved, message);
    }

    /** Get the full evolution chain description */
    public static String getEvolutionChain(int startId) {
        StringBuilder sb = new StringBuilder();
        int current = startId;
        boolean first = true;

        while (current != -1 && PokemonDatabase.exists(current)) {
            if (!first) sb.append(" → ");
            Pokemon p = PokemonDatabase.create(current, 1);
            sb.append(p.getName());
            current = p.getEvolutionId();
            if (current != -1) sb.append(" (Lv.").append(p.getEvolutionLevel()).append(")");
            first = false;
            if (current == startId) break; // safety: prevent infinite loop
        }
        return sb.toString();
    }
}
