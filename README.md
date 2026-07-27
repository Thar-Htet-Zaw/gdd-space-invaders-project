# GDD Space Invaders Project Starter

# Void Runner

A 2D space shooter game built in Java (Swing + javax.sound), developed as a GDD project.

## Team Members

| ID | Name |
|---|---|
| 6712157 | Han Min Khant Oo |
| 6712149 | Thar Htet Zaw |
| 6712096 | Phone Khant Aung |

## Game Overview

Void Runner is a 2D space shooter where the player pilots a small spaceship, fights off waves of alien enemies, collects power-ups, and finally faces a giant boss in a two-stage campaign.

The game has three main screens ("scenes"), each handled by its own class:

- **Title Scene** — The start screen. Shows the game logo, plays the title music, and waits for the player to press SPACE to begin.
- **Scene 1 (Stage 1)** — The main survival stage, where the player fights off waves of alien minions and collects power-ups for 5 minutes.
- **Scene 2 (Stage 2 / Boss Stage)** — The final stage, where the player fights "The Harbinger," a giant boss with multiple attacks and two phases.

`Game.java` manages switching between these three scenes, and `Global.java` stores shared constants such as screen size, image paths, and audio file paths.

### Controls

- **Arrow Keys** — Move the ship Up / Down / Left / Right
- **Space Bar** — Fire shots / Start the game from the title screen

## Stage 1 — Survival Stage (`Scene1.java`)

In Stage 1, the player must survive continuous waves of alien enemies for a 5-minute timer. If the timer runs out, the player wins the stage and automatically moves on to Stage 2. If the player's health reaches 0 before that, it's Game Over.

**How enemies appear:** Enemies and power-ups aren't placed randomly on the spot — they're scheduled ahead of time on a frame-by-frame spawn map (`spawnMap`), generated at the start of the stage using `SpawnDetails` objects (each holding a type and its x, y spawn position). As the game's frame counter reaches a scheduled frame, that enemy or power-up is created and added to the stage.

- Enemies spawn in waves/formations that move in from the right edge of the screen, with randomized vertical spacing between individual enemies in a wave.
- Occasional "lone" enemy spawns are inserted between waves for variety, so the stage doesn't feel purely wave-based.
- Power-ups (Speed Up, Multi-Shot, and several Heal Up pickups) are scheduled to appear at fixed points across the 5-minute timer.

## The Three Enemy Types ("Minions")

All enemies share a common parent class, `Enemy.java`, which handles hit points, taking damage, and randomly dropping bombs that fly toward the player. Each alien type overrides how it moves and, in Alien3's case, how tough it is.

### Alien 1 — "Scout"
- Fast and fragile: dies in a single hit (1 HP).
- Moves in a straight line toward the player at normal speed.
- Uses the alien-scout sprite.

### Alien 2 — "Wraith"
- Same horizontal speed as the Scout, but moves in a zig-zag / sinusoidal wave pattern up and down as it advances, making it harder to hit.
- Also dies in a single hit.
- Uses the alien-wraith sprite, giving it a ghostly appearance to match its erratic movement.

### Alien 3 — "Juggernaut"
- The tank of the group: takes 5 hits to destroy instead of 1.
- Moves slower than the other two types, since its threat comes from durability rather than speed.
- Uses the alien-juggernaut sprite — a bulkier, armored-looking design.

All enemy types have a small random chance each frame to drop a bomb that flies toward the player and can damage them on contact.

## Power-Ups

Power-ups drift left across the screen. If the player's ship touches one, it applies an upgrade effect and then disappears. All power-up types share a common parent class, `PowerUp.java`.

- **Speed Up** — Increases the player's movement speed, letting them dodge enemies and bombs more easily.
- **Multi-Shot** — Changes the player's firing pattern from a single bullet per shot to two parallel bullets per shot, roughly doubling damage output per key-press. Also increases the maximum number of shots that can be on screen at once.
- **Heal Up** — Restores 1 point of health, up to the player's maximum HP (10). Several Heal Up pickups are scheduled throughout Stage 1 to help the player recover from bomb hits and collisions.

## The Player

- Starts with 10 HP (maximum health).
- Starting speed of 4, which can be increased with Speed Up power-ups.
- Fires a single shot by default; gains a second parallel shot after collecting Multi-Shot.
- Takes damage from enemy bombs and collisions; when health reaches 0, the game ends.

## Stage 2 — The Boss Fight (`Scene2.java` / `Boss.java`)

After surviving Stage 1, the player faces the final boss: **The Harbinger**, a giant creature with 500 HP. The Harbinger first flies in from the right side of the screen and locks into position once it "engages" — only then can it actually be damaged, preventing free hits while it's still entering.

While engaged, the boss also has a small chance each frame to spawn one or more of the three regular alien minions, so the player deals with both the boss's attacks and a trickle of smaller enemies at the same time.

### Attack System

The boss uses a unified "danger zone" attack system: every attack follows the same telegraph → fire → cooldown cycle, just with a different warning shape/position on screen, giving the player a fair, visible warning before any attack can actually damage them.

- **Laser** — A full-width horizontal beam that sweeps across the player's vertical position. The only attack available in Phase 1.
- **Tentacle Slam** — A square danger zone that slams down directly on the player's position at the moment the attack starts charging.
- **Eye Beam Barrage** — The screen is divided into 5 horizontal bands; 3 light up as danger zones, leaving gaps to dodge into.
- **Spore Swarm** — Four small danger zones scattered in a cluster around the player's position.

Tentacle Slam, Eye Beam Barrage, and Spore Swarm are all locked behind Phase 2 — only the Laser is used while the boss is at full health. The boss also never repeats the same attack twice in a row.

### Phase 1 — Normal (100%–51% HP)
- Only alternates between Laser and Tentacle Slam attacks.
- Longest cooldown between attacks (~2.2 seconds) and never chains two attacks back-to-back.
- Minions spawn the least often in this phase.

### Phase 2 — Enraged (≤50% HP)
- Triggers automatically the instant the boss's health drops to half; its sprite swaps to a visibly different "enraged" appearance, and its name is displayed as "THE HARBINGER (ENRAGED)" on the health bar.
- Unlocks all four attacks, chosen randomly (never repeating the last one used).
- Shorter cooldown between attacks (~1.5 seconds) and has a 35% chance to immediately chain a second attack right after the first.
- Spawns minions more frequently and in slightly larger groups than Phase 1.

### Phase 3 — Desperation (≤20% HP)
- Triggers automatically once the boss drops to 20% health or below, while remaining visually in its Phase 2 (enraged) form.
- Fastest cooldown between attacks (~1 second) and always chains a second attack immediately after the first — the boss becomes relentless in its final stretch.
- Spawns the most minions at once (up to 3 at a time), and spawns them most frequently.

### Other Boss Details
- Flashes red briefly whenever it takes a hit, giving clear visual feedback that shots are landing.
- Has a defined hitbox smaller than its full sprite canvas, so shots only register when they actually touch the boss's visible body.
- Defeating the boss ends the game in victory, showing a Victory screen with the boss's name and end-of-game stats.

## Audio & Visual Presentation

- Each scene has its own background music: a title theme, a Stage 1 theme, and a dedicated Final Boss Theme for the Stage 2 fight.
- Victory and Game Over each have their own dedicated screens and sound effects.
- Explosions use alpha-safe image scaling so transparency renders correctly instead of looking like a faint smudge, and play for 24 frames so they're clearly visible.
- All sprite images are scaled to fixed on-screen sizes rather than their raw source resolution.

## Win & Lose Conditions

- **Stage 1 Victory** — Survive the full 5-minute timer without running out of health. Automatically proceeds to Stage 2.
- **Stage 2 Victory (Game Victory)** — Reduce the Harbinger's HP to 0. Displays the Victory screen with final stats.
- **Game Over** — The player's health reaches 0 in either stage, ending the game.
## References
This project is based from this 
[Space Invader](https://github.com/janbodnar/Java-Space-Invaders) repository.