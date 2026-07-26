package gdd;

public class Global {
    private Global() {
        // Prevent instantiation
    }

    public static final int SCALE_FACTOR = 3; // Scaling factor for sprites

    public static final int BOARD_WIDTH = 716; // Doubled from 358
    public static final int BOARD_HEIGHT = 700; // Doubled from 350
    public static final int BORDER_RIGHT = 60; // Doubled from 30
    public static final int BORDER_LEFT = 10; // Doubled from 5

    public static final int GROUND = 580; // Doubled from 290
    public static final int BOMB_HEIGHT = 10; // Doubled from 5

    public static final int ALIEN_HEIGHT = 24; // Doubled from 12
    public static final int ALIEN_WIDTH = 24; // Doubled from 12
    public static final int ALIEN_INIT_X = 300; // Doubled from 150
    public static final int ALIEN_INIT_Y = 10; // Doubled from 5
    public static final int ALIEN_GAP = 30; // Gap between aliens

    public static final int GO_DOWN = 30; // Doubled from 15
    public static final int NUMBER_OF_ALIENS_TO_DESTROY = 24;
    public static final int CHANCE = 5;
    public static final int DELAY = 17;
    public static final int PLAYER_WIDTH = 64; // Doubled from 15
    public static final int PLAYER_HEIGHT = 48; // Doubled from 10

    // Images
    public static final String IMG_ENEMY = "src/images/alien.png";
    public static final String IMG_ENEMY_SCOUT = "src/images/alien-scout.png";
    public static final String IMG_ENEMY_WRAITH = "src/images/alien-wraith.png";
    public static final String IMG_ENEMY_JUGGERNAUT = "src/images/alien-juggernaut.png";
    public static final String IMG_PLAYER = "src/images/player_spaceship.png";
    public static final String IMG_SHOT = "src/images/shot.png";
    public static final String IMG_EXPLOSION = "src/images/explosion.png";
    public static final String IMG_TITLE = "src/images/title.png";
    public static final String IMG_POWERUP_SPEEDUP = "src/images/powerup-s.png";
    public static final String IMG_POWERUP_MULTISHOT = "src/images/powerup-m.png";
    public static final String IMG_BOMB = "src/images/bomb.png";
    public static final String IMG_BOSS = "src/images/boss.png";
    public static final String IMG_GAME_OVER = "src/images/gameover.png";

    // Audio Files
    public static final String AUD_TITLE = "src/audio/title.wav";
    public static final String AUD_SCENE1 = "src/audio/scene1.wav";
    public static final String AUD_FIRE = "src/audio/fire.wav";
    public static final String AUD_EXPLODE = "src/audio/explode.wav";
    public static final String AUD_GAMEOVER = "src/audio/gameover-sound.wav";
    public static final String AUD_LEVEL_UP = "src/audio/level-up.wav";
    public static final String AUD_SCENE2 = "src/audio/stage2.wav";
    public static final String AUD_BOSS = "src/audio/boss-battle.wav";
}