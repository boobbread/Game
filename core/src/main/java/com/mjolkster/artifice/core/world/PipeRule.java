package com.mjolkster.artifice.core.world;

import com.badlogic.gdx.maps.tiled.TiledMapTile;

import java.nio.channels.Pipe;

public class PipeRule {
    final TiledMapTile tile;
    final DirectionOfTravel nextDir;

    public PipeRule(TiledMapTile tile, DirectionOfTravel nextDir) {
        this.tile = tile;
        this.nextDir = nextDir;
    }
}
