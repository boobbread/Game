package com.mjolkster.artifice.core.actions;

import com.mjolkster.artifice.core.entities.Entity;
import com.mjolkster.artifice.core.entities.PlayableCharacter;
import com.mjolkster.artifice.graphics.screen.GameScreen;

public interface Action {
    String getId();

    String getName();

    int getActionPointCost();

    boolean execute(PlayableCharacter effector, Entity target, GameScreen gameScreen);
}
