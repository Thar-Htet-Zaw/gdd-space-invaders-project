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
import gdd.sprite.Enemy.Bomb;
import gdd.sprite.Player;
import gdd.sprite.Shot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
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
    private String powerUpMessage = "";
    private int powerUpMessageTimer = 0;
    private String message = "Game Over";

    private final Random randomizer = new Random();
    private HashMap<Integer, SpawnDetails> spawnMap = new HashMap<>();
    private static final int BOSS_HIT_POINTS = 40;
    private int bossSpawnFrame;
    private static final int WARNING_DURATION_FRAMES = 180; // ~3s flickering warning before boss arrives

    // Danger-zone background theme: same scrolling mechanism as Scene1's
    // starfield, but a distinct warm-color hazard motif instead of white stars.
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

    private void loadSpawnDetails() {
        int frameCursor = 100;
        spawnMap.put(300, new SpawnDetails("PowerUp-SpeedUp", 720, 200));
        spawnMap.put(800, new SpawnDetails("PowerUp-MultiShot", 720, 350));
        spawnMap.put(1500, new SpawnDetails("PowerUp-SpeedUp", 720, 150));
        spawnMap.put(2200, new SpawnDetails("PowerUp-MultiShot", 720, 400));

        // Wave 1: Scouts (Alien1)
        for (int i = 0; i < 8; i++) {
            spawnMap.put(frameCursor, new SpawnDetails("Alien1", 720, 60 + randomizer.nextInt(500)));
            frameCursor += 150;
        }

        frameCursor += 200; // brief breather before the next wave

        // Wave 2: Wraiths (Alien2, zigzag)
        for (int i = 0; i < 8; i++) {
            spawnMap.put(frameCursor, new SpawnDetails("Alien2", 720, 60 + randomizer.nextInt(500)));
            frameCursor += 150;
        }

        frameCursor += 200;

        // Wave 3: Juggernauts (Alien3, tank)
        for (int i = 0; i < 6; i++) {
            spawnMap.put(frameCursor, new SpawnDetails("Alien3", 720, 60 + randomizer.nextInt(500)));
            frameCursor += 200;
        }

        frameCursor += 300; // dramatic pause before the boss

        bossSpawnFrame = frameCursor;
        spawnMap.put(frameCursor, new SpawnDetails("Boss", 720, 100));
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
        message = "Game Over";
        bossMusicStarted = false;
        
        // Reset back to Stage 2 background music
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

                    // Pulse harder once enraged (phase 2) for extra visual feedback.
                    // Pure drawing transform on the existing boss image, no new art.
                    double amp = boss.isPhaseTwo() ? 0.03 : 0.015;
                    double pulse = 1.0 + amp * Math.sin(boss.getAnimFrame() * 0.2);
                    int w = (int) (Boss.WIDTH * pulse);
                    int h = (int) (Boss.HEIGHT * pulse);
                    double centerX = boss.getX() + Boss.WIDTH / 2.0;
                    double centerY = boss.getY() + Boss.HEIGHT / 2.0;
                    int drawX = (int) (centerX - w / 2.0);
                    int drawY = (int) (centerY - h / 2.0);

                    // Flip horizontally so the boss faces left, toward the player
                    g.drawImage(boss.getImage(), drawX + w, drawY, -w, h, this);
                } else {
                    // Subtle "breathing" pulse -- pure drawing transform, no new art.
                    double pulse = 1.0 + 0.06 * Math.sin(enemy.getAnimFrame() * 0.15);
                    Image img = enemy.getImage();
                    int baseW = img.getWidth(this);
                    int baseH = img.getHeight(this);
                    int w = (int) (baseW * pulse);
                    int h = (int) (baseH * pulse);
                    int drawX = enemy.getX() - (w - baseW) / 2;
                    int drawY = enemy.getY() - (h - baseH) / 2;
                    g.drawImage(img, drawX, drawY, w, h, this);
                }
            }

            if (enemy.isDying()) {
                enemy.die();
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

    private void drawPlayer(Graphics g) {
        if (player != null && player.isVisible()) {
            player.tickAnimation();

            Graphics2D g2d = (Graphics2D) g.create();

            int x = player.getX();
            int y = player.getY();
            int width = player.getImage().getWidth(null);
            int height = player.getImage().getHeight(null);

            // Rotate to face right, matching Scene1's orientation fix
            g2d.rotate(Math.toRadians(90), x + width / 2.0, y + height / 2.0);

            // Subtle engine-thrust pulse -- scales the ship slightly each frame.
            // Pure drawing transform, no new art needed.
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
                int width = shot.getImage().getWidth(null);
                int height = shot.getImage().getHeight(null);

                g2d.rotate(Math.toRadians(90), x + width / 2.0, y + height / 2.0);

                // Faint trailing streak behind the bolt -- pure drawing, no new art.
                g2d.setColor(new Color(255, 255, 150, 90));
                g2d.fillRect(x - 12, y + height / 2 - 2, 12, 4);

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
        int y = 75; // below the player HP bar's row (40-55), so they never overlap

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
        // Kill breakdown, bottom-left of the health bars
        g.setColor(Color.WHITE);
        var small = new Font("Helvetica", Font.PLAIN, 13);
        g.setFont(small);
        g.drawString("Scout Kills: " + scoutKills, 10, 110);
        g.drawString("Wraith Kills: " + wraithKills, 10, 125);
        g.drawString("Juggernaut Kills: " + juggernautKills, 10, 140);

        // Time + score, top-right corner
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

        if (frame % 20 < 10) { // flicker
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

        // Flicker while charging (telegraph warning), solid while firing
        if (charging && frame % 10 >= 5) {
            return;
        }

        Color color;
        switch (boss.getCurrentAttack()) {
            case TENTACLE_SLAM:
                color = charging ? new Color(0, 100, 0, 120) : new Color(0, 180, 0, 220);
                break;
            case EYE_BEAM_BARRAGE:
                color = charging ? new Color(150, 0, 200, 120) : new Color(200, 0, 255, 220);
                break;
            case SPORE_SWARM:
                color = charging ? new Color(100, 200, 0, 120) : new Color(140, 255, 0, 220);
                break;
            case LASER:
            default:
                color = charging ? new Color(255, 0, 0, 120) : new Color(255, 60, 0, 220);
                break;
        }

        g.setColor(color);
        for (int[] zone : boss.getActiveZones()) {
            if (boss.getCurrentAttack() == Boss.AttackType.SPORE_SWARM) {
                g.fillOval(zone[0], zone[1], zone[2], zone[3]);
            } else {
                g.fillRect(zone[0], zone[1], zone[2], zone[3]);
            }
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

    // 1. Add the helper method here (above doDrawing)
    private void drawExplosions(Graphics g) {
        for (Explosion exp : explosions) {
            g.drawImage(exp.getImage(), exp.getX(), exp.getY(), this);
        }
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
            drawExplosions(g); 
            drawBombs(g);
            drawBossAttack(g);
            drawPlayer(g);
            drawShots(g);
            drawHealthBar(g);
            drawBossHealthBar(g);
            drawStatusHUD(g);
            drawBossWarning(g);
            drawPowerUpMessage(g);

        } else {
            if (timer.isRunning()) {
                timer.stop();
            }

            gameOver(g);
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
            g.setColor(new Color(255, 215, 0)); // Gold color
            
            // Draw centered at the top area of the screen
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
                default:
                    System.out.println("Unknown spawn type: " + sd.type);
                    break;
            }
        }

        if (player != null) {
            player.act();
        }

        // Update shots + check collision with enemies
        List<Shot> shotsToRemove = new ArrayList<>();
        for (Shot shot : shots) {
            if (shot.isVisible()) {
                int shotX = shot.getX();
                int shotY = shot.getY();

                for (Enemy enemy : enemies) {
                    int enemyX = enemy.getX();
                    int enemyY = enemy.getY();

                    // Boss has a much bigger visual footprint, so it needs a matching hitbox —
                    // otherwise it'd look huge but only be hittable near its top-left corner.
                    int hitWidth = (enemy instanceof Boss) ? Boss.WIDTH : ALIEN_WIDTH;
                    int hitHeight = (enemy instanceof Boss) ? Boss.HEIGHT : ALIEN_HEIGHT;

                    if (enemy.isVisible() && shot.isVisible()
                            && shotX >= enemyX
                            && shotX <= enemyX + hitWidth
                            && shotY >= enemyY
                            && shotY <= enemyY + hitHeight) {

                        boolean enemyDied = enemy.hit();
                        if (enemyDied) {
                            deaths++;
                            AudioPlayer.playSoundEffect(Global.AUD_EXPLODE);
                            explosions.add(new Explosion(enemy.getX(), enemy.getY()));

                            if (enemy instanceof Boss) {
                                score += 5000;
                                inGame = false;
                                timer.stop();
                                message = "VICTORY! ALL STAGES CLEARED!";
                                
                                // Stop background audio on victory as well
                                if (audioPlayer != null) {
                                    try {
                                        audioPlayer.stop();
                                    } catch (Exception e) {
                                        System.err.println("Error stopping background audio: " + e.getMessage());
                                    }
                                }
                                AudioPlayer.playSoundEffect(Global.AUD_LEVEL_UP);
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

        // Boss-specific behavior: minion spawning + laser telegraph attack (phase 2 only)
        Boss boss = findBoss();
        if (boss != null && boss.isVisible()) {

            if (!bossMusicStarted) {
                try {
                    if (audioPlayer != null) {
                        audioPlayer.stop(); // Stop stage2.wav
                    }
                    audioPlayer = new AudioPlayer(Global.AUD_BOSS); // Start boss-battle.wav
                    audioPlayer.play();
                    bossMusicStarted = true;
                } catch (Exception e) {
                    System.err.println("Error switching to boss music: " + e.getMessage());
                }
            }

            boss.updateAttack(player.getX(), player.getY());

            String minionType = boss.maybeSpawnMinionType();
            if (minionType != null) {
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
                    player.setImage(ii.getImage());
                }
            }
        }

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

                player.hit();
                bomb.die();
                bombsToRemove.add(bomb);

                if (player.isDying()) {
                    var ii = new ImageIcon(IMG_EXPLOSION);
                    player.setImage(ii.getImage());
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

            // Stop background music upon dying
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

        // Tick down the message display timer
        if (powerUpMessageTimer > 0) {
            powerUpMessageTimer--;
        }


        List<Explosion> expiredExplosions = new ArrayList<>();
        for (Explosion exp : explosions) {
            exp.update();
            if (exp.isExpired()) {
                expiredExplosions.add(exp);
            }
        }
        explosions.removeAll(expiredExplosions);

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

    // --- ADD THE EXPLOSION CLASS HERE ---
    private class Explosion {
        private int x, y;
        private int timer = 12; // ~0.2 seconds visual burst
        private Image image;

        public Explosion(int x, int y) {
            this.x = x;
            this.y = y;
            
            ImageIcon ii = new ImageIcon(IMG_EXPLOSION);
            // Scale the explosion to match enemy sprite dimensions
            this.image = ii.getImage().getScaledInstance(32, 32, Image.SCALE_FAST);
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public Image getImage() { return image; }

        public void update() {
            timer--;
        }

        public boolean isExpired() {
            return timer <= 0;
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

                if (shots.size() < player.getMaxShots()) {
                    shots.add(new Shot(x, y));
                    AudioPlayer.playSoundEffect(Global.AUD_FIRE);
                }
            }
        }
    }
}