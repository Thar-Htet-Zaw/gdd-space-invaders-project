package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class Explosion extends Sprite {

    // Fixed on-screen base size, regardless of source image resolution -- prevents
    // a high-res (e.g. AI-generated) explosion image from rendering at native
    // size * SCALE_FACTOR, which can be thousands of pixels wide. Scene1/Scene2's
    // grow-and-fade animation scales up from this base size, so this is
    // effectively the burst's starting size.
    //
    // Public + also exposed via getBaseSize() so Scene1/Scene2 can use this fixed
    // value directly instead of querying img.getWidth()/getHeight() at draw time
    // (Image.getScaledInstance() produces its pixels asynchronously, so querying
    // its dimensions immediately after creation is not guaranteed reliable).
    public static final int EXPLOSION_SIZE = 64;

    // How many frames the burst stays visible/animating. Sprite's default (10
    // frames, ~170ms at 60fps) was too brief to reliably notice -- bumped here.
    private static final int LIFETIME_FRAMES = 24;

    public Explosion(int x, int y) {

        initExplosion(x, y);
        this.visibleFrames = LIFETIME_FRAMES;
    }

    public static int getBaseSize() {
        return EXPLOSION_SIZE;
    }

    public static int getLifetimeFrames() {
        return LIFETIME_FRAMES;
    }

    private void initExplosion(int x, int y) {

        this.x = x;
        this.y = y;

        var ii = new ImageIcon(IMG_EXPLOSION);

        // Image.getScaledInstance() badly under-represents opacity when downscaling
        // an image that's mostly transparent with sparse bright detail (exactly what
        // an explosion/spark burst looks like) -- it doesn't correctly account for
        // alpha while averaging pixels, so the result comes out far more transparent
        // than it should, reading as a faint, nearly-invisible smudge on a black
        // background. Scaling through a BufferedImage + Graphics2D instead handles
        // alpha correctly during the resize.
        BufferedImage scaledImage = new BufferedImage(EXPLOSION_SIZE, EXPLOSION_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        // ii.getImage() is already fully loaded by this point (ImageIcon blocks until
        // loaded internally), so a null ImageObserver here is safe.
        g2d.drawImage(ii.getImage(), 0, 0, EXPLOSION_SIZE, EXPLOSION_SIZE, null);
        g2d.dispose();

        setImage(scaledImage);
    }

    public void act(int direction) {

        // this.x += direction;
    }

    @Override
    public void act() {
        // Explosions don't move on their own — required only to satisfy Sprite's contract.
    }


}