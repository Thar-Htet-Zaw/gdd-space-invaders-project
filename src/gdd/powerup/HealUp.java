package gdd.powerup;

import gdd.sprite.Player;
import javax.swing.ImageIcon;

public class HealUp extends PowerUp {

    public HealUp(int x, int y) {
        super(x, y);
        initHealUp();
    }

    private void initHealUp() {
        var ii = new ImageIcon("src/images/healing_object.png");
        var scaledImg = ii.getImage().getScaledInstance(32, 32, java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImg);
    }

    @Override
    public void act() {
        this.x -= 2; // Moves left across the screen toward player
    }

    @Override
    public void upgrade(Player player) {
        // Increases health by 1, up to maximum cap of 5
        if (player.getHealth() < 5) {
            player.setHealth(player.getHealth() + 1);
        }
        die();
    }
}