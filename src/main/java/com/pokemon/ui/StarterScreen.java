package com.pokemon.ui;

import com.pokemon.data.PokemonDatabase;
import com.pokemon.model.Pokemon;
import com.pokemon.util.ImageLoader;
import com.pokemon.util.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class StarterScreen extends JPanel {

    private static final int[] STARTER_IDS = {1, 4, 7};
    private static final String[] FLAVORS  = {
        "Stubborn & resilient.\nA reliable all-rounder.",
        "Fiery & passionate.\nHigh attack, glass cannon.",
        "Calm & defensive.\nTank with strong moves."
    };

    private final GameWindow window;
    private String playerName = "Player";

    private final BufferedImage[] artworks = new BufferedImage[3];
    private int hoveredCard = -1;

    public StarterScreen(GameWindow window) {
        this.window = window;
        setBackground(new Color(12, 16, 28));
        setLayout(new BorderLayout());
        preloadArtwork();
        buildUI();
    }

    public void setPlayerName(String name) { this.playerName = name; }

    private void preloadArtwork() {
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            artworks[i] = ImageLoader.getImage(STARTER_IDS[i], img -> {
                artworks[idx] = img;
                repaint();
            });
        }
    }

    private void buildUI() {
        // ── Header ──────────────────────────────────────────────────────────
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(12, 16, 28));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setFont(new Font("Arial Black", Font.BOLD, 26));
                g2.setColor(new Color(255, 215, 0));
                String t = "CHOOSE YOUR PARTNER";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2, 48);

                g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
                g2.setColor(new Color(140, 160, 200));
                String s = "Professor Oak has 3 Pokémon waiting for you, " + playerName + "!";
                fm = g2.getFontMetrics();
                g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, 72);
            }
        };
        header.setPreferredSize(new Dimension(GameWindow.WIDTH, 88));
        header.setOpaque(false);

        // ── Cards ────────────────────────────────────────────────────────────
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 18, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(8, 36, 8, 36));

        for (int i = 0; i < 3; i++) {
            cardsPanel.add(buildCard(i));
        }

        // ── Footer ───────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 6));
        footer.setOpaque(false);

        JButton back = new MainMenuScreen.PokeButton("← BACK", new Color(80, 80, 100));
        back.addActionListener(e -> window.showScreen(GameWindow.SCREEN_MENU));
        footer.add(back);

        JLabel hint = new JLabel("Click on a Pokémon card to select your partner");
        hint.setForeground(new Color(100, 110, 140));
        hint.setFont(new Font("SansSerif", Font.ITALIC, 12));
        footer.add(hint);

        add(header,     BorderLayout.NORTH);
        add(cardsPanel, BorderLayout.CENTER);
        add(footer,     BorderLayout.SOUTH);
    }

    private JPanel buildCard(int index) {
        int id = STARTER_IDS[index];
        Pokemon template = PokemonDatabase.create(id, 5);

        Color[] typeColors = {
            new Color(80, 160, 80),   // Grass
            new Color(210, 90, 30),   // Fire
            new Color(50, 110, 210),  // Water
        };
        Color typeColor = typeColors[index];

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawCard((Graphics2D) g, index, template, typeColor);
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(240, 460));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hoveredCard = index; repaint(); }
            @Override public void mouseExited (MouseEvent e) { hoveredCard = -1;    repaint(); }
            @Override public void mouseClicked(MouseEvent e) { confirmChoice(index, id, template.getName()); }
        });
        return card;
    }

    private void drawCard(Graphics2D g, int index, Pokemon template, Color accent) {
        int w = getWidth(), h = getHeight();
        boolean hovered = (hoveredCard == index);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Card background
        GradientPaint bg = hovered
                ? new GradientPaint(0, 0, new Color(accent.getRed()/2, accent.getGreen()/2, accent.getBlue()/2),
                                    0, h, new Color(20, 24, 40))
                : new GradientPaint(0, 0, new Color(22, 28, 46), 0, h, new Color(14, 18, 30));
        g.setPaint(bg);
        g.fillRoundRect(0, 0, w, h, 18, 18);

        // Border
        g.setColor(hovered ? accent : new Color(40, 50, 80));
        g.setStroke(new BasicStroke(hovered ? 2.5f : 1.5f));
        g.drawRoundRect(1, 1, w-2, h-2, 18, 18);

        // Pokemon artwork
        if (artworks[index] != null) {
            Image scaled = ImageLoader.scaleImage(artworks[index], 170, 170);
            int iw = scaled.getWidth(null), ih = scaled.getHeight(null);
            g.drawImage(scaled, (w - iw) / 2, 22, null);
        } else {
            g.setColor(new Color(60, 70, 100));
            g.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g.drawString("Loading...", w/2 - 30, 110);
        }

        // Type badge
        String typeName = template.getType1().getDisplayName().toUpperCase();
        g.setColor(accent);
        g.fillRoundRect(w/2 - 36, 200, 72, 20, 8, 8);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial Black", Font.BOLD, 9));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(typeName, w/2 - fm.stringWidth(typeName)/2, 214);

        // Name
        g.setFont(new Font("Arial Black", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        fm = g.getFontMetrics();
        g.drawString(template.getName(), (w - fm.stringWidth(template.getName())) / 2, 242);

        // Flavor text
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(160, 170, 200));
        String[] lines = FLAVORS[index].split("\n");
        for (int li = 0; li < lines.length; li++) {
            fm = g.getFontMetrics();
            g.drawString(lines[li], (w - fm.stringWidth(lines[li])) / 2, 262 + li * 16);
        }

        // Stat bars
        int barTop = 302;
        String[] labels = {"HP", "ATK", "DEF", "SPD"};
        int[]    values = {
            template.getBaseHp(), template.getBaseAtk(),
            template.getBaseDef(), template.getBaseSpd()
        };
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        for (int s = 0; s < 4; s++) {
            int y = barTop + s * 22;
            g.setColor(new Color(120, 130, 160));
            g.drawString(labels[s], 14, y + 10);

            int barX = 46, barW = w - 62;
            g.setColor(new Color(30, 35, 55));
            g.fillRoundRect(barX, y, barW, 11, 4, 4);

            float pct = values[s] / 130f;
            Color barColor = values[s] >= 65 ? new Color(80, 210, 80)
                           : values[s] >= 50 ? new Color(220, 200, 50)
                           :                   new Color(220, 90, 70);
            g.setColor(barColor);
            g.fillRoundRect(barX, y, (int)(barW * Math.min(1f, pct)), 11, 4, 4);

            g.setColor(new Color(180, 190, 220));
            g.setFont(new Font("SansSerif", Font.PLAIN, 9));
            g.drawString(String.valueOf(values[s]), w - 14 - g.getFontMetrics().stringWidth(String.valueOf(values[s])), y + 10);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
        }

        // Choose button (shown on hover)
        if (hovered) {
            g.setColor(accent);
            g.fillRoundRect(12, h - 48, w - 24, 36, 10, 10);
            g.setFont(new Font("Arial Black", Font.BOLD, 12));
            g.setColor(Color.WHITE);
            String btn = "Choose " + template.getName() + "!";
            fm = g.getFontMetrics();
            g.drawString(btn, (w - fm.stringWidth(btn)) / 2, h - 24);
        }
    }

    private void confirmChoice(int index, int id, String name) {
        int opt = JOptionPane.showConfirmDialog(this,
                playerName + " chose " + name + "!\nStart your adventure together?",
                "Confirm Choice", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            SoundManager.play(SoundManager.SoundEffect.WILD_ENCOUNTER);
            window.startNewGame(playerName, id);
        }
    }
}
