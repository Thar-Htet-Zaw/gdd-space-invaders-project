package gdd.sprite;

public class Alien1 extends Enemy {

    public Alien1(int x, int y) {
        super(x, y);
    }

    @Override
    public void act(int direction) {
        // Move towards the left side of the screen
        this.x -= 2;
    }

    /*
     * NOTE: the original template had an inner Bomb class + getBomb() here,
     * and a dead initEnemy(int, int) method that was never called (the
     * constructor already had "// initEnemy(x, y);" commented out).
     * Neither was ever used by any active code path.
     *
     * Bomb-dropping has since been moved up to Enemy (see Enemy.java —
     * maybeDropBomb() + the nested Bomb class), so every enemy type
     * (Alien1/Alien2/Alien3) shares one bomb implementation instead of
     * each having its own. Kept here as a comment for reference/history
     * rather than deleted outright.
     *
     * private Bomb bomb;
     *
     * private void initEnemy(int x, int y) {
     *     this.x = x;
     *     this.y = y;
     *     bomb = new Bomb(x, y);
     *     var ii = new ImageIcon(IMG_ENEMY);
     *     var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * SCALE_FACTOR,
     *             ii.getIconHeight() * SCALE_FACTOR,
     *             java.awt.Image.SCALE_SMOOTH);
     *     setImage(scaledImage);
     * }
     *
     * public Bomb getBomb() {
     *     return bomb;
     * }
     *
     * public class Bomb extends Sprite {
     *     private boolean destroyed;
     *
     *     public Bomb(int x, int y) {
     *         initBomb(x, y);
     *     }
     *
     *     private void initBomb(int x, int y) {
     *         setDestroyed(true);
     *         this.x = x;
     *         this.y = y;
     *         var bombImg = "src/images/bomb.png";
     *         var ii = new ImageIcon(bombImg);
     *         setImage(ii.getImage());
     *     }
     *
     *     public void setDestroyed(boolean destroyed) {
     *         this.destroyed = destroyed;
     *     }
     *
     *     public boolean isDestroyed() {
     *         return destroyed;
     *     }
     *
     *     @Override
     *     public void act() {
     *         // unimplemented in the original template
     *     }
     * }
     */
}