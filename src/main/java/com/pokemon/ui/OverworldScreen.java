package com.pokemon.ui;

import com.pokemon.GameState;
import com.pokemon.data.LocationDatabase;
import com.pokemon.data.PokemonDatabase;
import com.pokemon.data.TrainerDatabase;
import com.pokemon.engine.SaveManager;
import com.pokemon.model.Pokemon;
import com.pokemon.model.Trainer;
import com.pokemon.util.ImageLoader;
import com.pokemon.util.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

public class OverworldScreen extends JPanel {

    private final GameWindow window;
    private GameState gameState;
    private final Random rand = new Random();

    public OverworldScreen(GameWindow window) {
        this.window = window;
        setBackground(new Color(12, 18, 12));
        setLayout(new BorderLayout());
    }

    public void onShow(GameState gs) {
        this.gameState = gs;
        removeAll();
        if (gs != null) buildUI();
        revalidate();
        repaint();
    }

    private void buildUI() {
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        h.setBackground(new Color(14, 22, 14));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(40, 80, 40)));

        label(h, "📍 " + gameState.getCurrentLocation(), new Color(80, 220, 80),  13, Font.BOLD);
        label(h, "💰 $" + gameState.getMoney(),          new Color(255, 215, 0),   12, Font.BOLD);
        label(h, "⚪ ×" + gameState.getPokeballs()
                + "  🔵 ×" + gameState.getGreatballs()
                + "  🟡 ×" + gameState.getUltraballs(),  new Color(180, 200, 230), 11, Font.PLAIN);
        label(h, "👤 " + gameState.getPlayerName(),      new Color(160, 180, 220), 12, Font.PLAIN);
        label(h, "⏱ "  + gameState.getPlaytimeString(),  new Color(120, 160, 120), 11, Font.PLAIN);

        h.add(Box.createHorizontalGlue());

        h.add(hdrBtn("💾 Save",    new Color(40, 110, 40),  e -> onSave()));
        h.add(hdrBtn("📖 Pokédex", new Color(40, 60, 180),  e -> window.showPokedex()));
        h.add(hdrBtn("🏠 Menu",    new Color(140, 40, 40),  e -> window.showScreen(GameWindow.SCREEN_MENU)));

        return h;
    }

    private void label(JPanel p, String text, Color color, int size, int style) {
        JLabel l = new JLabel(text);
        l.setForeground(color);
        l.setFont(new Font("SansSerif", style, size));
        p.add(l);
    }

    private JButton hdrBtn(String text, Color color, java.awt.event.ActionListener action) {
        JButton b = new MainMenuScreen.PokeButton(text, color);
        b.setMaximumSize(new Dimension(110, 34));
        b.setPreferredSize(new Dimension(110, 34));
        b.setFont(new Font("Arial Black", Font.BOLD, 11));
        b.addActionListener(action);
        return b;
    }

    // ── Content ───────────────────────────────────────────────────────────────

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setOpaque(false);
        content.add(buildLocationList(), BorderLayout.WEST);
        content.add(buildMainArea(),     BorderLayout.CENTER);
        return content;
    }

    // Left: scrollable location list
    private JScrollPane buildLocationList() {
        JPanel list = new JPanel();
        list.setBackground(new Color(10, 16, 10));
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));

        JLabel heading = new JLabel("LOCATIONS");
        heading.setForeground(new Color(60, 80, 60));
        heading.setFont(new Font("Arial Black", Font.BOLD, 10));
        heading.setBorder(BorderFactory.createEmptyBorder(0, 4, 8, 0));
        list.add(heading);

        for (String locName : LocationDatabase.getAllLocationNames()) {
            list.add(buildLocEntry(locName));
            list.add(Box.createVerticalStrut(3));
        }

        JScrollPane scroll = new JScrollPane(list,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(180, GameWindow.HEIGHT - 60));
        scroll.setBackground(new Color(10, 16, 10));
        scroll.getViewport().setBackground(new Color(10, 16, 10));
        scroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(30, 60, 30)));
        return scroll;
    }

    private JPanel buildLocEntry(String locName) {
        boolean isCurrent  = locName.equals(gameState.getCurrentLocation());
        var loc = LocationDatabase.getLocation(gameState.getCurrentLocation());
        boolean isReachable = isCurrent || (loc != null && List.of(loc.connections()).contains(locName));

        JPanel entry = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (isCurrent) {
                    g.setColor(new Color(40, 90, 40));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
            }
        };
        entry.setOpaque(false);
        entry.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        entry.setMaximumSize(new Dimension(165, 40));

        JLabel name = new JLabel(locName);
        name.setFont(new Font("SansSerif", isCurrent ? Font.BOLD : Font.PLAIN, 11));
        name.setForeground(isCurrent   ? new Color(120, 240, 120) :
                isReachable ? new Color(180, 200, 180) :
                        new Color(60, 80, 60));
        entry.add(name, BorderLayout.CENTER);

        if (isReachable && !isCurrent) {
            entry.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            entry.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { entry.setBackground(new Color(20, 40, 20)); entry.setOpaque(true); }
                @Override public void mouseExited (MouseEvent e) { entry.setOpaque(false); entry.repaint(); }
                @Override public void mouseClicked(MouseEvent e) { travelTo(locName); }
            });
        }
        return entry;
    }

    // Right: main gameplay area
    private JPanel buildMainArea() {
        JPanel main = new JPanel();
        main.setOpaque(false);
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        String loc = gameState.getCurrentLocation();
        var locData = LocationDatabase.getLocation(loc);

        // Location card
        JPanel locCard = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(18, 30, 18, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(40, 90, 40));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 14, 14);
            }
        };
        locCard.setOpaque(false);
        locCard.setLayout(new BoxLayout(locCard, BoxLayout.Y_AXIS));
        locCard.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        locCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JLabel locTitle = new JLabel(loc);
        locTitle.setFont(new Font("Arial Black", Font.BOLD, 18));
        locTitle.setForeground(new Color(100, 220, 100));
        locCard.add(locTitle);

        if (locData != null) {
            JLabel desc = new JLabel("<html><body style='width:460px;color:#aaa;font-size:12px;'>"
                    + locData.description() + "</body></html>");
            desc.setBorder(BorderFactory.createEmptyBorder(4, 0, 10, 0));
            locCard.add(desc);

            // Wild pokemon preview row
            if (locData.hasWildPokemon()) {
                JLabel wLabel = new JLabel("Wild Pokémon:");
                wLabel.setForeground(new Color(100, 130, 100));
                wLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
                locCard.add(wLabel);

                JPanel sprites = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
                sprites.setOpaque(false);
                for (int[] enc : getEncounterIds(loc)) {
                    JLabel img = new JLabel() {
                        final int pid = enc[0];
                        final BufferedImage[] imgRef = {null};
                        { imgRef[0] = ImageLoader.getImage(pid, i -> { imgRef[0] = i; repaint(); }); }
                        @Override protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            if (imgRef[0] != null) {
                                Image sc = ImageLoader.scaleImage(imgRef[0], 38, 38);
                                g.drawImage(sc, 0, 0, null);
                            }
                        }
                    };
                    img.setPreferredSize(new Dimension(40, 40));
                    img.setToolTipText("Pokémon #" + enc[0]);
                    sprites.add(img);
                }
                locCard.add(sprites);
            }
        }
        main.add(locCard);
        main.add(Box.createVerticalStrut(14));

        // Action buttons
        int btnCount = 1; // always have Pokémart
        if (locData != null && locData.hasWildPokemon()) btnCount++;
        if (locData != null && locData.hasPokecenter()) btnCount++;
        if (locData != null && locData.hasTrainers()) btnCount++;

        JPanel actions = new JPanel(new GridLayout(1, btnCount, 12, 0));
        actions.setOpaque(false);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        if (locData != null && locData.hasWildPokemon()) {
            actions.add(actionBtn("🌿  Explore", new Color(40, 120, 40), e -> onExplore(loc)));
        }
        if (locData != null && locData.hasPokecenter()) {
            actions.add(actionBtn("❤  Heal Party", new Color(180, 40, 80), e -> onHeal()));
        }
        if (locData != null && locData.hasTrainers()) {
            actions.add(actionBtn("⚔  Trainer Battle", new Color(120, 40, 40), e -> onTrainerBattle(loc)));
        }
        actions.add(actionBtn("🏪  Pokémart", new Color(40, 90, 170), e -> onPokeMart()));
        main.add(actions);
        main.add(Box.createVerticalStrut(20));

        // Party panel
        JLabel partyLabel = new JLabel("YOUR PARTY");
        partyLabel.setFont(new Font("Arial Black", Font.BOLD, 12));
        partyLabel.setForeground(new Color(80, 120, 80));
        partyLabel.setAlignmentX(LEFT_ALIGNMENT);
        main.add(partyLabel);
        main.add(Box.createVerticalStrut(8));
        main.add(buildPartyPanel());

        return main;
    }

    private JPanel buildPartyPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 6, 10, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        for (Pokemon p : gameState.getParty()) {
            panel.add(buildPartyCard(p));
        }
        return panel;
    }

    private JPanel buildPartyCard(Pokemon p) {
        final BufferedImage[] imgRef = {null};
        imgRef[0] = ImageLoader.getImage(p.getId(), img -> { imgRef[0] = img; repaint(); });

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean fainted = p.isFainted();
                Color bg = fainted ? new Color(40, 20, 20) : new Color(18, 28, 18);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(fainted ? new Color(80, 30, 30) : new Color(40, 80, 40));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);

                // Sprite
                if (imgRef[0] != null) {
                    Image sc = ImageLoader.scaleImage(imgRef[0], 72, 72);
                    int ix = (getWidth() - sc.getWidth(null)) / 2;
                    g2.drawImage(sc, ix, 6, null);
                    if (fainted) {
                        g2.setColor(new Color(0, 0, 0, 100));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    }
                }

                // Name
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                g2.setColor(fainted ? new Color(130, 80, 80) : Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(p.getName(), (getWidth() - fm.stringWidth(p.getName())) / 2, 88);

                // Level
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2.setColor(new Color(140, 160, 140));
                String lv = "Lv." + p.getLevel();
                g2.drawString(lv, (getWidth() - fm.stringWidth(lv)) / 2, 100);

                // HP bar
                int bx = 6, by = 106, bw = getWidth() - 12;
                double hpPct = p.getHpPercent();
                g2.setColor(new Color(25, 35, 25));
                g2.fillRoundRect(bx, by, bw, 7, 3, 3);
                Color hpc = hpPct > 0.5 ? new Color(55, 195, 55)
                        : hpPct > 0.25 ? new Color(215, 190, 40)
                        :                new Color(215, 55, 55);
                g2.setColor(hpc);
                g2.fillRoundRect(bx, by, Math.max(0, (int)(bw * hpPct)), 7, 3, 3);
            }
        };
        card.setPreferredSize(new Dimension(110, 122));
        card.setOpaque(false);
        return card;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void travelTo(String dest) {
        SoundManager.play(SoundManager.SoundEffect.MENU_SELECT);
        gameState.setCurrentLocation(dest);
        onShow(gameState);
    }

    private void onExplore(String loc) {
        int[] enc = LocationDatabase.rollWildEncounter(loc);
        if (enc == null) {
            JOptionPane.showMessageDialog(this, "No Pokémon appeared...", "Explore", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Pokemon wild = PokemonDatabase.create(enc[0], enc[1]);
        SoundManager.play(SoundManager.SoundEffect.WILD_ENCOUNTER);
        window.startWildBattle(wild);
    }

    private void onHeal() {
        gameState.healAllPokemon();
        SoundManager.play(SoundManager.SoundEffect.HEAL);
        JOptionPane.showMessageDialog(this, "Your Pokémon have been fully restored! ❤", "Pokémon Center", JOptionPane.INFORMATION_MESSAGE);
        onShow(gameState);
    }

    private void onTrainerBattle(String loc) {
        List<Trainer> trainers = TrainerDatabase.getTrainersAt(loc);
        List<Trainer> available = trainers.stream().filter(t -> !t.isDefeated()).toList();
        if (available.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No trainers left to battle here!", "No Trainers", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Trainer t = available.get(rand.nextInt(available.size()));
        // Heal trainer for rematch
        t.getParty().forEach(Pokemon::fullHeal);
        SoundManager.play(SoundManager.SoundEffect.TRAINER_ENCOUNTER);
        window.startTrainerBattle(t);
    }

    private void onPokeMart() {
        // Prices
        final int PB_PRICE = 200, GB_PRICE = 600, UB_PRICE = 1200;

        String[] options = {
                "Pokéball  — $" + PB_PRICE + " each",
                "Great Ball — $" + GB_PRICE + " each",
                "Ultra Ball — $" + UB_PRICE + " each",
                "Cancel"
        };

        String chosen = (String) JOptionPane.showInputDialog(
                this,
                "💰 You have $" + gameState.getMoney()
                        + "   ⚪×" + gameState.getPokeballs()
                        + "  🔵×" + gameState.getGreatballs()
                        + "  🟡×" + gameState.getUltraballs(),
                "Pokémart",
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (chosen == null || chosen.equals("Cancel")) return;

        int price;
        String itemName;
        if (chosen.startsWith("Pokéball")) {
            price = PB_PRICE; itemName = "Pokéball";
        } else if (chosen.startsWith("Great Ball")) {
            price = GB_PRICE; itemName = "Great Ball";
        } else {
            price = UB_PRICE; itemName = "Ultra Ball";
        }

        if (gameState.getMoney() < price) {
            JOptionPane.showMessageDialog(this, "Not enough money!", "Pokémart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String qtyStr = JOptionPane.showInputDialog(this,
                "How many " + itemName + "s? ($" + price + " each, you have $" + gameState.getMoney() + ")");
        if (qtyStr == null) return;

        int qty;
        try { qty = Integer.parseInt(qtyStr.trim()); }
        catch (NumberFormatException e) { return; }

        if (qty <= 0) return;
        int total = price * qty;
        if (gameState.getMoney() < total) {
            JOptionPane.showMessageDialog(this,
                    "Not enough money! " + qty + "× costs $" + total + " but you only have $" + gameState.getMoney(),
                    "Pokémart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        gameState.spendMoney(total);
        if (itemName.equals("Pokéball"))   gameState.addPokeballs(qty);
        else if (itemName.equals("Great Ball")) gameState.addGreatballs(qty);
        else                                    gameState.addUltraballs(qty);

        SoundManager.play(SoundManager.SoundEffect.MENU_SELECT);
        JOptionPane.showMessageDialog(this,
                "Bought " + qty + "× " + itemName + " for $" + total + "!\n"
                        + "Remaining money: $" + gameState.getMoney(),
                "Pokémart", JOptionPane.INFORMATION_MESSAGE);
        onShow(gameState); // refresh header counts
    }

    private void onSave() {
        try {
            SaveManager.save(gameState);
            SoundManager.play(SoundManager.SoundEffect.SAVE);
            JOptionPane.showMessageDialog(this, "Game saved! 💾", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int[][] getEncounterIds(String loc) {
        return LocationDatabase.getEncounterTable(loc)
                .stream()
                .limit(6)
                .map(e -> new int[]{e.pokemonId()})
                .toArray(int[][]::new);
    }

    private JButton actionBtn(String text, Color color, java.awt.event.ActionListener action) {
        JButton b = new MainMenuScreen.PokeButton(text, color);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        b.setPreferredSize(new Dimension(180, 50));
        b.setFont(new Font("Arial Black", Font.BOLD, 13));
        b.addActionListener(action);
        return b;
    }
}