package com.pokemon.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ImageLoader {

    // Official high-res artwork (PNG, ~475x475)
    private static final String ARTWORK_URL = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/%d.png";
    // Smaller sprites as fallback
    private static final String SPRITE_URL  = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/%d.png";

    private static final String CACHE_DIR = System.getProperty("user.home") + "/.pokemon_game/sprites/";

    private static final Map<Integer, BufferedImage> memoryCache = new ConcurrentHashMap<>();
    private static final Map<Integer, Boolean> loading = new ConcurrentHashMap<>();

    // Placeholder image while loading
    private static BufferedImage placeholder;

    static {
        // Create cache directory
        try { Files.createDirectories(Paths.get(CACHE_DIR)); } catch (Exception ignored) {}
        // Create placeholder
        placeholder = createPlaceholder();
    }

    /**
     * Get a Pokemon image. Returns placeholder immediately and calls callback
     * when the real image is loaded (on the EDT).
     */
    public static BufferedImage getImage(int pokemonId, Consumer<BufferedImage> onLoad) {
        // Already in memory
        if (memoryCache.containsKey(pokemonId)) {
            return memoryCache.get(pokemonId);
        }

        // Already loading
        if (Boolean.TRUE.equals(loading.get(pokemonId))) {
            return placeholder;
        }

        // Start async load
        loading.put(pokemonId, true);
        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() {
                return loadImage(pokemonId);
            }

            @Override
            protected void done() {
                try {
                    BufferedImage img = get();
                    if (img != null) {
                        memoryCache.put(pokemonId, img);
                        if (onLoad != null) onLoad.accept(img);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load image for Pokemon #" + pokemonId);
                } finally {
                    loading.remove(pokemonId);
                }
            }
        };
        worker.execute();
        return placeholder;
    }

    /** Synchronous load - use only for non-UI thread */
    public static BufferedImage getImageSync(int pokemonId) {
        if (memoryCache.containsKey(pokemonId)) return memoryCache.get(pokemonId);
        BufferedImage img = loadImage(pokemonId);
        if (img != null) memoryCache.put(pokemonId, img);
        return img != null ? img : placeholder;
    }

    private static BufferedImage loadImage(int pokemonId) {
        // 1. Check disk cache
        String cachePath = CACHE_DIR + pokemonId + ".png";
        File cacheFile = new File(cachePath);
        if (cacheFile.exists() && cacheFile.length() > 0) {
            try {
                return ImageIO.read(cacheFile);
            } catch (IOException e) {
                cacheFile.delete(); // corrupt cache, re-download
            }
        }

        // 2. Download official artwork
        BufferedImage img = downloadImage(String.format(ARTWORK_URL, pokemonId));

        // 3. Fallback to sprite if artwork failed
        if (img == null) {
            img = downloadImage(String.format(SPRITE_URL, pokemonId));
        }

        // 4. Save to disk cache
        if (img != null) {
            try {
                ImageIO.write(img, "PNG", cacheFile);
            } catch (IOException ignored) {}
        }

        return img;
    }

    private static BufferedImage downloadImage(String urlString) {
        try {
            URL url = URI.create(urlString).toURL();
            var connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "PokemonGame/1.0");
            try (InputStream is = connection.getInputStream()) {
                return ImageIO.read(is);
            }
        } catch (Exception e) {
            return null;
        }
    }

    /** Scale image to fit within target dimensions while maintaining aspect ratio */
    public static Image scaleImage(BufferedImage img, int targetW, int targetH) {
        if (img == null) return placeholder.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
        double scale = Math.min((double) targetW / img.getWidth(), (double) targetH / img.getHeight());
        int w = (int)(img.getWidth() * scale);
        int h = (int)(img.getHeight() * scale);
        return img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }

    private static BufferedImage createPlaceholder() {
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw Pokéball silhouette
        g.setColor(new Color(60, 60, 60));
        g.fillOval(10, 10, 180, 180);
        g.setColor(new Color(200, 50, 50));
        g.fillArc(10, 10, 180, 180, 0, 180);
        g.setColor(new Color(40, 40, 40));
        g.fillRect(10, 96, 180, 8);
        g.setColor(new Color(80, 80, 80));
        g.fillOval(80, 82, 40, 40);
        g.setColor(new Color(50, 50, 50, 120));
        g.fillOval(85, 87, 30, 30);

        g.setColor(new Color(180, 180, 180));
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("Loading...", 65, 170);
        g.dispose();
        return img;
    }

    public static BufferedImage getPlaceholder() { return placeholder; }

    public static void preloadBatch(int[] ids) {
        for (int id : ids) {
            if (!memoryCache.containsKey(id)) {
                getImage(id, null);
            }
        }
    }

    public static void clearMemoryCache() { memoryCache.clear(); }
}
