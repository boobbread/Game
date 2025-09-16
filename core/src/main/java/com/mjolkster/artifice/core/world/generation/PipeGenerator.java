package com.mjolkster.artifice.core.world.generation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.mjolkster.artifice.util.data.Pair;
import com.mjolkster.artifice.util.geometry.Line;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PipeGenerator {

    private final List<Point> validPipeStarts;
    private final Set<Line> collisionVertexes;
    private final TiledMapTileLayer pipeLayer;
    private final int tileWidth;
    private final int tileHeight;
    public final HashMap<Point, Integer> gridVertices;

    public PipeGenerator(List<Point> pipeStarts, Set<Line> collisionVertexes, TiledMapTileLayer pipeLayer, int tileWidth, int tileHeight, HashMap<Point, Integer> gridVertices) {
        this.validPipeStarts = pipeStarts;
        this.collisionVertexes = collisionVertexes;
        this.pipeLayer = pipeLayer;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.gridVertices = gridVertices;
    }

    public void constructPipe() {

        Point startPoint = findPipeStart();
        Gdx.app.log("MapGenerator", "Pipe start:" + startPoint);

        DirectionOfTravel currentDirection = DirectionOfTravel.EAST;

        Deque<Pair<Point, DirectionOfTravel>> segments = stepForward(currentDirection, startPoint);
        ArrayList<DirectionOfTravel> directionTriplet = new ArrayList<>();
        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();

        cell.setTile(TileLookup.getPipeTile(6).first);
        pipeLayer.setCell(startPoint.x, startPoint.y, cell);
        directionTriplet.add(DirectionOfTravel.EAST);

        segments.forEach(segment -> {

            Gdx.app.log("Pipe", "Segment pos: " + segment.first);

            TiledMapTileLayer.Cell newCell = new TiledMapTileLayer.Cell();

            if (directionTriplet.size() == 3) {
                directionTriplet.remove(0);
            }

            directionTriplet.add(segment.second);

            // Store positions too so we can update previous cells
            if (directionTriplet.size() == 3) {
                DirectionOfTravel last = directionTriplet.get(0);
                DirectionOfTravel current = directionTriplet.get(1);
                DirectionOfTravel next = directionTriplet.get(2);

                DirectionOfTravel adjusted = adjustDirection(last, current, next);

                // Retroactively update the tile at the *previous* position
                Point currentPos = ((LinkedList<Pair<Point, DirectionOfTravel>>) segments)
                    .get(((LinkedList<Pair<Point, DirectionOfTravel>>) segments).indexOf(segment) - 1)
                    .first;

                Gdx.app.log("Pipe", "Segment pos: " + segment.first + ", " + "current pos: " + currentPos);

                TiledMapTileLayer.Cell cornerCell = new TiledMapTileLayer.Cell();
                cornerCell.setTile(TileLookup.getStraightPipeTile(adjusted));
                pipeLayer.setCell(currentPos.x, currentPos.y, cornerCell);

                // Still place the "next" segment with its raw direction for now
                newCell.setTile(TileLookup.getStraightPipeTile(segment.second));
                pipeLayer.setCell(segment.first.x, segment.first.y, newCell);

            } else {
                newCell.setTile(TileLookup.getStraightPipeTile(segment.second));
                pipeLayer.setCell(segment.first.x, segment.first.y, newCell);
            }

            if (segment == segments.getLast()) {
                newCell.setTile(TileLookup.getPipeTile(segment.second).second);
                pipeLayer.setCell(segment.first.x, segment.first.y, newCell);
            }

        });

    }

    private Point findPipeStart() {

        Collections.shuffle(validPipeStarts);
        Point samplePoint = validPipeStarts.get(0);

        return samplePoint;
    }

    private boolean isWall(int s) {
        return s == 3 || s == 6 || s == 9 || s == 12;
    }

    private boolean isPath(int s) {
        return s == 15;
    }

    private DirectionOfTravel[] orthogonalsFor(DirectionOfTravel dir) {
        switch (dir) {
            case NORTH:
            case SOUTH:
                return new DirectionOfTravel[]{DirectionOfTravel.EAST};
            case EAST:
            case WEST:
                return new DirectionOfTravel[]{DirectionOfTravel.NORTH, DirectionOfTravel.SOUTH};
            default:
                return new DirectionOfTravel[]{};
        }
    }

    private Deque<Pair<Point, DirectionOfTravel>> stepForward(DirectionOfTravel currentDirection, Point startPoint) {
        return explorePath(currentDirection, new Point(startPoint), 0, 0);
    }

    private Deque<Pair<Point, DirectionOfTravel>> explorePath(DirectionOfTravel dir, Point cur, int turnsMade, int stepCount) {
        Deque<Pair<Point, DirectionOfTravel>> path = new LinkedList<>();

        final int maxTurns = 5;
        final int maxRayLength = 8;
        final int maxSteps = 500;

        // Safety stop
        if (turnsMade > maxTurns || stepCount >= maxSteps) {
            return path;
        }

        // Step forward
        cur = new Point(cur); // copy
        cur.x += dir.dx;
        cur.y += dir.dy;
        path.add(new Pair<>(cur, dir));

        int state;
        try {
            state = getStateAt(cur.x, cur.y);
        } catch (Exception e) {
            return path; // out-of-bounds
        }

        if (isWall(state) || !isPath(state)) {
            return path;
        }

        // Collect possible directions
        List<DirectionOfTravel> nextDirs = new ArrayList<>();
        nextDirs.add(dir); // straight ahead is always an option

        for (DirectionOfTravel ortho : orthogonalsFor(dir)) {
            for (int i = 1; i <= maxRayLength; i++) {
                int rx = cur.x + ortho.dx * i;
                int ry = cur.y + ortho.dy * i;
                int rayState;
                try {
                    rayState = getStateAt(rx, ry);
                } catch (Exception e) {
                    break;
                }
                if (rayState != 15) {
                    if (isWall(rayState)) {
                        nextDirs.add(ortho); // valid turn
                    }
                    break;
                }
            }
        }

        // Explore all candidate directions
        Deque<Pair<Point, DirectionOfTravel>> bestPath = new LinkedList<>(path);
        for (DirectionOfTravel nextDir : nextDirs) {
            int newTurns = turnsMade + (nextDir == dir ? 0 : 1);
            Deque<Pair<Point, DirectionOfTravel>> candidate =
                explorePath(nextDir, cur, newTurns, stepCount + 1);

            if (candidate.size() + path.size() > bestPath.size()) {
                Deque<Pair<Point, DirectionOfTravel>> combined = new LinkedList<>(path);
                combined.addAll(candidate);
                bestPath = combined;
            }
        }

        return bestPath;
    }


    private DirectionOfTravel adjustDirection(DirectionOfTravel last, DirectionOfTravel current, DirectionOfTravel next) {
        // If last and next are different, it's a bend
        if (current != next) {
            if (current == DirectionOfTravel.NORTH && next == DirectionOfTravel.EAST)
                return DirectionOfTravel.SOUTH_WEST;
            if (current == DirectionOfTravel.EAST && next == DirectionOfTravel.NORTH)
                return DirectionOfTravel.NORTH_EAST;

            if (current == DirectionOfTravel.NORTH && next == DirectionOfTravel.WEST)
                return DirectionOfTravel.SOUTH_EAST;
            if (current == DirectionOfTravel.WEST && next == DirectionOfTravel.NORTH)
                return DirectionOfTravel.NORTH_WEST;

            if (current == DirectionOfTravel.SOUTH && next == DirectionOfTravel.EAST)
                return DirectionOfTravel.NORTH_WEST;
            if (current == DirectionOfTravel.EAST && next == DirectionOfTravel.SOUTH)
                return DirectionOfTravel.SOUTH_EAST;

            if (current == DirectionOfTravel.SOUTH && next == DirectionOfTravel.WEST)
                return DirectionOfTravel.NORTH_EAST;
            if (current == DirectionOfTravel.WEST && next == DirectionOfTravel.SOUTH)
                return DirectionOfTravel.SOUTH_WEST;
        }

        // Otherwise keep the same
        return current;
    }

    private int getState(int bl, int br, int tr, int tl) {
        return (bl) + (br << 1) + (tr << 2) + (tl << 3);
    }

    private int getStateAt(int x, int y) {
        int bl = gridVertices.getOrDefault(new Point(x, y), 0);
        int br = gridVertices.getOrDefault(new Point(x + 1, y), 0);
        int tr = gridVertices.getOrDefault(new Point(x + 1, y + 1), 0);
        int tl = gridVertices.getOrDefault(new Point(x, y + 1), 0);
        return getState(bl, br, tr, tl); // reuse your existing method
    }
}
