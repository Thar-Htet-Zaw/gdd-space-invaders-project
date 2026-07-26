package gdd.sprite;

import static gdd.Global.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    private final int engageX; // the boss never advances past this X — stays locked in place
    private int baseY;
    private double swayFrame = 0;

    private static final int APPROACH_SPEED = 2; // speed while first entering, before reaching engageX

    private static final Random minionRandomizer = new Random();
    private static final int MINION_CHANCE_RANGE = 400; // ~1-in-400 odds per frame, phase 2 only

    // --- Unified danger-zone attack system ---
    // All 4 attacks share one telegraph -> fire -> cooldown cycle, just with
    // different zone shapes/positions. LASER is the only attack available in
    // phase 1; phase 2 unlocks the other 3 and rotates randomly between all 4.
    public enum AttackType { LASER, TENTACLE_SLAM, EYE_BEAM_BARRAGE, SPORE_SWARM }
    private enum AttackState { IDLE, CHARGING, FIRING }

    private AttackState attackState = AttackState.IDLE;
    private AttackType currentAttack = AttackType.LASER;
    private int attackTimer = 0;
    private boolean hasHitPlayer = false;
    private final List<int[]> activeZones = new ArrayList<>(); // each: {x, y, width, height}

    private static final Random attackRandomizer = new Random();
    private static final int CHARGE_FRAMES = 75;       // ~1.25s telegraph warning
    private static final int FIRE_FRAMES = 20;         // ~0.33s active damage window
    private static final int COOLDOWN_PHASE1 = 240;    // ~4s between attacks
    private static final int COOLDOWN_PHASE2 = 150;    // ~2.5s once enraged — faster pace
    private static final int COOLDOWN_PHASE3 = 100;    // ~1.7s once desperate (<=20% HP) — relentless

    private static final int INITIAL_GRACE_FRAMES = 90; // ~1.5s buffer after engaging before the very first attack
    private static final int COMBO_GAP = 45;              // ~0.75s gap between two chained attacks
    private static final int MAX_COMBO_CHAIN = 1;         // at most 1 extra attack chained on, then a full cooldown

    private AttackType lastAttack = null;   // used so the same attack never fires twice in a row
    private boolean hasAttackedOnce = false; // gates the one-time initial grace period
    private boolean comboPending = false;    // true = next attack uses the short COMBO_GAP, not a full cooldown
    private int comboChainCount = 0;
    private boolean phaseThree = false;      // "desperation" — HP at/below 20%, faster + always combos

    public Boss(int x, int y, int startingHitPoints) {
        super(x, y);
        setHitPoints(startingHitPoints);
        this.maxHitPoints = startingHitPoints;
        this.engageX = BOARD_WIDTH - WIDTH - 3; // small right-edge margin
        this.baseY = y + 5; // small buffer below the boss HP bar, since there's little vertical room to spare

        // Override the default enemy-sized sprite with a larger, boss-appropriate size.
        var ii = new ImageIcon(IMG_BOSS);
        var scaledImage = ii.getImage().getScaledInstance(WIDTH, HEIGHT, java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    @Override
    public boolean hit() {
        if (!hasEngaged()) {
            // Still flying in — not fighting yet, so it can't be damaged. Prevents the
            // player from melting a big chunk of its health before the fight even starts.
            return false;
        }

        boolean died = super.hit();

        if (!phaseTwo && !died && getHitPoints() <= maxHitPoints / 2) {
            phaseTwo = true;
        }
        if (!phaseThree && !died && getHitPoints() <= maxHitPoints * 0.2) {
            phaseThree = true;
        }

        return died;
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
            // In position — very subtle drift now (the tentacle-ripple animation in
            // Scene2's drawBossAnimated() carries most of the "alive" feeling; this is
            // just a small residual float so the whole body isn't perfectly frozen).
            swayFrame += 0.01;
            this.x = engageX + (int) (4 * Math.sin(swayFrame));
            this.y = baseY + (int) (3 * Math.sin(swayFrame * 1.3 + 1.0));
        }
    }

    public boolean isPhaseTwo() {
        return phaseTwo;
    }

    public boolean isPhaseThree() {
        return phaseThree;
    }

    public boolean hasEngaged() {
        return this.x <= engageX;
    }

    /** Bombs are fully replaced by the danger-zone attack system below. */
    @Override
    public Bomb maybeDropBomb() {
        return null;
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
        int chanceRange = phaseThree ? MINION_CHANCE_RANGE / 2 : MINION_CHANCE_RANGE; // phase 3: roughly double the odds
        if (minionRandomizer.nextInt(chanceRange) == 0) {
            int roll = minionRandomizer.nextInt(3);
            return roll == 0 ? "Alien1" : (roll == 1 ? "Alien2" : "Alien3");
        }
        return null;
    }

    /**
     * Drives the telegraph/fire/cooldown cycle for whichever attack is active.
     * Call once per frame with the player's current position.
     */
    public void updateAttack(int playerX, int playerY) {
        if (!hasEngaged()) {
            return;
        }

        switch (attackState) {
            case IDLE:
                attackTimer++;
                int cooldown = comboPending ? COMBO_GAP : baseCooldown();
                if (!hasAttackedOnce) {
                    // Give the player a moment to get their bearings before the boss's
                    // very first attack, regardless of how short phase 1's cooldown is.
                    cooldown = Math.max(cooldown, INITIAL_GRACE_FRAMES);
                }
                if (attackTimer >= cooldown) {
                    startCharging(playerX, playerY);
                }
                break;
            case CHARGING:
                attackTimer++;
                if (attackTimer >= CHARGE_FRAMES) {
                    attackState = AttackState.FIRING;
                    attackTimer = 0;
                }
                break;
            case FIRING:
                attackTimer++;
                if (attackTimer >= FIRE_FRAMES) {
                    // Decide whether the NEXT attack chains in quickly (a "combo") or
                    // waits out a full cooldown. Phase 3 always chains (relentless);
                    // phase 2 chains sometimes; phase 1 never chains. Capped at
                    // MAX_COMBO_CHAIN so combos can't stack indefinitely and become unfair.
                    if (phaseThree && comboChainCount < MAX_COMBO_CHAIN) {
                        comboPending = true;
                        comboChainCount++;
                    } else if (phaseTwo && !phaseThree && comboChainCount < MAX_COMBO_CHAIN
                            && attackRandomizer.nextInt(100) < 35) {
                        comboPending = true;
                        comboChainCount++;
                    } else {
                        comboPending = false;
                        comboChainCount = 0;
                    }

                    attackState = AttackState.IDLE;
                    attackTimer = 0;
                    activeZones.clear();
                }
                break;
        }
    }

    private int baseCooldown() {
        if (phaseThree) {
            return COOLDOWN_PHASE3;
        }
        if (phaseTwo) {
            return COOLDOWN_PHASE2;
        }
        return COOLDOWN_PHASE1;
    }

    private void startCharging(int playerX, int playerY) {
        attackState = AttackState.CHARGING;
        attackTimer = 0;
        hasHitPlayer = false;
        hasAttackedOnce = true;
        activeZones.clear();

        if (!phaseTwo) {
            currentAttack = AttackType.LASER; // phase 1: laser only
        } else {
            // Pick uniformly among all attacks EXCEPT whichever one just fired, so the
            // same attack never happens twice in a row — keeps the pattern feeling
            // designed rather than a dice roll that can streak on one attack.
            AttackType[] all = AttackType.values();
            List<AttackType> candidates = new ArrayList<>();
            for (AttackType type : all) {
                if (type != lastAttack) {
                    candidates.add(type);
                }
            }
            currentAttack = candidates.get(attackRandomizer.nextInt(candidates.size()));
        }
        lastAttack = currentAttack;

        switch (currentAttack) {
            case LASER:
                activeZones.add(new int[]{0, playerY - LASER_HEIGHT / 2, BOARD_WIDTH, LASER_HEIGHT});
                break;

            case TENTACLE_SLAM: {
                // Close-range zone reaching out from the boss — punishes staying near it
                int slamWidth = 220;
                int slamX = Math.max(0, engageX - (slamWidth - WIDTH));
                activeZones.add(new int[]{slamX, 0, slamWidth, BOARD_HEIGHT});
                break;
            }

            case EYE_BEAM_BARRAGE: {
                // 3 of 5 horizontal bands light up, leaving gaps to dodge into
                int bandHeight = BOARD_HEIGHT / 5;
                List<Integer> bandIndices = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    bandIndices.add(i);
                }
                Collections.shuffle(bandIndices, attackRandomizer);
                for (int i = 0; i < 3; i++) {
                    int idx = bandIndices.get(i);
                    activeZones.add(new int[]{0, idx * bandHeight, BOARD_WIDTH, bandHeight - 5});
                }
                break;
            }

            case SPORE_SWARM: {
                // Cluster of small patches scattered near the player's row
                for (int i = 0; i < 4; i++) {
                    int zoneSize = 55;
                    int zx = 150 + attackRandomizer.nextInt(450);
                    int zy = Math.max(20, Math.min(BOARD_HEIGHT - zoneSize - 20,
                            playerY - 60 + attackRandomizer.nextInt(140)));
                    activeZones.add(new int[]{zx, zy, zoneSize, zoneSize});
                }
                break;
            }
        }
    }

    public boolean isCharging() {
        return attackState == AttackState.CHARGING;
    }

    public boolean isFiring() {
        return attackState == AttackState.FIRING;
    }

    public AttackType getCurrentAttack() {
        return currentAttack;
    }

    public List<int[]> getActiveZones() {
        return activeZones;
    }

    /** Returns true exactly once per attack if the player overlaps any active zone. */
    public boolean consumeHitIfPlayerInZone(int playerX, int playerY, int playerWidth, int playerHeight) {
        if (attackState != AttackState.FIRING || hasHitPlayer) {
            return false;
        }
        for (int[] zone : activeZones) {
            if (rectsOverlap(playerX, playerY, playerWidth, playerHeight,
                    zone[0], zone[1], zone[2], zone[3])) {
                hasHitPlayer = true;
                return true;
            }
        }
        return false;
    }

    private boolean rectsOverlap(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }
}