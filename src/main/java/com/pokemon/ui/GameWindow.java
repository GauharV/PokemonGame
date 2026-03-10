package com.pokemon.ui;

import com.pokemon.GameState;
import com.pokemon.data.PokemonDatabase;
import com.pokemon.engine.SaveManager;
import com.pokemon.model.Pokemon;
import com.pokemon.model.Trainer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameWindow extends JFrame {

    public static final int WIDTH  = 1280;
    public static final int HEIGHT = 860;

    public static final String SCREEN_MENU      = "MENU";
    public static final String SCREEN_STARTER   = "STARTER";
    public static final String SCREEN_OVERWORLD = "OVERWORLD";
    public static final String SCREEN_BATTLE    = "BATTLE";
    public static final String SCREEN_POKEDEX   = "POKEDEX";

    private final CardLayout cardLayout;
    private final JPanel     cardPanel;

    private GameState gameState;

    private MainMenuScreen  menuScreen;
    private StarterScreen   starterScreen;
    private OverworldScreen overworldScreen;
    private WalkingScreen   walkingScreen;
    private BattleScreen    battleScreen;
    private PokedexScreen   pokedexScreen;

    public static final String SCREEN_WALKING = "WALKING";

    public GameWindow() {
        setTitle("Pokémon — Kanto Adventure");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(new Color(15, 15, 25));

        menuScreen      = new MainMenuScreen(this);
        starterScreen   = new StarterScreen(this);
        overworldScreen = new OverworldScreen(this);
        walkingScreen   = new WalkingScreen(this);
        battleScreen    = new BattleScreen(this);
        pokedexScreen   = new PokedexScreen(this);

        cardPanel.add(menuScreen,      SCREEN_MENU);
        cardPanel.add(starterScreen,   SCREEN_STARTER);
        cardPanel.add(overworldScreen, SCREEN_OVERWORLD);
        cardPanel.add(walkingScreen,   SCREEN_WALKING);
        cardPanel.add(battleScreen,    SCREEN_BATTLE);
        cardPanel.add(pokedexScreen,   SCREEN_POKEDEX);

        add(cardPanel);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (gameState != null) {
                    int opt = JOptionPane.showConfirmDialog(
                            GameWindow.this,
                            "Save before quitting?",
                            "Quit",
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    if (opt == JOptionPane.CANCEL_OPTION) return;
                    if (opt == JOptionPane.YES_OPTION) {
                        try { SaveManager.save(gameState); }
                        catch (Exception ex) { ex.printStackTrace(); }
                    }
                }
                System.exit(0);
            }
        });

        showScreen(SCREEN_MENU);
        setVisible(true);
    }

    public void showScreen(String name) {
        cardLayout.show(cardPanel, name);
        switch (name) {
            case SCREEN_MENU    -> menuScreen.onShow();
            case SCREEN_WALKING -> walkingScreen.onShow(gameState);
            case SCREEN_POKEDEX -> pokedexScreen.onShow(gameState);
        }
    }

    public void startNewGame(String playerName, int starterId) {
        Pokemon starter = PokemonDatabase.create(starterId, 5);
        gameState = new GameState(playerName, starter);
        walkingScreen.onShow(gameState);
        showScreen(SCREEN_WALKING);
    }

    public void loadGame(GameState loaded) {
        this.gameState = loaded;
        walkingScreen.onShow(gameState);
        showScreen(SCREEN_WALKING);
    }

    public void startWildBattle(Pokemon wild) {
        battleScreen.startWildBattle(gameState, wild);
        showScreen(SCREEN_BATTLE);
    }

    public void startTrainerBattle(Trainer trainer) {
        battleScreen.startTrainerBattle(gameState, trainer);
        showScreen(SCREEN_BATTLE);
    }

    public void endBattle() {
        walkingScreen.onShow(gameState);
        showScreen(SCREEN_WALKING);
    }

    public void showPokedex() {
        pokedexScreen.onShow(gameState);
        showScreen(SCREEN_POKEDEX);
    }

    public GameState     getGameState()             { return gameState; }
    public void          setGameState(GameState gs) { this.gameState = gs; }
    public BattleScreen  getBattleScreen()          { return battleScreen; }
    public StarterScreen getStarterScreen()         { return starterScreen; }
}