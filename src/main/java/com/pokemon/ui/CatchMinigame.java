package com.pokemon.ui;

import com.pokemon.model.Pokemon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

/**
 * NBA 2K-style timing mini-game for catching Pokémon.
 *
 *  ┌──────────────────────────────────────────────────────┐
 *  │   Throw a Pokéball!                                  │
 *  │  ┌──────────────────────────────────────────────┐   │
 *  │  │░░░░░░░░░░░░▓▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░░░░░░░░░│   │  ← catch zone (green)
 *  │  │                  ▲                            │   │  ← moving needle
 *  │  └──────────────────────────────────────────────┘   │
 *  │         [ THROW ]  (or press SPACE)                 │
 *  └──────────────────────────────────────────────────────┘
 *
 * The green "sweet spot" width is based on how weak the enemy is.
 * A needle bounces back and forth — press THROW when it's in the green.
 * Landing perfectly in the center gives a PERFECT bonus.
 */
public class CatchMinigame extends JPanel {

    public enum Result { PERFECT, GOOD, MISS }

    // Callback: delivers (Result, catchSuccess)
    private final Consumer<Boolean> onComplete;

    private final Pokemon target;
    private final double ballMultiplier;  // 1.0 = Pokéball, 1.5 = Great, 2.0 = Ultra

    // Bar geometry
    private static final int BAR_X = 60;
    private static final int BAR_W = 460;
    private static final int BAR_H = 28;
    private static final int BAR_Y = 110;

    // Needle state
    private double needlePos  = 0.0;   // 0.0 – 1.0 across bar
    private double needleDir  = 1.0;
    private double needleSpeed;        // fraction of bar per tick

    // Green zone
    private final double zoneStart;
    private final double zoneEnd;

    // Pokéball bounce animation after throw
    private boolean thrown      = false;
    private int     bounceFrame = 0;
    private Result  result      = null;
    private boolean caught      = false;

    private Timer gameTimer;
    private Timer bounceTimer;

    private final String ballName;

    public CatchMinigame(Pokemon target, double ballMultiplier,
                         String ballName, Consumer<Boolean> onComplete) {
        this.target          = target;
        this.ballMultiplier  = ballMultiplier;
        this.ballName        = ballName;
        this.onComplete      = onComplete;

        setPreferredSize(new Dimension(580, 260));
        setBackground(new Color(10, 12, 22));
        setFocusable(true);

        // Green zone width: weaker pokemon = bigger zone
        double hpFraction = target.getHpPercent();
        // Zone width 0.12 (full HP) → 0.38 (1 HP), multiplied by ball bonus
        double rawWidth = (0.12 + (1.0 - hpFraction) * 0.26) * Math.min(ballMultiplier, 2.0);
        double zoneWidth = Math.min(rawWidth, 0.55);
        double zoneCenter = 0.35 + Math.random() * 0.30; // randomise position slightly
        zoneStart = Math.max(0.05, zoneCenter - zoneWidth / 2);
        zoneEnd   = Math.min(0.95, zoneCenter + zoneWidth / 2);

        // Needle speed: faster for stronger pokemon
        needleSpeed = 0.012 + hpFraction * 0.010;

        buildUI();
        startNeedle();
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    private void buildUI() {
        setLayout(null);

        // THROW button
        JButton throwBtn = new JButton("⚾  THROW") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = thrown ? new Color(60, 60, 80) : new Color(220, 60, 60);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(c.brighter());
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        throwBtn.setOpaque(false);
        throwBtn.setContentAreaFilled(false);
        throwBtn.setBorderPainted(false);
        throwBtn.setFocusPainted(false);
        throwBtn.setFont(new Font("Arial Black", Font.BOLD, 15));
        throwBtn.setForeground(Color.WHITE);
        throwBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        throwBtn.setBounds(BAR_X + BAR_W/2 - 80, 160, 160, 46);
        throwBtn.addActionListener(e -> onThrow());
        add(throwBtn);

        // Keyboard shortcut: SPACE
        getInputMap(WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "throw");
        getActionMap().put("throw", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { onThrow(); }
        });
    }

    private void startNeedle() {
        needlePos = 0.0;
        gameTimer = new Timer(16, e -> {
            if (thrown) return;
            needlePos += needleDir * needleSpeed;
            if (needlePos >= 1.0) { needlePos = 1.0; needleDir = -1; }
            if (needlePos <= 0.0) { needlePos = 0.0; needleDir =  1; }
            repaint();
        });
        gameTimer.start();
        requestFocusInWindow();
    }

    // ── Throw logic ───────────────────────────────────────────────────────────

    private void onThrow() {
        if (thrown) return;
        thrown = true;
        gameTimer.stop();

        // Evaluate timing
        if (needlePos >= zoneStart && needlePos <= zoneEnd) {
            double zoneCenter = (zoneStart + zoneEnd) / 2.0;
            double distFromCenter = Math.abs(needlePos - zoneCenter) / ((zoneEnd - zoneStart) / 2.0);
            result = distFromCenter < 0.25 ? Result.PERFECT : Result.GOOD;
        } else {
            result = Result.MISS;
        }

        // Calculate catch success
        caught = calculateCatch();
        startBounceAnimation();
    }

    private boolean calculateCatch() {
        // Base catch rate from target's HP %
        double hpFraction  = target.getHpPercent();
        double catchRate   = (1.0 - hpFraction * 0.7) * ballMultiplier;

        // Timing bonus
        catchRate *= switch (result) {
            case PERFECT -> 1.5;
            case GOOD    -> 1.0;
            case MISS    -> 0.3;
        };

        catchRate = Math.min(catchRate, 0.98);
        return Math.random() < catchRate;
    }

    // ── Bounce animation (3 shakes like the games) ───────────────────────────

    private void startBounceAnimation() {
        bounceFrame = 0;
        bounceTimer = new Timer(320, e -> {
            bounceFrame++;
            repaint();
            // 3 shakes then show result
            if (bounceFrame >= (caught ? 3 : 1)) {
                bounceTimer.stop();
                Timer finish = new Timer(600, ev -> {
                    onComplete.accept(caught);
                });
                finish.setRepeats(false);
                finish.start();
            }
        });
        bounceTimer.start();
        repaint();
    }

    // ── Painting ──────────────────────────────────────────────────────────────

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();

        if (!thrown) {
            paintBar(g2, w);
            paintInstructions(g2, w);
        } else {
            paintBallAnimation(g2, w);
        }
    }

    private void paintBar(Graphics2D g2, int w) {
        // Title
        g2.setFont(new Font("Arial Black", Font.BOLD, 16));
        g2.setColor(Color.WHITE);
        String title = "Throw a " + ballName + "!  (SPACE or click THROW)";
        g2.drawString(title, (w - g2.getFontMetrics().stringWidth(title)) / 2, 42);

        // Target name + HP hint
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(160, 170, 200));
        double hp = target.getHpPercent() * 100;
        String sub = target.getName() + "  ·  " + (int)hp + "% HP  ·  Press when needle is in the green zone!";
        g2.drawString(sub, (w - g2.getFontMetrics().stringWidth(sub)) / 2, 64);

        // Bar background
        g2.setColor(new Color(30, 32, 50));
        g2.fill(new RoundRectangle2D.Double(BAR_X, BAR_Y, BAR_W, BAR_H, 10, 10));

        // Red zones (outside)
        g2.setColor(new Color(180, 40, 40, 160));
        g2.fill(new RoundRectangle2D.Double(BAR_X, BAR_Y, BAR_W, BAR_H, 10, 10));

        // Green zone
        int gx = BAR_X + (int)(zoneStart * BAR_W);
        int gw = (int)((zoneEnd - zoneStart) * BAR_W);
        g2.setColor(new Color(50, 210, 80));
        g2.fillRect(gx, BAR_Y, gw, BAR_H);

        // Perfect zone (brighter center stripe)
        double zc = (zoneStart + zoneEnd) / 2.0;
        int px = BAR_X + (int)((zc - 0.03) * BAR_W);
        int pw = (int)(0.06 * BAR_W);
        g2.setColor(new Color(150, 255, 130));
        g2.fillRect(Math.max(gx, px), BAR_Y, Math.min(pw, gw), BAR_H);

        // Bar border
        g2.setColor(new Color(80, 90, 130));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Double(BAR_X, BAR_Y, BAR_W, BAR_H, 10, 10));

        // Needle
        int nx = BAR_X + (int)(needlePos * BAR_W);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(nx, BAR_Y - 6, nx, BAR_Y + BAR_H + 6);
        // Arrow tip
        int[] tx = {nx - 6, nx + 6, nx};
        int[] ty = {BAR_Y - 14, BAR_Y - 14, BAR_Y - 6};
        g2.setColor(Color.WHITE);
        g2.fillPolygon(tx, ty, 3);

        // Zone label
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(new Color(20, 20, 20));
        String zoneLabel = "CATCH ZONE";
        FontMetrics fm = g2.getFontMetrics();
        if (gw > fm.stringWidth(zoneLabel) + 4) {
            g2.drawString(zoneLabel, gx + (gw - fm.stringWidth(zoneLabel)) / 2, BAR_Y + 18);
        }
    }

    private void paintInstructions(Graphics2D g2, int w) {
        // Ball count hint already shown in title; add timing colour legend
        int legendY = 218;
        int lx = BAR_X;

        // Perfect
        g2.setColor(new Color(150, 255, 130));
        g2.fillRoundRect(lx, legendY, 14, 14, 4, 4);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(new Color(180, 200, 180));
        g2.drawString("PERFECT  +50% catch rate", lx + 18, legendY + 11);

        // Good
        g2.setColor(new Color(50, 210, 80));
        g2.fillRoundRect(lx + 200, legendY, 14, 14, 4, 4);
        g2.setColor(new Color(180, 200, 180));
        g2.drawString("GOOD  normal catch rate", lx + 218, legendY + 11);

        // Miss
        g2.setColor(new Color(180, 40, 40));
        g2.fillRoundRect(lx + 390, legendY, 14, 14, 4, 4);
        g2.setColor(new Color(180, 200, 180));
        g2.drawString("MISS  −70% catch rate", lx + 408, legendY + 11);
    }

    private void paintBallAnimation(Graphics2D g2, int w) {
        int cx = w / 2;
        int cy = 95;

        // Pokéball (simple drawn circle)
        boolean shake = (bounceFrame % 2 == 1) && !caught || (caught && bounceFrame < 3);
        int shakeX = shake ? (int)(Math.sin(bounceFrame * 2.5) * 10) : 0;
        int ballR = 34;

        // Shadow
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval(cx - ballR + shakeX + 4, cy + ballR + 4, ballR * 2, 14);

        // Ball body
        g2.setColor(new Color(210, 40, 40));
        g2.fillOval(cx - ballR + shakeX, cy - ballR, ballR * 2, ballR * 2);
        g2.setColor(Color.WHITE);
        g2.fillArc(cx - ballR + shakeX, cy, ballR * 2, ballR * 2, 0, 180);

        // Center band
        g2.setColor(new Color(20, 20, 20));
        g2.setStroke(new BasicStroke(4f));
        g2.drawLine(cx - ballR + shakeX, cy, cx + ballR + shakeX, cy);
        // Center button
        g2.setColor(Color.WHITE);
        g2.fillOval(cx - 8 + shakeX, cy - 8, 16, 16);
        g2.setColor(new Color(20, 20, 20));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(cx - 8 + shakeX, cy - 8, 16, 16);

        // Shake count dots
        for (int i = 0; i < 3; i++) {
            boolean filled = i < bounceFrame;
            g2.setColor(filled ? new Color(255, 215, 0) : new Color(50, 50, 70));
            g2.fillOval(cx - 30 + i * 30, cy + ballR + 20, 16, 16);
            g2.setColor(new Color(80, 80, 100));
            g2.drawOval(cx - 30 + i * 30, cy + ballR + 20, 16, 16);
        }

        // Result text (after animation completes)
        if (bounceFrame >= (caught ? 3 : 1) && result != null) {
            String line1, line2;
            Color c1;
            if (caught) {
                line1 = target.getName() + " was caught!";
                line2 = result == Result.PERFECT ? "PERFECT THROW! ✨" : "Nice throw!";
                c1 = new Color(80, 230, 80);
            } else {
                line1 = target.getName() + " broke free!";
                line2 = result == Result.MISS ? "MISS — try again!" : "So close!";
                c1 = new Color(230, 80, 80);
            }
            g2.setFont(new Font("Arial Black", Font.BOLD, 18));
            g2.setColor(c1);
            g2.drawString(line1, (w - g2.getFontMetrics().stringWidth(line1)) / 2, 195);
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.setColor(new Color(180, 190, 220));
            g2.drawString(line2, (w - g2.getFontMetrics().stringWidth(line2)) / 2, 218);
        }
    }
}
