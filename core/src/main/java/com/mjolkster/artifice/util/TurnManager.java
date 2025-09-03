package com.mjolkster.artifice.util;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.mjolkster.artifice.entities.NonPlayableCharacter;
import com.mjolkster.artifice.entities.PlayableCharacter;
import com.mjolkster.artifice.screen.GameScreen;
import com.mjolkster.artifice.util.wrappers.Line;

import java.util.List;
import java.util.Set;

public class TurnManager {

    private final PlayableCharacter player;
    private final List<NonPlayableCharacter> npcs;

    public TurnManager(PlayableCharacter player, List<NonPlayableCharacter> npcs) {
        this.player = player;
        this.npcs = npcs;
    }

    public void update(float delta) {

        // If the player just moved/acted, NPCs should also take a step
        if (player.hasCompletedMove()) {
            for (NonPlayableCharacter npc : npcs) {
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


