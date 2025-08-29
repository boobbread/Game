package com.mjolkster.artifice.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.mjolkster.artifice.util.tools.HitboxExtractor;

import java.util.EnumMap;
import java.util.Map;

public class Sprite implements Disposable {

    public enum Direction {
        UP, DOWN, LEFT, RIGHT, IDLE
    }

    private Map<Direction, Animation<TextureRegion>> animations;
    private Map<Direction, Hitbox> hitboxes;

    private Texture spriteSheet;
    private Direction currentDirection;
    private float stateTime;
    private int tileWidth;
    private boolean flashing = false;
    private float flashTimer = 0f;
    private static final float FLASH_DURATION = 0.15f; // 150ms flash

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

        // Split sprite sheet into frames
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet,
            spriteSheet.getWidth() / frameCols,
            spriteSheet.getHeight() / frameRows);

        this.tileWidth = spriteSheet.getWidth() / frameCols;

        // Assign animations per direction assuming rows = directions in order:
        // 0: DOWN, 1: LEFT, 2: RIGHT, 3: UP, 4: IDLE (optional)

        Direction[] dirs = Direction.values();
        int rowsToUse = Math.min(frameRows, dirs.length);

        for (int r = 0; r < rowsToUse; r++) {
            TextureRegion[] frames = tmp[r];
            animations.put(dirs[r], new Animation<>(frameDuration, frames));
            hitboxes.put(dirs[r], HitboxExtractor.extractHitbox(spriteSheet, 0, r * 32, tileWidth, 32, 5));
        }

        hitboxes.forEach((direction, hitbox) -> {
            hitbox.scale(1/32f, 1/32f);
        });
    }

    /** Set hitbox for a direction */
    public void setHitbox(Direction dir, Hitbox hitbox) {
        hitboxes.put(dir, hitbox);
    }

    /** Switch direction */
    public void setDirection(Direction dir) {
        if (dir != currentDirection) {
            currentDirection = dir;
            stateTime = 0f; // reset animation
        }
    }

    /** Makes the sprite flash red */
    public void flashRed() {
        flashing = true;
        flashTimer = FLASH_DURATION;
    }

    /** Switch direction */
    public Direction getCurrentDirection() {
        return currentDirection;
    }

    /** Update state time (deltaTime from game loop) */
    public void update(float deltaTime) {
        stateTime += deltaTime;

        if (flashing) {
            flashTimer -= deltaTime;
            if (flashTimer <= 0f) {
                flashing = false;
            }
        }
    }

    /** Get current frame for rendering */
    public TextureRegion getCurrentFrame() {
        Animation<TextureRegion> anim = animations.get(currentDirection);
        if (anim == null) anim = animations.get(Direction.IDLE); // fallback
        return anim != null ? anim.getKeyFrame(stateTime, true) : null;
    }

    /** Get the current hitbox */
    public Hitbox getHitbox() {
        Hitbox hb = hitboxes.get(currentDirection);

        if (hb == null) hb = hitboxes.get(Direction.IDLE);// fallback

        return hb;
    }

    /** Set absolute position for sprite and hitbox */
    public void setPosition(float x, float y) {
        Hitbox hb = getHitbox();
        if (hb != null) hb.setPosition(x, y);
    }

    /** Dispose of texture resources */
    @Override
    public void dispose() {
        if (spriteSheet != null) spriteSheet.dispose();
    }

    /** Draw the sprite at a specific position (overrides hitbox) */
    public void draw(SpriteBatch batch, float x, float y) {
        TextureRegion frame = getCurrentFrame();
        if (frame != null) {
            if (flashing) {
                // toggle red/white based on remaining time
                float blinkInterval = 0.1f;
                boolean redPhase = ((int)(flashTimer / blinkInterval)) % 2 == 0;

                if (redPhase) {
                    batch.setColor(1f, 0f, 0f, 1f);
                } else {
                    batch.setColor(1f, 1f, 1f, 1f); // normal
                }
            } else {
                batch.setColor(1f, 1f, 1f, 1f); // normal
            }

            batch.draw(frame, x, y, tileWidth / 32f, 1f);
            batch.setColor(1f, 1f, 1f, 1f); // reset
        }
    }

    /** Reset animation to start for the current direction */
    public void reset() {
        stateTime = 0f;
    }
}
