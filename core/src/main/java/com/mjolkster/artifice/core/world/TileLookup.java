package com.mjolkster.artifice.core.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.mjolkster.artifice.util.data.Pair;
import com.mjolkster.artifice.util.geometry.Line;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TileLookup {

    private TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("tiles.atlas"));
    private static Map<Integer, TiledMapTile> tiles = new HashMap<>();
    private static Map<Integer, Pair<TiledMapTile, TiledMapTile>> pipeTiles = new HashMap<>();
    private static Map<DirectionOfTravel, TiledMapTile> straightPipeTiles = new HashMap<>();

    public TileLookup() {

        tiles.put(0, new StaticTiledMapTile(atlas.findRegion("void")));
        tiles.put(1, new StaticTiledMapTile(atlas.findRegion("wall_southwest_outer")));
        tiles.put(2, new StaticTiledMapTile(atlas.findRegion("wall_southeast_outer")));
        tiles.put(3, new StaticTiledMapTile(atlas.findRegion("wall_north")));
        tiles.put(4, new StaticTiledMapTile(atlas.findRegion("wall_northeast_outer")));
        tiles.put(5, new StaticTiledMapTile(atlas.findRegion("wall_converge_2")));
        tiles.put(6, new StaticTiledMapTile(atlas.findRegion("wall_west")));
        tiles.put(7, new StaticTiledMapTile(atlas.findRegion("wall_northwest_inner")));
        tiles.put(8, new StaticTiledMapTile(atlas.findRegion("wall_northwest_outer")));
        tiles.put(9, new StaticTiledMapTile(atlas.findRegion("wall_east")));
        tiles.put(10, new StaticTiledMapTile(atlas.findRegion("wall_converge_1")));
        tiles.put(11, new StaticTiledMapTile(atlas.findRegion("wall_northeast_inner")));
        tiles.put(12, new StaticTiledMapTile(atlas.findRegion("wall_south")));
        tiles.put(13, new StaticTiledMapTile(atlas.findRegion("wall_southeast_inner")));
        tiles.put(14, new StaticTiledMapTile(atlas.findRegion("wall_southwest_inner")));
        tiles.put(15, new StaticTiledMapTile(atlas.findRegion("floor")));

        pipeTiles.put(3, new Pair<>(new StaticTiledMapTile(atlas.findRegion("pipe_north")), new StaticTiledMapTile(atlas.findRegion("canal_end_north"))));
        pipeTiles.put(6, new Pair<>(new StaticTiledMapTile(atlas.findRegion("pipe_west")), new StaticTiledMapTile(atlas.findRegion("canal_end_west_1"))));
        pipeTiles.put(9, new Pair<>(new StaticTiledMapTile(atlas.findRegion("pipe_east")), new StaticTiledMapTile(atlas.findRegion("canal_end_east_1"))));
        pipeTiles.put(12, new Pair<>(new StaticTiledMapTile(atlas.findRegion("pipe_south_1")), new StaticTiledMapTile(atlas.findRegion("wall_south"))));
        pipeTiles.put(0, new Pair<>(new StaticTiledMapTile(atlas.findRegion("canal_pipe_north_1")), new StaticTiledMapTile(atlas.findRegion("canal_pipe_north_2"))));

        straightPipeTiles.put(DirectionOfTravel.EAST, new StaticTiledMapTile(atlas.findRegion("canal_horizontal_1")));
        straightPipeTiles.put(DirectionOfTravel.WEST, new StaticTiledMapTile(atlas.findRegion("canal_horizontal_1")));
        straightPipeTiles.put(DirectionOfTravel.NORTH, new StaticTiledMapTile(atlas.findRegion("canal_vertical_1")));
        straightPipeTiles.put(DirectionOfTravel.SOUTH, new StaticTiledMapTile(atlas.findRegion("canal_vertical_1")));
        straightPipeTiles.put(DirectionOfTravel.NORTH_WEST, new StaticTiledMapTile(atlas.findRegion("canal_northeast_1")));
        straightPipeTiles.put(DirectionOfTravel.NORTH_EAST, new StaticTiledMapTile(atlas.findRegion("canal_northwest_1")));
        straightPipeTiles.put(DirectionOfTravel.SOUTH_WEST, new StaticTiledMapTile(atlas.findRegion("canal_southeast_1")));
        straightPipeTiles.put(DirectionOfTravel.SOUTH_EAST, new StaticTiledMapTile(atlas.findRegion("canal_southwest_1")));

    }

    public static TiledMapTile getTile(int state) {
        return tiles.get(state);
    }

    public static Pair<TiledMapTile, TiledMapTile> getPipeTile(int state) {
        return pipeTiles.get(state);
    }

    public static Pair<TiledMapTile, TiledMapTile> getPipeTile(DirectionOfTravel direction) {
        switch (direction) {
            case NORTH: return pipeTiles.get(3);
            case WEST: return pipeTiles.get(6);
            case EAST: return pipeTiles.get(9);
            case SOUTH: return pipeTiles.get(12);
        }
        return null;
    }

    public static TiledMapTile getStraightPipeTile(DirectionOfTravel key) {
        return straightPipeTiles.get(key);
    }

    public static Set<Line> getCollisionLines(int state, Point point, float tileWidthScaled, float tileHeightScaled) {
        Set<Line> collisionVertexes = new HashSet<>();

        switch (state) {
            case 0: {
            }
            break;
            case 1: {
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled * 11 / 16f,
                    point.x, point.y + tileHeightScaled * 11 / 16f));
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled * 11 / 16f,
                    point.x + tileWidthScaled * 0.5f, point.y));
            }
            break;
            case 2: {
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled * 11 / 16f,
                    point.x + tileWidthScaled, point.y + tileHeightScaled * 11 / 16f));
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled * 11 / 16f,
                    point.x + tileWidthScaled * 0.5f, point.y));
            }
            break;
            case 3: {
                collisionVertexes.add(new Line(
                    point.x, point.y + tileHeightScaled * 11 / 16f,
                    point.x + tileWidthScaled, point.y + tileHeightScaled * 11 / 16f));
            }
            break;
            case 4: {
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled,
                    point.x + tileWidthScaled, point.y + tileHeightScaled));
            }
            break;
            case 5: {
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled * 11 / 16f,
                    point.x + tileWidthScaled * 0.5f, point.y));
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled * 11 / 16f,
                    point.x, point.y + tileHeightScaled * 11 / 16f));
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled,
                    point.x + tileWidthScaled, point.y + tileHeightScaled));
            }
            break;
            case 6: {
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y,
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled));
            }
            break;
            case 7: {
                collisionVertexes.add(new Line(
                    point.x, point.y + tileHeightScaled * 11 / 16f,
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled * 11 / 16f));
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled,
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled * 11 / 16f));
            }
            break;
            case 8: {
                collisionVertexes.add(new Line(
                    point.x, point.y + tileHeightScaled,
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled));
            }
            break;
            case 9: {
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y,
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled));
            }
            break;
            case 10: {
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y,
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled * 11 / 16f));
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled * 11 / 16f,
                    point.x + tileWidthScaled, point.y + tileHeightScaled * 11 / 16f));
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled,
                    point.x, point.y + tileHeightScaled));
            }
            break;
            case 11: {
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled * 11 / 16f,
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled));
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled * 11 / 16f,
                    point.x + tileWidthScaled, point.y + tileHeightScaled * 11 / 16f));
            }
            break;
            case 12: {
                collisionVertexes.add(new Line(
                    point.x, point.y + tileHeightScaled,
                    point.x + tileWidthScaled, point.y + tileHeightScaled));
            }
            break;
            case 13: {
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y,
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled));
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled,
                    point.x + tileWidthScaled, point.y + tileHeightScaled));
            }
            break;
            case 14: {
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y,
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled));
                collisionVertexes.add(new Line(
                    point.x + tileWidthScaled * 0.5f, point.y + tileHeightScaled,
                    point.x, point.y + tileHeightScaled));
            }
            break;
            case 15: {
                break;
            }
        }

        return collisionVertexes;
    }
}
