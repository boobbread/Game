package com.mjolkster.artifice.graphics.drawables;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class FlipDrawable extends TextureRegionDrawable {

    // Fields
    private TextureRegion[] frames;
    private float frameDuration;
    private float stateTime = 0f;
    private boolean looping = true;

    // Constructors
    public FlipDrawable(TextureRegion[] frames, float frameDuration) {
        super(frames[0]);
        this.frames = frames;
        this.frameDuration = frameDuration;
    }

    // Methods
    public void draw(Batch batch, float x, float y, float width, float height) {
        int frameIndex = (int)(stateTime / frameDuration);
        if (looping) {
            frameIndex %= frames.length;
        } else {
            frameIndex = Math.min(frameIndex, frames.length - 1);
        }

        setRegion(frames[frameIndex]);
        super.draw(batch, x, y, width, height);

        stateTime += Gdx.graphics.getDeltaTime();
    }

    public void reset() {
        stateTime = 0f;
    }

    // Getters and setters
    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public static class FlipDrawableSerializer implements Json.Serializer<FlipDrawable> {

        private final Skin skin;

        public FlipDrawableSerializer(Skin skin) {
            this.skin = skin;
        }

        @Override
        public void write(Json json, FlipDrawable object, Class knownType) {}

        @Override
        public FlipDrawable read(Json json, JsonValue jsonData, Class type) {

            float frameDuration = jsonData.getFloat("frameDuration", 0.1f);
            String[] frameNames = jsonData.get("frames").asStringArray();

            TextureRegion[] regions = new TextureRegion[frameNames.length];
            for (int i = 0; i < frameNames.length; i++) {
                regions[i] = skin.getRegion(frameNames[i]);
            }

            FlipDrawable drawable = new FlipDrawable(regions, frameDuration);
            if (jsonData.has("looping")) {
                drawable.setLooping(jsonData.getBoolean("looping"));
            }
            return drawable;
        }
    }
}

