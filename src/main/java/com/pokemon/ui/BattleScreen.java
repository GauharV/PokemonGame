package com.pokemon.ui;

import com.pokemon.GameState;
import com.pokemon.engine.BattleEngine;
import com.pokemon.engine.EvolutionEngine;
import com.pokemon.engine.SaveManager;
import com.pokemon.model.Move;
import com.pokemon.model.Pokemon;
import com.pokemon.model.PokemonType;
import com.pokemon.model.Trainer;
import com.pokemon.util.ImageLoader;
import com.pokemon.util.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class BattleScreen extends JPanel {

    private final GameWindow window;
    private GameState gameState;
    private Pokemon playerPokemon;
    private Pokemon enemyPokemon;
    private Trainer enemyTrainer;
    private boolean isWild;

    private BufferedImage playerImg, enemyImg;
    private int shakeEnemy = 0, shakePlayer = 0;
    private boolean flashEnemy = false, flashPlayer = false;

    private final List<String> log = new ArrayList<>();
    private JTextArea logArea;

    private JPanel bottomPanel;
    private JPanel actionPanel;
    private JPanel movePanel;
    private JPanel fieldPanel;
    private JButton[] moveBtns = new JButton[4];

    private boolean buttonsLocked = false;

    public BattleScreen(GameWindow window) {
        this.window = window;
        setLayout(new BorderLayout());
        setBackground(new Color(18, 22, 38));
        buildUI();
    }

    // ── Build UI ──────────────────────────────────────────────────────────────

    private void buildUI() {
        // Field (painted)
        fieldPanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintField((Graphics2D) g, getWidth(), getHeight());
            }
        };
        fieldPanel.setOpaque(false);
        fieldPanel.setPreferredSize(new Dimension(GameWindow.WIDTH, 370));

        // Bottom panel
        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(12, 14, 24));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(50, 70, 110)));

        // Log area
        logArea = new JTextArea(3, 50);
        logArea.setEditable(false);
        logArea.setBackground(new Color(16, 20, 32));
        logArea.setForeground(new Color(220, 230, 255));
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBorder(BorderFactory.createEmptyBorder(8, 14, 6, 14));
        JScrollPane logScroll = new JScrollPane(logArea,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        logScroll.setOpaque(false);
        logScroll.getViewport().setBackground(new Color(16, 20, 32));
        logScroll.setBorder(null);
        logScroll.setPreferredSize(new Dimension(GameWindow.WIDTH, 72));

        // Action panel (Fight / Pokemon / Bag / Run)
        actionPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(8, 14, 12, 14));

        JButton fightBtn = makeBigBtn("⚔  FIGHT",   new Color(190, 50, 50));
        JButton pokeBtn  = makeBigBtn("🔴  POKÉMON", new Color(60, 120, 210));
        JButton bagBtn   = makeBigBtn("🎒  BAG",     new Color(60, 160, 60));
        JButton runBtn   = makeBigBtn("🏃  RUN",     new Color(130, 100, 40));

        fightBtn.addActionListener(e -> showMovePanel());
        pokeBtn.addActionListener(e -> onSwitchPokemon());
        bagBtn.addActionListener(e -> onOpenBag());
        runBtn.addActionListener(e -> onRun());

        actionPanel.add(fightBtn);
        actionPanel.add(pokeBtn);
        actionPanel.add(bagBtn);
        actionPanel.add(runBtn);

        // Move panel
        movePanel = new JPanel(new GridLayout(2, 2, 8, 8));
        movePanel.setOpaque(false);
        movePanel.setBorder(BorderFactory.createEmptyBorder(8, 14, 12, 14));
        for (int i = 0; i < 4; i++) {
            moveBtns[i] = makeBigBtn("—", new Color(60, 60, 80));
            movePanel.add(moveBtns[i]);
        }
        JButton backBtn = makeBigBtn("← BACK", new Color(60, 60, 80));
        backBtn.addActionListener(e -> showActionPanel());

        JPanel moveWrapper = new JPanel(new BorderLayout(8, 0));
        moveWrapper.setOpaque(false);
        moveWrapper.add(movePanel, BorderLayout.CENTER);
        JPanel backWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        backWrap.setOpaque(false);
        backWrap.add(backBtn);
        moveWrapper.add(backWrap, BorderLayout.EAST);

        bottomPanel.add(logScroll,   BorderLayout.NORTH);
        bottomPanel.add(actionPanel, BorderLayout.CENTER);

        add(fieldPanel,  BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ── Start battle ──────────────────────────────────────────────────────────

    public void startWildBattle(GameState gs, Pokemon wild) {
        this.gameState     = gs;
        this.isWild        = true;
        this.enemyTrainer  = null;
        this.enemyPokemon  = wild;
        this.playerPokemon = gs.getFirstAlive();
        this.playerImg     = null;
        this.enemyImg      = null;
        this.log.clear();
        buttonsLocked = false;

        loadImages();
        gs.seePokedmon(wild.getId());
        SoundManager.play(SoundManager.SoundEffect.WILD_ENCOUNTER);
        addLog("A wild " + wild.getName() + " appeared!");
        addLog("Go, " + playerPokemon.getName() + "!");
        showActionPanel();
        repaint();
    }

    public void startTrainerBattle(GameState gs, Trainer trainer) {
        this.gameState     = gs;
        this.isWild        = false;
        this.enemyTrainer  = trainer;
        this.enemyPokemon  = trainer.getActivePokemon();
        this.playerPokemon = gs.getFirstAlive();
        this.playerImg     = null;
        this.enemyImg      = null;
        this.log.clear();
        buttonsLocked = false;

        loadImages();
        SoundManager.play(SoundManager.SoundEffect.TRAINER_ENCOUNTER);
        addLog(trainer.getFullTitle() + " wants to battle!");
        addLog(trainer.getPreDialog());
        addLog(trainer.getName() + " sent out " + enemyPokemon.getName() + "!");
        showActionPanel();
        repaint();
    }

    private void loadImages() {
        if (playerPokemon != null)
            playerImg = ImageLoader.getImage(playerPokemon.getId(), img -> { playerImg = img; repaint(); });
        if (enemyPokemon != null)
            enemyImg  = ImageLoader.getImage(enemyPokemon.getId(),  img -> { enemyImg  = img; repaint(); });
    }

    // ── Painting ──────────────────────────────────────────────────────────────

    private void paintField(Graphics2D g, int w, int h) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Sky gradient
        GradientPaint sky = new GradientPaint(0, 0, new Color(30, 55, 110), 0, h, new Color(18, 32, 65));
        g.setPaint(sky);
        g.fillRect(0, 0, w, h);

        // Ground
        g.setColor(new Color(45, 75, 45));
        g.fillRoundRect(20, h - 90, w - 40, 75, 24, 24);
        g.setColor(new Color(60, 100, 60));
        g.fillRoundRect(20, h - 90, w - 40, 26, 12, 12);

        // Enemy platform
        g.setColor(new Color(80, 65, 45));
        g.fillOval(480, 70, 320, 56);
        g.setColor(new Color(105, 85, 58));
        g.fillOval(486, 70, 308, 30);

        // Player platform
        g.setColor(new Color(80, 65, 45));
        g.fillOval(60, 205, 250, 48);
        g.setColor(new Color(105, 85, 58));
        g.fillOval(66, 205, 238, 26);

        // Enemy Pokemon sprite (top-right)
        if (enemyPokemon != null) {
            drawSprite(g, enemyImg, enemyPokemon.getId(),
                    490 + shakeEnemy, 0, 220, 220, false, flashEnemy);
            drawInfoBox(g, enemyPokemon, 20, 14, false);
        }

        // Player Pokemon sprite (bottom-left) — mirrored for "back view"
        if (playerPokemon != null) {
            drawSprite(g, playerImg, playerPokemon.getId(),
                    60 + shakePlayer, 140, 220, 220, true, flashPlayer);
            drawInfoBox(g, playerPokemon, 460, 195, true);
        }
    }

    private void drawSprite(Graphics2D g, BufferedImage img, int id,
                             int x, int y, int w, int h, boolean flip, boolean flash) {
        if (img == null) {
            // trigger load, draw placeholder ball
            ImageLoader.getImage(id, loaded -> {
                if (flip) playerImg = loaded; else enemyImg = loaded;
                repaint();
            });
            g.setColor(new Color(80, 80, 100));
            g.fillOval(x + w/2 - 30, y + h/2 - 30, 60, 60);
            return;
        }
        Image scaled = ImageLoader.scaleImage(img, w, h);
        int iw = scaled.getWidth(null), ih = scaled.getHeight(null);

        if (flip) {
            g.drawImage(scaled, x + iw, y, -iw, ih, null);
        } else {
            g.drawImage(scaled, x, y, null);
        }

        // White flash overlay drawn on top of sprite
        if (flash) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
            g.setColor(Color.WHITE);
            if (flip) g.fillRect(x, y, iw, ih);
            else      g.fillRect(x, y, iw, ih);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }

    private void drawInfoBox(Graphics2D g, Pokemon p, int x, int y, boolean showHp) {
        int bw = 340, bh = showHp ? 100 : 88;
        g.setColor(new Color(14, 18, 32, 215));
        g.fillRoundRect(x, y, bw, bh, 14, 14);
        g.setColor(new Color(55, 80, 130));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, bw, bh, 14, 14);

        // Name + level
        g.setFont(new Font("Arial Black", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString(p.getName(), x + 12, y + 22);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(new Color(180, 200, 240));
        g.drawString("Lv." + p.getLevel(), x + bw - 52, y + 22);

        // Status badge
        if (p.getStatus() != Pokemon.Status.NONE) {
            Color sc = switch (p.getStatus()) {
                case BURN      -> new Color(210, 80, 30);
                case POISON    -> new Color(140, 40, 190);
                case PARALYSIS -> new Color(190, 170, 30);
                case SLEEP     -> new Color(70, 110, 200);
                case FREEZE    -> new Color(110, 190, 230);
                default        -> Color.GRAY;
            };
            g.setColor(sc);
            g.fillRoundRect(x + 12, y + 28, 56, 16, 6, 6);
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            g.setColor(Color.WHITE);
            g.drawString(p.getStatus().name(), x + 14, y + 40);
        }

        // HP bar
        int bx = x + 12, by = y + 52, barW = bw - 24, barH = 13;
        double hpPct = p.getHpPercent();
        g.setColor(new Color(30, 35, 55));
        g.fillRoundRect(bx, by, barW, barH, 5, 5);
        Color hpC = hpPct > 0.5 ? new Color(55, 200, 55)
                  : hpPct > 0.25 ? new Color(215, 195, 40)
                  :                new Color(215, 55, 55);
        int filled = Math.max(0, (int)(barW * hpPct));
        g.setColor(hpC);
        if (filled > 0) g.fillRoundRect(bx, by, filled, barH, 5, 5);
        g.setColor(new Color(70, 90, 130));
        g.drawRoundRect(bx, by, barW, barH, 5, 5);

        g.setFont(new Font("Consolas", Font.BOLD, 11));
        g.setColor(new Color(180, 200, 240));
        if (showHp) {
            g.drawString(p.getCurrentHp() + " / " + p.getMaxHp() + " HP", bx, y + 82);
            // XP bar
            int xpY = y + 90;
            int xpW = bw - 80;
            int xpCur = p.getExperience() - Pokemon.xpForLevel(p.getLevel());
            int xpNxt = Pokemon.xpForLevel(p.getLevel() + 1) - Pokemon.xpForLevel(p.getLevel());
            double xpPct = xpNxt > 0 ? (double) xpCur / xpNxt : 0;
            g.setColor(new Color(25, 28, 48));
            g.fillRect(bx, xpY, xpW, 5);
            g.setColor(new Color(50, 110, 210));
            g.fillRect(bx, xpY, (int)(xpW * Math.min(1, xpPct)), 5);
            g.setFont(new Font("SansSerif", Font.PLAIN, 9));
            g.setColor(new Color(90, 110, 160));
            g.drawString("EXP", bx + xpW + 4, xpY + 5);
        } else {
            g.drawString((int)(hpPct * 100) + "% HP", bx, y + 78);
        }
    }

    // ── Panel switching ───────────────────────────────────────────────────────

    private void showActionPanel() {
        // Remove whatever is currently in CENTER (could be movePanel wrapper or actionPanel)
        BorderLayout layout = (BorderLayout) bottomPanel.getLayout();
        Component center = layout.getLayoutComponent(BorderLayout.CENTER);
        if (center != null) bottomPanel.remove(center);
        bottomPanel.add(actionPanel, BorderLayout.CENTER);
        bottomPanel.revalidate();
        bottomPanel.repaint();
        setAllEnabled(true);
    }

    private void showMovePanel() {
        if (playerPokemon == null) return;
        List<Move> moves = playerPokemon.getMoves();
        // Rebuild move buttons with current data
        movePanel.removeAll();
        for (int i = 0; i < 4; i++) {
            if (i < moves.size()) {
                Move m = moves.get(i);
                Color tc = typeToColor(m.getType());
                String label = "<html><center><b>" + m.getName() + "</b><br>"
                        + "<small>" + m.getType().getDisplayName()
                        + " &nbsp;|&nbsp; PP " + m.getCurrentPP() + "/" + m.getMaxPP()
                        + "</small></center></html>";
                JButton btn = makeBigBtn(label, tc.darker());
                btn.setEnabled(m.isUsable());
                Move finalMove = m;
                btn.addActionListener(e -> executePlayerMove(finalMove));
                movePanel.add(btn);
            } else {
                movePanel.add(makeBigBtn("—", new Color(45, 45, 60)));
            }
        }

        JButton backBtn = makeBigBtn("← BACK", new Color(60, 60, 80));
        backBtn.addActionListener(e -> showActionPanel());

        // Wrap movePanel + back button
        JPanel wrapper = new JPanel(new BorderLayout(8, 0));
        wrapper.setOpaque(false);
        wrapper.add(movePanel, BorderLayout.CENTER);
        JPanel backWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        backWrap.setOpaque(false);
        backWrap.add(backBtn);
        wrapper.add(backWrap, BorderLayout.EAST);

        bottomPanel.remove(actionPanel);
        bottomPanel.add(wrapper, BorderLayout.CENTER);
        bottomPanel.revalidate();
        bottomPanel.repaint();
    }

    // ── Battle logic ──────────────────────────────────────────────────────────

    private void executePlayerMove(Move move) {
        if (buttonsLocked) return;
        if (!move.isUsable()) { addLog("No PP left for " + move.getName() + "!"); return; }

        buttonsLocked = true;
        showActionPanel();
        setAllEnabled(false);

        boolean playerFirst = playerPokemon.getEffectiveSpeed() >= enemyPokemon.getEffectiveSpeed();

        Timer t = new Timer(550, null);
        final int[] step = {0};
        t.addActionListener(e -> {
            switch (step[0]++) {
                case 0 -> { if (playerFirst) doPlayerTurn(move); else doEnemyTurn(); }
                case 1 -> { if (checkEnd(t)) return; if (playerFirst) doEnemyTurn(); else doPlayerTurn(move); }
                case 2 -> {
                    if (checkEnd(t)) return;
                    applyEoT();
                }
                case 3 -> {
                    t.stop();
                    if (!checkEnd(t)) { buttonsLocked = false; setAllEnabled(true); }
                    repaint();
                }
            }
            repaint();
        });
        t.start();
    }

    private void doPlayerTurn(Move move) {
        if (!playerPokemon.canAct()) {
            addLog(playerPokemon.getName() + " is " + playerPokemon.getStatus().name().toLowerCase() + " and can't move!");
            return;
        }
        var res = BattleEngine.executeMove(playerPokemon, move, enemyPokemon);
        addLog(res.message());
        if (res.hit() && res.damage() > 0) {
            triggerShake(false);
            SoundManager.play(res.critical()
                    ? SoundManager.SoundEffect.SUPER_EFFECTIVE
                    : SoundManager.SoundEffect.MOVE_HIT);
        }
    }

    private void doEnemyTurn() {
        if (enemyPokemon.isFainted()) return;
        if (!enemyPokemon.canAct()) {
            addLog(enemyPokemon.getName() + " is " + enemyPokemon.getStatus().name().toLowerCase() + " and can't move!");
            return;
        }
        Move aiMove = BattleEngine.selectAIMove(enemyPokemon, playerPokemon);
        var res = BattleEngine.executeMove(enemyPokemon, aiMove, playerPokemon);
        addLog(res.message());
        if (res.hit() && res.damage() > 0) {
            triggerShake(true);
            SoundManager.play(SoundManager.SoundEffect.MOVE_HIT);
        }
    }

    private void applyEoT() {
        String pm = BattleEngine.applyEndOfTurn(playerPokemon);
        String em = BattleEngine.applyEndOfTurn(enemyPokemon);
        if (!pm.isEmpty()) addLog(pm);
        if (!em.isEmpty()) addLog(em);
    }

    /** Returns true if battle has ended (and handles the end). */
    private boolean checkEnd(Timer t) {
        if (enemyPokemon.isFainted()) {
            t.stop();
            onEnemyFainted();
            return true;
        }
        if (playerPokemon.isFainted()) {
            t.stop();
            onPlayerFainted();
            return true;
        }
        return false;
    }

    private void onEnemyFainted() {
        SoundManager.play(SoundManager.SoundEffect.FAINT);
        addLog(enemyPokemon.getName() + " fainted!");

        if (!isWild && enemyTrainer != null) {
            Pokemon next = enemyTrainer.getActivePokemon();
            if (next != null) {
                enemyPokemon = next;
                enemyImg = null;
                enemyImg = ImageLoader.getImage(enemyPokemon.getId(), img -> { enemyImg = img; repaint(); });
                addLog(enemyTrainer.getName() + " sent out " + next.getName() + "!");
                buttonsLocked = false;
                setAllEnabled(true);
                repaint();
                return;
            }
            // Trainer fully beaten
            addLog(enemyTrainer.getPostDialog());
            addLog("You won $" + enemyTrainer.getRewardMoney() + "!");
            gameState.addMoney(enemyTrainer.getRewardMoney());
            gameState.recordTrainerDefeated(enemyTrainer.getName());
            enemyTrainer.setDefeated(true);
            SoundManager.play(SoundManager.SoundEffect.VICTORY);
        }

        int xp = BattleEngine.calculateXP(enemyPokemon, isWild);
        addLog(playerPokemon.getName() + " gained " + xp + " XP!");
        boolean leveled = playerPokemon.gainXP(xp);
        if (leveled) {
            SoundManager.play(SoundManager.SoundEffect.LEVEL_UP);
            addLog(playerPokemon.getName() + " grew to level " + playerPokemon.getLevel() + "!");
            checkEvolution();
        }
        gameState.recordBattle(true);

        Timer delay = new Timer(2200, e -> {
            SoundManager.stopBattleMusic();
            try { SaveManager.save(gameState); } catch (Exception ex) { ex.printStackTrace(); }
            window.endBattle();
        });
        delay.setRepeats(false);
        delay.start();
        repaint();
    }

    private void onPlayerFainted() {
        SoundManager.play(SoundManager.SoundEffect.FAINT);
        addLog(playerPokemon.getName() + " fainted!");

        Pokemon next = gameState.getFirstAlive();
        if (next != null) {
            playerPokemon = next;
            playerImg = null;
            playerImg = ImageLoader.getImage(playerPokemon.getId(), img -> { playerImg = img; repaint(); });
            addLog("Go, " + playerPokemon.getName() + "!");
            buttonsLocked = false;
            setAllEnabled(true);
            repaint();
            return;
        }

        addLog("All your Pokémon fainted!");
        addLog("You blacked out...");
        gameState.healAllPokemon();
        gameState.recordBattle(false);
        Timer delay = new Timer(2500, e -> {
            SoundManager.stopBattleMusic();
            gameState.setCurrentLocation("Pallet Town");
            window.endBattle();
        });
        delay.setRepeats(false);
        delay.start();
        repaint();
    }

    private void checkEvolution() {
        var result = EvolutionEngine.checkAndEvolve(playerPokemon);
        if (result == null) return;
        SoundManager.play(SoundManager.SoundEffect.EVOLUTION);
        addLog(result.message());
        int idx = gameState.getParty().indexOf(playerPokemon);
        if (idx >= 0) {
            gameState.getParty().set(idx, result.evolved());
            playerPokemon = result.evolved();
            playerImg = null;
            playerImg = ImageLoader.getImage(playerPokemon.getId(), img -> { playerImg = img; repaint(); });
        }
    }

    private void onOpenBag() {
        if (!isWild) { addLog("You can't use Pokéballs in a trainer battle!"); return; }
        if (buttonsLocked) return;

        // Build ball selection dialog
        int pb = gameState.getPokeballs();
        int gb = gameState.getGreatballs();
        int ub = gameState.getUltraballs();

        if (pb + gb + ub == 0) {
            JOptionPane.showMessageDialog(this,
                    "You have no Pokéballs!\nBuy some at a Pokémart in town.",
                    "No Balls!", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] options = new String[]{
            pb > 0 ? "Pokéball  ×" + pb   : null,
            gb > 0 ? "Great Ball ×" + gb  : null,
            ub > 0 ? "Ultra Ball ×" + ub  : null,
            "Cancel"
        };
        // Filter out nulls
        java.util.List<String> validOpts = new java.util.ArrayList<>();
        for (String s : options) if (s != null) validOpts.add(s);

        String chosen = (String) JOptionPane.showInputDialog(
                this, "Choose a ball to throw:", "Bag",
                JOptionPane.PLAIN_MESSAGE, null,
                validOpts.toArray(), validOpts.get(0));

        if (chosen == null || chosen.equals("Cancel")) return;

        double mult;
        String ballName;
        if (chosen.startsWith("Pokéball")) {
            if (!gameState.usePokeball()) return;
            mult = 1.0; ballName = "Pokéball";
        } else if (chosen.startsWith("Great Ball")) {
            if (!gameState.useGreatball()) return;
            mult = 1.5; ballName = "Great Ball";
        } else {
            if (!gameState.useUltraball()) return;
            mult = 2.0; ballName = "Ultra Ball";
        }

        showCatchMinigame(mult, ballName);
    }

    private void showCatchMinigame(double ballMult, String ballName) {
        buttonsLocked = true;
        setAllEnabled(false);

        // Show minigame overlaid on field
        CatchMinigame minigame = new CatchMinigame(enemyPokemon, ballMult, ballName, caught -> {
            // Remove minigame overlay
            fieldPanel.removeAll();
            fieldPanel.revalidate();
            fieldPanel.repaint();

            if (caught) {
                addLog("Gotcha! " + enemyPokemon.getName() + " was caught!");
                SoundManager.play(SoundManager.SoundEffect.SAVE); // celebration tone
                boolean added = gameState.addPokemonToParty(enemyPokemon);
                if (!added) {
                    addLog(enemyPokemon.getName() + " was added to your PC box (party full).");
                    gameState.getCaughtPokemon().add(enemyPokemon.getId());
                }
                gameState.recordBattle(true);
                Timer delay = new Timer(1400, e -> {
                    SoundManager.stopBattleMusic();
                    try { SaveManager.save(gameState); } catch (Exception ex) { ex.printStackTrace(); }
                    window.endBattle();
                });
                delay.setRepeats(false);
                delay.start();
            } else {
                addLog(enemyPokemon.getName() + " broke free!");
                // Enemy attacks back after a failed catch
                buttonsLocked = false;
                setAllEnabled(true);
                Timer counterAttack = new Timer(600, e -> {
                    doEnemyTurn();
                    repaint();
                });
                counterAttack.setRepeats(false);
                counterAttack.start();
            }
        });

        // Center the minigame over the field
        minigame.setOpaque(true);
        minigame.setBackground(new Color(10, 12, 22, 230));
        int mx = (fieldPanel.getWidth()  - 580) / 2;
        int my = (fieldPanel.getHeight() - 260) / 2;
        minigame.setBounds(mx > 0 ? mx : 40, my > 0 ? my : 20, 580, 260);
        fieldPanel.setLayout(null);
        fieldPanel.add(minigame);
        fieldPanel.revalidate();
        fieldPanel.repaint();
        minigame.requestFocusInWindow();
    }

    private void onRun() {
        if (!isWild) { addLog("Can't run from a trainer battle!"); return; }
        addLog("Got away safely!");
        SoundManager.stopBattleMusic();
        SoundManager.play(SoundManager.SoundEffect.MENU_SELECT);
        Timer d = new Timer(800, e -> window.endBattle());
        d.setRepeats(false);
        d.start();
    }

    private void onSwitchPokemon() {
        List<Pokemon> party = gameState.getParty();
        List<String> options = new ArrayList<>();
        List<Pokemon> candidates = new ArrayList<>();
        for (Pokemon p : party) {
            if (p != playerPokemon && !p.isFainted()) {
                options.add(p.getName() + " Lv." + p.getLevel()
                        + "  [" + p.getCurrentHp() + "/" + p.getMaxHp() + " HP]");
                candidates.add(p);
            }
        }
        if (options.isEmpty()) { addLog("No other healthy Pokémon!"); return; }

        String chosen = (String) JOptionPane.showInputDialog(
                this, "Choose Pokémon:", "Switch",
                JOptionPane.PLAIN_MESSAGE, null,
                options.toArray(), options.get(0));
        if (chosen == null) return;

        int idx = options.indexOf(chosen);
        if (idx < 0) return;
        addLog(playerPokemon.getName() + ", come back!");
        playerPokemon = candidates.get(idx);
        playerImg = null;
        playerImg = ImageLoader.getImage(playerPokemon.getId(), img -> { playerImg = img; repaint(); });
        addLog("Go, " + playerPokemon.getName() + "!");
        doEnemyTurn();
        repaint();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void triggerShake(boolean isPlayer) {
        // Multi-frame shake pattern: left, right, left, right, center
        final int[] shakePattern = {-14, 12, -10, 8, -6, 5, -3, 2, 0};
        final int[] frame = {0};

        Timer t = new Timer(45, null);
        t.addActionListener(e -> {
            int f = frame[0];
            if (f < shakePattern.length) {
                if (isPlayer) {
                    shakePlayer  = shakePattern[f];
                    flashPlayer  = (f < 2); // flash on first 2 frames only
                } else {
                    shakeEnemy   = shakePattern[f];
                    flashEnemy   = (f < 2);
                }
            } else {
                if (isPlayer) { shakePlayer = 0; flashPlayer = false; }
                else          { shakeEnemy  = 0; flashEnemy  = false; }
                t.stop();
            }
            frame[0]++;
            repaint();
        });
        // Start immediately with first frame
        if (isPlayer) { shakePlayer = shakePattern[0]; flashPlayer = true; }
        else          { shakeEnemy  = shakePattern[0]; flashEnemy  = true; }
        repaint();
        t.start();
    }

    private void addLog(String msg) {
        log.add(msg);
        StringBuilder sb = new StringBuilder();
        int from = Math.max(0, log.size() - 5);
        for (int i = from; i < log.size(); i++) sb.append("▶ ").append(log.get(i)).append("\n");
        logArea.setText(sb.toString());
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void setAllEnabled(boolean on) {
        for (Component c : actionPanel.getComponents()) c.setEnabled(on);
    }

    private JButton makeBigBtn(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = isEnabled()
                        ? (getModel().isRollover() ? color.brighter() : color.darker())
                        : color.darker().darker();
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(isEnabled() ? color : color.darker());
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial Black", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private Color typeToColor(PokemonType type) {
        return switch (type) {
            case FIRE     -> new Color(180, 60, 20);
            case WATER    -> new Color(40, 90, 190);
            case GRASS    -> new Color(50, 140, 50);
            case ELECTRIC -> new Color(180, 160, 20);
            case PSYCHIC  -> new Color(180, 40, 90);
            case ICE      -> new Color(70, 170, 170);
            case FIGHTING -> new Color(160, 30, 30);
            case POISON   -> new Color(110, 30, 150);
            case GROUND   -> new Color(170, 130, 20);
            case FLYING   -> new Color(100, 80, 190);
            case BUG      -> new Color(120, 140, 20);
            case ROCK     -> new Color(140, 110, 30);
            case GHOST    -> new Color(70, 50, 120);
            case DRAGON   -> new Color(60, 30, 200);
            case DARK     -> new Color(70, 50, 40);
            case STEEL    -> new Color(110, 110, 150);
            default       -> new Color(90, 90, 110);
        };
    }
}