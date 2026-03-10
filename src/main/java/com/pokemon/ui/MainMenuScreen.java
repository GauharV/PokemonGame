package com.pokemon.ui;

import com.pokemon.engine.SaveManager;
import com.pokemon.util.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainMenuScreen extends JPanel {

    private final GameWindow window;
    private Timer animTimer;
    private long startTime;

    public MainMenuScreen(GameWindow window) {
        this.window = window;
        setBackground(new Color(10, 10, 20));
        setLayout(new BorderLayout());
        startTime = System.currentTimeMillis();
        buildUI();
        startAnimation();
    }

    private void buildUI() {
        // Custom-painted title area
        JPanel titlePanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintTitle((Graphics2D) g);
            }
        };
        titlePanel.setOpaque(false);
        titlePanel.setPreferredSize(new Dimension(GameWindow.WIDTH, 300));

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        PokeButton newGameBtn  = new PokeButton("NEW GAME",  new Color(210, 50, 50));
        PokeButton continueBtn = new PokeButton("CONTINUE",  new Color(40, 120, 210));
        PokeButton pokedexBtn  = new PokeButton("POKÉDEX",   new Color(50, 160, 50));
        PokeButton settingsBtn = new PokeButton("SETTINGS",  new Color(100, 100, 120));

        continueBtn.setEnabled(SaveManager.hasSaveFile());
        if (!SaveManager.hasSaveFile()) continueBtn.setAlpha(0.4f);

        newGameBtn.addActionListener(e -> onNewGame());
        continueBtn.addActionListener(e -> onContinue());
        pokedexBtn.addActionListener(e -> { window.showPokedex(); });
        settingsBtn.addActionListener(e -> onSettings());

        for (PokeButton btn : new PokeButton[]{newGameBtn, continueBtn, pokedexBtn, settingsBtn}) {
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonPanel.add(btn);
            buttonPanel.add(Box.createVerticalStrut(10));
        }

        // Footer
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        JLabel ver = new JLabel("v1.0  ·  151 Original Pokémon  ·  Kanto Region");
        ver.setForeground(new Color(60, 60, 80));
        ver.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footer.add(ver);
        footer.setPreferredSize(new Dimension(GameWindow.WIDTH, 30));

        add(titlePanel,  BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(footer,      BorderLayout.SOUTH);
    }

    private void paintTitle(Graphics2D g) {
        int w = getWidth(), h = 300;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Dark starfield background
        GradientPaint bg = new GradientPaint(0, 0, new Color(8, 8, 18), 0, h, new Color(15, 15, 35));
        g.setPaint(bg);
        g.fillRect(0, 0, w, h);

        long t = System.currentTimeMillis() - startTime;

        // Animated stars
        for (int i = 0; i < 80; i++) {
            double px = ((Math.sin(i * 127.3 + 1000) + 1) / 2.0) * w;
            double py = ((Math.cos(i * 311.7 + 2000) + 1) / 2.0) * h;
            float brightness = (float)(0.2 + 0.8 * Math.abs(Math.sin(t / 1200.0 + i * 0.7)));
            int sz = (i % 7 == 0) ? 3 : 1;
            g.setColor(new Color(1f, 1f, 1f, Math.min(1f, brightness)));
            g.fillOval((int)px, (int)py, sz, sz);
        }

        // Pokéball glow in background
        g.setColor(new Color(200, 40, 40, 18));
        g.fillOval(w/2 - 220, 20, 440, 440);
        g.setColor(new Color(200, 40, 40, 10));
        g.fillOval(w/2 - 160, 50, 320, 320);

        // "POKÉMON" letters with wave animation
        g.setFont(new Font("Arial Black", Font.BOLD, 72));
        FontMetrics fm = g.getFontMetrics();
        String title = "POKEMON";
        int totalW = fm.stringWidth(title);
        int startX = (w - totalW) / 2;
        int baseY  = 160;

        for (int i = 0; i < title.length(); i++) {
            String ch = String.valueOf(title.charAt(i));
            int charW = fm.charWidth(title.charAt(i));
            int offsetX = fm.stringWidth(title.substring(0, i));
            double wave = Math.sin(t / 400.0 + i * 0.7) * 8;
            float hue = (i * 40 + (t / 20f)) % 360 / 360f;
            Color letterColor = Color.getHSBColor(hue, 0.85f, 1.0f);

            // Shadow
            g.setColor(new Color(0, 0, 0, 100));
            g.drawString(ch, startX + offsetX + 4, (int)(baseY + wave) + 4);
            // Glow
            g.setColor(new Color(letterColor.getRed(), letterColor.getGreen(), letterColor.getBlue(), 60));
            g.drawString(ch, startX + offsetX - 2, (int)(baseY + wave) - 2);
            g.drawString(ch, startX + offsetX + 2, (int)(baseY + wave) + 2);
            // Letter
            g.setColor(letterColor);
            g.drawString(ch, startX + offsetX, (int)(baseY + wave));
        }

        // Subtitle
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.setColor(new Color(80, 160, 255));
        String sub = "KANTO ADVENTURE";
        g.drawString(sub, (w - g.getFontMetrics().stringWidth(sub)) / 2, 210);

        // Pikachu silhouette hint
        g.setFont(new Font("SansSerif", Font.PLAIN, 50));
        g.setColor(new Color(255, 220, 0, 40));
        g.drawString("⚡", w/2 - 20, 270);
    }

    private void startAnimation() {
        animTimer = new Timer(40, e -> repaint());
        animTimer.start();
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void onNewGame() {
        SoundManager.play(SoundManager.SoundEffect.MENU_SELECT);
        if (SaveManager.hasSaveFile()) {
            int c = JOptionPane.showConfirmDialog(this,
                    "A save already exists. Overwrite it?",
                    "New Game", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (c != JOptionPane.YES_OPTION) return;
        }
        String name = JOptionPane.showInputDialog(this, "Enter your trainer name:", "New Game", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.isBlank()) return;
        window.getStarterScreen().setPlayerName(name.trim());
        window.showScreen(GameWindow.SCREEN_STARTER);
    }

    private void onContinue() {
        SoundManager.play(SoundManager.SoundEffect.MENU_SELECT);
        try {
            var state = SaveManager.load();
            if (state != null) window.loadGame(state);
            else JOptionPane.showMessageDialog(this, "No save file found.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load: " + ex.getMessage());
        }
    }

    private void onSettings() {
        String[] opts = {
            "Sound: " + (SoundManager.isEnabled() ? "ON ✓" : "OFF ✗"),
            "Delete Save File",
            "Close"
        };
        int c = JOptionPane.showOptionDialog(this, "Settings", "Settings",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[2]);
        if (c == 0) {
            SoundManager.toggle();
            JOptionPane.showMessageDialog(this, "Sound is now " + (SoundManager.isEnabled() ? "ON" : "OFF"));
        } else if (c == 1) {
            int confirm = JOptionPane.showConfirmDialog(this, "Delete save file permanently?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                SaveManager.deleteSave();
                JOptionPane.showMessageDialog(this, "Save file deleted.");
            }
        }
    }

    public void onShow() { repaint(); }

    // ── PokeButton ────────────────────────────────────────────────────────────

    public static class PokeButton extends JButton {
        private final Color base;
        private float alpha = 1f;

        public PokeButton(String text, Color base) {
            super(text);
            this.base = base;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("Arial Black", Font.BOLD, 15));
            setForeground(Color.WHITE);
            setMaximumSize(new Dimension(260, 50));
            setPreferredSize(new Dimension(260, 50));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { repaint(); }
                @Override public void mouseExited (MouseEvent e) { repaint(); }
            });
        }

        public void setAlpha(float a) { this.alpha = a; }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            Color fill   = getModel().isRollover() ? base.brighter() : base.darker();
            Color border = getModel().isRollover() ? base.brighter().brighter() : base;

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
