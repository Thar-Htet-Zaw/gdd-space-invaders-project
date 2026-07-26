package gdd.scene;
 
import gdd.AudioPlayer;
import gdd.Game;
import gdd.Global;
 
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.powerup.MultiShot;
import gdd.sprite.Alien1;
import gdd.sprite.Alien2;
import gdd.sprite.Alien3;
import gdd.sprite.Enemy;
import gdd.sprite.Enemy.Bomb;
import gdd.sprite.Explosion;
import gdd.sprite.Player;
import gdd.sprite.Shot;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
 
public class Scene1 extends JPanel {
 
    private int frame = 0;
    private List<PowerUp> powerups;
    private List<Enemy> enemies;
    private List<Explosion> explosions;
    private List<Shot> shots;
    private List<Bomb> bombs;
    private Player player;
    // private Shot shot;
 
    final int BLOCKHEIGHT = 50;
    final int BLOCKWIDTH = 50;
 
    final int BLOCKS_TO_DRAW = BOARD_HEIGHT / BLOCKHEIGHT;
 
    private int direction = -1;
    private int deaths = 0;
    private int scoutKills = 0;       // Alien1 kills
    private int wraithKills = 0;      // Alien2 (zigzag) kills
    private int juggernautKills = 0;  // Alien3 (tank) kills
 
    private String pickupMessage = null;
    private int pickupMessageFrame = 0;
    private static final int PICKUP_MESSAGE_HOLD_FRAMES = 90;  // ~1.5s fully visible
    private static final int PICKUP_MESSAGE_FADE_FRAMES = 60;  // ~1s fading out
 
    private boolean inGame = true;
    private boolean isVictory = false;
    private String message = "Game Over";
 
    // --- Stage 1 dashboard ---
    private long score = 0;
    private int shotsFired = 0;
    private boolean showDashboard = false;
    private static final int STAGE_DURATION_FRAMES = 1 * 60 * 60; // TEMP: 1 minute for testing — change back to 5 * 60 * 60 before submitting
    private Rectangle continueButtonBounds;
 
    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private final Random randomizer = new Random();
 
    private Timer timer;
    private final Game game;
 
    private int currentRow = -1;
    // TODO load this map from a file
    private int mapOffset = 0;
    private final int[][] MAP = {
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}
    };
 
    private HashMap<Integer, SpawnDetails> spawnMap = new HashMap<>();
    private AudioPlayer audioPlayer;
    private int lastRowToShow;
    private int firstRowToShow;
 
    public Scene1(Game game) {
        this.game = game;
        // initBoard();
        // gameInit();
        loadSpawnDetails();
    }
 
    private void initAudio() {
        try {
            String filePath = "src/audio/scene1.wav";
            audioPlayer = new AudioPlayer(filePath);
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error initializing audio player: " + e.getMessage());
        }
    }
 
    // --- Enemy formation templates ---
    // Each entry is {dx, dy}: dx is how much further off-screen (to the right) that member
    // spawns, dy is its vertical offset from the wave's base Y. Because every enemy in a
    // formation is the SAME type (so they all move at the same speed), these offsets stay
    // fixed relative to each other the whole time they cross the screen — the shape holds.
    private static final int[][] FORMATION_V_SMALL = {
        {0, 0}, {45, -45}, {45, 45}
    };
    private static final int[][] FORMATION_V_LARGE = {
        {0, 0}, {45, -45}, {45, 45}, {90, -90}, {90, 90}
    };
    private static final int[][] FORMATION_WALL_SMALL = {
        {0, -50}, {0, 0}, {0, 50}
    };
    private static final int[][] FORMATION_WALL_LARGE = {
        {0, -100}, {0, -50}, {0, 0}, {0, 50}, {0, 100}
    };
    private static final int[][] FORMATION_DIAGONAL_SMALL = {
        {0, -50}, {60, 0}, {120, 50}
    };
    private static final int[][] FORMATION_DIAGONAL_LARGE = {
        {0, -100}, {50, -50}, {100, 0}, {150, 50}, {200, 100}
    };
    private static final int[][][] SMALL_FORMATIONS = {
        FORMATION_V_SMALL, FORMATION_WALL_SMALL, FORMATION_DIAGONAL_SMALL
    };
    private static final int[][][] LARGE_FORMATIONS = {
        FORMATION_V_LARGE, FORMATION_WALL_LARGE, FORMATION_DIAGONAL_LARGE
    };
 
    private void loadSpawnDetails() {
        // Power-up spawns first, well before any enemies — gives the player
        // a clear window to grab it without an enemy arriving at the same time.
        spawnMap.put(30, new SpawnDetails("PowerUp-SpeedUp", 720, 200));
        spawnMap.put(1600, new SpawnDetails("PowerUp-MultiShot", 720, 300));
 
        // --- Formation-based enemy spawning ---
        // Instead of lone enemies trickling in, enemies now spawn together in tight,
        // recognizable formations (a V, a wall, a diagonal line) that cross the screen
        // as a group. Early waves use smaller 3-enemy formations of easy Scouts; later
        // waves use bigger 5-enemy formations and tougher enemy types, and the breather
        // between waves shrinks over time so the stage keeps ramping up.
        int numberOfWaves = 95;
        int frameCursor = 300; // small buffer before the first wave starts
 
        for (int wave = 0; wave < numberOfWaves; wave++) {
            boolean useLargeFormation = wave >= 25;
            int[][][] formationSet = useLargeFormation ? LARGE_FORMATIONS : SMALL_FORMATIONS;
            int[][] formation = formationSet[wave % formationSet.length];
 
            String enemyType = pickEnemyTypeForWave(wave);
            // Base Y clamped so every member of the formation (offsets up to +-100) stays
            // fully on-screen vertically.
            int baseY = 160 + randomizer.nextInt(300);
 
            for (int i = 0; i < formation.length; i++) {
                int dx = formation[i][0];
                int dy = formation[i][1];
                // The +i is just to keep HashMap keys unique — it's 1 frame apart at most,
                // so it doesn't noticeably affect the formation's timing. The dx offset
                // (spawning further off-screen) is what creates the staggered, shaped entry.
                spawnMap.put(frameCursor + i, new SpawnDetails(enemyType, 720 + dx, baseY + dy));
            }
 
            int lullAfterWave = Math.max(120, 400 - wave * 8); // shrinks from ~6.7s to ~2s
 
            // Randomly sneak a lone enemy into the lull, at an unpredictable moment, so
            // the breather between formations isn't 100% safe/telegraphed every time.
            int loneSpawnChance = 55; // % chance per wave
            if (lullAfterWave > 60 && randomizer.nextInt(100) < loneSpawnChance) {
                int loneOffset = formation.length + randomizer.nextInt(lullAfterWave);
                int loneFrame = frameCursor + loneOffset;
                String loneType = pickEnemyTypeForWave(wave);
                int loneY = 60 + randomizer.nextInt(500);
                spawnMap.put(loneFrame, new SpawnDetails(loneType, 720, loneY));
            }
 
            frameCursor += formation.length + lullAfterWave;
        }
    }
 
    /**
     * Picks a single enemy type to use for an entire formation. Early waves lean almost
     * entirely on Alien1 (fast, fragile "Scouts"). Later waves mix in more Alien2
     * ("Wraiths", zigzag movement) and Alien3 ("Juggernauts", tanky) so the stage
     * escalates smoothly. Using one type per formation keeps the shape intact, since
     * mixing enemy types with different speeds would stretch the formation apart.
     */
    private String pickEnemyTypeForWave(int wave) {
        int roll = randomizer.nextInt(100);
 
        int juggernautChance = Math.min(40, wave * 3);    // grows from 0% to ~40%
        int wraithChance = Math.min(40, 10 + wave * 2);   // grows from ~10% to ~40%
 
        if (roll < juggernautChance) {
            return "Alien3";
        } else if (roll < juggernautChance + wraithChance) {
            return "Alien2";
        } else {
            return "Alien1";
        }
    }
 
    private void initBoard() {
 
    }
 
    public void start() {
        addKeyListener(new TAdapter());
        addMouseListener(new MAdapter());
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.black);
 
        timer = new Timer(1000 / 60, new GameCycle());
        timer.start();
 
        gameInit();
        //initAudio();
    }
 
    public void stop() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        if (audioPlayer != null) {
            try {
                audioPlayer.stop(); 
                audioPlayer = null;
            } catch (Exception e) {
                System.err.println("Error stopping audio: " + e.getMessage());
            }
        }
    }
 
    private void gameInit() {
 
        inGame = true;
        showDashboard = false;
        frame = 0;
        deaths = 0;
        scoutKills = 0;
        wraithKills = 0;
        juggernautKills = 0;
        score = 0;
        shotsFired = 0;
 
        player = new Player();
 
        enemies = new ArrayList<>();
        powerups = new ArrayList<>();
        explosions = new ArrayList<>();
        shots = new ArrayList<>();
        bombs = new ArrayList<>();
 
        try {
            if (audioPlayer != null) {
                audioPlayer.stop();
            }
            audioPlayer = new AudioPlayer("src/audio/scene1.wav");
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error playing audio: " + e.getMessage());
        }
    }
 
    private void drawMap(Graphics g) {
        // Calculate smooth scrolling offset horizontally (1 pixel per frame)
        int scrollOffset = (frame) % BLOCKWIDTH;
 
        // Calculate which columns to draw based on screen position
        int baseCol = (frame) / BLOCKWIDTH;
        int colsNeeded = (BOARD_WIDTH / BLOCKWIDTH) + 2; // +2 for smooth scrolling
 
        // Loop through columns that should be visible on screen
        for (int screenCol = 0; screenCol < colsNeeded; screenCol++) {
            // Calculate which MAP column to use (with wrapping)
            // Note: Since MAP is 2D array [row][col], we wrap based on the row length
            int mapCol = (baseCol + screenCol) % MAP[0].length;
 
            // Calculate X position for this column to scroll left
            int x = BOARD_WIDTH - ((screenCol * BLOCKWIDTH) - scrollOffset);
 
            // Skip if column is completely off-screen
            if (x > BOARD_WIDTH || x < -BLOCKWIDTH) {
                continue;
            }
 
            // Draw each row in this column
            for (int row = 0; row < MAP.length; row++) {
                if (MAP[row][mapCol] == 1) {
                    // Calculate Y position
                    int y = row * BLOCKHEIGHT;
 
                    // Draw a cluster of stars
                    drawStarCluster(g, x, y, BLOCKWIDTH, BLOCKHEIGHT);
                }
            }
        }
    }
 
    private void drawStarCluster(Graphics g, int x, int y, int width, int height) {
        // Set star color to white
        g.setColor(Color.WHITE);
 
        // Draw multiple stars in a cluster pattern
        // Main star (larger)
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        g.fillOval(centerX - 2, centerY - 2, 4, 4);
 
        // Smaller surrounding stars
        g.fillOval(centerX - 15, centerY - 10, 2, 2);
        g.fillOval(centerX + 12, centerY - 8, 2, 2);
        g.fillOval(centerX - 8, centerY + 12, 2, 2);
        g.fillOval(centerX + 10, centerY + 15, 2, 2);
 
        // Tiny stars for more detail
        g.fillOval(centerX - 20, centerY + 5, 1, 1);
        g.fillOval(centerX + 18, centerY - 15, 1, 1);
        g.fillOval(centerX - 5, centerY - 18, 1, 1);
        g.fillOval(centerX + 8, centerY + 20, 1, 1);
    }
 
    private void drawAliens(Graphics g) {

        for (Enemy enemy : enemies) {

            if (enemy.isVisible()) {

                enemy.tickAnimation();

                // Subtle "breathing" pulse -- a pure-drawing transform on the existing
                // sprite image (no new art). Small amplitude so it reads as alive
                // rather than glitchy.
                double pulse = 1.0 + 0.06 * Math.sin(enemy.getAnimFrame() * 0.15);
                Image img = enemy.getImage();
                int baseW = img.getWidth(this);
                int baseH = img.getHeight(this);
                int w = (int) (baseW * pulse);
                int h = (int) (baseH * pulse);
                int drawX = enemy.getX() - (w - baseW) / 2;
                int drawY = enemy.getY() - (h - baseH) / 2;

                // Source art faces downward by default; rotate 90° (same direction as
                // the player/shot rotation) so these enemies face left, toward the player.
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.rotate(Math.toRadians(90), drawX + w / 2.0, drawY + h / 2.0);
                g2d.drawImage(img, drawX, drawY, w, h, this);
                g2d.dispose();
            }
            
            if (enemy.isDying()) {

                enemy.die();
            }
        }
    }
 
    private void drawPowreUps(Graphics g) {

        for (PowerUp p : powerups) {

            if (p.isVisible()) {

                p.tickAnimation();

                // Fake "coin spin" -- squash horizontally with a cosine wave to
                // simulate rotation using the existing icon, no new art needed.
                double scaleX = Math.abs(Math.cos(p.getAnimFrame() * 0.08));
                Image img = p.getImage();
                int baseW = img.getWidth(this);
                int baseH = img.getHeight(this);
                int w = Math.max(2, (int) (baseW * scaleX));
                int drawX = p.getX() + (baseW - w) / 2;

                g.drawImage(img, drawX, p.getY(), w, baseH, this);
            }

            if (p.isDying()) {

                p.die();
            }
        }
    }
 
    private void drawPlayer(Graphics g) {
        if (player != null && player.isVisible()) {
            player.tickAnimation();

            Graphics2D g2d = (Graphics2D) g.create();
            
            // Get player coordinates and image dimensions
            int x = player.getX();
            int y = player.getY();
            int width = player.getImage().getWidth(null);
            int height = player.getImage().getHeight(null);
 
            // Rotate around the center of the player ship
            g2d.rotate(Math.toRadians(90), x + width / 2.0, y + height / 2.0);

            // Subtle engine-thrust pulse -- scales the ship slightly each frame.
            // Pure drawing transform, no new art needed.
            double pulse = 1.0 + 0.04 * Math.sin(player.getAnimFrame() * 0.3);
            int w = (int) (width * pulse);
            int h = (int) (height * pulse);
            int dx = x - (w - width) / 2;
            int dy = y - (h - height) / 2;

            // Draw the player
            g2d.drawImage(player.getImage(), dx, dy, w, h, this);
            
            // Dispose the graphics context copy
            g2d.dispose();
        }
    }
 
    private void drawShot(Graphics g) {

        // Inside Scene1.java where you draw shots:
        for (Shot shot : shots) {
            if (shot.isVisible()) {
                shot.tickAnimation();

                Graphics2D g2d = (Graphics2D) g.create();

                int x = shot.getX();
                int y = shot.getY();
                int width = shot.getImage().getWidth(null);
                int height = shot.getImage().getHeight(null);

                // Rotate 90 degrees so the vertical line becomes horizontal
                g2d.rotate(Math.toRadians(90), x + width / 2.0, y + height / 2.0);

                g2d.drawImage(shot.getImage(), x, y, this);
                g2d.dispose();
            }
        }
    }
 
    private void drawBombs(Graphics g) {
        for (Bomb bomb : bombs) {
            if (bomb.isVisible()) {
                bomb.tickAnimation();

                // Small pulse so the bomb reads as active/dangerous, not a
                // frozen icon. Pure drawing transform, no new art.
                double pulse = 1.0 + 0.15 * Math.sin(bomb.getAnimFrame() * 0.4);
                Image img = bomb.getImage();
                int baseW = img.getWidth(this);
                int baseH = img.getHeight(this);
                int w = (int) (baseW * pulse);
                int h = (int) (baseH * pulse);
                int drawX = bomb.getX() - (w - baseW) / 2;
                int drawY = bomb.getY() - (h - baseH) / 2;

                g.drawImage(img, drawX, drawY, w, h, this);
            }
        }
    }
 
    private void drawBombing(Graphics g) {
 
        // for (Enemy e : enemies) {
        //     Enemy.Bomb b = e.getBomb();
        //     if (!b.isDestroyed()) {
        //         g.drawImage(b.getImage(), b.getX(), b.getY(), this);
        //     }
        // }
    }
 
    private void drawExplosions(Graphics g) {

        List<Explosion> toRemove = new ArrayList<>();

        for (Explosion explosion : explosions) {

            if (explosion.isVisible()) {
                explosion.tickAnimation();

                // Grow-and-fade burst. Uses Explosion's own known base size/lifetime
                // constants rather than querying the scaled image's width/height --
                // Image.getScaledInstance() fills in pixels asynchronously, so its
                // dimensions aren't reliably queryable immediately after creation.
                int lifeFrames = Explosion.getLifetimeFrames();
                float progress = Math.min(1f, explosion.getAnimFrame() / (float) lifeFrames);
                double scale = 0.6 + 0.9 * progress;
                float alpha = Math.max(0f, 1f - progress);

                Image img = explosion.getImage();
                int baseW = Explosion.getBaseSize();
                int baseH = Explosion.getBaseSize();
                int w = (int) (baseW * scale);
                int h = (int) (baseH * scale);
                int drawX = explosion.getX() - (w - baseW) / 2;
                int drawY = explosion.getY() - (h - baseH) / 2;

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.drawImage(img, drawX, drawY, w, h, this);
                g2d.dispose();

                explosion.visibleCountDown();
                if (!explosion.isVisible()) {
                    toRemove.add(explosion);
                }
            }
        }

        explosions.removeAll(toRemove);
    }
 
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
 
        doDrawing(g);
    }
 
    private void showPickupMessage(String text) {
        pickupMessage = text;
        pickupMessageFrame = 0;
    }
 
    private void drawPickupMessage(Graphics g) {
        if (pickupMessage == null) {
            return;
        }
 
        float alpha;
        if (pickupMessageFrame <= PICKUP_MESSAGE_HOLD_FRAMES) {
            alpha = 1.0f; // fully visible during the hold period
        } else {
            int fadeProgress = pickupMessageFrame - PICKUP_MESSAGE_HOLD_FRAMES;
            alpha = 1.0f - ((float) fadeProgress / PICKUP_MESSAGE_FADE_FRAMES);
            alpha = Math.max(0f, Math.min(1f, alpha));
        }
 
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Helvetica", Font.BOLD, 20));
        var fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(pickupMessage);
        int x = (BOARD_WIDTH - textWidth) / 2;
        int y = 100; // below the top HUD text, centered horizontally
        g2d.drawString(pickupMessage, x, y);
        g2d.dispose();
    }
 
    private void drawKillCounts(Graphics g) {
        g.setColor(Color.white);
        g.drawString("Scout Kills: " + scoutKills, 10, 30);
        g.drawString("Wraith Kills: " + wraithKills, 10, 45);
        g.drawString("Juggernaut Kills: " + juggernautKills, 10, 60);
    }
 
    private void drawTimer(Graphics g) {
        int framesElapsed = Math.min(frame, STAGE_DURATION_FRAMES);
        int framesLeft = STAGE_DURATION_FRAMES - framesElapsed;
        String timeText = "Time: " + formatTime(framesElapsed);
 
        var timerFont = new Font("Helvetica", Font.BOLD, 18);
        g.setFont(timerFont);
        var fm = g.getFontMetrics(timerFont);
 
        // Flash red in the last 10 seconds to warn the player
        if (framesLeft <= 10 * 60) {
            g.setColor(frame % 30 < 15 ? Color.RED : Color.WHITE);
        } else {
            g.setColor(Color.WHITE);
        }
 
        g.drawString(timeText, BOARD_WIDTH - fm.stringWidth(timeText) - 10, 25);
 
        // Also show current score in the same corner, right under the timer
        g.setColor(Color.WHITE);
        var scoreFont = new Font("Helvetica", Font.PLAIN, 14);
        g.setFont(scoreFont);
        var fmScore = g.getFontMetrics(scoreFont);
        String scoreText = "Score: " + score;
        g.drawString(scoreText, BOARD_WIDTH - fmScore.stringWidth(scoreText) - 10, 45);

        // Speed and Shots-upgrade level, same corner, under the score
        String speedText = "Speed: " + player.getSpeed();
        g.drawString(speedText, BOARD_WIDTH - fmScore.stringWidth(speedText) - 10, 65);
        String shotsText = "Shots: " + player.getMaxShots();
        g.drawString(shotsText, BOARD_WIDTH - fmScore.stringWidth(shotsText) - 10, 85);
        
    }
 
    private void drawHealthBar(Graphics g) {
        if (player == null) return;
 
        int totalHealth = 5;
        int currentHealth = player.getHealth(); // gets the health (0 to 5)
 
        // Health Bar position & size
        int x = 10;
        int y = 80;
        int barWidth = 150;
        int barHeight = 15;
 
        // 1. Draw Background (Red for missing health)
        g.setColor(Color.RED);
        g.fillRect(x, y, barWidth, barHeight);
 
        // 2. Draw Current Health (Green portion)
        if (currentHealth > 0) {
            int currentWidth = (barWidth * currentHealth) / totalHealth;
            g.setColor(Color.GREEN);
            g.fillRect(x, y, currentWidth, barHeight);
        }
 
        // 3. Draw Outer Border & Text
        g.setColor(Color.WHITE);
        g.drawRect(x, y, barWidth, barHeight);
        g.drawString("HP: " + Math.max(0, currentHealth) + " / " + totalHealth, x + 160, y + 12);
    }
 
    private void doDrawing(Graphics g) {
 
        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);
 
        g.setColor(Color.white);
        g.drawString("FRAME: " + frame, 10, 10);
 
        g.setColor(Color.green);
 
        if (inGame) {
 
            drawMap(g);  // Draw background stars first
            drawPowreUps(g);
            drawAliens(g);
            drawBombs(g);
            drawPlayer(g);
            drawShot(g);
            drawExplosions(g); // drawn last so bursts always render on top, never hidden behind a sprite
            drawKillCounts(g);
            drawHealthBar(g);
            drawPickupMessage(g);
            drawTimer(g);
 
        } else if (showDashboard) {
 
            if (timer.isRunning()) {
                timer.stop();
            }
 
            drawDashboard(g);
 
        } else {
 
            if (timer.isRunning()) {
                timer.stop();
            }
 
            gameOver(g);
        }
 
        Toolkit.getDefaultToolkit().sync();
    }
 
    private String formatTime(int frames) {
        int totalSeconds = frames / 60; // timer runs at 60 FPS
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
 
    private void drawDashboard(Graphics g) {
 
        g.setColor(Color.black);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
 
        int panelX = 80;
        int panelY = 120;
        int panelWidth = BOARD_WIDTH - 160;
        int panelHeight = 440;
 
        g.setColor(new Color(0, 32, 48));
        g.fillRect(panelX, panelY, panelWidth, panelHeight);
        g.setColor(Color.white);
        g.drawRect(panelX, panelY, panelWidth, panelHeight);
 
        var titleFont = new Font("Helvetica", Font.BOLD, 22);
        var labelFont = new Font("Helvetica", Font.PLAIN, 16);
        var fmTitle = this.getFontMetrics(titleFont);
 
        String title = "STAGE 1 COMPLETE!";
        g.setColor(Color.YELLOW);
        g.setFont(titleFont);
        g.drawString(title, panelX + (panelWidth - fmTitle.stringWidth(title)) / 2, panelY + 40);
 
        int totalKills = scoutKills + wraithKills + juggernautKills;
        int accuracy = shotsFired > 0 ? Math.min(100, (int) (100.0 * totalKills / shotsFired)) : 0;
 
        g.setColor(Color.white);
        g.setFont(labelFont);
        int lineX = panelX + 30;
        int lineY = panelY + 85;
        int lineGap = 28;
 
        g.drawString("Score: " + score, lineX, lineY);
        lineY += lineGap;
        g.drawString("Time Survived: " + formatTime(frame), lineX, lineY);
        lineY += lineGap;
        g.drawString("Total Enemies Destroyed: " + totalKills, lineX, lineY);
        lineY += lineGap;
        g.drawString("  Scout Kills: " + scoutKills, lineX, lineY);
        lineY += lineGap;
        g.drawString("  Wraith Kills: " + wraithKills, lineX, lineY);
        lineY += lineGap;
        g.drawString("  Juggernaut Kills: " + juggernautKills, lineX, lineY);
        lineY += lineGap;
        g.drawString("Shots Fired: " + shotsFired + "   Accuracy: " + accuracy + "%", lineX, lineY);
        lineY += lineGap;
       g.drawString("Health Remaining: " + Math.max(0, player.getHealth()) + " / 5", lineX, lineY);
        lineY += lineGap;
        g.drawString("Final Speed: " + player.getSpeed(), lineX, lineY);
        lineY += lineGap;
        g.drawString("Max Simultaneous Shots: " + player.getMaxShots(), lineX, lineY);

        // Continue button
        int buttonWidth = 220;
        int buttonHeight = 45;
        int buttonX = panelX + (panelWidth - buttonWidth) / 2;
        int buttonY = panelY + panelHeight - 70;
 
        continueButtonBounds = new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight);
 
        g.setColor(new Color(0, 90, 40));
        g.fillRect(buttonX, buttonY, buttonWidth, buttonHeight);
        g.setColor(Color.white);
        g.drawRect(buttonX, buttonY, buttonWidth, buttonHeight);
 
        String buttonText = "CONTINUE TO STAGE 2";
        var fmButton = this.getFontMetrics(labelFont);
        g.drawString(buttonText,
                buttonX + (buttonWidth - fmButton.stringWidth(buttonText)) / 2,
                buttonY + buttonHeight / 2 + 5);
 
        String hint = "(click Continue, or press ENTER)";
        var small = new Font("Helvetica", Font.PLAIN, 12);
        g.setFont(small);
        g.setColor(Color.LIGHT_GRAY);
        var fmHint = this.getFontMetrics(small);
        g.drawString(hint, panelX + (panelWidth - fmHint.stringWidth(hint)) / 2, buttonY + buttonHeight + 20);
    }
 
    private void proceedToScene2() {
        stop();
        game.loadScene2();
    }
 
    private void gameOver(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        ImageIcon ii = new ImageIcon(IMG_GAME_OVER);
        Image gameOverImg = ii.getImage();
        
        g.drawImage(gameOverImg, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, this);
    }
 
    private void update() {
 
        // Check enemy spawn
        // TODO this approach can only spawn one enemy at a frame
        SpawnDetails sd = spawnMap.get(frame);
        if (sd != null) {
            // Create a new enemy based on the spawn details
            switch (sd.type) {
                case "Alien1":
                    Enemy enemy = new Alien1(sd.x, sd.y);
                    enemies.add(enemy);
                    break;
                // Add more cases for different enemy types if needed
                case "Alien2":
                    Enemy enemy2 = new Alien2(sd.x, sd.y);
                    enemies.add(enemy2);
                    break;
                case "Alien3":
                    Enemy enemy3 = new Alien3(sd.x, sd.y);
                    enemies.add(enemy3);
                    break;
                case "PowerUp-SpeedUp":
                    // Handle speed up item spawn
                    PowerUp speedUp = new SpeedUp(sd.x, sd.y);
                    powerups.add(speedUp);
                    break;
                case "PowerUp-MultiShot":
                    PowerUp multiShot = new MultiShot(sd.x, sd.y);
                    powerups.add(multiShot);
                    break;
                default:
                    System.out.println("Unknown enemy type: " + sd.type);
                    break;
            }
        }
 
        if (inGame && frame >= STAGE_DURATION_FRAMES) {
            inGame = false;
            showDashboard = true;
            isVictory = true;
            timer.stop();
 
            if (audioPlayer != null) {
                try {
                    audioPlayer.stop();
                } catch (Exception e) {
                    System.err.println("Error stopping audio: " + e.getMessage());
                }
            }
        }
 
        // player
        player.act();
 
        // Power-ups
        // Power-ups
        for (PowerUp powerup : powerups) {
            if (powerup.isVisible()) {
                powerup.act();
                if (powerup.collidesWith(player)) {
                    powerup.upgrade(player);
 
                    AudioPlayer.playSoundEffect(Global.AUD_LEVEL_UP);
 
                    if (powerup instanceof SpeedUp) {
                        showPickupMessage("Speed Increased!");
                    } else if (powerup instanceof MultiShot) {
                        showPickupMessage("Obtained Multi-Shot!");
                    }
                }
            }
        }
 
        // Pickup message countdown (hold, then fade, then clear)
        if (pickupMessage != null) {
            pickupMessageFrame++;
            if (pickupMessageFrame > PICKUP_MESSAGE_HOLD_FRAMES + PICKUP_MESSAGE_FADE_FRAMES) {
                pickupMessage = null;
                pickupMessageFrame = 0;
            }
        }
 
        // Enemies
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.act(direction);
 
                // Roll the odds for this enemy to drop a bomb this frame
                Bomb newBomb = enemy.maybeDropBomb();
                if (newBomb != null) {
                    bombs.add(newBomb);
                }
            }
        }
 
        // Bombs: move them, check collision with player, clean up off-screen ones
        // Bombs: move them, check collision with player, clean up off-screen ones
        List<Bomb> bombsToRemove = new ArrayList<>();
        for (Bomb bomb : bombs) {
            if (!bomb.isVisible()) {
                bombsToRemove.add(bomb);
                continue;
            }
 
            bomb.act();
 
            int bombX = bomb.getX();
            int bombY = bomb.getY();
            int playerX = player.getX();
            int playerY = player.getY();
 
            if (player.isVisible()
                    && bombX >= playerX
                    && bombX <= playerX + PLAYER_WIDTH
                    && bombY >= playerY
                    && bombY <= playerY + PLAYER_HEIGHT) {
 
                // 1. Subtract health and destroy the bomb
                player.hit();
                bomb.die();
                bombsToRemove.add(bomb);
 
                // 2. Only change the image to an explosion if health reached 0
                if (player.isDying()) {
                    var ii = new ImageIcon(IMG_EXPLOSION);
                    var scaledDeathImg = ii.getImage().getScaledInstance(
                            PLAYER_WIDTH, PLAYER_HEIGHT, java.awt.Image.SCALE_SMOOTH);
                    player.setImage(scaledDeathImg);
                }
            } else if (bombX < 0) {
                bomb.die();
                bombsToRemove.add(bomb);
            }
        }
        bombs.removeAll(bombsToRemove);
 
        // Player death from a bomb hit ends the stage
        if (player.isDying()) {
            player.die();
            inGame = false;
            timer.stop();
            message = "Game Over";
 
            if (audioPlayer != null) {
                try {
                    audioPlayer.stop(); // Stops background music
                } catch (Exception e) {
                    System.err.println("Error stopping audio: " + e.getMessage());
                }
            }
 
            // Play game over sound effect
            AudioPlayer.playSoundEffect(Global.AUD_GAMEOVER);
        }
 
        // shot
        List<Shot> shotsToRemove = new ArrayList<>();
        for (Shot shot : shots) {
 
            if (shot.isVisible()) {
                int shotX = shot.getX();
                int shotY = shot.getY();
 
                for (Enemy enemy : enemies) {
                    // Collision detection: shot and enemy
                    int enemyX = enemy.getX();
                    int enemyY = enemy.getY();
 
                    if (enemy.isVisible() && shot.isVisible()
                            && shotX >= (enemyX)
                            && shotX <= (enemyX + ALIEN_WIDTH)
                            && shotY >= (enemyY)
                            && shotY <= (enemyY + ALIEN_HEIGHT)) {
 
                        boolean enemyDied = enemy.hit(); // decrements HP; true only when HP hits 0
 
                        if (enemyDied) {
                            // NOTE: previously also did enemy.setImage(raw IMG_EXPLOSION) here,
                            // unscaled. With a small template image that was invisible; with a
                            // full-res AI-generated image it flashed a giant unscaled sprite for
                            // one frame. Removed -- the animated Explosion sprite below (already
                            // scaled to EXPLOSION_SIZE and grow-and-fade animated) is the visual.
                            explosions.add(new Explosion(enemyX, enemyY));
                            deaths++;
 
                            // Play explosion sound
                            AudioPlayer.playSoundEffect("src/audio/explode.wav");
 
                            if (enemy instanceof Alien3) {
                                juggernautKills++;
                                score += 300;
                            } else if (enemy instanceof Alien2) {
                                wraithKills++;
                                score += 150;
                            } else if (enemy instanceof Alien1) {
                                scoutKills++;
                                score += 100;
                            }
                        }
 
                        shot.die(); // shot is consumed on impact either way
                        shotsToRemove.add(shot);
                    }
                }
 
                // Replace the existing y-axis logic in the shot loop:
                int x = shot.getX();
                x += 20; // Move right instead of up
 
                if (x > BOARD_WIDTH) {
                    shot.die();
                    shotsToRemove.add(shot);
                } else {
                    shot.setX(x); 
                }
            }
        }
        shots.removeAll(shotsToRemove);
 
        // enemies
        // for (Enemy enemy : enemies) {
        //     int x = enemy.getX();
        //     if (x >= BOARD_WIDTH - BORDER_RIGHT && direction != -1) {
        //         direction = -1;
        //         for (Enemy e2 : enemies) {
        //             e2.setY(e2.getY() + GO_DOWN);
        //         }
        //     }
        //     if (x <= BORDER_LEFT && direction != 1) {
        //         direction = 1;
        //         for (Enemy e : enemies) {
        //             e.setY(e.getY() + GO_DOWN);
        //         }
        //     }
        // }
        // for (Enemy enemy : enemies) {
        //     if (enemy.isVisible()) {
        //         int y = enemy.getY();
        //         if (y > GROUND - ALIEN_HEIGHT) {
        //             inGame = false;
        //             message = "Invasion!";
        //         }
        //         enemy.act(direction);
        //     }
        // }
        // bombs - collision detection
        // Bomb is with enemy, so it loops over enemies
        /*
        for (Enemy enemy : enemies) {
 
            int chance = randomizer.nextInt(15);
            Enemy.Bomb bomb = enemy.getBomb();
 
            if (chance == CHANCE && enemy.isVisible() && bomb.isDestroyed()) {
 
                bomb.setDestroyed(false);
                bomb.setX(enemy.getX());
                bomb.setY(enemy.getY());
            }
 
            int bombX = bomb.getX();
            int bombY = bomb.getY();
            int playerX = player.getX();
            int playerY = player.getY();
 
            if (player.isVisible() && !bomb.isDestroyed()
                    && bombX >= (playerX)
                    && bombX <= (playerX + PLAYER_WIDTH)
                    && bombY >= (playerY)
                    && bombY <= (playerY + PLAYER_HEIGHT)) {
 
                var ii = new ImageIcon(IMG_EXPLOSION);
                player.setImage(ii.getImage());
                player.setDying(true);
                bomb.setDestroyed(true);
            }
 
            if (!bomb.isDestroyed()) {
                bomb.setY(bomb.getY() + 1);
                if (bomb.getY() >= GROUND - BOMB_HEIGHT) {
                    bomb.setDestroyed(true);
                }
            }
        }
         */
    }
 
    private void doGameCycle() {
        frame++;
        update();
        repaint();
    }
 
    private class GameCycle implements ActionListener {
 
        @Override
        public void actionPerformed(ActionEvent e) {
            doGameCycle();
        }
    }
 
    private class TAdapter extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent e) {
        if (inGame && player != null) {
            player.keyReleased(e);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // 1. Check if the player is on the victory screen and pressed ENTER
        if (!inGame) {
            if (key == KeyEvent.VK_ENTER) {
                    if (isVictory || showDashboard) {
                        Scene1.this.stop(); 
                        game.loadScene2();
                    } else {
                        Scene1.this.stop(); 
                        game.loadScene1();
                    }
                }
                return;
        }

        player.keyPressed(e);

        int x = player.getX();
        int y = player.getY();

        if (key == KeyEvent.VK_SPACE) {
            if (shots.size() < player.getMaxShots()) {
                    Shot shot = new Shot(x, y);
                    shots.add(shot);
                    shotsFired++;

                    // Sound effect triggers on shot creation
                    AudioPlayer.playSoundEffect("src/audio/fire.wav");
                }
            }
        }
    }

    private class MAdapter extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (showDashboard && continueButtonBounds != null
                    && continueButtonBounds.contains(e.getPoint())) {
                proceedToScene2();
            }
        }
    }
}