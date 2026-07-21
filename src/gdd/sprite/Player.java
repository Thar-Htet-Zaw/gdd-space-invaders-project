package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;

public class Player extends Sprite {

    private static final int START_X = 50; 
    private static final int START_Y = 250; // Hardcoded middle Y coordinate
    private int currentSpeed = 4;

    public Player() {
        initPlayer();
    }

    private void initPlayer() {
        ImageIcon ii = new ImageIcon(IMG_PLAYER);

        int baseWidth = ii.getIconWidth() * SCALE_FACTOR;
        int baseHeight = ii.getIconHeight() * SCALE_FACTOR;

        Image scaledImage = ii.getImage().getScaledInstance(
                baseWidth > 0 ? baseWidth : 32,
                baseHeight > 0 ? baseHeight : 32,
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

    // In Player.java — exactly like original, just adding vertical velocity (dy)
    @Override
    public void act() {
        x += dx; // Original horizontal movement[cite: 13]
        y += dy; // Added vertical movement

        // Simple boundary checks matching original style[cite: 13]
        if (y <= 2) {
            y = 2;
        }
        if (y >= BOARD_HEIGHT - 60) {
            y = BOARD_HEIGHT - 60;
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