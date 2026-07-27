package gdd.sprite;

import static gdd.Global.*;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
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

    // TEMPORARY placeholder inset -- WIDTH/HEIGHT above is the full padded canvas,
    // not the visible creature's silhouette. Using the full canvas as the hitbox
    // means shots can register as "hits" (and spawn impact explosions) while still
    // in empty transparent space nowhere near the actual art. These four numbers
    // should be replaced with the real bounding box of the non-transparent pixels
    // in boss.png / boss_phase2.png once available -- placeholder assumes the body
    // sits roughly centered, occupying ~70% of the canvas.
    private static final int HITBOX_INSET_LEFT = 90;
    private static final int HITBOX_INSET_TOP = 90;
    private static final int HITBOX_WIDTH = 400;
    private static final int HITBOX_HEIGHT = 400;

    public int getHitboxX() {
        return this.x + HITBOX_INSET_LEFT;
    }

    public int getHitboxY() {
        return this.y + HITBOX_INSET_TOP;
    }

    public int getHitboxWidth() {
        return HITBOX_WIDTH;
    }

    public int getHitboxHeight() {
        return HITBOX_HEIGHT;
    }

    public static final int LASER_HEIGHT = 40; // vertical thickness of the laser beam band

    private final int maxHitPoints;
    private boolean phaseTwo = false;

    private final int engageX; // the boss never advances past this X — stays locked in place
    private int baseY;
    private double swayFrame = 0;

    private static final int APPROACH_SPEED = 2; // speed while first entering, before reaching engageX

    private static final Random minionRandomizer = new Random();

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
    private static final int CHARGE_FRAMES = 45;       // ~1s telegraph warning (was ~1.25s)
    private static final int FIRE_FRAMES = 20;         // ~0.33s active damage window
    private static final int COOLDOWN_PHASE1 = 80;    // ~2.2s between attacks (was ~4s)
    private static final int COOLDOWN_PHASE2 = 60;     // ~1.5s once enraged (was ~2.5s)
    private static final int COOLDOWN_PHASE3 = 40;     // ~1s once desperate (<=20% HP) — relentless

    private static final int INITIAL_GRACE_FRAMES = 45; // ~0.75s buffer after engaging (was ~1.5s)
    private static final int COMBO_GAP = 45;              // ~0.75s gap between two chained attacks
    private static final int MAX_COMBO_CHAIN = 1;         // at most 1 extra attack chained on, then a full cooldown

    private AttackType lastAttack = null;   // used so the same attack never fires twice in a row
    private boolean hasAttackedOnce = false; // gates the one-time initial grace period
    private boolean comboPending = false;    // true = next attack uses the short COMBO_GAP, not a full cooldown
    private int comboChainCount = 0;
    private boolean phaseThree = false;      // "desperation" — HP at/below 20%, faster + always combos

    private final Image phase1Image;
    private final Image phase2Image;

    // Minecraft-style "flash red when hit" -- counts down each real game frame
    // (ticked in act(), not in draw code, so its timing is tied to game logic
    // rather than however many times Swing happens to call paintComponent).
    private static final int DAMAGE_FLASH_DURATION = 10; // ~170ms at 60fps
    private int damageFlashTimer = 0;

    public Boss(int x, int y, int startingHitPoints) {
        super(x, y);
        setHitPoints(startingHitPoints);
        this.maxHitPoints = startingHitPoints;
        this.engageX = BOARD_WIDTH - (int) (WIDTH * 0.7); // ~30% of the boss's width extends past the right edge
        this.baseY = y + 5; // small buffer below the boss HP bar, since there's little vertical room to spare

        // Both forms are pre-scaled once up front (not re-scaled every phase switch).
        // Uses the same alpha-safe BufferedImage/Graphics2D scaling as Explosion --
        // Image.getScaledInstance() under-represents opacity on images that are mostly
        // transparent with sparse bright detail, which can read as a faint, nearly
        // invisible result. Overrides the default enemy-sized sprite with a larger,
        // boss-appropriate size either way.
        phase1Image = loadBossImage(IMG_BOSS);
        phase2Image = loadBossImage(IMG_BOSS_PHASE2);
        setImage(phase1Image);
    }

    private static Image loadBossImage(String path) {
        var ii = new ImageIcon(path);
        BufferedImage scaled = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        // ii.getImage() is already fully loaded by this point (ImageIcon blocks until
        // loaded internally), so a null ImageObserver here is safe.
        g2d.drawImage(ii.getImage(), 0, 0, WIDTH, HEIGHT, null);
        g2d.dispose();
        return scaled;
    }

    @Override
    public boolean hit() {
        if (!hasEngaged()) {
            // Still flying in — not fighting yet, so it can't be damaged. Prevents the
            // player from melting a big chunk of its health before the fight even starts.
            return false;
        }

        boolean died = super.hit();
        damageFlashTimer = DAMAGE_FLASH_DURATION; // flash red on every hit that actually lands

        if (!phaseTwo && !died && getHitPoints() <= maxHitPoints / 2) {
            phaseTwo = true;
            setImage(phase2Image); // swap to the enraged form the instant phase 2 triggers
        }
        if (!phaseThree && !died && getHitPoints() <= maxHitPoints * 0.2) {
            phaseThree = true;
        }

        return died;
    }

    @Override
    public void act(int direction) {
        if (damageFlashTimer > 0) {
            damageFlashTimer--;
        }

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

    /** 1.0 = just hit (full red), fading linearly to 0.0 = no flash. */
    public float getDamageFlashIntensity() {
        return damageFlashTimer / (float) DAMAGE_FLASH_DURATION;
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

    private static final int MINION_CHANCE_PHASE1 = 200; // ~1-in-200 odds per frame (~3.4s average)
    private static final int MINION_CHANCE_PHASE2 = 110; // ~1-in-110 (~1.9s average)
    private static final int MINION_CHANCE_PHASE3 = 65;  // ~1-in-65 (~1.1s average) — relentless

    /**
     * Rolls the odds for the boss to spawn minions this frame. Active as soon as it
     * engages (phase 1 included, not just once enraged), and spawns MORE minions at
     * once, MORE often, as it progresses through phases. Returns a list of enemy
     * type strings ("Alien1"/"Alien2"/"Alien3"), empty if nothing spawned this frame.
     */
    public List<String> maybeSpawnMinionTypes() {
        if (!hasEngaged()) {
            return new ArrayList<>();
        }

        int chanceRange = phaseThree ? MINION_CHANCE_PHASE3 : (phaseTwo ? MINION_CHANCE_PHASE2 : MINION_CHANCE_PHASE1);
        if (minionRandomizer.nextInt(chanceRange) != 0) {
            return new ArrayList<>();
        }

        // How many spawn together scales with phase -- more chaotic once enraged.
        int count = phaseThree ? 3 : (phaseTwo ? 2 : 1);
        List<String> types = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int roll = minionRandomizer.nextInt(3);
            types.add(roll == 0 ? "Alien1" : (roll == 1 ? "Alien2" : "Alien3"));
        }
        return types;
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
            // Phase 1: alternates between the two "starter" attacks, so the boss is
            // never just passively sitting there taking hits before it enrages.
            // Eye Beam Barrage and Spore Swarm stay exclusive to phase 2+, so there's
            // still a real escalation once it does enrage.
            AttackType[] phase1Pool = {AttackType.LASER, AttackType.TENTACLE_SLAM};
            List<AttackType> candidates = new ArrayList<>();
            for (AttackType type : phase1Pool) {
                if (type != lastAttack) {
                    candidates.add(type);
                }
            }
            if (candidates.isEmpty()) {
                candidates.add(phase1Pool[0]); // safety fallback, shouldn't normally trigger
            }
            currentAttack = candidates.get(attackRandomizer.nextInt(candidates.size()));
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
                // Targets the player's exact position at the moment the attack starts
                // charging (like a ground-slam) -- guarantees it's always a real threat,
                // rather than a fixed zone the player could just avoid by staying away
                // from it. Clamped so the zone never extends past the screen edges.
                int slamSize = 140;
                int slamX = Math.max(0, Math.min(BOARD_WIDTH - slamSize, playerX - slamSize / 2));
                int slamY = Math.max(0, Math.min(BOARD_HEIGHT - slamSize, playerY - slamSize / 2));
                activeZones.add(new int[]{slamX, slamY, slamSize, slamSize});
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
                // Cluster of small patches scattered around the player's position at
                // the moment the attack starts charging -- both X and Y are now tied
                // to the player, so the cluster is always a genuine dodge challenge
                // instead of sometimes landing nowhere near them.
                for (int i = 0; i < 4; i++) {
                    int zoneSize = 55;
                    int offsetX = -70 + attackRandomizer.nextInt(140);
                    int offsetY = -70 + attackRandomizer.nextInt(140);
                    int zx = Math.max(0, Math.min(BOARD_WIDTH - zoneSize, playerX + offsetX));
                    int zy = Math.max(0, Math.min(BOARD_HEIGHT - zoneSize, playerY + offsetY));
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