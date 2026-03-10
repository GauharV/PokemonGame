package com.pokemon.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Trainer implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String trainerClass;
    private List<Pokemon> party;
    private String preDialog;
    private String postDialog;
    private int rewardMoney;
    private boolean defeated;

    public Trainer(String name, String trainerClass, String preDialog,
                   String postDialog, int rewardMoney) {
        this.name = name;
        this.trainerClass = trainerClass;
        this.preDialog = preDialog;
        this.postDialog = postDialog;
        this.rewardMoney = rewardMoney;
        this.party = new ArrayList<>();
        this.defeated = false;
    }

    public void addPokemon(Pokemon p) {
        party.add(p);
    }

    public boolean isDefeated() { return defeated; }
    public void setDefeated(boolean d) { this.defeated = d; }

    public Pokemon getActivePokemon() {
        for (Pokemon p : party) {
            if (!p.isFainted()) return p;
        }
        return null;
    }

    public boolean isAllFainted() {
        return party.stream().allMatch(Pokemon::isFainted);
    }

    public void healAll() {
        party.forEach(Pokemon::fullHeal);
    }

    // Getters
    public String getName() { return name; }
    public String getTrainerClass() { return trainerClass; }
    public List<Pokemon> getParty() { return party; }
    public String getPreDialog() { return preDialog; }
    public String getPostDialog() { return postDialog; }
    public int getRewardMoney() { return rewardMoney; }
    public String getFullTitle() { return trainerClass + " " + name; }
}
