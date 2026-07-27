package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;

public class Player extends Sprite {

    private static final int START_X = 50; 
    private static final int START_Y = 250; // Hardcoded middle Y coordinate
    private int currentSpeed = 4;
    private int maxShots = 4; // matches the game's existing base simultaneous-shot cap
    
    private int health = 5;

    public Player() {
        initPlayer();
    }

    private void initPlayer() {
        ImageIcon ii = new ImageIcon(IMG_PLAYER);

        Image scaledImage = ii.getImage().getScaledInstance(
                PLAYER_WIDTH,
                PLAYER_HEIGHT,
                Image.SCALE_SMOOTH);

        setImage(scaledImage);
        setVisible(true);
        setX(START_X);
        setY(START_Y);
    }

    public int getSpeed() {
        return currentSpeed;
    }

   public int setSpeed(int speed) {
        if (speed < 1) {
            speed = 1;
        }
        this.currentSpeed = speed;
        return currentSpeed;
    }

    public int getMaxShots() {
        return maxShots;
    }

    public int setMaxShots(int maxShots) {
        if (maxShots < 1) {
            maxShots = 1;
        }
        this.maxShots = maxShots;
        return maxShots;
    }

    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = health;
    }

    public void hit() {
        health--;
        if (health <= 0) {
            setDying(true);
        }
    }

    @Override
    public void act() {
        x += dx;
        y += dy;

        // --- LEFT & RIGHT BORDER BOUNDS ---
        if (x < 0) {
            x = 0; // Stop player from leaving left edge
        }
        
        if (x > BOARD_WIDTH - PLAYER_WIDTH) {
            x = BOARD_WIDTH - PLAYER_WIDTH; // Stop player from leaving right edge
        }

        // --- UP & DOWN BORDER BOUNDS ---
        if (y < 0) {
            y = 0;
        }

        if (y > BOARD_HEIGHT - PLAYER_HEIGHT - 40) {
            y = BOARD_HEIGHT - PLAYER_HEIGHT - 40;
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_UP) {
            dy = -currentSpeed;
        }
        if (key == KeyEvent.VK_DOWN) {
            dy = currentSpeed;
        }
        if (key == KeyEvent.VK_LEFT) {
            dx = -currentSpeed;
        }
        if (key == KeyEvent.VK_RIGHT) {
            dx = currentSpeed;
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
            dy = 0;
        }
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
            dx = 0;
        }
    }
}