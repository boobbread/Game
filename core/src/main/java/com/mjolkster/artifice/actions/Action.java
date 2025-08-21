package com.mjolkster.artifice.actions;

import com.mjolkster.artifice.entities.Entity;

public interface Action {
    String getId();
    String getName();
    int getActionPointCost();
    void execute(Entity effector, Entity target);
}
