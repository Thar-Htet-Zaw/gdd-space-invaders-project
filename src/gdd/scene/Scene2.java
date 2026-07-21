package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import gdd.sprite.Enemy;
import gdd.sprite.Player;
import gdd.sprite.Shot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Scene2 extends JPanel {

    private Dimension d;
    private List<Enemy> enemies;
    private Player player;
    private List<Shot> shots;

    private int deaths = 0;
    private boolean inGame = true;
    private String message = "Game Over";

    private Timer timer;
    private Game game;

    public Scene2(Game game) {
        this.game = game;
        initBoard();
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
        timer.start();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void gameInit() {
        enemies = new ArrayList<>();

        // Stage 2 setup (e.g., higher difficulty or Boss enemy layout)
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 6; j++) {
                var enemy = new Enemy(ALIEN_INIT_X + 40 * j, ALIEN_INIT_Y + 40 * i);
                enemies.add(enemy);
            }
        }

        player = new Player();
        shots = new ArrayList<>();
    }

    private void drawEnemies(Graphics g) {
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                g.drawImage(enemy.getImage(), enemy.getX(), enemy.getY(), this);
            }

            if (enemy.isDying()) {
                enemy.die();
            }
        }
    }

    private void drawPlayer(Graphics g) {
        if (player != null && player.isVisible()) {
            g.drawImage(player.getImage(), player.getX(), player.getY(), this);
        }

        if (player.isDying()) {
            player.die();
            inGame = false;
        }
    }

    private void drawShots(Graphics g) {
        for (Shot shot : shots) {
            if (shot != null && shot.isVisible()) {
                g.drawImage(shot.getImage(), shot.getX(), shot.getY(), this);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);

        if (inGame) {
            // Draw Stage 2 banner indicator
            g.setColor(Color.WHITE);
            g.drawString("STAGE 2 - FINAL STAGE", 10, 20);

            drawEnemies(g);
            drawPlayer(g);
            drawShots(g);
        } else {
            if (timer.isRunning()) {
                timer.stop();
            }

            gameOver(g);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void gameOver(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g.setColor(new Color(0, 32, 48));
        g.fillRect(50, BOARD_WIDTH / 2 - 30, BOARD_WIDTH - 100, 50);
        g.setColor(Color.white);
        g.drawRect(50, BOARD_WIDTH / 2 - 30, BOARD_WIDTH - 100, 50);

        var small = new Font("Helvetica", Font.BOLD, 14);
        var fontMetrics = this.getFontMetrics(small);

        g.setColor(Color.white);
        g.setFont(small);
        g.drawString(message, (BOARD_WIDTH - fontMetrics.stringWidth(message)) / 2,
                BOARD_WIDTH / 2);
    }

    private void update() {
        if (deaths == NUMBER_OF_ALIENS_TO_DESTROY) {
            inGame = false;
            timer.stop();
            message = "VICTORY! ALL STAGES CLEARED!";
        }

        if (player != null) {
            player.act();
        }

        // Update shots
        for (int i = 0; i < shots.size(); i++) {
            Shot shot = shots.get(i);
            if (shot.isVisible()) {
                shot.act();
            } else {
                shots.remove(i);
                i--;
            }
        }

        // Update enemies
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.act(1);
            }
        }
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
            if (player != null) {
                player.keyReleased(e);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (player != null) {
                player.keyPressed(e);
            }

            int key = e.getKeyCode();

            if (key == KeyEvent.VK_SPACE && inGame) {
                int x = player.getX();
                int y = player.getY();

                if (shots.size() < 5) {
                    shots.add(new Shot(x, y));
                }
            }
        }
    }
}