package com.mjolkster.artifice.core.actions;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mjolkster.artifice.core.entities.Entity;
import com.mjolkster.artifice.core.entities.PlayableCharacter;
import com.mjolkster.artifice.graphics.screen.GameScreen;

/**
 * A pretty bare-bones interface for making Actions
 */
public interface Action {

    // Getters
    String getId();
    String getName();
    int getActionPointCost();

    // Methods
    void draw(SpriteBatch batch, float x, float y);

    void update(float delta);

    boolean execute(PlayableCharacter effector, Entity target, GameScreen gameScreen);
}
