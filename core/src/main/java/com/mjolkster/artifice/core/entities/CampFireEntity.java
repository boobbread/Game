package com.mjolkster.artifice.core.entities;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.mjolkster.artifice.graphics.screen.GameScreen;
import com.mjolkster.artifice.util.graphics.Sprite;

public class CampFireEntity extends Entity {
    private final PointLight light;

    public CampFireEntity(GameScreen gameScreen, RayHandler rayHandler) {
        super(10, 10, 0, 0, new Sprite("campfire.png", 4, 5, 0.3f), gameScreen);

        light = new PointLight(rayHandler, 64, new Color(1f, 0.6f, 0.2f, 0.5f), 3f, 0, 0);
        light.setSoftnessLength(0.5f);
        light.setStaticLight(false);
    }

    public void setPosition(Vector2 pos) {
        this.x = pos.x / 32f;
        this.y = pos.y / 32f;
        light.setPosition(this.x + 0.5f, this.y + 0.5f);
    }

    @Override
    public void update(float delta, com.badlogic.gdx.graphics.OrthographicCamera camera,
                       java.util.Set<com.mjolkster.artifice.util.geometry.Line> collisionBoxes) {
        super.update(delta, camera, collisionBoxes);

        // flicker effect
        float flicker = 2.5f + (float) Math.random() * 0.2f;
        light.setDistance(flicker);
        light.setPosition(this.x + 0.5f, this.y + 0.5f);
    }

    public void dispose() {
        light.remove();
    }
}

