package com.pokemon.util;

import javazoom.jl.player.Player;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundManager {

    private static boolean enabled = true;

    // Single-thread executor for synth sounds
    private static final ExecutorService sfxExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SFX-Thread");
        t.setDaemon(true);
        return t;
    });

    // Dedicated thread for the looping battle music
    private static Thread musicThread;
    private static volatile boolean musicPlaying = false;
    private static volatile boolean stopMusic    = false;
    private static volatile Player  currentPlayer = null;  // ← hold reference so we can close it

    public enum SoundEffect {
        BATTLE_START, MOVE_HIT, SUPER_EFFECTIVE, NOT_VERY_EFFECTIVE,
        LEVEL_UP, EVOLUTION, FAINT, HEAL, SAVE, MENU_SELECT,
        WILD_ENCOUNTER, TRAINER_ENCOUNTER, VICTORY,
        STOP_MUSIC
    }

    public static void play(SoundEffect effect) {
        if (!enabled) return;
        switch (effect) {
            case WILD_ENCOUNTER, TRAINER_ENCOUNTER, BATTLE_START -> startBattleMusic();
            case STOP_MUSIC                                       -> stopBattleMusic();
            case VICTORY -> {
                stopBattleMusic();
                sfxExecutor.submit(() -> safePlayTone(
                        new int[]{523,659,784,523,659,784,1047,784,659,523},
                        new int[]{150,150,150,150,150,150, 300,150,150,400}, 0.5f));
            }
            default -> sfxExecutor.submit(() -> playSfx(effect));
        }
    }

    // ── Battle music (loops the MP3 until stopped) ────────────────────────────

    private static void startBattleMusic() {
        if (musicPlaying) return;         // already playing
        stopMusic    = false;
        musicPlaying = true;

        musicThread = new Thread(() -> {
            while (!stopMusic) {
                try {
                    InputStream raw = SoundManager.class
                            .getResourceAsStream("/battle_theme.mp3");
                    if (raw == null) {
                        System.err.println("battle_theme.mp3 not found in resources!");
                        musicPlaying = false;
                        return;
                    }
                    currentPlayer = new Player(new BufferedInputStream(raw));
                    currentPlayer.play();   // blocks until track ends OR close() is called
                    currentPlayer = null;
                } catch (Exception e) {
                    if (!stopMusic) e.printStackTrace();
                }
            }
            musicPlaying = false;
        }, "Music-Thread");

        musicThread.setDaemon(true);
        musicThread.start();
    }

    public static void stopBattleMusic() {
        stopMusic    = true;
        musicPlaying = false;
        if (currentPlayer != null) {
            try { currentPlayer.close(); } catch (Exception ignored) {}
            currentPlayer = null;
        }
        if (musicThread != null) musicThread.interrupt();
    }

    // ── Synth sound effects ───────────────────────────────────────────────────

    private static void playSfx(SoundEffect effect) {
        try {
            switch (effect) {
                case MOVE_HIT          -> playNoise(80, 0.3f);
                case SUPER_EFFECTIVE   -> safePlayTone(new int[]{660,880,1100},     new int[]{80,80,150},       0.5f);
                case NOT_VERY_EFFECTIVE-> safePlayTone(new int[]{330,220},           new int[]{100,200},         0.3f);
                case LEVEL_UP          -> safePlayTone(new int[]{523,659,784,1047},  new int[]{100,100,100,300}, 0.5f);
                case EVOLUTION         -> playEvolutionSound();
                case FAINT             -> safePlayTone(new int[]{440,330,220,165},   new int[]{100,150,200,400}, 0.4f);
                case HEAL              -> safePlayTone(new int[]{523,659,784},        new int[]{100,100,200},     0.4f);
                case SAVE              -> safePlayTone(new int[]{784,1047},           new int[]{100,300},         0.3f);
                case MENU_SELECT       -> safePlayTone(new int[]{880},               new int[]{80},              0.2f);
                default -> {}
            }
        } catch (Exception ignored) {}
    }

    private static void playEvolutionSound() throws Exception {
        int[] freqs = {262,330,392,523,659,784,1047,1319,1047,784,659,523,392,330,262};
        int[] durs  = { 60, 60, 60, 60, 60, 60,  80,  80,  80, 60, 60, 60, 60, 60,300};
        safePlayTone(freqs, durs, 0.4f);
    }

    private static void safePlayTone(int[] frequencies, int[] durations, float volume) {
        try {
            AudioFormat format = new AudioFormat(44100, 8, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) return;
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(format);
                line.start();
                for (int i = 0; i < frequencies.length; i++) {
                    line.write(generateTone(frequencies[i], durations[i], 44100, volume), 0,
                            (int)(44100 * durations[i] / 1000.0));
                }
                line.drain();
            }
        } catch (Exception ignored) {}
    }

    private static void playNoise(int durationMs, float volume) {
        try {
            AudioFormat format = new AudioFormat(44100, 8, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) return;
            int samples = (int)(44100 * durationMs / 1000.0);
            byte[] buf = new byte[samples];
            for (int i = 0; i < samples; i++)
                buf[i] = (byte)((Math.random() * 2 - 1) * 127 * volume);
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(format);
                line.start();
                line.write(buf, 0, buf.length);
                line.drain();
            }
        } catch (Exception ignored) {}
    }

    private static byte[] generateTone(int freq, int durationMs, int sampleRate, float volume) {
        int samples = (int)(sampleRate * durationMs / 1000.0);
        byte[] buf  = new byte[samples];
        for (int i = 0; i < samples; i++) {
            double angle = 2.0 * Math.PI * freq * i / sampleRate;
            double fade  = Math.min(1.0, Math.min(i / 50.0, (samples - i) / 50.0));
            buf[i] = (byte)(Math.sin(angle) * 127 * volume * fade);
        }
        return buf;
    }

    // ── Settings ──────────────────────────────────────────────────────────────

    public static boolean isEnabled()        { return enabled; }
    public static void    setEnabled(boolean e) {
        enabled = e;
        if (!e) stopBattleMusic();
    }
    public static void toggle() { setEnabled(!enabled); }
}