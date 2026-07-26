package gdd.sprite;

import static gdd.Global.*;

public class Alien2 extends Enemy {

    private final int baseY;
    private int zigzagFrame = 0;

    private static final int AMPLITUDE = 60;      // how far up/down it swings, in pixels
    private static final double FREQUENCY = 0.05;  // how fast it oscillates

    public Alien2(int x, int y) {
        super(x, y); // Enemy's constructor already sets position + scaled sprite image
        loadImage(IMG_ENEMY_WRAITH); // zigzagging — ghostly sprite
        this.baseY = y;
    }

    @Override
    public void act(int direction) {
        this.x -= 2; // same horizontal speed as Alien1

        zigzagFrame++;
        this.y = baseY + (int) (AMPLITUDE * Math.sin(zigzagFrame * FREQUENCY));
    }
}