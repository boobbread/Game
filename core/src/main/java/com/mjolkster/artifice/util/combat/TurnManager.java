package com.mjolkster.artifice.util.combat;

import com.mjolkster.artifice.core.entities.enemy.BaseEnemy;
import com.mjolkster.artifice.core.entities.PlayableCharacter;

import java.util.List;

public class TurnManager {

    private final PlayableCharacter player;
    private final List<BaseEnemy> npcs;

    public TurnManager(PlayableCharacter player, List<BaseEnemy> npcs) {
        this.player = player;
        this.npcs = npcs;
    }

    public void update(float delta) {

        // If the player just moved/acted, NPCs should also take a step
        if (player.hasCompletedMove()) {
            for (BaseEnemy npc : npcs) {
                npc.recalculatePath(); // update pathfinding toward the player
                npc.followPath(delta); // take one movement step
                npc.performAction();   // attack if possible
                npc.damagedPlayer(false); // reset attack flag for next turn
            }

            // Reset player for next move
            player.resetForNewTurn();
        }
    }
}


