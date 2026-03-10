package com.pokemon.engine;

import com.pokemon.GameState;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SaveManager {

    private static final String SAVE_DIR = System.getProperty("user.home") + "/.pokemon_game";
    private static final String SAVE_FILE = SAVE_DIR + "/save.dat";
    private static final String BACKUP_FILE = SAVE_DIR + "/save_backup.dat";

    public static void save(GameState state) throws IOException {
        // Update playtime before saving
        state.updatePlaytime();

        // Ensure directory exists
        Files.createDirectories(Paths.get(SAVE_DIR));

        // Backup existing save
        File current = new File(SAVE_FILE);
        if (current.exists()) {
            File backup = new File(BACKUP_FILE);
            current.renameTo(backup);
        }

        // Write new save
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(SAVE_FILE)))) {
            oos.writeObject(state);
        }
        System.out.println("Game saved to: " + SAVE_FILE);
    }

    public static GameState load() throws IOException, ClassNotFoundException {
        File saveFile = new File(SAVE_FILE);
        if (!saveFile.exists()) return null;

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(saveFile)))) {
            GameState state = (GameState) ois.readObject();
            state.startSession();
            System.out.println("Game loaded: " + state.getPlayerName());
            return state;
        }
    }

    public static boolean hasSaveFile() {
        return new File(SAVE_FILE).exists();
    }

    public static void deleteSave() {
        new File(SAVE_FILE).delete();
        new File(BACKUP_FILE).delete();
    }

    public static String getSavePath() { return SAVE_FILE; }
}
