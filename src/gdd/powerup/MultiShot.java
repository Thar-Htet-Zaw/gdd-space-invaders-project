package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import javax.swing.ImageIcon;

public class MultiShot extends PowerUp {

    private static final int ICON_SIZE = 32; // fixed on-screen size, regardless of source image resolution

    public MultiShot(int x, int y) {
        super(x, y);
        // Set image
        ImageIcon ii = new ImageIcon(IMG_POWERUP_MULTISHOT);
        var scaledImage = ii.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    public void act() {
        // Power-ups move left across the screen, same as SpeedUp
        this.x -= 2;
    }

    public void upgrade(Player player) {
        // The actual visible effect: switches firing from 1 straight bullet per
        // press to 2 parallel bullets per press (see Player.hasMultiShot()).
        player.activateMultiShot();

        // Also grants extra on-screen shot capacity, matching the assignment's
        // literal "4 steps" wording -- this is a secondary bonus (lets more shots
        // exist in flight at once) and is NOT what makes multi-shot visible; the
        // parallel-bullet pattern above is.
        player.setMaxShots(player.getMaxShots() + 4);
        this.die(); // Remove the power-up after use
    }
}