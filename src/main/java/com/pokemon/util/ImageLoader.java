package com.pokemon.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Loads Pokemon sprites from the bundled resources/sprites/ folder.
 *
 * Put your PNG files in:  src/main/resources/sprites/1.png, 2.png, ...
 * Run download_sprites.py once to populate that folder.
 */
public class ImageLoader {

    private static final Map<Integer, BufferedImage> cache   = new ConcurrentHashMap<>();
    private static final Map<Integer, Boolean>       loading = new ConcurrentHashMap<>();

    // ── Load ──────────────────────────────────────────────────────────────────

    /**
     * Returns the image immediately if cached, otherwise returns a placeholder
     * and calls onLoad on the EDT when done.
     */
    public static BufferedImage getImage(int pokemonId, Consumer<BufferedImage> onLoad) {
        if (cache.containsKey(pokemonId)) return cache.get(pokemonId);
        if (Boolean.TRUE.equals(loading.get(pokemonId))) return placeholder(pokemonId);

        loading.put(pokemonId, true);
        new SwingWorker<BufferedImage, Void>() {
            @Override protected BufferedImage doInBackground() {
                return loadFromResources(pokemonId);
            }
            @Override protected void done() {
                try {
                    BufferedImage img = get();
                    if (img == null) img = placeholder(pokemonId);
                    cache.put(pokemonId, img);
                    if (onLoad != null) onLoad.accept(img);
                } catch (Exception e) {
                    System.err.println("[ImageLoader] error for #" + pokemonId + ": " + e.getMessage());
                } finally {
                    loading.remove(pokemonId);
                }
            }
        }.execute();

        return placeholder(pokemonId);
    }

    public static BufferedImage getImageSync(int pokemonId) {
        if (cache.containsKey(pokemonId)) return cache.get(pokemonId);
        BufferedImage img = loadFromResources(pokemonId);
        if (img == null) img = placeholder(pokemonId);
        cache.put(pokemonId, img);
        return img;
    }

    // ── Internal load ─────────────────────────────────────────────────────────

    private static BufferedImage loadFromResources(int pokemonId) {
        String fileName = pokemonId + ".png";

        // 1. Try classpath (works after mvn package or when resources are on classpath)
        try {
            URL url = ImageLoader.class.getResource("/sprites/" + fileName);
            if (url != null) {
                BufferedImage img = ImageIO.read(url);
                if (img != null) {
                    System.out.println("[ImageLoader] Classpath OK: #" + pokemonId);
                    return img;
                }
            }
        } catch (Exception e) { /* try next */ }

        // 2. Try every likely absolute and relative path
        String home = System.getProperty("user.home");
        String userDir = System.getProperty("user.dir");
        String[] paths = {
            // Exact path where download_sprites.py saved them on Windows
            home + "\\.vscode\\PokemonGame\\src\\main\\resources\\sprites\\" + fileName,
            home + "/PokemonGame/src/main/resources/sprites/" + fileName,
            // Relative to wherever Java's working directory is
            userDir + "/src/main/resources/sprites/" + fileName,
            userDir + "\\src\\main\\resources\\sprites\\" + fileName,
            "src/main/resources/sprites/" + fileName,
            "sprites/" + fileName,
        };

        for (String p : paths) {
            File f = new File(p);
            if (f.exists() && f.length() > 500) {
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        System.out.println("[ImageLoader] Found at: " + p);
                        return img;
                    }
                } catch (Exception e) { /* try next */ }
            }
        }

        // 3. Print all tried paths so the user can see what's wrong
        System.err.println("[ImageLoader] NOT FOUND #" + pokemonId);
        System.err.println("  user.home = " + home);
        System.err.println("  user.dir  = " + userDir);
        System.err.println("  Tried paths:");
        for (String p : paths) System.err.println("    " + p + " -> exists=" + new File(p).exists());
        return null;
    }

    // ── Scale ─────────────────────────────────────────────────────────────────

    public static Image scaleImage(BufferedImage img, int targetW, int targetH) {
        if (img == null) return placeholder(0).getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
        double scale = Math.min((double) targetW / img.getWidth(),
                                (double) targetH / img.getHeight());
        int w = Math.max(1, (int)(img.getWidth()  * scale));
        int h = Math.max(1, (int)(img.getHeight() * scale));
        return img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }

    // ── Placeholder ───────────────────────────────────────────────────────────

    public static BufferedImage placeholder(int id) {
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float hue = (id * 37f % 360f) / 360f;
        Color base = Color.getHSBColor(hue, 0.55f, 0.60f);
        g.setColor(base);
        g.fillOval(10, 10, 180, 180);
        g.setColor(base.darker());
        g.setStroke(new java.awt.BasicStroke(4));
        g.drawOval(10, 10, 180, 180);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial Black", Font.BOLD, 30));
        String lbl = "#" + id;
        FontMetrics fm = g.getFontMetrics();
        g.drawString(lbl, 100 - fm.stringWidth(lbl) / 2, 108);

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(new Color(255, 255, 200));
        g.drawString("No sprite", 62, 135);
        g.drawString("Run download_sprites.py", 18, 153);

        g.dispose();
        return img;
    }

    public static BufferedImage getPlaceholder() { return placeholder(0); }
    public static void preloadBatch(int[] ids) { for (int id : ids) getImage(id, null); }
    public static void clearMemoryCache() { cache.clear(); }
}