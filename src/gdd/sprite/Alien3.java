package gdd.sprite;

import static gdd.Global.*;

public class Alien3 extends Enemy {

    public Alien3(int x, int y) {
        super(x, y);
        loadImage(IMG_ENEMY_JUGGERNAUT); // tanky — bulky armored sprite
        setHitPoints(5); // takes 5 hits to kill instead of 1
    }

    @Override
    public void act(int direction) {
        this.x -= 1; // slower than Alien1's -2, since it's tankier, not faster
    }
}