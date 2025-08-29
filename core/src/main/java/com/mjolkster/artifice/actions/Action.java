package com.mjolkster.artifice.actions;

import com.mjolkster.artifice.entities.Entity;
import com.mjolkster.artifice.entities.PlayableCharacter;

public interface Action {
    String getId();
    String getName();
    int getActionPointCost();
    void execute(PlayableCharacter effector, Entity target);
}
