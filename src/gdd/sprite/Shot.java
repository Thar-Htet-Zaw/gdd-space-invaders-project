package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;

public class Shot extends Sprite {

    private static final int H_SPACE = 20;
    private static final int V_SPACE = 1;

    // Fixed size, regardless of source image resolution -- same fix applied to
    // SpeedUp/MultiShot in Entry 10, applied here pre-emptively so swapping in a
    // new (likely high-res, AI-generated) shot image is safe.
    //
    // IMPORTANT: these are PRE-rotation dimensions. drawShot()/drawShots() rotate
    // this image 90 degrees at draw time ("rotate so the vertical line becomes
    // horizontal"), so the source art must be authored as a TALL, NARROW vertical
    // bolt -- narrow width, tall height -- for it to appear as a horizontal shot
    // on screen after rotation.
    private static final int SHOT_WIDTH = 10;
    private static final int SHOT_HEIGHT = 28;

    public Shot() {
    }

    public Shot(int x, int y) {

        initShot(x, y);
    }

    private void initShot(int x, int y) {

        var ii = new ImageIcon(IMG_SHOT);

        // Scale to a fixed on-screen size, not the source image's native resolution
        var scaledImage = ii.getImage().getScaledInstance(
                SHOT_WIDTH,
                SHOT_HEIGHT,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);

        // Assuming player base height/width are around 32-40px after scaling:
        // Position shot at the front (right edge) and middle vertical center of the player ship
        int offsetRight = 32; // Moves shot to the right tip of ship
        int offsetCenterY = 8; // Adjusts shot vertically to fire from middle

        setX(x + offsetRight);
        setY(y + offsetCenterY);
    }

    @Override
    public void act() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'act'");
    }
}