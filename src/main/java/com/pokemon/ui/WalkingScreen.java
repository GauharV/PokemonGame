package com.pokemon.ui;

import com.pokemon.GameState;
import com.pokemon.data.LocationDatabase;
import com.pokemon.data.PokemonDatabase;
import com.pokemon.engine.SaveManager;
import com.pokemon.model.Pokemon;
import com.pokemon.util.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Top-down tile overworld.
 *
 * KEY RULE: Doors (16, 17) are SOLID — player cannot walk onto them.
 * Stand one tile south, face north (press UP once), then press ENTER.
 * Only EXIT tiles (10-13) auto-trigger travel when walked onto.
 */
public class WalkingScreen extends JPanel implements KeyListener {

    // ── Tile IDs ──────────────────────────────────────────────────────────────
    static final int T_GRASS       =  0;
    static final int T_TREE        =  1;
    static final int T_PATH        =  2;
    static final int T_WATER       =  3;
    static final int T_HOUSE       =  4;
    static final int T_HOUSE_DOOR  =  5;  // solid — no interaction
    static final int T_FLOWER      =  6;
    static final int T_SAND        =  7;
    static final int T_ROCK        =  8;
    static final int T_SIGN        =  9;  // solid — face + ENTER
    static final int T_EXIT_N      = 10;  // walkable → travel
    static final int T_EXIT_S      = 11;
    static final int T_EXIT_E      = 12;
    static final int T_EXIT_W      = 13;
    static final int T_MART_WALL   = 14;  // solid
    static final int T_CENTER_WALL = 15;  // solid
    static final int T_CENTER_DOOR = 16;  // SOLID door — face + ENTER → heal
    static final int T_MART_DOOR   = 17;  // SOLID door — face + ENTER → buy

    private static boolean isSolid(int t) {
        return t == T_TREE || t == T_WATER  || t == T_HOUSE    || t == T_ROCK
            || t == T_SIGN || t == T_HOUSE_DOOR
            || t == T_CENTER_WALL || t == T_MART_WALL
            || t == T_CENTER_DOOR || t == T_MART_DOOR;  // solid — face + ENTER to use
    }

    // ── Sizes ─────────────────────────────────────────────────────────────────
    private static final int TILE = 48;
    private static final int COLS = 21;
    private static final int ROWS = 17;

    // ── Player ────────────────────────────────────────────────────────────────
    private int playerTileX, playerTileY;
    private int playerPixX, playerPixY;
    private int targetPixX, targetPixY;
    private boolean walking   = false;
    private int     walkFrame = 0;
    private int     facing    = 2;  // 0=up 1=right 2=down 3=left

    // ── Map ───────────────────────────────────────────────────────────────────
    private int[][]  map;
    private String   currentMapName;
    private float    fadeAlpha = 0f;

    // ── Input ─────────────────────────────────────────────────────────────────
    private final Set<Integer> heldKeys = new HashSet<>();
    private Timer walkTimer, animTimer;
    private int   animTick = 0;

    // ── Wild encounter ────────────────────────────────────────────────────────
    private int grassSteps  = 0;
    private int stepsNeeded = 12;

    // ── Hint bar ──────────────────────────────────────────────────────────────
    private String hintText = null;

    // ── Refs ──────────────────────────────────────────────────────────────────
    private final GameWindow window;
    private GameState gameState;

    // ── Static map data ───────────────────────────────────────────────────────
    private static final Map<String, int[][]>  MAPS        = new HashMap<>();
    private static final Map<String, int[]>    SPAWNS      = new HashMap<>();
    private static final Map<String, String[]> CONNECTIONS = new HashMap<>();

    static { buildAllMaps(); }

    // =========================================================================
    //  Constructor
    // =========================================================================

    public WalkingScreen(GameWindow w) {
        this.window = w;
        setBackground(new Color(15, 22, 15));
        setFocusable(true);
        addKeyListener(this);
        walkTimer = new Timer(16, e -> tickWalk());
        animTimer = new Timer(130, e -> {
            animTick++;
            walkFrame = walking ? (walkFrame + 1) % 4 : 0;
            repaint();
        });
        walkTimer.start();
        animTimer.start();
    }

    public void onShow(GameState gs) {
        this.gameState = gs;
        loadMap(gs.getCurrentLocation());
        requestFocusInWindow();
    }

    // =========================================================================
    //  Map loading
    // =========================================================================

    private void loadMap(String name) {
        currentMapName = name;
        map = MAPS.containsKey(name) ? MAPS.get(name) : makeGenericMap(name);
        int[] sp = SPAWNS.getOrDefault(name, new int[]{COLS / 2, ROWS / 2});
        playerTileX = sp[0];
        playerTileY = sp[1];
        playerPixX = targetPixX = playerTileX * TILE;
        playerPixY = targetPixY = playerTileY * TILE;
        walking = false;
        heldKeys.clear();
        grassSteps  = 0;
        stepsNeeded = 10 + new Random().nextInt(8);
        hintText    = null;
        repaint();
    }

    // =========================================================================
    //  Walk tick
    // =========================================================================

    private void tickWalk() {
        if (walking) {
            int dx = Integer.signum(targetPixX - playerPixX);
            int dy = Integer.signum(targetPixY - playerPixY);
            playerPixX += dx * 6;
            playerPixY += dy * 6;
            if (Math.abs(playerPixX - targetPixX) < 6) playerPixX = targetPixX;
            if (Math.abs(playerPixY - targetPixY) < 6) playerPixY = targetPixY;
            if (playerPixX == targetPixX && playerPixY == targetPixY) {
                walking = false;
                playerTileX = playerPixX / TILE;
                playerTileY = playerPixY / TILE;
                onArrived();
            }
            repaint();
        } else {
            tryStep();
        }
    }

    private void onArrived() {
        int tile = map[playerTileY][playerTileX];

        // Auto-travel on route exit tiles
        if (tile >= T_EXIT_N && tile <= T_EXIT_W) {
            String dest = getDestination(tile);
            if (dest != null) { doTransition(dest); return; }
        }

        // Wild grass
        if (tile == T_GRASS || tile == T_FLOWER) {
            LocationDatabase.Location loc = LocationDatabase.getLocation(currentMapName);
            if (loc != null && loc.hasWildPokemon()) {
                grassSteps++;
                if (grassSteps >= stepsNeeded) {
                    grassSteps  = 0;
                    stepsNeeded = 10 + new Random().nextInt(8);
                    triggerWild();
                    return;
                }
            }
        }

        refreshHint();
    }

    // Recompute hint based on what tile is in front of player
    private void refreshHint() {
        int[] fc = facedCoord();
        if (fc == null) { hintText = null; repaint(); return; }
        int ft = map[fc[1]][fc[0]];
        hintText = switch (ft) {
            case T_SIGN        -> "Press ENTER to read the sign";
            case T_CENTER_DOOR -> "Press ENTER to enter Pokémon Center";
            case T_MART_DOOR   -> "Press ENTER to enter Pokémart";
            default            -> null;
        };
        repaint();
    }

    private int[] facedCoord() {
        int fx = playerTileX + (facing == 1 ? 1 : facing == 3 ? -1 : 0);
        int fy = playerTileY + (facing == 0 ? -1 : facing == 2 ? 1 : 0);
        if (fx < 0 || fy < 0 || fx >= COLS || fy >= ROWS) return null;
        return new int[]{fx, fy};
    }

    private void tryStep() {
        if (walking) return;
        int dx = 0, dy = 0;
        if      (heldKeys.contains(KeyEvent.VK_UP)    || heldKeys.contains(KeyEvent.VK_W)) { dy = -1; facing = 0; }
        else if (heldKeys.contains(KeyEvent.VK_DOWN)  || heldKeys.contains(KeyEvent.VK_S)) { dy =  1; facing = 2; }
        else if (heldKeys.contains(KeyEvent.VK_LEFT)  || heldKeys.contains(KeyEvent.VK_A)) { dx = -1; facing = 3; }
        else if (heldKeys.contains(KeyEvent.VK_RIGHT) || heldKeys.contains(KeyEvent.VK_D)) { dx =  1; facing = 1; }
        else return;

        refreshHint();  // update hint when direction changes even if blocked

        int nx = playerTileX + dx;
        int ny = playerTileY + dy;
        if (nx < 0 || ny < 0 || nx >= COLS || ny >= ROWS) return;
        if (isSolid(map[ny][nx])) return;

        targetPixX = nx * TILE;
        targetPixY = ny * TILE;
        walking    = true;
    }

    // =========================================================================
    //  Interaction
    // =========================================================================

    private void onInteract() {
        int[] fc = facedCoord();
        if (fc == null) return;
        int ft = map[fc[1]][fc[0]];
        switch (ft) {
            case T_SIGN -> JOptionPane.showMessageDialog(this,
                    "Welcome to " + currentMapName + "!\nExplore routes and catch wild Pokémon!",
                    "Sign", JOptionPane.PLAIN_MESSAGE);
            case T_CENTER_DOOR -> healParty();
            case T_MART_DOOR   -> openMart();
        }
        requestFocusInWindow();
    }

    private void healParty() {
        gameState.healAllPokemon();
        SoundManager.play(SoundManager.SoundEffect.HEAL);
        JOptionPane.showMessageDialog(this,
                "Your Pokémon have been fully restored! ❤", "Pokémon Center", JOptionPane.PLAIN_MESSAGE);
        requestFocusInWindow();
    }

    private void openMart() {
        final int PB = 200, GB = 600, UB = 1200;
        String[] opts = {
            "Pokéball  $" + PB  + "  (×" + gameState.getPokeballs()  + ")",
            "Great Ball $" + GB + "  (×" + gameState.getGreatballs() + ")",
            "Ultra Ball $" + UB + "  (×" + gameState.getUltraballs() + ")",
            "Leave"
        };
        String pick = (String) JOptionPane.showInputDialog(this,
                "Pokémart — You have $" + gameState.getMoney(), "Pokémart",
                JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
        requestFocusInWindow();
        if (pick == null || pick.startsWith("Leave")) return;

        int price;  String ballName;
        if      (pick.startsWith("Pokéball"))  { price = PB; ballName = "Pokéball"; }
        else if (pick.startsWith("Great Ball")){ price = GB; ballName = "Great Ball"; }
        else                                   { price = UB; ballName = "Ultra Ball"; }

        String qs = JOptionPane.showInputDialog(this, "How many " + ballName + "s?");
        requestFocusInWindow();
        if (qs == null) return;
        try {
            int qty = Integer.parseInt(qs.trim());
            int tot = price * qty;
            if (qty <= 0 || gameState.getMoney() < tot) {
                JOptionPane.showMessageDialog(this, "Not enough money!", "Pokémart", JOptionPane.WARNING_MESSAGE);
                return;
            }
            gameState.spendMoney(tot);
            if      (ballName.equals("Pokéball"))   gameState.addPokeballs(qty);
            else if (ballName.equals("Great Ball"))  gameState.addGreatballs(qty);
            else                                     gameState.addUltraballs(qty);
            JOptionPane.showMessageDialog(this, "Bought " + qty + "× " + ballName + " for $" + tot + "!");
        } catch (NumberFormatException ignored) {}
        requestFocusInWindow();
    }

    // =========================================================================
    //  Travel
    // =========================================================================

    private String getDestination(int exitTile) {
        String[] conns = CONNECTIONS.get(currentMapName);
        if (conns == null) {
            LocationDatabase.Location loc = LocationDatabase.getLocation(currentMapName);
            if (loc != null) conns = loc.connections();
        }
        if (conns == null || conns.length == 0) return null;
        return switch (exitTile) {
            case T_EXIT_N -> conns.length > 0 ? conns[0] : null;
            case T_EXIT_S -> conns.length > 1 ? conns[1] : (conns.length > 0 ? conns[0] : null);
            case T_EXIT_E -> conns.length > 1 ? conns[1] : (conns.length > 0 ? conns[0] : null);
            case T_EXIT_W -> conns.length > 0 ? conns[0] : null;
            default       -> null;
        };
    }

    private void doTransition(String dest) {
        walking = false;
        heldKeys.clear();
        Timer out = new Timer(18, null);
        float[] a = {0f};
        out.addActionListener(e -> {
            a[0] = Math.min(1f, a[0] + 0.09f);
            fadeAlpha = a[0];
            repaint();
            if (a[0] >= 1f) {
                out.stop();
                gameState.setCurrentLocation(dest);
                loadMap(dest);
                Timer in = new Timer(18, null);
                float[] b = {1f};
                in.addActionListener(e2 -> {
                    b[0] = Math.max(0f, b[0] - 0.09f);
                    fadeAlpha = b[0];
                    repaint();
                    if (b[0] <= 0f) in.stop();
                });
                in.start();
            }
        });
        out.start();
    }

    private void triggerWild() {
        walking = false;
        heldKeys.clear();
        walkTimer.stop();
        int[] enc = LocationDatabase.rollWildEncounter(currentMapName);
        if (enc == null) { walkTimer.start(); return; }
        Pokemon wild = PokemonDatabase.create(enc[0], enc[1]);
        SoundManager.play(SoundManager.SoundEffect.WILD_ENCOUNTER);
        Timer t = new Timer(280, e -> { ((Timer)e.getSource()).stop(); walkTimer.start(); window.startWildBattle(wild); });
        t.setRepeats(false);
        t.start();
    }

    // =========================================================================
    //  Painting
    // =========================================================================

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int sw = getWidth(), sh = getHeight();

        // Camera centred on player
        int camX = playerPixX + TILE / 2 - sw / 2;
        int camY = playerPixY + TILE / 2 - sh / 2;

        for (int row = 0; row < ROWS; row++)
            for (int col = 0; col < COLS; col++)
                drawTile(g, map[row][col], col * TILE - camX, row * TILE - camY, col, row);

        drawPlayer(g, playerPixX - camX, playerPixY - camY);
        drawHUD(g, sw, sh);

        // Hint bar at bottom
        if (hintText != null) {
            g.setColor(new Color(0, 0, 0, 190));
            g.fillRoundRect(sw / 2 - 210, sh - 78, 420, 34, 10, 10);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.setColor(new Color(255, 230, 80));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(hintText, sw / 2 - fm.stringWidth(hintText) / 2, sh - 55);
        }

        // Fade overlay
        if (fadeAlpha > 0) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, sw, sh);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }

    // ── Tile drawing ──────────────────────────────────────────────────────────

    private void drawTile(Graphics2D g, int type, int tx, int ty, int col, int row) {
        if (tx < -TILE || ty < -TILE || tx > getWidth() + TILE || ty > getHeight() + TILE) return;
        int seed = col * 13 + row * 7;

        switch (type) {
            case T_GRASS -> {
                g.setColor(new Color(80, 160, 60)); g.fillRect(tx, ty, TILE, TILE);
                g.setColor(new Color(55, 135, 40));
                for (int i = 0; i < 3; i++) {
                    int bx = tx + 6 + i * 14 + seed % 8, by = ty + 10 + seed % 6;
                    g.drawLine(bx, by+7, bx-2, by); g.drawLine(bx, by+7, bx+2, by);
                }
            }
            case T_FLOWER -> {
                g.setColor(new Color(85, 165, 60)); g.fillRect(tx, ty, TILE, TILE);
                Color[] fc = {new Color(255,80,80),new Color(255,200,50),new Color(200,80,220),new Color(80,150,255)};
                g.setColor(fc[seed % 4]); g.fillOval(tx+7+seed%9, ty+9+seed%7, 11, 11);
                g.setColor(fc[(seed+2)%4]); g.fillOval(tx+26+seed%6, ty+20+seed%8, 9, 9);
            }
            case T_TREE -> {
                g.setColor(new Color(30, 80, 20)); g.fillRect(tx, ty, TILE, TILE);
                g.setColor(new Color(95, 60, 18)); g.fillRect(tx+TILE/2-5, ty+TILE/2+2, 10, TILE/2-2);
                g.setColor(new Color(28, 105, 18)); g.fillOval(tx+3, ty+2, TILE-6, TILE-8);
                g.setColor(new Color(50, 140, 30)); g.fillOval(tx+8, ty+5, TILE-18, TILE-16);
            }
            case T_PATH -> {
                g.setColor(new Color(185, 165, 112)); g.fillRect(tx, ty, TILE, TILE);
                g.setColor(new Color(165, 145, 95, 50)); g.drawRect(tx, ty, TILE, TILE);
            }
            case T_WATER -> {
                g.setColor(new Color(55, 110, 200)); g.fillRect(tx, ty, TILE, TILE);
                double w = Math.sin(animTick * 0.14 + col * 0.8 + row * 0.5);
                g.setColor(new Color(90, 150, 225, 110));
                g.fillOval(tx+4+(int)(w*3), ty+9,  TILE-10, 8);
                g.fillOval(tx+12+(int)(w*-2), ty+24, TILE-22, 6);
            }
            case T_HOUSE -> {
                g.setColor(new Color(185, 105, 60)); g.fillRect(tx, ty, TILE, TILE);
                g.setColor(new Color(140, 65, 28)); g.fillRect(tx, ty, TILE, 9);
                g.setColor(new Color(175, 215, 255)); g.fillRect(tx+8, ty+10, 13, 11);
                g.setColor(new Color(100, 75, 40)); g.drawRect(tx+8, ty+10, 13, 11);
            }
            case T_HOUSE_DOOR -> {
                g.setColor(new Color(130, 80, 35)); g.fillRect(tx, ty, TILE, TILE);
                g.setColor(new Color(195, 160, 75)); g.fillRect(tx+10, ty+14, TILE-20, TILE-14);
                g.setColor(new Color(240, 210, 80)); g.fillOval(tx+TILE-17, ty+TILE/2-4, 7, 7);
            }
            case T_ROCK -> {
                g.setColor(new Color(105, 95, 85)); g.fillRect(tx, ty, TILE, TILE);
                g.setColor(new Color(125, 115, 105)); g.fillOval(tx+5, ty+7, TILE-12, TILE-14);
                g.setColor(new Color(155, 145, 135)); g.fillOval(tx+11, ty+10, 10, 8);
            }
            case T_SIGN -> {
                g.setColor(new Color(185, 165, 112)); g.fillRect(tx, ty, TILE, TILE);
                g.setColor(new Color(100, 70, 30)); g.fillRect(tx+TILE/2-3, ty+TILE/2, 6, TILE/2);
                g.setColor(new Color(165, 140, 90)); g.fillRect(tx+7, ty+8, TILE-14, TILE/2-4);
                g.setColor(new Color(80, 55, 20)); g.drawRect(tx+7, ty+8, TILE-14, TILE/2-4);
                g.setFont(new Font("Arial Black", Font.BOLD, 14));
                g.setColor(new Color(60, 40, 10)); g.drawString("!", tx+TILE/2-4, ty+TILE/2-2);
            }
            // ── Exit tiles with destination label ────────────────────────────
            case T_EXIT_N, T_EXIT_S, T_EXIT_E, T_EXIT_W -> {
                g.setColor(new Color(110, 195, 110)); g.fillRect(tx, ty, TILE, TILE);
                g.setColor(new Color(80, 170, 80, 80)); g.drawRect(tx, ty, TILE, TILE);
                // Arrow
                g.setColor(new Color(30, 120, 30));
                int ax = tx+TILE/2, ay = ty+TILE/2;
                int[] axp, ayp;
                if      (type==T_EXIT_N){axp=new int[]{ax,ax-9,ax+9};ayp=new int[]{ay-11,ay+8,ay+8};}
                else if (type==T_EXIT_S){axp=new int[]{ax,ax-9,ax+9};ayp=new int[]{ay+11,ay-8,ay-8};}
                else if (type==T_EXIT_E){axp=new int[]{ax+11,ax-8,ax-8};ayp=new int[]{ay,ay-9,ay+9};}
                else                   {axp=new int[]{ax-11,ax+8,ax+8};ayp=new int[]{ay,ay-9,ay+9};}
                g.fillPolygon(axp, ayp, 3);

                // Destination label — floating sign above/beside the exit tile
                String dest = getDestination(type);
                if (dest != null) {
                    g.setFont(new Font("Arial Black", Font.BOLD, 11));
                    FontMetrics fm = g.getFontMetrics();
                    int lw = fm.stringWidth(dest);
                    int lh = 18;
                    int lx = tx + TILE/2 - lw/2 - 6;
                    int ly = ty - lh - 4;  // float above the tile
                    // Sign background
                    g.setColor(new Color(20, 20, 20, 210));
                    g.fillRoundRect(lx, ly, lw + 12, lh, 6, 6);
                    g.setColor(new Color(100, 220, 100));
                    g.setStroke(new BasicStroke(1.5f));
                    g.drawRoundRect(lx, ly, lw + 12, lh, 6, 6);
                    // Arrow direction prefix
                    String arrow = switch(type) {
                        case T_EXIT_N -> "↑ ";
                        case T_EXIT_S -> "↓ ";
                        case T_EXIT_E -> "→ ";
                        case T_EXIT_W -> "← ";
                        default -> "";
                    };
                    g.setColor(new Color(255, 230, 80));
                    g.drawString(arrow + dest, lx + 6, ly + lh - 4);
                }
            }
            // ── Buildings ────────────────────────────────────────────────────
            case T_CENTER_WALL -> {
                g.setColor(new Color(215, 50, 50)); g.fillRect(tx, ty, TILE, TILE);
                g.setFont(new Font("Arial Black", Font.BOLD, 8));
                g.setColor(Color.WHITE);
                g.drawString("PK", tx+4, ty+TILE/2-1);
                g.drawString("CTR", tx+2, ty+TILE/2+9);
            }
            case T_MART_WALL -> {
                g.setColor(new Color(50, 105, 195)); g.fillRect(tx, ty, TILE, TILE);
                g.setFont(new Font("Arial Black", Font.BOLD, 8));
                g.setColor(Color.WHITE);
                g.drawString("PK", tx+4, ty+TILE/2-1);
                g.drawString("MART", tx+1, ty+TILE/2+9);
            }
            case T_CENTER_DOOR -> {
                // Visually distinct red door — SOLID, cannot walk through
                g.setColor(new Color(180, 40, 40)); g.fillRect(tx, ty, TILE, TILE);
                g.setColor(new Color(235, 190, 190)); g.fillRect(tx+9, ty+8, TILE-18, TILE-8);
                // Red cross symbol
                g.setColor(new Color(200, 40, 40));
                g.fillRect(tx+TILE/2-8, ty+15, 16, 5);
                g.fillRect(tx+TILE/2-2, ty+10, 5, 15);
                g.setFont(new Font("SansSerif", Font.BOLD, 7));
                g.setColor(new Color(120, 20, 20));
                g.drawString("ENTER", tx+3, ty+TILE-4);
            }
            case T_MART_DOOR -> {
                // Blue door — SOLID
                g.setColor(new Color(40, 85, 185)); g.fillRect(tx, ty, TILE, TILE);
                g.setColor(new Color(190, 205, 240)); g.fillRect(tx+9, ty+8, TILE-18, TILE-8);
                // $ symbol
                g.setFont(new Font("Arial Black", Font.BOLD, 16));
                g.setColor(new Color(35, 70, 160));
                g.drawString("$", tx+TILE/2-6, ty+TILE/2+7);
                g.setFont(new Font("SansSerif", Font.BOLD, 7));
                g.setColor(new Color(25, 55, 130));
                g.drawString("ENTER", tx+3, ty+TILE-4);
            }
            default -> { g.setColor(new Color(80,160,60)); g.fillRect(tx, ty, TILE, TILE); }
        }
    }

    // ── Player ────────────────────────────────────────────────────────────────

    private void drawPlayer(Graphics2D g, int px, int py) {
        g.setColor(new Color(0,0,0,45)); g.fillOval(px+8, py+TILE-9, TILE-16, 8);
        boolean step = (walkFrame == 1 || walkFrame == 2);
        // Legs
        g.setColor(new Color(45, 75, 180));
        if (walking && step) { g.fillRect(px+12, py+28, 8, 14); g.fillRect(px+22, py+24, 8, 10); }
        else if (walking)    { g.fillRect(px+12, py+24, 8, 10); g.fillRect(px+22, py+28, 8, 14); }
        else                 { g.fillRect(px+13, py+28, 8, 12); g.fillRect(px+23, py+28, 8, 12); }
        // Body
        g.setColor(new Color(215, 45, 45)); g.fillRoundRect(px+10, py+14, 22, 17, 4, 4);
        // Arms
        int sw = walking ? (step ? -4 : 4) : 0;
        g.setColor(new Color(238, 188, 138));
        g.fillRect(px+5,  py+14+sw, 6, 12);
        g.fillRect(px+31, py+14-sw, 6, 12);
        // Head
        g.setColor(new Color(238, 188, 138)); g.fillOval(px+11, py+3, 20, 18);
        // Hat
        g.setColor(new Color(195, 28, 28)); g.fillRoundRect(px+9, py+3, 24, 10, 4, 4);
        g.fillRect(px+8, py+8, 27, 5);
        g.setColor(Color.WHITE); g.fillRect(px+14, py+5, 6, 3);
        // Eyes
        g.setColor(new Color(25, 25, 25));
        switch (facing) {
            case 2 -> { g.fillOval(px+14, py+12, 3, 3); g.fillOval(px+25, py+12, 3, 3); }
            case 1 -> g.fillOval(px+25, py+12, 3, 3);
            case 3 -> g.fillOval(px+14, py+12, 3, 3);
        }
    }

    // ── HUD ───────────────────────────────────────────────────────────────────

    private void drawHUD(Graphics2D g, int sw, int sh) {
        g.setColor(new Color(0,0,0,175)); g.fillRect(0, 0, sw, 36);
        g.setFont(new Font("Arial Black", Font.BOLD, 12));
        g.setColor(new Color(100, 230, 100)); g.drawString("📍 " + currentMapName, 12, 23);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(new Color(255, 215, 0)); g.drawString("$" + gameState.getMoney(), 280, 23);
        g.setColor(new Color(200, 200, 225));
        g.drawString("⚪" + gameState.getPokeballs() + "  🔵" + gameState.getGreatballs() + "  🟡" + gameState.getUltraballs(), 365, 23);
        // Right-side buttons
        hudBtn(g, sw-370, 5, 80, 26, "POKÉDEX", new Color(50,75,185));
        hudBtn(g, sw-280, 5, 55, 26, "PARTY",   new Color(35,115,40));
        hudBtn(g, sw-215, 5, 48, 26, "SAVE",    new Color(115,85,28));
        hudBtn(g, sw-157, 5, 46, 26, "HEAL ❤",  new Color(180,40,40));
        hudBtn(g, sw- 99, 5, 55, 26, "MART $",  new Color(40,80,180));
        hudBtn(g, sw- 36, 5, 30, 26, "≡",       new Color(80,80,80));
        g.setColor(new Color(0,0,0,130)); g.fillRect(0, sh-26, sw, 26);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.setColor(new Color(155,155,175));
        g.drawString("Arrow keys/WASD: Move", 12, sh-8);
    }

    private void hudBtn(Graphics2D g, int x, int y, int w, int h, String lbl, Color c) {
        g.setColor(c); g.fillRoundRect(x,y,w,h,6,6);
        g.setColor(c.brighter()); g.setStroke(new BasicStroke(1f)); g.drawRoundRect(x,y,w,h,6,6);
        g.setFont(new Font("Arial Black", Font.BOLD, 9)); g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(lbl, x+(w-fm.stringWidth(lbl))/2, y+17);
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────

    @Override public void addNotify() {
        super.addNotify();
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int sw=getWidth(), x=e.getX(), y=e.getY();
                if (y<5||y>31) return;
                if      (x>=sw-370&&x<=sw-290) window.showPokedex();
                else if (x>=sw-280&&x<=sw-225) showParty();
                else if (x>=sw-215&&x<=sw-167) doSave();
                else if (x>=sw-157&&x<=sw-111) healParty();
                else if (x>=sw- 99&&x<=sw- 44) openMart();
                else if (x>=sw- 36)            window.showScreen(GameWindow.SCREEN_MENU);
            }
        });
    }

    private void showParty() {
        StringBuilder sb=new StringBuilder("<html><body style='font-family:sans-serif;padding:4'>");
        for (Pokemon p : gameState.getParty()) {
            String c = p.isFainted()?"red":p.getHpPercent()<0.25?"orange":"green";
            sb.append("<b>").append(p.getName()).append("</b> Lv.").append(p.getLevel())
              .append("  <font color='").append(c).append("'>")
              .append(p.getCurrentHp()).append("/").append(p.getMaxHp()).append(" HP</font><br>");
        }
        JOptionPane.showMessageDialog(this, sb+"</body></html>","Your Party",JOptionPane.PLAIN_MESSAGE);
        requestFocusInWindow();
    }

    private void doSave() {
        try { SaveManager.save(gameState); SoundManager.play(SoundManager.SoundEffect.SAVE);
              JOptionPane.showMessageDialog(this,"Game saved! 💾"); }
        catch(Exception ex){JOptionPane.showMessageDialog(this,"Save failed: "+ex.getMessage());}
        requestFocusInWindow();
    }

    // ── Keys ──────────────────────────────────────────────────────────────────

    @Override public void keyPressed(KeyEvent e) {
        heldKeys.add(e.getKeyCode());
        if (e.getKeyCode()==KeyEvent.VK_ENTER||e.getKeyCode()==KeyEvent.VK_SPACE) onInteract();
    }
    @Override public void keyReleased(KeyEvent e) { heldKeys.remove(e.getKeyCode()); }
    @Override public void keyTyped(KeyEvent e) {}

    // =========================================================================
    //  Map data
    //  Tile key: 0=grass 1=tree 2=path 3=water 4=house 5=house-door
    //            6=flower 7=sand 8=rock 9=sign
    //           10=exitN 11=exitS 12=exitE 13=exitW
    //           14=mart-wall 15=center-wall 16=center-DOOR 17=mart-DOOR
    //
    //  16 and 17 are SOLID WALLS. Stand south of them, face up, press ENTER.
    // =========================================================================

    private static void buildAllMaps() {

        // ── Pallet Town ──────────────────────────────────────────────────────
        MAPS.put("Pallet Town", new int[][]{
            {1,1,1,1,1,1,1,1,1,1, 1,1,1,1,1,1,1,1,1,1,1},
            {1,1,1,1,1,1,1,1,1,1,10,1,1,1,1,1,1,1,1,1,1},  // ↑ Route 1
            {1,0,0,0,0,1,1,1,1,1, 2,1,1,1,1,1,0,0,0,0,1},
            {1,0,4,4,0,1,1,1,1,1, 2,1,1,1,1,1,0,4,4,0,1},
            {1,0,4,5,0,1,1,1,1,1, 2,1,1,1,1,1,0,4,5,0,1},
            {1,0,0,0,0,0,0,0,0,0, 2,0,0,0,0,0,0,0,0,0,1},
            {1,6,6,6,6,6,6,6,6,6, 2,6,6,6,6,6,6,6,6,6,1},
            {1,6,9,6,6,6,6,6,6,6, 2,6,6,6,6,6,6,9,6,6,1},
            {1,6,6,6,6,6,6,6,6,6, 2,6,6,6,6,6,6,6,6,6,1},
            {1,0,0,0,0,0,0,0,0,0, 2,0,0,0,0,0,0,0,0,0,1},
            {1,0,4,4,0,1,1,0,4,4, 2,4,4,0,1,1,0,4,4,0,1},
            {1,0,4,5,0,1,1,0,4,5, 2,4,5,0,1,1,0,4,5,0,1},
            {1,0,0,0,0,1,1,0,0,0, 2,0,0,0,1,1,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1, 2,1,1,1,1,1,1,1,1,1,1},
            {1,1,1,1,1,1,1,1,1,1, 2,1,1,1,1,1,1,1,1,1,1},
            {1,1,1,1,1,1,1,1,1,1, 3,1,1,1,1,1,1,1,1,1,1},
            {1,1,1,1,1,1,1,1,1,1, 3,1,1,1,1,1,1,1,1,1,1},
        });
        SPAWNS.put("Pallet Town", new int[]{10, 8});
        CONNECTIONS.put("Pallet Town", new String[]{"Route 1"});

        // ── Route 1 ──────────────────────────────────────────────────────────
        MAPS.put("Route 1", new int[][]{
            {1,1,1,1,1,1,1,1,1,1,10,1,1,1,1,1,1,1,1,1,1},  // ↑ Viridian City
            {1,0,0,6,6,6,6,0,0,0, 2,0,0,0,6,6,6,0,0,0,1},
            {1,0,1,6,6,1,6,6,0,0, 2,0,0,6,6,1,6,6,0,0,1},
            {1,0,0,0,0,0,0,6,0,0, 2,0,0,6,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,6,6,6,0, 2,0,6,6,6,0,0,0,0,0,1},
            {1,1,0,0,6,6,6,1,6,0, 2,0,6,1,6,6,6,0,0,1,1},
            {1,1,0,6,6,1,0,0,0,0, 2,0,0,0,0,1,6,6,0,1,1},
            {1,0,0,6,0,0,0,0,9,0, 2,0,9,0,0,0,0,6,0,0,1},
            {1,0,6,6,6,0,0,0,0,0, 2,0,0,0,0,0,6,6,6,0,1},
            {1,0,6,1,6,6,0,0,0,0, 2,0,0,0,0,6,6,1,6,0,1},
            {1,0,0,0,0,6,6,0,0,0, 2,0,0,0,6,6,0,0,0,0,1},
            {1,0,0,0,6,6,1,6,0,0, 2,0,0,6,1,6,6,0,0,0,1},
            {1,0,6,6,6,1,0,6,6,0, 2,0,6,6,0,1,6,6,6,0,1},
            {1,0,6,1,0,0,0,0,6,0, 2,0,6,0,0,0,0,1,6,0,1},
            {1,0,0,0,0,0,0,6,6,6, 2,6,6,6,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,6,6,1,0, 2,0,1,6,6,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,11,1,1,1,1,1,1,1,1,1,1},  // ↓ Pallet Town
        });
        SPAWNS.put("Route 1", new int[]{10, 14});
        CONNECTIONS.put("Route 1", new String[]{"Viridian City", "Pallet Town"});

        // ── Viridian City ─────────────────────────────────────────────────────
        // PkCenter door=16 at col7 row4. Player stands at col7 row5 facing up → presses ENTER
        // PkMart   door=17 at col12 row4. Player stands at col12 row5 facing up → presses ENTER
        MAPS.put("Viridian City", new int[][]{
            {1,1,1,1,1,1,1,1, 1,1,10,1,1,1,1,1,1,1,1,1,1},  // ↑ Mt. Moon
            {1,0,0,0,0,0,0,0, 0,0, 2,0,0,0,0,0,0,0,0,0,1},
            {1,0,4,4,4,0, 0,15,15, 0,2,0,14,14,0,0,4,4,4,0,1},
            {1,0,4,4,4,0, 0,15,15, 0,2,0,14,14,0,0,4,4,4,0,1},
            {1,0,0,0,0,0, 0,16, 2,  0,2,0,17,  2,0,0,0,0,0,0,1},  // PkCenter=16, Mart=17 — face+ENTER
            {1,0,0,0,0,0, 0, 0,0,  0,2,0, 0, 0,0,0,0,0,0,0,1},  // player stands HERE to use door
            {1,0,0,6,6,0, 0, 0,0,  0,2,0, 0, 0,0,0,6,6,0,0,1},
            {1,0,6,6,6,6, 0, 0,9,  0,2,0, 9, 0,0,6,6,6,6,0,1},
            {1,0,6,1,1,6, 0, 0,0,  0,2,0, 0, 0,0,6,1,1,6,0,1},
            {1,0,0,0,0,0, 0, 0,0,  0,2,0, 0, 0,0,0,0,0,0,0,1},
            {1,0,4,4,4,0, 0, 4,4,  0,2,0, 4, 4,0,0,4,4,4,0,1},
            {1,0,4,4,4,0, 0, 4,4,  0,2,0, 4, 4,0,0,4,4,4,0,1},
            {1,0,4,5,4,0, 0, 4,5,  0,2,0, 4, 5,0,0,4,5,4,0,1},
            {1,0,0,0,0,0, 0, 0,0,  0,2,0, 0, 0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0, 0, 0,0,  0,2,0, 0, 0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0, 0, 0,0,  0,2,0, 0, 0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1, 1, 1,1,  1,11,1,1, 1,1,1,1,1,1,1,1},  // ↓ Route 1
        });
        SPAWNS.put("Viridian City", new int[]{10, 10});
        CONNECTIONS.put("Viridian City", new String[]{"Mt. Moon", "Route 1"});

        // ── Mt. Moon ──────────────────────────────────────────────────────────
        MAPS.put("Mt. Moon", new int[][]{
            {1,1,1,1,1,1,1,1,1,1,10,1,1,1,1,1,1,1,1,1,1},  // ↑ Cerulean City
            {1,8,8,0,0,8,0,0,0,0, 2,0,0,0,0,8,0,0,8,8,1},
            {1,8,0,0,8,0,0,8,0,0, 2,0,0,8,0,0,8,0,0,8,1},
            {1,0,0,8,0,6,6,0,0,0, 2,0,0,0,6,6,0,8,0,0,1},
            {1,0,8,0,6,6,8,6,0,0, 2,0,0,6,8,6,6,0,8,0,1},
            {1,0,0,6,6,0,0,6,6,0, 2,0,6,6,0,0,6,6,0,0,1},
            {1,8,6,6,0,0,8,0,6,0, 2,0,6,0,8,0,0,6,6,8,1},
            {1,0,6,0,0,8,0,0,0,0, 2,0,0,0,0,8,0,0,6,0,1},
            {1,0,0,0,8,0,6,6,0,0, 2,0,0,6,6,0,8,0,0,0,1},
            {1,8,0,6,0,0,6,0,8,0, 2,0,8,0,6,0,0,6,0,8,1},
            {1,0,6,6,8,0,0,6,0,0, 2,0,0,6,0,0,8,6,6,0,1},
            {1,0,6,0,0,0,8,0,6,0, 2,0,6,0,8,0,0,0,6,0,1},
            {1,8,0,0,6,6,0,0,0,0, 2,0,0,0,0,6,6,0,0,8,1},
            {1,0,0,8,0,6,8,6,0,0, 2,0,0,6,8,6,0,8,0,0,1},
            {1,0,8,0,0,0,6,0,0,0, 2,0,0,0,6,0,0,0,8,0,1},
            {1,8,0,0,0,8,0,0,0,0, 2,0,0,0,0,8,0,0,0,8,1},
            {1,1,1,1,1,1,1,1,1,1,11,1,1,1,1,1,1,1,1,1,1},  // ↓ Viridian City
        });
        SPAWNS.put("Mt. Moon", new int[]{10, 10});
        CONNECTIONS.put("Mt. Moon", new String[]{"Cerulean City", "Viridian City"});

        // ── Cerulean City ─────────────────────────────────────────────────────
        MAPS.put("Cerulean City", new int[][]{
            {1,1,1,1,1,1,1,1, 1,1,10,1,1,1,1,1,1,1,1,1,1},  // ↑ Route 24
            {1,0,0,0,0,0,0,0, 0,0, 2,0,0,0,0,0,0,0,0,0,1},
            {1,0,4,4,4,0, 0,15,15, 0,2,0,14,14,0,0,4,4,4,0,1},
            {1,0,4,4,4,0, 0,15,15, 0,2,0,14,14,0,0,4,4,4,0,1},
            {1,0,0,0,0,0, 0,16,  2, 0,2,0,17,  2,0,0,0,0,0,0,1},  // PkCenter=16, Mart=17 — face+ENTER
            {1,0,0,0,0,0, 0, 0, 0, 0,2,0, 0, 0,0,0,0,0,0,0,1},
            {3,3,3,0,0,0, 0, 0, 0, 0,2,0, 0, 0,0,0,0,0,3,3,3},
            {3,3,3,0,6,6, 0, 0, 9, 0,2,0, 9, 0,0,6,6,0,3,3,3},
            {3,3,3,0,6,1, 6, 0, 0, 0,2,0, 0, 0,6,1,6,0,3,3,3},
            {1,0,0,0,0,0, 0, 0, 0, 0,2,0, 0, 0,0,0,0,0,0,0,1},
            {1,0,4,4,4,0, 0, 4, 4, 0,2,0, 4, 4,0,0,4,4,4,0,1},
            {1,0,4,4,4,0, 0, 4, 4, 0,2,0, 4, 4,0,0,4,4,4,0,1},
            {1,0,4,5,4,0, 0, 4, 5, 0,2,0, 4, 5,0,0,4,5,4,0,1},
            {1,0,0,0,0,0, 0, 0, 0, 0,2,0, 0, 0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0, 0, 0, 0, 0,2,0, 0, 0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0, 0, 0, 0, 0,2,0, 0, 0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1, 1, 1, 1, 1,11,1, 1, 1,1,1,1,1,1,1,1},  // ↓ Mt. Moon
        });
        SPAWNS.put("Cerulean City", new int[]{10, 10});
        CONNECTIONS.put("Cerulean City", new String[]{"Route 24", "Mt. Moon"});
    }

    private int[][] makeGenericMap(String name) {
        int[][] m = new int[ROWS][COLS];
        Random r  = new Random(name.hashCode());
        for (int row = 0; row < ROWS; row++)
            for (int col = 0; col < COLS; col++) {
                if (row==0||row==ROWS-1||col==0||col==COLS-1){m[row][col]=T_TREE;continue;}
                if (col==COLS/2){m[row][col]=T_PATH;continue;}
                int v=r.nextInt(10);
                m[row][col]=v<4?T_GRASS:v<6?T_FLOWER:v<7?T_TREE:T_PATH;
            }
        m[1][COLS/2]=T_EXIT_N;
        m[ROWS-2][COLS/2]=T_EXIT_S;
        m[3][4]=T_CENTER_WALL; m[3][5]=T_CENTER_WALL; m[4][4]=T_CENTER_DOOR;
        m[3][COLS-5]=T_MART_WALL; m[3][COLS-4]=T_MART_WALL; m[4][COLS-5]=T_MART_DOOR;
        SPAWNS.put(name, new int[]{COLS/2, ROWS/2});
        return m;
    }
}