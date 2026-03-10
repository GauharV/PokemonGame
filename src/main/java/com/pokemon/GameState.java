package com.pokemon;

import com.pokemon.model.Pokemon;
import com.pokemon.model.Trainer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private String playerName;
    private List<Pokemon> party;
    private int money;
    private String currentLocation;
    private Set<String> defeatedTrainers;
    private Set<Integer> seenPokemon;
    private Set<Integer> caughtPokemon;
    private int totalBattles;
    private int totalWins;
    private long playtimeMillis;
    private long sessionStart;
    private int pokeballs;
    private int greatballs;
    private int ultraballs;

    public GameState(String playerName, Pokemon starterPokemon) {
        this.playerName = playerName;
        this.party = new ArrayList<>();
        this.party.add(starterPokemon);
        this.money = 3000;
        this.currentLocation = "Pallet Town";
        this.defeatedTrainers = new HashSet<>();
        this.seenPokemon = new HashSet<>();
        this.caughtPokemon = new HashSet<>();
        this.totalBattles = 0;
        this.totalWins = 0;
        this.playtimeMillis = 0;
        this.sessionStart = System.currentTimeMillis();
        this.pokeballs  = 5;   // start with 5 free Pokéballs
        this.greatballs = 0;
        this.ultraballs = 0;
        seenPokemon.add(starterPokemon.getId());
        caughtPokemon.add(starterPokemon.getId());
    }

    public void startSession() {
        sessionStart = System.currentTimeMillis();
    }

    public void updatePlaytime() {
        playtimeMillis += System.currentTimeMillis() - sessionStart;
        sessionStart = System.currentTimeMillis();
    }

    public String getPlaytimeString() {
        long seconds = playtimeMillis / 1000;
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public boolean addPokemonToParty(Pokemon p) {
        if (party.size() < 6) {
            party.add(p);
            seenPokemon.add(p.getId());
            caughtPokemon.add(p.getId());
            return true;
        }
        return false;
    }

    public Pokemon getFirstAlive() {
        for (Pokemon p : party) {
            if (!p.isFainted()) return p;
        }
        return null;
    }

    public boolean isBlackedOut() {
        return party.stream().allMatch(Pokemon::isFainted);
    }

    public void healAllPokemon() {
        party.forEach(Pokemon::fullHeal);
    }

    public void recordTrainerDefeated(String trainerId) {
        defeatedTrainers.add(trainerId);
    }

    public boolean isTrainerDefeated(String trainerId) {
        return defeatedTrainers.contains(trainerId);
    }

    // Getters & Setters
    public String getPlayerName() { return playerName; }
    public List<Pokemon> getParty() { return party; }
    public int getMoney() { return money; }
    public void addMoney(int amount) { money += amount; }
    public void spendMoney(int amount) { money = Math.max(0, money - amount); }
    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String loc) { currentLocation = loc; }
    public Set<String> getDefeatedTrainers() { return defeatedTrainers; }
    public Set<Integer> getSeenPokemon() { return seenPokemon; }
    public Set<Integer> getCaughtPokemon() { return caughtPokemon; }
    public void seePokedmon(int id) { seenPokemon.add(id); }
    public int getTotalBattles() { return totalBattles; }
    public int getTotalWins() { return totalWins; }
    public void recordBattle(boolean won) { totalBattles++; if (won) totalWins++; }

    // ── Pokéballs ──────────────────────────────────────────────────────────────
    public int  getPokeballs()                { return pokeballs;  }
    public int  getGreatballs()               { return greatballs; }
    public int  getUltraballs()               { return ultraballs; }
    public void addPokeballs(int n)           { pokeballs  += n; }
    public void addGreatballs(int n)          { greatballs += n; }
    public void addUltraballs(int n)          { ultraballs += n; }
    public boolean usePokeball()  { if (pokeballs  > 0) { pokeballs--;  return true; } return false; }
    public boolean useGreatball() { if (greatballs > 0) { greatballs--; return true; } return false; }
    public boolean useUltraball() { if (ultraballs > 0) { ultraballs--; return true; } return false; }
}