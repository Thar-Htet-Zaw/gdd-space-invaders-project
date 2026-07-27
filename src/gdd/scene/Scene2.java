package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import gdd.Global;
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.sprite.Alien1;
import gdd.sprite.Alien2;
import gdd.sprite.Alien3;
import gdd.sprite.Boss;
import gdd.sprite.Enemy;
import gdd.sprite.Explosion;
import gdd.sprite.Enemy.Bomb;
import gdd.sprite.Player;
import gdd.sprite.Shot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

import gdd.powerup.MultiShot;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.powerup.HealUp;

public class Scene2 extends JPanel {

    private static final String BOSS_NAME = "THE HARBINGER";

    private Dimension d;
    private List<Enemy> enemies;
    private Player player;
    private List<Shot> shots;
    private List<Bomb> bombs;
    private List<Explosion> explosions = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();

    private AudioPlayer audioPlayer;
    private boolean bossMusicStarted = false;
    

    private int frame = 0;
    private int deaths = 0;
    private int scoutKills = 0;
    private int wraithKills = 0;
    private int juggernautKills = 0;
    private long score = 0;
    private boolean inGame = true;
    private boolean isVictory = false;
    private String powerUpMessage = "";
    private int powerUpMessageTimer = 0;
    private String message = "Game Over";

    private final Random randomizer = new Random();
    private HashMap<Integer, SpawnDetails> spawnMap = new HashMap<>();
    private static final int BOSS_HIT_POINTS = 500;
    private int bossSpawnFrame;
    private static final int WARNING_DURATION_FRAMES = 180; // ~3s flickering warning before boss arrives

    // Danger-zone background theme
    final int BLOCKHEIGHT = 50;
    final int BLOCKWIDTH = 50;
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

    private Timer timer;
    private Game game;

    public Scene2(Game game) {
        this.game = game;
        initBoard();
        loadSpawnDetails();
        gameInit();
    }

    private void initBoard() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
        setBackground(Color.black);

        timer = new Timer(DELAY, new GameCycle());
    }

    public void start() {
        inGame = true;      
        isVictory = false;
        frame = 0;
        try {
            if (audioPlayer != null) {
                audioPlayer.stop();
            }
            audioPlayer = new AudioPlayer(Global.AUD_SCENE2);
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error playing Stage 2 audio: " + e.getMessage());
        }

        bossMusicStarted = false;

        setFocusable(true);
        requestFocusInWindow();

        timer.start();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
        if (audioPlayer != null) {
            try {
                audioPlayer.stop();
            } catch (Exception e) {
                System.err.println("Error stopping Stage 2 audio: " + e.getMessage());
            }
        }
    }

    // --- Enemy Formation Templates ---
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
    private static final int[][] FORMATION_DIAGONAL_LARGE = {
        {0, -100}, {50, -50}, {100, 0}, {150, 50}, {200, 100}
    };
    private static final int[][][] FORMATIONS = {
        FORMATION_V_SMALL, FORMATION_WALL_SMALL, FORMATION_V_LARGE, FORMATION_WALL_LARGE, FORMATION_DIAGONAL_LARGE
    };

    private static final boolean SKIP_WAVES_FOR_TESTING = false; // Turned off testing mode

    private void loadSpawnDetails() {
        if (SKIP_WAVES_FOR_TESTING) {
            bossSpawnFrame = 100;
            spawnMap.put(bossSpawnFrame, new SpawnDetails("Boss", 720, 100));
            return;
        }

        // --- PRE-BOSS HEALTH PACKS & POWER-UPS (0 to 1 Minute) ---
        spawnMap.put(300,  new SpawnDetails("PowerUp-HealUp", 720, 200));   // 5 sec
        spawnMap.put(900,  new SpawnDetails("PowerUp-HealUp", 720, 350));   // 15 sec
        spawnMap.put(1800, new SpawnDetails("PowerUp-HealUp", 720, 150));   // 30 sec
        spawnMap.put(2700, new SpawnDetails("PowerUp-HealUp", 720, 400));   // 45 sec
        spawnMap.put(3300, new SpawnDetails("PowerUp-HealUp", 720, 250));   // 55 sec (Just before Boss)

        // Pre-boss utility power-ups
        spawnMap.put(600,  new SpawnDetails("PowerUp-SpeedUp", 720, 150));
        spawnMap.put(1200, new SpawnDetails("PowerUp-MultiShot", 720, 300));
        spawnMap.put(2100, new SpawnDetails("PowerUp-SpeedUp", 720, 250));
        spawnMap.put(3000, new SpawnDetails("PowerUp-MultiShot", 720, 350));

        // --- ENEMY WAVES (Packed into 1 Minute) ---
        int frameCursor = 120;

        // Rapid Warmup Waves
        for (int i = 0; i < 6; i++) {
            String type = (i % 2 == 0) ? "Alien2" : "Alien1";
            for (int k = 0; k < 3; k++) {
                spawnMap.put(frameCursor, new SpawnDetails(type, 720, 80 + randomizer.nextInt(460)));
                frameCursor += 30;
            }
            frameCursor += 150;
        }

        // Formation Assault
        for (int wave = 0; wave < 8; wave++) {
            int[][] formation = FORMATIONS[wave % FORMATIONS.length];
            String enemyType = (wave % 3 == 0) ? "Alien3" : ((wave % 2 == 0) ? "Alien2" : "Alien1");
            int baseY = 160 + randomizer.nextInt(250);

            for (int i = 0; i < formation.length; i++) {
                int dx = formation[i][0];
                int dy = formation[i][1];
                spawnMap.put(frameCursor + i, new SpawnDetails(enemyType, 720 + dx, baseY + dy));
            }
            frameCursor += formation.length + 180;
        }

        // Heavy Minion Rush
        while (frameCursor < 3420) {
            spawnMap.put(frameCursor, new SpawnDetails("Alien3", 720, 150 + randomizer.nextInt(300)));
            spawnMap.put(frameCursor + 15, new SpawnDetails("Alien1", 720, 100));
            spawnMap.put(frameCursor + 30, new SpawnDetails("Alien1", 720, 500));
            frameCursor += 120;
        }

        // --- BOSS ARRIVAL AT EXACTLY 1 MINUTE (3600 FRAMES) ---
        bossSpawnFrame = 3600; 
        spawnMap.put(bossSpawnFrame, new SpawnDetails("Boss", 720, 100));

        // --- MID-BOSS FIGHT POWER-UPS & HEALTH PACKS ---
        // Spawns health packs every ~15-20 seconds AFTER the boss arrives to keep the player alive
        spawnMap.put(4500,  new SpawnDetails("PowerUp-HealUp", 720, 200));   // 1m 15s
        spawnMap.put(5400,  new SpawnDetails("PowerUp-HealUp", 720, 400));   // 1m 30s
        spawnMap.put(6300,  new SpawnDetails("PowerUp-MultiShot", 720, 300)); // 1m 45s (Weapon boost)
        spawnMap.put(7200,  new SpawnDetails("PowerUp-HealUp", 720, 150));   // 2m 00s
        spawnMap.put(8100,  new SpawnDetails("PowerUp-HealUp", 720, 350));   // 2m 15s
        spawnMap.put(9000,  new SpawnDetails("PowerUp-SpeedUp", 720, 250));   // 2m 30s
        spawnMap.put(9900,  new SpawnDetails("PowerUp-HealUp", 720, 200));   // 2m 45s
        spawnMap.put(10800, new SpawnDetails("PowerUp-HealUp", 720, 400));   // 3m 00s
    }

    private void gameInit() {
        enemies = new ArrayList<>();
        shots = new ArrayList<>();
        bombs = new ArrayList<>();
        explosions = new ArrayList<>();
        powerUps = new ArrayList<>();
        player = new Player();
    }

    private void restartStage() {
        frame = 0;
        deaths = 0;
        scoutKills = 0;
        wraithKills = 0;
        juggernautKills = 0;
        score = 0;
        inGame = true;
        isVictory = false;
        message = "Game Over";
        bossMusicStarted = false;
        
        try {
            if (audioPlayer != null) {
                audioPlayer.stop();
            }
            audioPlayer = new AudioPlayer(Global.AUD_SCENE2);
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error resetting audio: " + e.getMessage());
        }

        gameInit(); 
        timer.start();
    }

    private void drawMap(Graphics g) {
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
                    drawHazardCluster(g, x, y, BLOCKWIDTH, BLOCKHEIGHT);
                }
            }
        }
    }

    private void drawHazardCluster(Graphics g, int x, int y, int width, int height) {
        int centerX = x + width / 2;
        int centerY = y + height / 2;

        g.setColor(new Color(255, 100, 0));
        g.fillRect(centerX - 2, centerY - 2, 4, 4);

        g.setColor(new Color(200, 40, 0));
        g.fillRect(centerX - 7, centerY - 7, 3, 3);
        g.fillRect(centerX + 5, centerY + 4, 3, 3);
    }

    private void drawEnemies(Graphics g) {
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.tickAnimation();

                if (enemy instanceof Boss) {
                    Boss boss = (Boss) enemy;
                    drawBossAnimated(g, boss);
                } else {
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
            }

            if (enemy.isDying()) {
                enemy.die();
            }
        }
    }

    private void drawBossAnimated(Graphics g, Boss boss) {
        Image img = boss.getImage();
        int srcW = Boss.WIDTH;
        int srcH = Boss.HEIGHT;

        float flash = boss.getDamageFlashIntensity();
        if (flash > 0f) {
            img = applyRedTint(img, srcW, srcH, flash);
        }

        double amp = boss.isPhaseTwo() ? 0.02 : 0.01;
        double pulse = 1.0 + amp * Math.sin(boss.getAnimFrame() * 0.2);
        int w = (int) (srcW * pulse);
        int h = (int) (srcH * pulse);
        double centerX = boss.getX() + srcW / 2.0;
        double centerY = boss.getY() + srcH / 2.0;
        int baseDrawX = (int) (centerX - w / 2.0);
        int baseDrawY = (int) (centerY - h / 2.0);

        int strips = 40;
        int stripSrcH = Math.max(1, srcH / strips);
        int stripDstH = Math.max(1, h / strips);

        float waveSpeed = boss.isPhaseTwo() ? 0.25f : 0.15f;
        float waveFrequency = 0.35f;

        for (int i = 0; i < strips; i++) {
            int srcY1 = i * stripSrcH;
            int srcY2 = Math.min(srcH, srcY1 + stripSrcH);
            int dstY1 = baseDrawY + i * stripDstH;
            int dstY2 = dstY1 + stripDstH;

            float verticalProgress = (float) i / strips;
            float stripAmplitude = 18f * verticalProgress * verticalProgress;

            int xOffset = (int) (stripAmplitude * Math.sin(boss.getAnimFrame() * waveSpeed + i * waveFrequency));

            g.drawImage(img,
                    baseDrawX + xOffset + w, dstY1, baseDrawX + xOffset, dstY2,
                    0, srcY1, srcW, srcY2,
                    this);
        }
    }

    private Image applyRedTint(Image source, int width, int height, float intensity) {
        BufferedImage tinted = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = tinted.createGraphics();
        g2d.drawImage(source, 0, 0, width, height, this);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, intensity));
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return tinted;
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

    private void drawShots(Graphics g) {
        for (Shot shot : shots) {
            if (shot.isVisible()) {
                shot.tickAnimation();

                Graphics2D g2d = (Graphics2D) g.create();

                int x = shot.getX();
                int y = shot.getY();

                g2d.rotate(Math.toRadians(90), x + shot.getImage().getWidth(null) / 2.0, y + shot.getImage().getHeight(null) / 2.0);

                g2d.drawImage(shot.getImage(), x, y, this);
                g2d.dispose();
            }
        }
    }

    private void drawHealthBar(Graphics g) {
        if (player == null) {
            return;
        }

        int totalHealth = 5;
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

    private void drawBossHealthBar(Graphics g) {
        Boss boss = findBoss();
        if (boss == null || !boss.isVisible()) {
            return;
        }

        int barWidth = 400;
        int barHeight = 20;
        int x = (BOARD_WIDTH - barWidth) / 2;
        int y = 75;

        int currentHp = Math.max(0, boss.getHitPoints());
        int maxHp = BOSS_HIT_POINTS;

        g.setColor(Color.RED);
        g.fillRect(x, y, barWidth, barHeight);

        g.setColor(boss.isPhaseTwo() ? new Color(255, 140, 0) : Color.MAGENTA);
        int currentWidth = (barWidth * currentHp) / maxHp;
        g.fillRect(x, y, currentWidth, barHeight);

        g.setColor(Color.WHITE);
        g.drawRect(x, y, barWidth, barHeight);

        String label = boss.isPhaseTwo() ? BOSS_NAME + " (ENRAGED)" : BOSS_NAME;
        var font = new Font("Helvetica", Font.BOLD, 14);
        g.setFont(font);
        var fm = g.getFontMetrics(font);
        g.drawString(label, (BOARD_WIDTH - fm.stringWidth(label)) / 2, y - 5);
    }

    private void drawStatusHUD(Graphics g) {
        g.setColor(Color.WHITE);
        var small = new Font("Helvetica", Font.PLAIN, 13);
        g.setFont(small);
        g.drawString("Scout Kills: " + scoutKills, 10, 110);
        g.drawString("Wraith Kills: " + wraithKills, 10, 125);
        g.drawString("Juggernaut Kills: " + juggernautKills, 10, 140);

        var timerFont = new Font("Helvetica", Font.BOLD, 18);
        g.setFont(timerFont);
        var fmTimer = g.getFontMetrics(timerFont);
        String timeText = "Time: " + formatTime(frame);
        g.setColor(Color.WHITE);
        g.drawString(timeText, BOARD_WIDTH - fmTimer.stringWidth(timeText) - 10, 20);

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

    private String formatTime(int frames) {
        int totalSeconds = frames / 60;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private void drawBossWarning(Graphics g) {
        int warningStart = bossSpawnFrame - WARNING_DURATION_FRAMES;
        if (frame < warningStart || frame >= bossSpawnFrame) {
            return;
        }

        if (frame % 20 < 10) {
            String warning = "WARNING: " + BOSS_NAME + " IS COMING!";
            var font = new Font("Helvetica", Font.BOLD, 26);
            g.setFont(font);
            g.setColor(Color.RED);
            var fm = g.getFontMetrics(font);
            g.drawString(warning, (BOARD_WIDTH - fm.stringWidth(warning)) / 2, BOARD_HEIGHT / 2 - 100);
        }
    }

    private void drawBossAttack(Graphics g) {
        Boss boss = findBoss();
        if (boss == null || !boss.isVisible()) {
            return;
        }

        if (!boss.isCharging() && !boss.isFiring()) {
            return;
        }

        boolean charging = boss.isCharging();

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (boss.getCurrentAttack()) {
            case LASER:
                drawLaserAttack(g2d, boss, charging);
                break;
            case TENTACLE_SLAM:
                drawSlamAttack(g2d, boss, charging);
                break;
            case EYE_BEAM_BARRAGE:
                drawEyeBeamAttack(g2d, boss, charging);
                break;
            case SPORE_SWARM:
                drawSporeSwarmAttack(g2d, boss, charging);
                break;
        }

        g2d.dispose();
    }

    private void drawLaserAttack(Graphics2D g2d, Boss boss, boolean charging) {
        for (int[] zone : boss.getActiveZones()) {
            int zx = zone[0], zy = zone[1], zw = zone[2], zh = zone[3];
            int centerY = zy + zh / 2;

            if (charging) {
                if (frame % 10 < 5) {
                    g2d.setColor(new Color(255, 80, 40, 60));
                    g2d.fillRect(zx, centerY - 8, zw, 16);

                    g2d.setColor(new Color(255, 60, 40, 210));
                    g2d.setStroke(new BasicStroke(3f));
                    g2d.drawLine(zx, centerY, zx + zw, centerY);
                }
            } else {
                GradientPaint upperHalf = new GradientPaint(
                        zx, zy, new Color(255, 120, 40, 0),
                        zx, centerY, new Color(255, 255, 240, 230));
                g2d.setPaint(upperHalf);
                g2d.fillRect(zx, zy, zw, Math.max(1, zh / 2));

                GradientPaint lowerHalf = new GradientPaint(
                        zx, centerY, new Color(255, 255, 240, 230),
                        zx, zy + zh, new Color(255, 120, 40, 0));
                g2d.setPaint(lowerHalf);
                g2d.fillRect(zx, centerY, zw, Math.max(1, zh / 2));

                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(3f));
                g2d.drawLine(zx, centerY, zx + zw, centerY);

                g2d.setColor(new Color(255, 200, 120, 160));
                g2d.setStroke(new BasicStroke(1.5f));
                int shimmerOffset = (frame * 5) % 40;
                for (int sx = zx - 40 + shimmerOffset; sx < zx + zw; sx += 40) {
                    g2d.drawLine(sx, zy, sx + 12, zy + zh);
                }
            }
        }
    }

    private void drawSlamAttack(Graphics2D g2d, Boss boss, boolean charging) {
        for (int[] zone : boss.getActiveZones()) {
            int zx = zone[0], zy = zone[1], zw = zone[2], zh = zone[3];
            int cx = zx + zw / 2;
            int cy = zy + zh / 2;
            float radius = Math.min(zw, zh) / 2f;

            if (charging) {
                g2d.setStroke(new BasicStroke(3f));
                for (int ring = 0; ring < 3; ring++) {
                    float ringProgress = ((frame * 3 + ring * 30) % 90) / 90f;
                    float r = radius * ringProgress;
                    int alpha = (int) (200 * (1f - ringProgress));
                    g2d.setColor(new Color(60, 220, 90, Math.max(0, alpha)));
                    g2d.drawOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));
                }
            } else {
                float[] fractions = {0f, 0.6f, 1f};
                Color[] colors = {
                    new Color(220, 255, 200, 230),
                    new Color(60, 200, 90, 180),
                    new Color(20, 100, 40, 0)
                };
                RadialGradientPaint burst = new RadialGradientPaint(
                        cx, cy, Math.max(1f, radius), fractions, colors);
                g2d.setPaint(burst);
                g2d.fillOval((int) (cx - radius), (int) (cy - radius), (int) (radius * 2), (int) (radius * 2));

                g2d.setColor(new Color(230, 255, 210, 200));
                g2d.setStroke(new BasicStroke(2f));
                Random crackRandom = new Random((long) cx * 73856093L ^ (long) cy * 19349663L);
                for (int i = 0; i < 8; i++) {
                    double angle = crackRandom.nextDouble() * Math.PI * 2;
                    double len = radius * (0.6 + crackRandom.nextDouble() * 0.4);
                    int ex = (int) (cx + Math.cos(angle) * len);
                    int ey = (int) (cy + Math.sin(angle) * len);
                    g2d.drawLine(cx, cy, ex, ey);
                }
            }
        }
    }

    private void drawEyeBeamAttack(Graphics2D g2d, Boss boss, boolean charging) {
        for (int[] zone : boss.getActiveZones()) {
            int zx = zone[0], zy = zone[1], zw = zone[2], zh = zone[3];
            int centerY = zy + zh / 2;

            if (charging) {
                if (frame % 8 < 4) {
                    g2d.setColor(new Color(190, 60, 255, 130));
                    g2d.fillRect(zx, zy, zw, zh);
                }
            } else {
                GradientPaint upperHalf = new GradientPaint(
                        zx, zy, new Color(180, 40, 255, 0),
                        zx, centerY, new Color(230, 150, 255, 220));
                g2d.setPaint(upperHalf);
                g2d.fillRect(zx, zy, zw, Math.max(1, zh / 2));

                GradientPaint lowerHalf = new GradientPaint(
                        zx, centerY, new Color(230, 150, 255, 220),
                        zx, zy + zh, new Color(180, 40, 255, 0));
                g2d.setPaint(lowerHalf);
                g2d.fillRect(zx, centerY, zw, Math.max(1, zh / 2));

                g2d.setColor(new Color(255, 255, 255, 180));
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawLine(zx, centerY, zx + zw, centerY);
            }
        }
    }

    private void drawSporeSwarmAttack(Graphics2D g2d, Boss boss, boolean charging) {
        int zoneIndex = 0;
        for (int[] zone : boss.getActiveZones()) {
            int zx = zone[0], zy = zone[1], zw = zone[2], zh = zone[3];
            Random particleRandom = new Random((long) zx * 92821L ^ (long) zy * 68917L ^ zoneIndex);

            int particleCount = 6;
            for (int i = 0; i < particleCount; i++) {
                double baseAngle = particleRandom.nextDouble() * Math.PI * 2;
                double baseDist = particleRandom.nextDouble() * (Math.min(zw, zh) / 2.0);
                double px = zx + zw / 2.0 + Math.cos(baseAngle) * baseDist;
                double py = zy + zh / 2.0 + Math.sin(baseAngle) * baseDist;

                double pulse = 0.7 + 0.3 * Math.sin(frame * 0.3 + i);
                int size = (int) ((charging ? 8 : 14) * pulse);
                int alpha = charging ? 110 : 200;

                g2d.setColor(new Color(140, 255, 90, alpha));
                g2d.fillOval((int) (px - size / 2.0), (int) (py - size / 2.0), size, size);

                int coreSize = Math.max(1, size / 2);
                g2d.setColor(new Color(220, 255, 180, alpha));
                g2d.fillOval((int) (px - coreSize / 2.0), (int) (py - coreSize / 2.0), coreSize, coreSize);
            }
            zoneIndex++;
        }
    }

    private Boss findBoss() {
        for (Enemy enemy : enemies) {
            if (enemy instanceof Boss) {
                return (Boss) enemy;
            }
        }
        return null;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
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

    private void doDrawing(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);

        if (inGame) {
            drawMap(g);

            g.setColor(Color.WHITE);
            g.drawString("STAGE 2 - FINAL STAGE", 10, 20);

            drawEnemies(g);
            drawPowerUps(g);
            drawBombs(g);
            drawBossAttack(g);
            drawPlayer(g);
            drawShots(g);
            drawExplosions(g);
            drawHealthBar(g);
            drawBossHealthBar(g);
            drawStatusHUD(g);
            drawBossWarning(g);
            drawPowerUpMessage(g);

        } else {
            if (timer.isRunning()) {
                timer.stop();
            }

            if (isVictory) {
                drawVictory(g);
            } else {
                gameOver(g);
            }
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawPowerUps(Graphics g) {
        for (PowerUp pu : powerUps) {
            if (pu.isVisible()) {
                g.drawImage(pu.getImage(), pu.getX(), pu.getY(), this);
            }
        }
    }

    private void drawPowerUpMessage(Graphics g) {
        if (powerUpMessageTimer > 0 && !powerUpMessage.isEmpty()) {
            g.setFont(new Font("Helvetica", Font.BOLD, 18));
            g.setColor(new Color(255, 215, 0));
            g.drawString(powerUpMessage, 200, 80); 
        }
    }

    private void gameOver(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        ImageIcon ii = new ImageIcon(IMG_GAME_OVER);
        Image gameOverImg = ii.getImage();
        
        g.drawImage(gameOverImg, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, this);
    }

    private void drawVictory(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(new Color(5, 12, 10, 220));
        g2d.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        int panelWidth = 540;
        int panelHeight = 440;
        int panelX = (BOARD_WIDTH - panelWidth) / 2;
        int panelY = (BOARD_HEIGHT - panelHeight) / 2;

        g2d.setColor(new Color(10, 32, 20, 240));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);

        GradientPaint winGlow = new GradientPaint(
            panelX, panelY, new Color(50, 225, 100, 40),
            panelX, panelY + panelHeight, new Color(0, 0, 0, 0)
        );
        g2d.setPaint(winGlow);
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);

        g2d.setColor(new Color(255, 215, 0, 200));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);

        Font titleFont = new Font("Helvetica", Font.BOLD, 26);
        g2d.setFont(titleFont);
        FontMetrics fmTitle = g2d.getFontMetrics();
        String title = "VICTORY! ALL STAGES CLEARED";
        
        g2d.setColor(new Color(0, 255, 120));
        g2d.drawString(title, panelX + (panelWidth - fmTitle.stringWidth(title)) / 2, panelY + 50);

        g2d.setColor(new Color(255, 215, 0, 120));
        g2d.drawLine(panelX + 40, panelY + 68, panelX + panelWidth - 40, panelY + 68);

        Font labelFont = new Font("Helvetica", Font.PLAIN, 15);
        Font valFont = new Font("Helvetica", Font.BOLD, 15);
        int startY = panelY + 110;
        int rowGap = 32;
        int col1X = panelX + 50;
        int col2X = panelX + panelWidth / 2 + 10;

        int totalKills = scoutKills + wraithKills + juggernautKills;

        drawStatRow(g2d, "Final Score:", String.valueOf(score), col1X, startY, labelFont, valFont, new Color(255, 215, 0));
        drawStatRow(g2d, "Clear Time:", formatTime(frame), col1X, startY + rowGap, labelFont, valFont, Color.WHITE);
        drawStatRow(g2d, "Total Kills:", String.valueOf(totalKills), col1X, startY + rowGap * 2, labelFont, valFont, Color.CYAN);
        drawStatRow(g2d, "Boss Eliminated:", BOSS_NAME, col1X, startY + rowGap * 3, labelFont, valFont, new Color(255, 100, 100));

        drawStatRow(g2d, "Scouts:", String.valueOf(scoutKills), col2X, startY, labelFont, valFont, Color.LIGHT_GRAY);
        drawStatRow(g2d, "Wraiths:", String.valueOf(wraithKills), col2X, startY + rowGap, labelFont, valFont, Color.LIGHT_GRAY);
        drawStatRow(g2d, "Juggernauts:", String.valueOf(juggernautKills), col2X, startY + rowGap * 2, labelFont, valFont, Color.LIGHT_GRAY);

        int promptY = panelY + panelHeight - 75;
        g2d.setColor(new Color(0, 190, 255, 80));
        g2d.drawLine(panelX + 40, promptY - 15, panelX + panelWidth - 40, promptY - 15);

        Font promptFont = new Font("Helvetica", Font.BOLD, 16);
        g2d.setFont(promptFont);
        g2d.setColor(Color.YELLOW);
        FontMetrics fmPrompt = g2d.getFontMetrics();
        String promptText = "PRESS ENTER TO PLAY AGAIN";
        g2d.drawString(promptText, panelX + (panelWidth - fmPrompt.stringWidth(promptText)) / 2, promptY + 15);

        g2d.dispose();
    }

    private void drawStatRow(Graphics2D g2d, String label, String value, int x, int y, Font labelFont, Font valFont, Color valColor) {
        g2d.setFont(labelFont);
        g2d.setColor(new Color(200, 230, 210));
        g2d.drawString(label, x, y);

        g2d.setFont(valFont);
        g2d.setColor(valColor);
        int valueX = x + g2d.getFontMetrics(labelFont).stringWidth(label) + 8;
        g2d.drawString(value, valueX, y);
    }

    private boolean rectsOverlap(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }

    private void update() {
        SpawnDetails sd = spawnMap.get(frame);
        if (sd != null) {
            switch (sd.type) {
                case "Alien1":
                    enemies.add(new Alien1(sd.x, sd.y));
                    break;
                case "Alien2":
                    enemies.add(new Alien2(sd.x, sd.y));
                    break;
                case "Alien3":
                    enemies.add(new Alien3(sd.x, sd.y));
                    break;
                case "Boss":
                    enemies.add(new Boss(sd.x, sd.y, BOSS_HIT_POINTS));
                    break;
                case "PowerUp-SpeedUp":
                    powerUps.add(new SpeedUp(sd.x, sd.y));
                    break;
                case "PowerUp-MultiShot":
                    powerUps.add(new MultiShot(sd.x, sd.y));
                    break;
                case "PowerUp-HealUp": 
                    powerUps.add(new HealUp(sd.x, sd.y));
                    break;
                default:
                    System.out.println("Unknown spawn type: " + sd.type);
                    break;
            }
        }

        if (player != null) {
            player.act();
        }

        // --- PLAYER VS ENEMY DIRECT COLLISION CHECK ---
        if (player != null && player.isVisible()) {
            for (Enemy enemy : enemies) {
                int enemyX = (enemy instanceof Boss) ? ((Boss) enemy).getHitboxX() : enemy.getX();
                int enemyY = (enemy instanceof Boss) ? ((Boss) enemy).getHitboxY() : enemy.getY();
                int hitWidth = (enemy instanceof Boss) ? ((Boss) enemy).getHitboxWidth() : ALIEN_WIDTH;
                int hitHeight = (enemy instanceof Boss) ? ((Boss) enemy).getHitboxHeight() : ALIEN_HEIGHT;

                if (enemy.isVisible() && rectsOverlap(player.getX(), player.getY(), PLAYER_WIDTH, PLAYER_HEIGHT,
                        enemyX, enemyY, hitWidth, hitHeight)) {
                    
                    player.hit(); // Lose 1 HP on impact

                    // Non-boss enemies are destroyed on impact with the player
                    if (!(enemy instanceof Boss)) {
                        enemy.die();
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                    }

                    if (player.isDying()) {
                        var ii = new ImageIcon(IMG_EXPLOSION);
                        var scaledDeathImg = ii.getImage().getScaledInstance(
                                PLAYER_WIDTH, PLAYER_HEIGHT, java.awt.Image.SCALE_SMOOTH);
                        player.setImage(scaledDeathImg);
                    }
                    break; // Process one collision frame at a time
                }
            }
        }

        // Update shots + check collision with enemies
        List<Shot> shotsToRemove = new ArrayList<>();
        for (Shot shot : shots) {
            if (shot.isVisible()) {
                int shotX = shot.getX();
                int shotY = shot.getY();

                for (Enemy enemy : enemies) {
                    int enemyX = (enemy instanceof Boss) ? ((Boss) enemy).getHitboxX() : enemy.getX();
                    int enemyY = (enemy instanceof Boss) ? ((Boss) enemy).getHitboxY() : enemy.getY();

                    int hitWidth = (enemy instanceof Boss) ? ((Boss) enemy).getHitboxWidth() : ALIEN_WIDTH;
                    int hitHeight = (enemy instanceof Boss) ? ((Boss) enemy).getHitboxHeight() : ALIEN_HEIGHT;

                    if (enemy.isVisible() && shot.isVisible()
                            && shotX >= enemyX
                            && shotX <= enemyX + hitWidth
                            && shotY >= enemyY
                            && shotY <= enemyY + hitHeight) {

                        boolean bossTookRealDamage = (enemy instanceof Boss) && ((Boss) enemy).hasEngaged();

                        boolean enemyDied = enemy.hit();

                        if (enemy instanceof Boss && bossTookRealDamage) {
                            explosions.add(new Explosion(shotX, shotY));
                        }

                        if (enemyDied) {
                            deaths++;
                            AudioPlayer.playSoundEffect(Global.AUD_EXPLODE);
                            if (!(enemy instanceof Boss)) {
                                explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                            }

                            if (enemy instanceof Boss) {
                                score += 5000;
                                inGame = false;
                                isVictory = true;
                                timer.stop();
                                message = "VICTORY! ALL STAGES CLEARED!";
                                
                                if (audioPlayer != null) {
                                    try {
                                        audioPlayer.stop();
                                    } catch (Exception e) {
                                        System.err.println("Error stopping background audio: " + e.getMessage());
                                    }
                                }
                                AudioPlayer.playSoundEffect(Global.AUD_VICTORY);

                            } else if (enemy instanceof Alien3) {
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
                        break;
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
            } else {
                shotsToRemove.add(shot);
            }
        }
        shots.removeAll(shotsToRemove);

        // Update enemies + roll bomb drops
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.act(1);

                Bomb newBomb = enemy.maybeDropBomb();
                if (newBomb != null) {
                    bombs.add(newBomb);
                }
            }
        }

        Boss boss = findBoss();
        if (boss != null && boss.isVisible()) {

            // OLD CODE (Line ~757):
            if (!bossMusicStarted) {
                try {
                    if (audioPlayer != null) {
                        audioPlayer.stop();
                    }
                    audioPlayer = new AudioPlayer(Global.AUD_BOSS);
                    audioPlayer.play();
                    bossMusicStarted = true;
                } catch (Exception e) {
                    System.err.println("Error switching to boss music: " + e.getMessage());
                }
            }

            boss.updateAttack(player.getX(), player.getY());

            List<String> minionTypes = boss.maybeSpawnMinionTypes();
            for (String minionType : minionTypes) {
                int minionY = 60 + randomizer.nextInt(500);
                switch (minionType) {
                    case "Alien1":
                        enemies.add(new Alien1(720, minionY));
                        break;
                    case "Alien2":
                        enemies.add(new Alien2(720, minionY));
                        break;
                    case "Alien3":
                        enemies.add(new Alien3(720, minionY));
                        break;
                }
            }

            if (player.isVisible()
                    && boss.consumeHitIfPlayerInZone(player.getX(), player.getY(), PLAYER_WIDTH, PLAYER_HEIGHT)) {
                player.hit();
                player.hit();
                if (player.isDying()) {
                    var ii = new ImageIcon(IMG_EXPLOSION);
                    var scaledDeathImg = ii.getImage().getScaledInstance(
                            PLAYER_WIDTH, PLAYER_HEIGHT, java.awt.Image.SCALE_SMOOTH);
                    player.setImage(scaledDeathImg);
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

        if (player.isDying()) {
            player.die();
            inGame = false;
            timer.stop();
            message = "Game Over";

            if (audioPlayer != null) {
                try {
                    audioPlayer.stop();
                } catch (Exception e) {
                    System.err.println("Error stopping background music: " + e.getMessage());
                }
            }

            AudioPlayer.playSoundEffect(Global.AUD_GAMEOVER);
        }

        List<PowerUp> powerUpsToRemove = new ArrayList<>();
        for (PowerUp pu : powerUps) {
            if (pu.isVisible()) {
                pu.act();

                if (pu.collidesWith(player)) {
                    pu.upgrade(player);
                    AudioPlayer.playSoundEffect(Global.AUD_LEVEL_UP);
                    
                    if (pu instanceof SpeedUp) {
                        powerUpMessage = "Speed Increased!";
                    } else if (pu instanceof MultiShot) {
                        powerUpMessage = "Multi-Shot Activated!";
                    } else if (pu instanceof HealUp) {
                        powerUpMessage = "Health Restored (+1 HP)!";
                    }

                    powerUpMessageTimer = 60; 
                    powerUpsToRemove.add(pu);
                    
                } else if (pu.getX() < -32) {
                    pu.die();
                    powerUpsToRemove.add(pu);
                }
            } else {
                powerUpsToRemove.add(pu);
            }
        }
        powerUps.removeAll(powerUpsToRemove);

        if (powerUpMessageTimer > 0) {
            powerUpMessageTimer--;
        }

        frame++;
    }

    private void doGameCycle() {
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
                    restartStage();
                }
                return;
            }

            if (player != null) {
                player.keyPressed(e);
            }

            if (key == KeyEvent.VK_SPACE) {
                int x = player.getX();
                int y = player.getY();

                int maxShots = player.getMaxShots();
                if (shots.size() < maxShots) {
                    if (maxShots >= 3) {
                        shots.add(new Shot(x, y - 10));
                        shots.add(new Shot(x, y));
                        shots.add(new Shot(x, y + 10));
                    } else if (maxShots == 2) {
                        shots.add(new Shot(x, y - 8));
                        shots.add(new Shot(x, y + 8));
                    } else {
                        shots.add(new Shot(x, y));
                    }
                    AudioPlayer.playSoundEffect(Global.AUD_FIRE);
                }
            }
        }
    }
}