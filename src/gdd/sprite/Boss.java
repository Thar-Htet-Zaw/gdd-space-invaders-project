package gdd.sprite;

import static gdd.Global.*;
import java.util.Random;
import javax.swing.ImageIcon;

public class Boss extends Enemy {

    // Fixed on-screen size, regardless of the source image's resolution — same lesson
    // learned from the power-up icon bug: never scale to the image's own dimensions.
    // Sized so the boss's head sits just below the boss HP bar (bottom edge at y=95)
    // and its base sits just above the bottom of the screen (BOARD_HEIGHT=700).
    public static final int WIDTH = 585;
    public static final int HEIGHT = 585;

    public static final int LASER_HEIGHT = 40; // vertical thickness of the laser beam band

    private final int maxHitPoints;
    private boolean phaseTwo = false;
    private int baseY;

    private final int engageX; // the boss never advances past this X — stays locked in place
    private double swayFrame = 0;

    private static final int APPROACH_SPEED = 2; // speed while first entering, before reaching engageX
    private static final int PHASE1_BOMB_CHANCE_RANGE = 150;
    private static final int PHASE2_BOMB_CHANCE_RANGE = 60;

    private static final Random minionRandomizer = new Random();
    private static final int MINION_CHANCE_RANGE = 400; // ~1-in-400 odds per frame, phase 2 only

    // --- Laser telegraph attack (phase 2 only) ---
    private enum AttackState { IDLE, CHARGING_LASER, FIRING_LASER }
    private AttackState attackState = AttackState.IDLE;
    private int attackTimer = 0;
    private int laserTargetY = 0;
    private boolean laserHasHitPlayer = false;

    private static final int LASER_CHARGE_FRAMES = 75;    // ~1.25s telegraph warning
    private static final int LASER_FIRE_FRAMES = 20;      // ~0.33s active beam
    private static final int LASER_COOLDOWN_FRAMES = 240; // ~4s between laser attacks

    public Boss(int x, int y, int startingHitPoints) {
        super(x, y);
        setHitPoints(startingHitPoints);
        this.maxHitPoints = startingHitPoints;
        this.bombChanceRange = PHASE1_BOMB_CHANCE_RANGE;
        this.engageX = BOARD_WIDTH - WIDTH - 3; // small right-edge margin (shifted further right per feedback)
        this.baseY = y + 5; // small buffer below the boss HP bar, since there's little vertical room to spare
        // Override the default enemy-sized sprite with a larger, boss-appropriate size.
        var ii = new ImageIcon(IMG_BOSS);
        var scaledImage = ii.getImage().getScaledInstance(WIDTH, HEIGHT, java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    @Override
    public boolean hit() {
        boolean died = super.hit();

        if (!phaseTwo && !died && getHitPoints() <= maxHitPoints / 2) {
            enterPhaseTwo();
        }

        return died;
    }

    private void enterPhaseTwo() {
        phaseTwo = true;
        this.bombChanceRange = PHASE2_BOMB_CHANCE_RANGE;
    }

    @Override
    public void act(int direction) {
        if (this.x > engageX) {
            // Still approaching — moves in like a regular enemy, then locks in place
            this.x -= APPROACH_SPEED;
            if (this.x < engageX) {
                this.x = engageX;
            }
        } else {
            // In position — slow, smooth elliptical drift (not a fast shake). x/y use
            // different frequencies + a phase offset so it traces a gentle loop rather
            // than snapping side to side.
            swayFrame += 0.015;
            this.x = engageX + (int) (10 * Math.sin(swayFrame));
            this.y = baseY + (int) (6 * Math.sin(swayFrame * 1.3 + 1.0));
        }
    }

    public boolean isPhaseTwo() {
        return phaseTwo;
    }

    public boolean hasEngaged() {
        return this.x <= engageX;
    }

    /**
     * Rolls the odds for the boss to spawn a minion this frame. Only active once
     * enraged (phase 2) and only once the boss has reached its fighting position.
     * Returns "Alien1"/"Alien2"/"Alien3", or null if nothing spawned this frame.
     */
    public String maybeSpawnMinionType() {
        if (!phaseTwo || !hasEngaged()) {
            return null;
        }
        if (minionRandomizer.nextInt(MINION_CHANCE_RANGE) == 0) {
            int roll = minionRandomizer.nextInt(3);
            return roll == 0 ? "Alien1" : (roll == 1 ? "Alien2" : "Alien3");
        }
        return null;
    }

    /**
     * Drives the laser telegraph/fire/cooldown cycle. Call once per frame.
     * Only active once enraged (phase 2) and once the boss has reached position.
     */
    public void updateAttack(int playerY) {
        if (!phaseTwo || !hasEngaged()) {
            return;
        }

        switch (attackState) {
            case IDLE:
                attackTimer++;
                if (attackTimer >= LASER_COOLDOWN_FRAMES) {
                    attackState = AttackState.CHARGING_LASER;
                    attackTimer = 0;
                    laserTargetY = playerY; // locks onto the player's row at the moment of charge
                    laserHasHitPlayer = false;
                }
                break;
            case CHARGING_LASER:
                attackTimer++;
                if (attackTimer >= LASER_CHARGE_FRAMES) {
                    attackState = AttackState.FIRING_LASER;
                    attackTimer = 0;
                }
                break;
            case FIRING_LASER:
                attackTimer++;
                if (attackTimer >= LASER_FIRE_FRAMES) {
                    attackState = AttackState.IDLE;
                    attackTimer = 0;
                }
                break;
        }
    }

    public boolean isChargingLaser() {
        return attackState == AttackState.CHARGING_LASER;
    }

    public boolean isFiringLaser() {
        return attackState == AttackState.FIRING_LASER;
    }

    public int getLaserTargetY() {
        return laserTargetY;
    }

    /** Returns true exactly once per beam if the player is caught in the laser's path. */
    public boolean consumeLaserHit() {
        if (attackState == AttackState.FIRING_LASER && !laserHasHitPlayer) {
            laserHasHitPlayer = true;
            return true;
        }
        return false;
    }

    @Override
    public Bomb maybeDropBomb() {
        Bomb bomb = super.maybeDropBomb();
        if (bomb != null) {
            bomb.setX(this.x + WIDTH / 2);
            bomb.setY(this.y + HEIGHT / 2);
        }
        return bomb;
    }
}