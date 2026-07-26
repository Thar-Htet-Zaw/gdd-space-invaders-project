package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;

public class Explosion extends Sprite {

    // Fixed on-screen base size, regardless of source image resolution -- prevents
    // a high-res (e.g. AI-generated) explosion image from rendering at native
    // size * SCALE_FACTOR, which can be thousands of pixels wide. Scene1/Scene2's
    // grow-and-fade animation scales up from this base size, so this is
    // effectively the burst's starting size.
    private static final int EXPLOSION_SIZE = 48;

    public Explosion(int x, int y) {

        initExplosion(x, y);
    }

    private void initExplosion(int x, int y) {

        this.x = x;
        this.y = y;

        var ii = new ImageIcon(IMG_EXPLOSION);

        // Scale to a fixed on-screen size, not the source image's native resolution
        var scaledImage = ii.getImage().getScaledInstance(EXPLOSION_SIZE,
                EXPLOSION_SIZE,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    public void act(int direction) {

        // this.x += direction;
    }

    @Override
    public void act() {
        // Explosions don't move on their own — required only to satisfy Sprite's contract.
    }


}