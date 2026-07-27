package gdd.scene;
 
import gdd.AudioPlayer;
import gdd.Game;
import gdd.Global;

import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.powerup.HealUp;
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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.Rectangle;
import java.awt.RenderingHints;
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
    // Set to 5 minutes (5 min * 60 sec * 60 frames/sec = 18,000 frames)
    private static final int STAGE_DURATION_FRAMES = 5 * 60 * 60; 
    private Rectangle continueButtonBounds;
 
    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private final Random randomizer = new Random();
 
    private Timer timer;
    private final Game game;
 
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
 
    public Scene1(Game game) {
        this.game = game;
        loadSpawnDetails();
    }
 
    // --- Enemy formation templates ---
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
        // --- POWER-UPS ---
        // Speed-Up: Only appears once (early game)
        spawnMap.put(30, new SpawnDetails("PowerUp-SpeedUp", 720, 200));

        // Multi-Shot: Only appears once (early-mid game)
        spawnMap.put(1200, new SpawnDetails("PowerUp-MultiShot", 720, 300));

        // Heal-Ups: Plentiful recovery packs spread evenly throughout the full 5 minutes
        spawnMap.put(1500, new SpawnDetails("PowerUp-HealUp", 720, 250)); // ~0:25
        spawnMap.put(3800, new SpawnDetails("PowerUp-HealUp", 720, 350)); // ~1:03
        spawnMap.put(6200, new SpawnDetails("PowerUp-HealUp", 720, 180)); // ~1:43
        spawnMap.put(8800, new SpawnDetails("PowerUp-HealUp", 720, 420)); // ~2:26
        spawnMap.put(11200, new SpawnDetails("PowerUp-HealUp", 720, 280)); // ~3:06
        spawnMap.put(13800, new SpawnDetails("PowerUp-HealUp", 720, 150)); // ~3:50
        spawnMap.put(16200, new SpawnDetails("PowerUp-HealUp", 720, 380)); // ~4:30

        // --- CONTINUOUS ENEMY WAVES (Up to 5:00 / 18,000 frames) ---
        int frameCursor = 300; 
        int wave = 0;

        // Keep generating waves until we get within 8–10 seconds of the 5-minute victory mark
        while (frameCursor < STAGE_DURATION_FRAMES - 500) {
            wave++;
            
            // Keep small formations longer so medium difficulty is maintained
            boolean useLargeFormation = wave >= 20; 
            int[][][] formationSet = useLargeFormation ? LARGE_FORMATIONS : SMALL_FORMATIONS;
            int[][] formation = formationSet[wave % formationSet.length];

            String enemyType = pickEnemyTypeForWave(wave);
            int baseY = 160 + randomizer.nextInt(280);

            for (int i = 0; i < formation.length; i++) {
                int dx = formation[i][0];
                int dy = formation[i][1];
                spawnMap.put(frameCursor + (i * 2), new SpawnDetails(enemyType, 720 + dx, baseY + dy));
            }

            // Steady pacing: ~4 seconds rest between waves (240 frames)
            int lullAfterWave = Math.max(210, 300 - wave * 2); 

            // Occasional single scout during lulls (30% chance)
            int loneSpawnChance = 30; 
            if (lullAfterWave > 100 && randomizer.nextInt(100) < loneSpawnChance) {
                int loneOffset = formation.length + randomizer.nextInt(lullAfterWave);
                int loneFrame = frameCursor + loneOffset;
                
                // Ensure lone spawns don't spill past the 5-minute timer
                if (loneFrame < STAGE_DURATION_FRAMES) {
                    int loneY = 80 + randomizer.nextInt(440);
                    spawnMap.put(loneFrame, new SpawnDetails("Alien1", 720, loneY));
                }
            }

            frameCursor += formation.length + lullAfterWave;
        }
    }

    /**
     * Rebalanced to keep late-game manageable and fun.
     */
    private String pickEnemyTypeForWave(int wave) {
        int roll = randomizer.nextInt(100);

        // Capped so easier Scouts remain common throughout the stage
        int juggernautChance = Math.min(20, wave * 2);  // Max 20% Juggernaut chance
        int wraithChance = Math.min(30, 10 + wave * 2); // Max 30% Wraith chance

        if (roll < juggernautChance) {
            return "Alien3";
        } else if (roll < juggernautChance + wraithChance) {
            return "Alien2";
        } else {
            return "Alien1";
        }
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
    }

    // Called specifically when the 5-minute timer finishes / Victory condition triggers
    private void onTimerFinished() {
        inGame = false;
        isVictory = true;

        // 1. Stop the Scene 1 loop timer
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        // 2. Stop the BGM ONLY because the timer expired and victory is achieved
        if (audioPlayer != null) {
            try {
                audioPlayer.stop(); 
            } catch (Exception e) {
                System.err.println("Error stopping audio on timer end: " + e.getMessage());
            }
        }

        // 3. Play victory SFX and display victory overlay
        AudioPlayer.playSoundEffect(Global.AUD_STAGE1_VICTORY);
        showVictoryScreen();
    }

    private void showVictoryScreen() {
        showDashboard = true;
        repaint();
    }

    // Called when transitioning out of Scene 1 (e.g., advancing to Scene 2 early)
    public void stop() {
        // Stop the frame loop timer so it doesn't continue ticking in the background
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        // Notice: audioPlayer is NOT stopped here, allowing BGM to continue playing into Scene 2!
    }
 
    private void gameInit() {
        inGame = true;
        isVictory = false;
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
            
            // USE loop() INSTEAD OF play() FOR SEAMLESS BGM REPEAT
            audioPlayer.loop(); 

        } catch (Exception e) {
            System.err.println("Error playing audio: " + e.getMessage());
        }
    }
 
    private void drawMap(Graphics g) {
        double progress = Math.min(1.0, (double) frame / STAGE_DURATION_FRAMES);
        float[] zoneWeights = computeZoneWeights(progress);

        Color mainColor = blendColor(
                new Color(210, 230, 255),
                new Color(255, 190, 100),
                new Color(255, 70, 40),
                zoneWeights);
        Color accentColor = blendColor(
                new Color(140, 180, 255),
                new Color(255, 140, 40),
                new Color(200, 30, 20),
                zoneWeights);
        Color tintColor = blendColor(
                new Color(20, 30, 60),
                new Color(60, 35, 10),
                new Color(60, 5, 5),
                zoneWeights);

        Graphics2D tintG2d = (Graphics2D) g.create();
        tintG2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
        tintG2d.setColor(tintColor);
        tintG2d.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        tintG2d.dispose();

        int scrollOffset = (frame) % BLOCKWIDTH;
        int baseCol = (frame) / BLOCKWIDTH;
        int colsNeeded = (BOARD_WIDTH / BLOCKWIDTH) + 2; 

        for (int screenCol = 0; screenCol < colsNeeded; screenCol++) {
            int mapCol = (baseCol + screenCol) % MAP[0].length;
            int x = BOARD_WIDTH - ((screenCol * BLOCKWIDTH) - scrollOffset);

            if (x > BOARD_WIDTH || x < -BLOCKWIDTH) {
                continue;
            }

            for (int row = 0; row < MAP.length; row++) {
                if (MAP[row][mapCol] == 1) {
                    int y = row * BLOCKHEIGHT;
                    drawStarCluster(g, x, y, BLOCKWIDTH, BLOCKHEIGHT, mainColor, accentColor);
                }
            }
        }
    }

    private float[] computeZoneWeights(double progress) {
        double boundary1 = 0.35;
        double boundary2 = 0.70;
        double half = 0.04;

        float easyW, mediumW, hardW;

        if (progress <= boundary1 - half) {
            easyW = 1f; mediumW = 0f; hardW = 0f;
        } else if (progress < boundary1 + half) {
            float t = (float) ((progress - (boundary1 - half)) / (2 * half));
            easyW = 1f - t; mediumW = t; hardW = 0f;
        } else if (progress <= boundary2 - half) {
            easyW = 0f; mediumW = 1f; hardW = 0f;
        } else if (progress < boundary2 + half) {
            float t = (float) ((progress - (boundary2 - half)) / (2 * half));
            easyW = 0f; mediumW = 1f - t; hardW = t;
        } else {
            easyW = 0f; mediumW = 0f; hardW = 1f;
        }
        return new float[]{easyW, mediumW, hardW};
    }

    private Color blendColor(Color easy, Color medium, Color hard, float[] w) {
        int r = (int) (easy.getRed() * w[0] + medium.getRed() * w[1] + hard.getRed() * w[2]);
        int gg = (int) (easy.getGreen() * w[0] + medium.getGreen() * w[1] + hard.getGreen() * w[2]);
        int b = (int) (easy.getBlue() * w[0] + medium.getBlue() * w[1] + hard.getBlue() * w[2]);
        return new Color(clampColorChannel(r), clampColorChannel(gg), clampColorChannel(b));
    }

    private int clampColorChannel(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private void drawStarCluster(Graphics g, int x, int y, int width, int height, Color mainColor, Color accentColor) {
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        g.setColor(mainColor);
        g.fillOval(centerX - 2, centerY - 2, 4, 4);

        g.setColor(accentColor);
        g.fillOval(centerX - 15, centerY - 10, 2, 2);
        g.fillOval(centerX + 12, centerY - 8, 2, 2);
        g.fillOval(centerX - 8, centerY + 12, 2, 2);
        g.fillOval(centerX + 10, centerY + 15, 2, 2);

        g.fillOval(centerX - 20, centerY + 5, 1, 1);
        g.fillOval(centerX + 18, centerY - 15, 1, 1);
        g.fillOval(centerX - 5, centerY - 18, 1, 1);
        g.fillOval(centerX + 8, centerY + 20, 1, 1);
    }
 
    private void drawAliens(Graphics g) {
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.tickAnimation();
                double pulse = 1.0 + 0.06 * Math.sin(enemy.getAnimFrame() * 0.15);
                Image img = enemy.getImage();
                int baseW = img.getWidth(this);
                int baseH = img.getHeight(this);
                int w = (int) (baseW * pulse);
                int h = (int) (baseH * pulse);
                int drawX = enemy.getX() - (w - baseW) / 2;
                int drawY = enemy.getY() - (h - baseH) / 2;

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
            int x = player.getX();
            int y = player.getY();
            int width = player.getImage().getWidth(null);
            int height = player.getImage().getHeight(null);
 
            g2d.rotate(Math.toRadians(90), x + width / 2.0, y + height / 2.0);

            double pulse = 1.0 + 0.04 * Math.sin(player.getAnimFrame() * 0.3);
            int w = (int) (width * pulse);
            int h = (int) (height * pulse);
            int dx = x - (w - width) / 2;
            int dy = y - (h - height) / 2;

            g2d.drawImage(player.getImage(), dx, dy, w, h, this);
            g2d.dispose();
        }
    }
 
    private void drawShot(Graphics g) {
        for (Shot shot : shots) {
            if (shot.isVisible()) {
                shot.tickAnimation();
                Graphics2D g2d = (Graphics2D) g.create();

                int x = shot.getX();
                int y = shot.getY();
                int width = shot.getImage().getWidth(null);
                int height = shot.getImage().getHeight(null);

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
 
    private void drawExplosions(Graphics g) {
        List<Explosion> toRemove = new ArrayList<>();

        for (Explosion explosion : explosions) {
            if (explosion.isVisible()) {
                explosion.tickAnimation();
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
        if (pickupMessage == null) return;
 
        float alpha;
        if (pickupMessageFrame <= PICKUP_MESSAGE_HOLD_FRAMES) {
            alpha = 1.0f;
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
        int y = 100;
        g2d.drawString(pickupMessage, x, y);
        g2d.dispose();
    }
 
    private void drawKillCounts(Graphics g) {
        g.setColor(Color.WHITE);
        var small = new Font("Helvetica", Font.PLAIN, 13);
        g.setFont(small);
        g.drawString("Scout Kills: " + scoutKills, 10, 110);
        g.drawString("Wraith Kills: " + wraithKills, 10, 125);
        g.drawString("Juggernaut Kills: " + juggernautKills, 10, 140);
    }
 
    private void drawTimer(Graphics g) {
        int framesElapsed = Math.min(frame, STAGE_DURATION_FRAMES);
        int framesLeft = STAGE_DURATION_FRAMES - framesElapsed;
        String timeText = "Time: " + formatTime(framesElapsed);
 
        var timerFont = new Font("Helvetica", Font.BOLD, 18);
        g.setFont(timerFont);
        var fm = g.getFontMetrics(timerFont);
 
        if (framesLeft <= 10 * 60) {
            g.setColor(frame % 30 < 15 ? Color.RED : Color.WHITE);
        } else {
            g.setColor(Color.WHITE);
        }
 
        g.drawString(timeText, BOARD_WIDTH - fm.stringWidth(timeText) - 10, 20);
 
        g.setColor(Color.WHITE);
        var scoreFont = new Font("Helvetica", Font.PLAIN, 14);
        g.setFont(scoreFont);
        var fmScore = g.getFontMetrics(scoreFont);
        String scoreText = "Score: " + score;
        g.drawString(scoreText, BOARD_WIDTH - fmScore.stringWidth(scoreText) - 10, 40);

        String speedText = "Speed: " + player.getSpeed();
        g.drawString(speedText, BOARD_WIDTH - fmScore.stringWidth(speedText) - 10, 60);
        String shotsText = "Shots: " + player.getMaxShots();
        g.drawString(shotsText, BOARD_WIDTH - fmScore.stringWidth(shotsText) - 10, 80);
    }
 
    private void drawHealthBar(Graphics g) {
        if (player == null) return;
 
        int totalHealth = Player.MAX_HEALTH;
        int currentHealth = player.getHealth();
        int x = 10;
        int y = 40;
        int barWidth = 150;
        int barHeight = 15;
 
        g.setColor(Color.RED);
        g.fillRect(x, y, barWidth, barHeight);
 
        if (currentHealth > 0) {
            int currentWidth = (barWidth * currentHealth) / totalHealth;
            g.setColor(Color.GREEN);
            g.fillRect(x, y, currentWidth, barHeight);
        }
 
        g.setColor(Color.WHITE);
        g.drawRect(x, y, barWidth, barHeight);
        g.drawString("HP: " + Math.max(0, currentHealth) + " / " + totalHealth, x + 160, y + 12);
    }
 
    private void doDrawing(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);
        g.setColor(Color.green);
 
        if (inGame) {
            drawMap(g);  
            drawPowreUps(g);
            drawAliens(g);
            drawBombs(g);
            drawPlayer(g);
            drawShot(g);
            drawExplosions(g); 
            drawHealthBar(g);
            drawKillCounts(g);
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
        int totalSeconds = frames / 60; 
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
 
    private void drawDashboard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(new Color(5, 10, 20, 210));
        g2d.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        int panelWidth = 520;
        int panelHeight = 480;
        int panelX = (BOARD_WIDTH - panelWidth) / 2;
        int panelY = (BOARD_HEIGHT - panelHeight) / 2;

        g2d.setColor(new Color(12, 24, 40, 235));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);

        GradientPaint glassShine = new GradientPaint(
            panelX, panelY, new Color(0, 180, 255, 30),
            panelX, panelY + panelHeight, new Color(0, 0, 0, 0)
        );
        g2d.setPaint(glassShine);
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);

        g2d.setColor(new Color(0, 190, 255, 180));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);

        int notchSize = 12;
        g2d.setColor(new Color(255, 215, 0, 220));
        g2d.drawLine(panelX + 20, panelY + 12, panelX + 20 + notchSize, panelY + 12);
        g2d.drawLine(panelX + 12, panelY + 20, panelX + 12, panelY + 20 + notchSize);
        g2d.drawLine(panelX + panelWidth - 20 - notchSize, panelY + 12, panelX + panelWidth - 20, panelY + 12);
        g2d.drawLine(panelX + panelWidth - 12, panelY + 20, panelX + panelWidth - 12, panelY + 20 + notchSize);

        Font titleFont = new Font("Helvetica", Font.BOLD, 24);
        g2d.setFont(titleFont);
        FontMetrics fmTitle = g2d.getFontMetrics();
        String title = "STAGE 1 COMPLETE";
        
        g2d.setColor(new Color(255, 215, 0, 80));
        g2d.drawString(title, panelX + (panelWidth - fmTitle.stringWidth(title)) / 2 + 1, panelY + 45 + 1);
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString(title, panelX + (panelWidth - fmTitle.stringWidth(title)) / 2, panelY + 45);

        g2d.setColor(new Color(0, 190, 255, 100));
        g2d.drawLine(panelX + 30, panelY + 62, panelX + panelWidth - 30, panelY + 62);

        Font labelFont = new Font("Helvetica", Font.PLAIN, 14);
        Font valFont = new Font("Helvetica", Font.BOLD, 14);
        
        int startY = panelY + 92;
        int rowGap = 24;
        int col1X = panelX + 45;
        int col2X = panelX + panelWidth / 2 + 15;

        int totalKills = scoutKills + wraithKills + juggernautKills;
        int accuracy = shotsFired > 0 ? Math.min(100, (int) (100.0 * totalKills / shotsFired)) : 0;

        drawStatRow(g2d, "Score:", String.valueOf(score), col1X, startY, labelFont, valFont, new Color(255, 215, 0));
        drawStatRow(g2d, "Time Survived:", formatTime(frame), col1X, startY + rowGap, labelFont, valFont, Color.WHITE);
        drawStatRow(g2d, "Enemies Slain:", String.valueOf(totalKills), col1X, startY + rowGap * 2, labelFont, valFont, Color.CYAN);
        drawStatRow(g2d, "  • Scout Kills:", String.valueOf(scoutKills), col1X, startY + rowGap * 3, labelFont, valFont, Color.LIGHT_GRAY);
        drawStatRow(g2d, "  • Wraith Kills:", String.valueOf(wraithKills), col1X, startY + rowGap * 4, labelFont, valFont, Color.LIGHT_GRAY);
        drawStatRow(g2d, "  • Juggernaut Kills:", String.valueOf(juggernautKills), col1X, startY + rowGap * 5, labelFont, valFont, Color.LIGHT_GRAY);

        drawStatRow(g2d, "Shots Fired:", String.valueOf(shotsFired), col2X, startY, labelFont, valFont, Color.WHITE);
        drawStatRow(g2d, "Accuracy:", accuracy + "%", col2X, startY + rowGap, labelFont, valFont, accuracy >= 50 ? Color.GREEN : Color.ORANGE);
        drawStatRow(g2d, "Health:", Math.max(0, player.getHealth()) + " / " + Player.MAX_HEALTH, col2X, startY + rowGap * 2, labelFont, valFont, Color.GREEN);
        drawStatRow(g2d, "Final Speed:", String.valueOf(player.getSpeed()), col2X, startY + rowGap * 3, labelFont, valFont, Color.WHITE);
        drawStatRow(g2d, "Max Shots:", String.valueOf(player.getMaxShots()), col2X, startY + rowGap * 4, labelFont, valFont, Color.WHITE);

        g2d.setColor(new Color(0, 190, 255, 100));
        g2d.drawLine(panelX + 30, panelY + panelHeight - 95, panelX + panelWidth - 30, panelY + panelHeight - 95);

        int buttonWidth = 240;
        int buttonHeight = 44;
        int buttonX = panelX + (panelWidth - buttonWidth) / 2;
        int buttonY = panelY + panelHeight - 80;

        continueButtonBounds = new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight);

        GradientPaint btnGrad = new GradientPaint(
            buttonX, buttonY, new Color(0, 180, 80),
            buttonX, buttonY + buttonHeight, new Color(0, 90, 40)
        );

        g2d.setPaint(btnGrad);
        g2d.fillRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 14, 14);

        g2d.setColor(new Color(120, 255, 160));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 14, 14);

        Font btnFont = new Font("Helvetica", Font.BOLD, 15);
        g2d.setFont(btnFont);
        g2d.setColor(Color.WHITE);
        FontMetrics fmBtn = g2d.getFontMetrics();
        String btnText = "CONTINUE TO STAGE 2";
        g2d.drawString(btnText, buttonX + (buttonWidth - fmBtn.stringWidth(btnText)) / 2, buttonY + 27);

        Font hintFont = new Font("Helvetica", Font.ITALIC, 11);
        g2d.setFont(hintFont);
        g2d.setColor(new Color(180, 210, 230));
        FontMetrics fmHint = g2d.getFontMetrics();
        String hintText = "(Click button or press ENTER to advance)";
        g2d.drawString(hintText, panelX + (panelWidth - fmHint.stringWidth(hintText)) / 2, buttonY + buttonHeight + 18);

        g2d.dispose();
    }

    private void drawStatRow(Graphics2D g2d, String label, String value, int x, int y, Font labelFont, Font valFont, Color valColor) {
        g2d.setFont(labelFont);
        g2d.setColor(new Color(200, 220, 240));
        g2d.drawString(label, x, y);

        g2d.setFont(valFont);
        g2d.setColor(valColor);
        int valueX = x + g2d.getFontMetrics(labelFont).stringWidth(label) + 8;
        g2d.drawString(value, valueX, y);
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
        SpawnDetails sd = spawnMap.get(frame);
        if (sd != null) {
            switch (sd.type) {
                case "Alien1":
                    Enemy enemy = new Alien1(sd.x, sd.y);
                    enemies.add(enemy);
                    break;
                case "Alien2":
                    Enemy enemy2 = new Alien2(sd.x, sd.y);
                    enemies.add(enemy2);
                    break;
                case "Alien3":
                    Enemy enemy3 = new Alien3(sd.x, sd.y);
                    enemies.add(enemy3);
                    break;
                case "PowerUp-SpeedUp":
                    PowerUp speedUp = new SpeedUp(sd.x, sd.y);
                    powerups.add(speedUp);
                    break;
                case "PowerUp-MultiShot":
                    PowerUp multiShot = new MultiShot(sd.x, sd.y);
                    powerups.add(multiShot);
                    break;
                case "PowerUp-HealUp": 
                    PowerUp healUp = new HealUp(sd.x, sd.y);
                    powerups.add(healUp);
                    break;
                default:
                    System.out.println("Unknown enemy type: " + sd.type);
                    break;
            }
        }
 
        // Handles 5-minute timer expiration (Victory)
        if (inGame && frame >= STAGE_DURATION_FRAMES) {
            onTimerFinished();
        }
 
        // Player update
        player.act();
 
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
                    } else if (powerup instanceof HealUp) { 
                        showPickupMessage("Health Restored!");
                    }
                }
            }
        }
 
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

                if (player.isVisible() && !player.isDying()) {
                    if (player.collidesWith(enemy)) { 
                        player.hit();
                        AudioPlayer.playSoundEffect(Global.AUD_EXPLODE);

                        boolean enemyDied = enemy.hit();
                        if (enemyDied) {
                            explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                            deaths++;
                            
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

                        if (player.isDying()) {
                            var ii = new ImageIcon(IMG_EXPLOSION);
                            var scaledDeathImg = ii.getImage().getScaledInstance(
                                    PLAYER_WIDTH, PLAYER_HEIGHT, java.awt.Image.SCALE_SMOOTH);
                            player.setImage(scaledDeathImg);
                        }
                    }
                }

                Bomb newBomb = enemy.maybeDropBomb();
                if (newBomb != null) {
                    bombs.add(newBomb);
                }
            }
        }
 
        // Bombs
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
 
                player.hit();
                bomb.die();
                bombsToRemove.add(bomb);
 
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
            if (timer != null && timer.isRunning()) {
                timer.stop();
            }
            message = "Game Over";
 
            if (audioPlayer != null) {
                try {
                    audioPlayer.stop(); 
                } catch (Exception e) {
                    System.err.println("Error stopping audio: " + e.getMessage());
                }
            }
 
            AudioPlayer.playSoundEffect(Global.AUD_GAMEOVER);
        }
 
        // Shots
        List<Shot> shotsToRemove = new ArrayList<>();
        for (Shot shot : shots) {
            if (shot.isVisible()) {
                int shotX = shot.getX();
                int shotY = shot.getY();
 
                for (Enemy enemy : enemies) {
                    int enemyX = enemy.getX();
                    int enemyY = enemy.getY();
 
                    if (enemy.isVisible() && shot.isVisible()
                            && shotX >= (enemyX)
                            && shotX <= (enemyX + ALIEN_WIDTH)
                            && shotY >= (enemyY)
                            && shotY <= (enemyY + ALIEN_HEIGHT)) {
 
                        boolean enemyDied = enemy.hit(); 
 
                        if (enemyDied) {
                            explosions.add(new Explosion(enemyX, enemyY));
                            deaths++;
 
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
 
                        shot.die(); 
                        shotsToRemove.add(shot);
                    }
                }
 
                int x = shot.getX();
                x += 20; 
 
                if (x > BOARD_WIDTH) {
                    shot.die();
                    shotsToRemove.add(shot);
                } else {
                    shot.setX(x); 
                }
            }
        }
        shots.removeAll(shotsToRemove);
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

            if (!inGame) {
                if (key == KeyEvent.VK_ENTER) {
                    if (isVictory || showDashboard) {
                        proceedToScene2();
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
                    if (player.hasMultiShot()) {
                        shots.add(new Shot(x, y - 10));
                        shots.add(new Shot(x, y + 10));
                        shotsFired += 2;
                    } else {
                        shots.add(new Shot(x, y));
                        shotsFired++;
                    }

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