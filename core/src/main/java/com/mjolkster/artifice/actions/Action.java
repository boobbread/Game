package com.mjolkster.artifice.actions;

import com.mjolkster.artifice.entities.Entity;
import com.mjolkster.artifice.entities.PlayableCharacter;
import com.mjolkster.artifice.screen.GameScreen;

public interface Action {
    String getId();
    String getName();
    int getActionPointCost();
    boolean execute(PlayableCharacter effector, Entity target, GameScreen gameScreen);
}
