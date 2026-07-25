package gdd.sprite;

import static gdd.Global.*;
import java.util.Random;
import javax.swing.ImageIcon;

public class Enemy extends Sprite {

    // private Bomb bomb;

    public Enemy(int x, int y) {

        initEnemy(x, y);
    }

    private void initEnemy(int x, int y) {

        this.x = x;
        this.y = y;

        // bomb = new Bomb(x, y);

        var ii = new ImageIcon(IMG_ENEMY);

        // Scale the image to use the global scaling factor
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    public void act(int direction) {

        this.x += direction;
    }

    @Override
    public void act() {
        // not used — Enemy movement is driven by act(int direction) instead.
        // This empty method exists only to satisfy Sprite's abstract contract.
    }

    protected int hitPoints = 1; // default = dies in one hit, matches current game behavior

    public void setHitPoints(int hitPoints) {
        this.hitPoints = hitPoints;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public boolean hit() {
        hitPoints--;
        if (hitPoints <= 0) {
            setDying(true);
            return true;  // enemy has died
        }
        return false;      // enemy survived this hit
    }

    private static final Random bombRandomizer = new Random();
    protected int bombChanceRange = 300; // ~1-in-300 odds per enemy, per frame (subclasses may override)

    /**
     * Rolls the odds for this enemy to drop a bomb this frame.
     * Returns a new Bomb positioned at this enemy's location, or null if it didn't roll.
     */
    public Bomb maybeDropBomb() {
        if (!isVisible()) {
            return null;
        }
       if (bombRandomizer.nextInt(bombChanceRange) == 0) {
            return new Bomb(this.x, this.y);
        }
        return null;
    }

   public class Bomb extends Sprite {

        private static final int SPEED = 4;

        public Bomb(int x, int y) {
            this.x = x;
            this.y = y;

            var ii = new ImageIcon(IMG_BOMB);
            var scaledImage = ii.getImage().getScaledInstance(
                    ii.getIconWidth() * SCALE_FACTOR,
                    ii.getIconHeight() * SCALE_FACTOR,
                    java.awt.Image.SCALE_SMOOTH);
            setImage(scaledImage);
        }

        @Override
        public void act() {
            this.x -= SPEED; // fires leftward toward the player, mirroring Shot's rightward flight
        }
    }
/* 
    public Bomb getBomb() {

        return bomb;
    }

    public class Bomb extends Sprite {

        private boolean destroyed;

        public Bomb(int x, int y) {

            initBomb(x, y);
        }

        private void initBomb(int x, int y) {

            setDestroyed(true);

            this.x = x;
            this.y = y;

            var bombImg = "src/images/bomb.png";
            var ii = new ImageIcon(bombImg);
            setImage(ii.getImage());
        }

        public void setDestroyed(boolean destroyed) {

            this.destroyed = destroyed;
        }

        public boolean isDestroyed() {

            return destroyed;
        }
    }
*/
}
