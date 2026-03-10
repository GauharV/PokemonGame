package com.pokemon.ui;

import com.pokemon.GameState;
import com.pokemon.data.PokemonDatabase;
import com.pokemon.model.Pokemon;
import com.pokemon.util.ImageLoader;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class PokedexScreen extends JPanel {

    private final GameWindow window;
    private GameState gameState;

    private JPanel gridPanel;
    private JPanel detailPanel;
    private JTextField searchField;
    private JLabel countLabel;

    private int selectedId = -1;

    public PokedexScreen(GameWindow window) {
        this.window = window;
        setBackground(new Color(12, 12, 22));
        setLayout(new BorderLayout());
        buildUI();
    }

    public void onShow(GameState gs) {
        this.gameState = gs;
        refreshGrid("");
        updateCount();
    }

    private void buildUI() {
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        h.setBackground(new Color(18, 14, 30));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(100, 30, 30)));

        JButton back = new MainMenuScreen.PokeButton("← BACK", new Color(80, 60, 60));
        back.setPreferredSize(new Dimension(100, 34));
        back.setFont(new Font("Arial Black", Font.BOLD, 11));
        back.addActionListener(e -> {
            if (gameState != null) window.showScreen(GameWindow.SCREEN_OVERWORLD);
            else                   window.showScreen(GameWindow.SCREEN_MENU);
        });
        h.add(back);

        JLabel title = new JLabel("📖  POKÉDEX");
        title.setFont(new Font("Arial Black", Font.BOLD, 18));
        title.setForeground(new Color(220, 60, 60));
        h.add(title);

        countLabel = new JLabel("0 / 151 caught");
        countLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        countLabel.setForeground(new Color(140, 140, 160));
        h.add(countLabel);

        h.add(Box.createHorizontalGlue());

        searchField = new JTextField(14);
        searchField.setBackground(new Color(28, 28, 44));
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 120)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField.setToolTipText("Search Pokémon by name or #");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { refreshGrid(searchField.getText()); }
            public void removeUpdate(DocumentEvent e)  { refreshGrid(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { refreshGrid(searchField.getText()); }
        });
        h.add(new JLabel("🔍 ") {{ setForeground(new Color(140,140,160)); }});
        h.add(searchField);

        return h;
    }

    // ── Body ──────────────────────────────────────────────────────────────────

    private JSplitPane buildBody() {
        // Grid (left)
        gridPanel = new JPanel(new GridLayout(0, 8, 6, 6));
        gridPanel.setBackground(new Color(12, 12, 22));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane gridScroll = new JScrollPane(gridPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        gridScroll.getViewport().setBackground(new Color(12, 12, 22));
        gridScroll.setBorder(null);

        // Detail (right)
        detailPanel = new JPanel();
        detailPanel.setBackground(new Color(16, 14, 28));
        detailPanel.setPreferredSize(new Dimension(260, GameWindow.HEIGHT));
        detailPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(40, 30, 70)));
        showEmptyDetail();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, gridScroll, detailPanel);
        split.setDividerLocation(GameWindow.WIDTH - 270);
        split.setDividerSize(2);
        split.setBorder(null);
        split.setBackground(new Color(30, 20, 60));
        return split;
    }

    // ── Grid ──────────────────────────────────────────────────────────────────

    private void refreshGrid(String query) {
        gridPanel.removeAll();
        String q = query.trim().toLowerCase();

        for (int id = 1; id <= 151; id++) {
            if (!PokemonDatabase.exists(id)) continue;
            String name = PokemonDatabase.getName(id).toLowerCase();
            String numStr = "#" + id;
            if (!q.isEmpty() && !name.contains(q) && !numStr.contains(q)) continue;

            gridPanel.add(buildGridCell(id));
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel buildGridCell(int id) {
        boolean caught = gameState != null && gameState.getCaughtPokemon().contains(id);
        boolean seen   = gameState != null && gameState.getSeenPokemon().contains(id);
        boolean isSelected = (selectedId == id);

        final BufferedImage[] imgRef = {null};
        imgRef[0] = ImageLoader.getImage(id, img -> { imgRef[0] = img; repaint(); });

        JPanel cell = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                Color bg = isSelected ? new Color(60, 40, 100)
                         : caught     ? new Color(18, 30, 18)
                         : seen       ? new Color(28, 20, 10)
                         :              new Color(16, 16, 26);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Border
                Color border = isSelected ? new Color(160, 100, 255)
                             : caught     ? new Color(50, 110, 50)
                             :              new Color(35, 35, 55);
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);

                // Sprite
                if (imgRef[0] != null) {
                    Image sc = ImageLoader.scaleImage(imgRef[0], 52, 52);
                    int ix = (getWidth() - sc.getWidth(null)) / 2;
                    g2.drawImage(sc, ix, 4, null);
                    if (!caught) {
                        // Silhouette for unseen, faded for seen
                        if (!seen) {
                            g2.setColor(new Color(10, 10, 20, 200));
                            g2.fillRoundRect(0, 0, getWidth(), getHeight() - 22, 8, 8);
                        } else {
                            g2.setColor(new Color(20, 20, 35, 130));
                            g2.fillRoundRect(0, 0, getWidth(), getHeight() - 22, 8, 8);
                        }
                    }
                }

                // Number
                g2.setFont(new Font("SansSerif", Font.PLAIN, 8));
                g2.setColor(new Color(80, 80, 100));
                g2.drawString("#" + String.format("%03d", id), 4, getHeight() - 14);

                // Caught dot
                if (caught) {
                    g2.setColor(new Color(80, 200, 80));
                    g2.fillOval(getWidth() - 10, 4, 6, 6);
                }
            }
        };
        cell.setPreferredSize(new Dimension(70, 82));
        cell.setOpaque(false);
        cell.setCursor(caught || seen
                ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                : Cursor.getDefaultCursor());
        cell.setToolTipText(caught || seen ? PokemonDatabase.getName(id) + " (#" + id + ")" : "???");

        if (caught || seen) {
            cell.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    selectedId = id;
                    refreshGrid(searchField.getText());
                    showDetail(id);
                }
                @Override public void mouseEntered(MouseEvent e) { cell.repaint(); }
                @Override public void mouseExited (MouseEvent e) { cell.repaint(); }
            });
        }
        return cell;
    }

    // ── Detail Panel ──────────────────────────────────────────────────────────

    private void showEmptyDetail() {
        detailPanel.removeAll();
        detailPanel.setLayout(new BorderLayout());

        JLabel hint = new JLabel("<html><center>Select a<br>Pokémon<br>to see<br>details</center></html>");
        hint.setForeground(new Color(60, 55, 90));
        hint.setFont(new Font("SansSerif", Font.PLAIN, 14));
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        detailPanel.add(hint, BorderLayout.CENTER);

        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private void showDetail(int id) {
        detailPanel.removeAll();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));

        Pokemon p = PokemonDatabase.create(id, 50); // use level 50 for display stats

        // Artwork
        final BufferedImage[] artRef = {null};
        artRef[0] = ImageLoader.getImage(id, img -> { artRef[0] = img; detailPanel.repaint(); });

        JPanel artPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (artRef[0] != null) {
                    Image sc = ImageLoader.scaleImage(artRef[0], 160, 160);
                    int ix = (getWidth() - sc.getWidth(null)) / 2;
                    Graphics2D g2 = (Graphics2D) g;
                    // Type-colored glow behind art
                    String typeHex = p.getType1().getColor();
                    Color glowColor;
                    try { glowColor = Color.decode(typeHex); } catch (Exception e) { glowColor = Color.GRAY; }
                    g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 35));
                    g2.fillOval(ix - 10, 5, sc.getWidth(null) + 20, sc.getHeight(null) + 20);
                    g2.drawImage(sc, ix, 10, null);
                }
            }
        };
        artPanel.setPreferredSize(new Dimension(240, 175));
        artPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 175));
        artPanel.setOpaque(false);
        detailPanel.add(artPanel);

        // ID + Name
        detailPanel.add(centeredLabel("#" + String.format("%03d", id), new Color(100, 90, 140), 11, Font.PLAIN));
        detailPanel.add(centeredLabel(p.getName().toUpperCase(), Color.WHITE, 15, Font.BOLD));
        detailPanel.add(Box.createVerticalStrut(6));

        // Types
        JPanel typesRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        typesRow.setOpaque(false);
        typesRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        addTypeBadge(typesRow, p.getType1().getDisplayName(), p.getType1().getColor());
        if (p.getType2().name().equals("NONE") == false) {
            addTypeBadge(typesRow, p.getType2().getDisplayName(), p.getType2().getColor());
        }
        detailPanel.add(typesRow);
        detailPanel.add(Box.createVerticalStrut(12));

        // Evolution info
        if (p.getEvolutionId() != -1 && PokemonDatabase.exists(p.getEvolutionId())) {
            String evoName = PokemonDatabase.getName(p.getEvolutionId());
            detailPanel.add(centeredLabel("Evolves → " + evoName + " (Lv." + p.getEvolutionLevel() + ")",
                    new Color(200, 180, 80), 9, Font.PLAIN));
            detailPanel.add(Box.createVerticalStrut(8));
        }

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(40, 35, 65));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        detailPanel.add(sep);
        detailPanel.add(Box.createVerticalStrut(10));

        // Stats
        detailPanel.add(sectionLabel("BASE STATS"));
        detailPanel.add(Box.createVerticalStrut(6));

        String[] statNames = {"HP", "Attack", "Defense", "Sp.Atk", "Sp.Def", "Speed"};
        int[]    statVals  = {p.getBaseHp(), p.getBaseAtk(), p.getBaseDef(),
                              p.getBaseSpAtk(), p.getBaseSpDef(), p.getBaseSpd()};

        for (int s = 0; s < statNames.length; s++) {
            detailPanel.add(buildStatRow(statNames[s], statVals[s]));
            detailPanel.add(Box.createVerticalStrut(4));
        }

        detailPanel.add(Box.createVerticalStrut(10));
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(new Color(40, 35, 65));
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        detailPanel.add(sep2);
        detailPanel.add(Box.createVerticalStrut(8));

        // Moves
        detailPanel.add(sectionLabel("MOVES"));
        detailPanel.add(Box.createVerticalStrut(6));
        for (var move : p.getMoves()) {
            JPanel movePill = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            movePill.setOpaque(false);
            movePill.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
            JLabel mName = new JLabel(move.getName());
            mName.setFont(new Font("SansSerif", Font.PLAIN, 11));
            mName.setForeground(new Color(200, 200, 220));
            movePill.add(mName);
            String th = move.getType().getColor();
            Color tc;
            try { tc = Color.decode(th); } catch (Exception e) { tc = Color.GRAY; }
            JLabel mType = new JLabel(move.getType().getDisplayName());
            mType.setFont(new Font("SansSerif", Font.BOLD, 9));
            mType.setForeground(tc);
            movePill.add(mType);
            detailPanel.add(movePill);
        }

        // Caught badge
        if (gameState != null && gameState.getCaughtPokemon().contains(id)) {
            detailPanel.add(Box.createVerticalStrut(10));
            JLabel caught = new JLabel("✓ CAUGHT");
            caught.setFont(new Font("Arial Black", Font.BOLD, 11));
            caught.setForeground(new Color(80, 210, 80));
            caught.setAlignmentX(CENTER_ALIGNMENT);
            detailPanel.add(caught);
        }

        detailPanel.revalidate();
        detailPanel.repaint();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JPanel buildStatRow(String name, final int value) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));

        JLabel lbl = new JLabel(name);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lbl.setForeground(new Color(130, 120, 160));
        lbl.setPreferredSize(new Dimension(52, 14));
        row.add(lbl, BorderLayout.WEST);

        JPanel barWrap = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 28, 50));
                g2.fillRoundRect(0, 3, getWidth(), 8, 4, 4);
                float pct = value / 255f;
                Color bc = value >= 100 ? new Color(60, 200, 60)
                         : value >= 60  ? new Color(200, 200, 60)
                         :                new Color(200, 80, 60);
                g2.setColor(bc);
                g2.fillRoundRect(0, 3, (int)(getWidth() * pct), 8, 4, 4);
            }
        };
        barWrap.setOpaque(false);
        row.add(barWrap, BorderLayout.CENTER);

        JLabel val = new JLabel(String.valueOf(value));
        val.setFont(new Font("SansSerif", Font.BOLD, 10));
        val.setForeground(Color.WHITE);
        val.setPreferredSize(new Dimension(28, 14));
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(val, BorderLayout.EAST);

        return row;
    }

    private JLabel centeredLabel(String text, Color color, int size, int style) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial Black", style, size));
        l.setForeground(color);
        l.setAlignmentX(CENTER_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, size + 10));
        return l;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial Black", Font.BOLD, 10));
        l.setForeground(new Color(100, 90, 140));
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        return l;
    }

    private void addTypeBadge(JPanel parent, String typeName, String colorHex) {
        Color temp;
        try { temp = Color.decode(colorHex); } catch (Exception e) { temp = Color.GRAY; }
        final Color c = temp;   // ← now truly final, anonymous class can use it
        JLabel badge = new JLabel(typeName.toUpperCase()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Arial Black", Font.BOLD, 10));
        badge.setForeground(Color.WHITE);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        badge.setOpaque(false);
        parent.add(badge);
    }

    private void updateCount() {
        if (gameState == null || countLabel == null) return;
        int caught = gameState.getCaughtPokemon().size();
        countLabel.setText(caught + " / 151 caught");
    }
}
