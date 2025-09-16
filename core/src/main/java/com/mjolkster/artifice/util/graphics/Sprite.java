package com.mjolkster.artifice.util.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.mjolkster.artifice.util.geometry.Hitbox;
import com.mjolkster.artifice.util.geometry.HitboxExtractor;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class Sprite implements Disposable {

    private static final float FLASH_DURATION = 0.300f; // in secs
    private final Map<Direction, Animation<TextureRegion>> animations;
    private final Map<Direction, Hitbox> hitboxes;

    private final Texture spriteSheet;
    private Direction currentDirection;
    private float stateTime;
    private boolean flashing = false;
    private float flashTimer = 0f;
    private final int spriteWidth;
    private final int spriteHeight;

    private final Map<String, Animation<TextureRegion>> customAnimations = new HashMap<>();
    private String currentAnimation = null;
    private boolean loopCurrent = true;

    private ParticleEffect effect;
    private boolean playEffect;

    /**
     * Creates animations from a sprite sheet.
     *
     * @param spriteSheetPath Path to sprite sheet
     * @param frameCols       Number of columns in the sheet
     * @param frameRows       Number of rows in the sheet
     * @param frameDuration   Duration of each frame
     */
    public Sprite(String spriteSheetPath, int frameCols, int frameRows, float frameDuration) {
        this.spriteSheet = new Texture(Gdx.files.internal(spriteSheetPath));
        this.animations = new EnumMap<>(Direction.class);
        this.hitboxes = new EnumMap<>(Direction.class);
        this.currentDirection = Direction.IDLE;
        this.stateTime = 0f;

        this.spriteWidth = spriteSheet.getWidth() / frameCols;
        this.spriteHeight = spriteSheet.getHeight() / frameRows;

        TextureRegion[][] tmp = TextureRegion.split(spriteSheet,
            spriteWidth,
            spriteHeight);

        // Assign animations per direction assuming rows = directions in order:
        // UP, DOWN, LEFT, RIGHT, IDLE

        Direction[] dirs = Direction.values();
        int rowsToUse = Math.min(frameRows, dirs.length);

        for (int r = 0; r < rowsToUse; r++) {
            TextureRegion[] frames = tmp[r];
            animations.put(dirs[r], new Animation<>(frameDuration, frames));
        }

        effect = new ParticleEffect();
        effect.load(Gdx.files.internal("particles/death_particle/death_particle.p"), Gdx.files.internal("particles/death_particle"));
        playEffect = false;
    }

    /**
     * Set hitbox for a direction
     */
    public void setHitbox(Direction dir, Hitbox hitbox) {
        hitboxes.put(dir, hitbox);
    }

    /**
     * Switch direction
     */
    public void setDirection(Direction dir) {
        if (dir != currentDirection) {
            currentDirection = dir;
            stateTime = 0f; // reset animation
        }
    }

    /**
     * Makes the sprite flash red
     */
    public void flashRed() {
        flashing = true;
        flashTimer = FLASH_DURATION;
    }

    /**
     * Switch direction
     */
    public Direction getCurrentDirection() {
        return currentDirection;
    }

    /**
     * Update state time (deltaTime from game loop)
     */
    public void update(float deltaTime) {
        stateTime += deltaTime;
        effect.update(deltaTime);

        if (flashing) {
            flashTimer -= deltaTime;
            if (flashTimer <= 0f) {
                flashing = false;
            }
        }
    }

    public boolean isAnimationFinished() {
        if (currentAnimation == null) return false;
        Animation<TextureRegion> anim = customAnimations.get(currentAnimation);
        return anim != null && anim.isAnimationFinished(stateTime);
    }

    public void stopAnimation() {
        currentAnimation = null;
        stateTime = 0f;
    }

    /**
     * Get current frame for rendering
     */
    public TextureRegion getCurrentFrame() {
        if (currentAnimation != null) {
            Animation<TextureRegion> anim = customAnimations.get(currentAnimation);
            if (anim != null) {
                return anim.getKeyFrame(stateTime, loopCurrent);
            }
        }

        Animation<TextureRegion> anim = animations.get(currentDirection);
        if (anim == null) anim = animations.get(Direction.IDLE); // fallback
        return anim != null ? anim.getKeyFrame(stateTime, true) : null;
    }

    /**
     * Get the current hitbox
     */
    public Hitbox getHitbox() {
        Hitbox hb = hitboxes.get(currentDirection);

        if (hb == null) hb = hitboxes.get(Direction.IDLE);// fallback

        return hb;
    }

    /**
     * Set absolute position for sprite and hitbox
     */
    public void setPosition(float x, float y) {
        Hitbox hb = getHitbox();
        if (hb != null) hb.setOrigin(x, y);
    }

    /**
     * Dispose of texture resources
     */
    @Override
    public void dispose() {
        if (spriteSheet != null) spriteSheet.dispose();
    }

    /**
     * Draw the sprite at a specific position (overrides hitbox)
     */
    public void draw(SpriteBatch batch, float x, float y) {
        TextureRegion frame = getCurrentFrame();
        if (frame != null) {
            if (flashing) {
                // toggle red/white based on remaining time
                float blinkInterval = 0.1f;
                boolean redPhase = ((int) (flashTimer / blinkInterval)) % 2 == 0;

                if (redPhase) {
                    batch.setColor(1f, 0f, 0f, 1f);
                } else {
                    batch.setColor(1f, 1f, 1f, 1f); // normal
                }
            } else {
                batch.setColor(1f, 1f, 1f, 1f); // normal
            }

            batch.draw(frame, x, y, spriteWidth / 32f, spriteHeight / 32f);
            batch.setColor(1f, 1f, 1f, 1f); // reset
        }

        if (playEffect) {
            effect.draw(batch);
        }

    }

    /**
     * Reset animation to start for the current direction
     */
    public void reset() {
        stateTime = 0f;
    }

    public void addAnimation(String key, TextureRegion[] frames, float frameDuration, boolean loop) {
        Array<TextureRegion> array = new Array<>(frames);
        Animation<TextureRegion> anim = new Animation<>(
            frameDuration,
            array,
            loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL
        );
        customAnimations.put(key, anim);
    }

    public void playAnimation(String key, boolean loop) {
        if (!customAnimations.containsKey(key)) return;

        if (!key.equals(currentAnimation)) {
            currentAnimation = key;
            stateTime = 0f; // restart animation
            loopCurrent = loop;
        }
    }

    public void playDeathEffect(float x, float y) {
        effect.setPosition(x, y);
        effect.start();

        playEffect = true;
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT, IDLE
    }
}
