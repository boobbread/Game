package com.mjolkster.artifice.core.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mjolkster.artifice.util.geometry.Line;
import com.mjolkster.artifice.util.data.Pair;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

public class MapGenerator {

    private final long seed;
    private final Texture mossSet;
    private final StaticTiledMapTile[] mossTiles;
    private final Random random;
    public final HashMap<Point, Integer> gridVertices = new HashMap<>();
    private final List<Point> validPipeStarts = new ArrayList<>();
    private final int tileWidth;
    private final int tileHeight;
    public int width;
    public int height;
    public Set<Line> collisionVertexes = new HashSet<>();
    public Set<Rectangle> subroomEntrance = new HashSet<>();
    PerlinNoiseGenerator noise = new PerlinNoiseGenerator();
    List<Set<Point>> islands;
    TiledMapTileLayer layer;
    TiledMapTileLayer mossLayer;
    TiledMapTileLayer pipeLayer;
    double[][] moistureMap;
    Integer[][] AStarGrid;
    private int xMax, yMax;
    private final Vector2 spawnPoint = new Vector2();
    private final Vector2 endPoint = new Vector2();

    // Generator constructor

    /**
     * Initialises the map generator
     *
     * @param tileWidth  Width of the tiles in pixels
     * @param tileHeight Height of the tiles in pixels
     * @param seed       The seed for generation
     */

    public MapGenerator(int tileWidth, int tileHeight, long seed) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.seed = seed;

        islands = new ArrayList<>();

        new TileLookup();

        mossSet = new Texture(Gdx.files.internal("mosstexture.png"));
        TextureRegion[][] mossSplit = TextureRegion.split(mossSet, tileWidth, tileHeight);
        mossTiles = new StaticTiledMapTile[mossSplit.length * mossSplit[0].length];
        int index = 0;
        for (int row = 0; row < mossSplit.length; row++) {
            for (int col = 0; col < mossSplit[0].length; col++) {
                mossTiles[index] = new StaticTiledMapTile(mossSplit[row][col]);
                index++;
            }
        }

        random = new Random(seed);
    }

    public static boolean isCollinear(Vector2 a, Vector2 b, Vector2 c, double e) {
        double cross = (b.x - a.x) * (c.y - b.y) - (b.y - a.y) * (c.x - b.x);
        return Math.abs(cross) < e;
    }

    // Base mapGen stuff

    public static List<Vector2> unifySegments(List<Vector2> points, double epsilon) {
        if (points.size() < 3) return points;

        List<Vector2> result = new ArrayList<>();
        result.add(points.get(0)); // first point always included

        for (int i = 1; i < points.size() - 1; i++) {
            Vector2 prev = result.get(result.size() - 1);
            Vector2 curr = points.get(i);
            Vector2 next = points.get(i + 1);

            if (isCollinear(prev, curr, next, epsilon)) {
                // Skip curr (it's redundant)
            } else {
                result.add(curr);
            }
        }

        result.add(points.get(points.size() - 1)); // last point
        return result;
    }

    public static List<List<Vector2>> orderOutline(Set<Line> lines) {
        Set<Line> working = new HashSet<>(lines);
        List<List<Vector2>> result = new ArrayList<>();

        while (!working.isEmpty()) {
            Line first = working.iterator().next();
            working.remove(first);

            List<Vector2> polyline = new ArrayList<>();
            polyline.add(first.start);
            polyline.add(first.end);

            boolean extended = true;
            while (!working.isEmpty() && extended) {
                extended = false;

                Vector2 last = polyline.get(polyline.size() - 1);
                for (Iterator<Line> it = working.iterator(); it.hasNext(); ) {
                    Line l = it.next();
                    if (last.equals(l.start)) {
                        polyline.add(l.end);
                        it.remove();
                        extended = true;
                        break;
                    } else if (last.equals(l.end)) {
                        polyline.add(l.start);
                        it.remove();
                        extended = true;
                        break;
                    }
                }
            }

            // extend backwards as well
            extended = true;
            while (!working.isEmpty() && extended) {
                extended = false;

                Vector2 firstPt = polyline.get(0);
                for (Iterator<Line> it = working.iterator(); it.hasNext(); ) {
                    Line l = it.next();
                    if (firstPt.equals(l.start)) {
                        polyline.add(0, l.end);
                        it.remove();
                        extended = true;
                        break;
                    } else if (firstPt.equals(l.end)) {
                        polyline.add(0, l.start);
                        it.remove();
                        extended = true;
                        break;
                    }
                }
            }

            result.add(polyline);
        }

        return result;
    }

    public static Set<Line> reconstructLines(List<Vector2> points) {
        Set<Line> result = new HashSet<>();
        for (int i = 0; i < points.size() - 1; i++) {
            result.add(new Line(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y));
        }
        return result;
    }

    /**
     * Generates the map
     *
     * @param worldWidthTiles  Width of the world in tiles
     * @param worldHeightTiles Height of the world in tiles
     * @param resolution       Does literally nothing, why is this here
     */

    public TiledMap generate(int worldWidthTiles, int worldHeightTiles, int resolution) {
        noise.init(worldWidthTiles * 32, worldHeightTiles * 32, 23, this.seed);
        TiledMap map = new TiledMap();
        layer = createGrid(worldWidthTiles, worldHeightTiles, resolution);
        mossLayer = new TiledMapTileLayer(worldWidthTiles, worldHeightTiles, 32, 32);
        pipeLayer = new TiledMapTileLayer(worldWidthTiles, worldHeightTiles + 20, 32, 32);
        AStarGrid = new Integer[worldWidthTiles + 3][worldHeightTiles + 3];
        Arrays.stream(AStarGrid).forEach(row -> Arrays.fill(row, 1));

        this.width = worldWidthTiles;
        this.height = worldHeightTiles;

        processIslands(20, 1);
        connectIslands();
        determineSpawnPoint();
        draw(layer);
        drawMossLayer();

        constructPipe(pipeLayer);
        constructPipe(pipeLayer);

        map.getLayers().add(layer);
        map.getLayers().add(mossLayer);
        map.getLayers().add(pipeLayer);

        return map;
    }

    private TiledMapTileLayer createGrid(int widthTiles, int heightTiles, int resolution) {
        gridVertices.clear();
        xMax = 0;
        yMax = 0;

        int buffer = 2;

        for (int j = -1; j <= heightTiles + 1; j++) {
            for (int i = -1; i <= widthTiles + 1; i++) {

                if (i < buffer || j < buffer || i > widthTiles - 1 - buffer || j > heightTiles - 1 - buffer) {
                    gridVertices.put(new Point(i, j), 0);
                } else {
                    int state = random.nextFloat() >= 0.5f ? 1 : 0;
                    gridVertices.put(new Point(i, j), state);
                }

                if (i > xMax) xMax = i;
                if (j > yMax) yMax = j;
            }
        }

        moistureMap = new double[widthTiles][heightTiles];

        for (int x = 0; x < widthTiles; x++) {
            for (int y = 0; y < heightTiles; y++) {

                float nx = x / 32.0f;
                float ny = y / 32.0f;

                moistureMap[x][y] = noise.sampleNoiseAt(nx, ny);
            }
        }


        return new TiledMapTileLayer(widthTiles + 2, heightTiles + 2, tileWidth, tileHeight);
    }

    private void draw(TiledMapTileLayer layer) {
        float tileWidthScaled = tileWidth / 32f;
        float tileHeightScaled = tileHeight / 32f;

        gridVertices.forEach((point, vecState) -> {
            int state = 0;

            if (point.x < xMax + 1 && point.y < yMax + 1) {
                Point tL = new Point(point.x, point.y + 1);
                Point tR = new Point(point.x + 1, point.y + 1);
                Point bR = new Point(point.x + 1, point.y);

                Integer bl = gridVertices.get(point);
                Integer br = gridVertices.get(bR);
                Integer tr = gridVertices.get(tR);
                Integer tl = gridVertices.get(tL);

                bl = bl == null ? 0 : bl;
                br = br == null ? 0 : br;
                tr = tr == null ? 0 : tr;
                tl = tl == null ? 0 : tl;
                state = getState(bl, br, tr, tl);

            }

            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();

            if (point.x >= 0 && point.y >= 0 &&
                point.x < AStarGrid.length && point.y < AStarGrid[0].length) {
                AStarGrid[point.x][point.y] = (state == 15) ? 0 : 1;
            }

            cell.setTile(TileLookup.getTile(state));
            collisionVertexes.addAll(TileLookup.getCollisionLines(state, point, tileWidthScaled, tileHeightScaled));

            int wallState = getStateAt(point.x, point.y);
            if (wallState == 6) {
                validPipeStarts.add(point);
            }

            layer.setCell(point.x, point.y, cell);
        });
    }

    private void drawMossLayer() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (AStarGrid[x][y] == 0 && moistureMap[x][y] > 0.6) {

                    double noiseValue = Math.random();

                    TiledMapTileLayer.Cell moss = new TiledMapTileLayer.Cell();
                    moss.setTile(mossTiles[(int) (noiseValue * 15)]);
                    mossLayer.setCell(x, y, moss);
                }
            }
        }
    }

    private int getState(int bl, int br, int tr, int tl) {
        return (bl) + (br << 1) + (tr << 2) + (tl << 3);
    }

    private void processIslands(int minSizeToKeep, int growthAmount) {
        Set<Point> visited = new HashSet<>();

        for (Point p : gridVertices.keySet()) {
            if (gridVertices.get(p) == 1 && !visited.contains(p)) {
                Set<Point> island = new HashSet<>();
                floodFill(p, island, visited);
                islands.add(island);
            }
        }

        for (Set<Point> island : islands) {
            if (island.size() < minSizeToKeep) {
                for (Point p : island) gridVertices.put(p, 0);
            }
        }

        for (Set<Point> island : islands) {
            if (island.size() >= minSizeToKeep) growIsland(island, growthAmount);
        }
    }

    private void floodFill(Point start, Set<Point> island, Set<Point> visited) {
        Queue<Point> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Point p = queue.poll();
            island.add(p);

            Point[] neighbors = {
                new Point(p.x + 1, p.y),
                new Point(p.x - 1, p.y),
                new Point(p.x, p.y + 1),
                new Point(p.x, p.y - 1)
            };

            for (Point n : neighbors) {
                if (gridVertices.containsKey(n) && gridVertices.get(n) == 1 && !visited.contains(n)) {
                    queue.add(n);
                    visited.add(n);
                }
            }
        }
    }

    private void growIsland(Set<Point> island, int growthAmount) {
        Set<Point> newCells = new HashSet<>();
        for (int i = 0; i < growthAmount; i++) {
            Set<Point> borderNeighbors = new HashSet<>();
            for (Point p : island) {
                Point[] neighbors = {
                    new Point(p.x + 1, p.y),
                    new Point(p.x - 1, p.y),
                    new Point(p.x, p.y + 1),
                    new Point(p.x, p.y - 1)
                };
                for (Point n : neighbors) {
                    if (gridVertices.containsKey(n) && gridVertices.get(n) == 0) {
                        borderNeighbors.add(n);
                    }
                }
            }
            island.addAll(borderNeighbors);
            newCells.addAll(borderNeighbors);
        }
        for (Point p : newCells) gridVertices.put(p, 1);
    }

    // Additional generation stuff

    private void connectIslands() {

        updateIslandsList();

        Set<Set<Point>> connected = new HashSet<>();
        Set<Set<Point>> unconnected = new HashSet<>(islands);

        for (Set<Point> island : islands) {
            if (island.stream().anyMatch(p -> p.x == 0)) {
                connected.add(island);
                unconnected.remove(island);
            }
        }

        if (connected.isEmpty()) {
            Set<Point> leftmost = islands.stream()
                .min(Comparator.comparingInt(island -> island.stream().mapToInt(p -> p.x).min().orElse(Integer.MAX_VALUE)))
                .orElse(null);
            if (leftmost != null) {
                connected.add(leftmost);
                unconnected.remove(leftmost);
            }
        }

        while (!unconnected.isEmpty()) {
            double minDist = Double.MAX_VALUE;
            Pair<Point, Point> closestPair = null;
            Set<Point> closestIsland = null;

            for (Set<Point> connIsland : connected) {
                for (Set<Point> unconnIsland : unconnected) {
                    Pair<Pair<Point, Point>, Double> result = closestPoints(connIsland, unconnIsland);
                    if (result.second < minDist) {
                        minDist = result.second;
                        closestPair = result.first;
                        closestIsland = unconnIsland;
                    }
                }
            }

            if (closestPair != null && closestIsland != null) {
                createPath(closestPair, 3);
                connected.add(closestIsland);
                unconnected.remove(closestIsland);
            } else {
                break;
            }
        }
    }

    private void createPath(Pair<Point, Point> points, int width) {
        List<Point> linePoints = new ArrayList<>();
        int x1 = points.first.x;
        int y1 = points.first.y;
        int x2 = points.second.x;
        int y2 = points.second.y;

        // Bresenham's line algorithm
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            linePoints.add(new Point(x1, y1));
            if (x1 == x2 && y1 == y2) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }

        for (Point p : linePoints) {
            for (int wx = -width / 2; wx <= width / 2; wx++) {
                for (int wy = -width / 2; wy <= width / 2; wy++) {
                    Point np = new Point(p.x + wx, p.y + wy);
                    if (gridVertices.containsKey(np)) {
                        gridVertices.put(np, 1); // Set to land
                    }
                }
            }
        }

    }

    // getBlank methods for data retrieval

    private Pair<Pair<Point, Point>, Double> closestPoints(Set<Point> island1, Set<Point> island2) {

        Point islandPoint1 = new Point();
        Point islandPoint2 = new Point();

        double minDistance = 10000;

        for (Point point1 : island1) {
            for (Point point2 : island2) {
                double dist = Math.sqrt(Math.pow(point2.x - point1.x, 2) + Math.pow(point2.y - point1.y, 2));
                if (dist < minDistance) {
                    minDistance = dist;
                    islandPoint1 = point1;
                    islandPoint2 = point2;
                }

            }
        }

        return new Pair<>(new Pair<>(islandPoint1, islandPoint2), minDistance);
    }

    private void updateIslandsList() {
        islands.clear();
        Set<Point> visited = new HashSet<>();

        for (Point p : gridVertices.keySet()) {
            if (gridVertices.get(p) == 1 && !visited.contains(p)) {
                Set<Point> island = new HashSet<>();
                floodFill(p, island, visited);
                islands.add(island);
            }
        }
    }

    private void determineSpawnPoint() {
        Point leftmostWalkable = null;
        Point rightmostWalkable = null;

        for (Map.Entry<Point, Integer> entry : gridVertices.entrySet()) {
            Point point = entry.getKey();

            // Only consider walkable tiles (state 15 = fully walkable)
            int bl = getStateAt(point.x, point.y); // implement a small helper
            if (bl != 15) continue;

            if (leftmostWalkable == null || point.x < leftmostWalkable.x) {
                leftmostWalkable = point;
            }
            if (rightmostWalkable == null || point.x > rightmostWalkable.x) {
                rightmostWalkable = point;
            }
        }

        // Fallback if none found
        if (leftmostWalkable != null) {
            spawnPoint.set(
                (leftmostWalkable.x + 0.5f) * tileWidth,
                (leftmostWalkable.y + 0.5f) * tileHeight
            );
        } else {
            spawnPoint.set(tileWidth * 0.5f, tileHeight * 0.5f);
        }

        if (rightmostWalkable != null) {
            endPoint.set(
                (rightmostWalkable.x + 0.5f) * tileWidth,
                (rightmostWalkable.y + 0.5f) * tileHeight
            );
        } else {
            endPoint.set(
                (width - 1 + 0.5f) * tileWidth,
                (height / 2 + 0.5f) * tileHeight
            );
        }
    }

    private int getStateAt(int x, int y) {
        int bl = gridVertices.getOrDefault(new Point(x, y), 0);
        int br = gridVertices.getOrDefault(new Point(x + 1, y), 0);
        int tr = gridVertices.getOrDefault(new Point(x + 1, y + 1), 0);
        int tl = gridVertices.getOrDefault(new Point(x, y + 1), 0);
        return getState(bl, br, tr, tl); // reuse your existing method
    }

    public Vector2 getSpawnPoint() {
        return spawnPoint;
    }

    public Set<Line> getCollisionLines() {
        Set<Line> collisionLines = new HashSet<>();
        for (List<Vector2> lines : orderOutline(collisionVertexes)) {
            collisionLines.addAll(reconstructLines(unifySegments(lines, 1e-1)));
        }

        return collisionLines;
    }

    public List<Vector2> getSpawnableAreas() {
        List<Vector2> spawnableAreas = new ArrayList<>();

        // Iterate through the AStarGrid to find walkable areas (where value is 0)
        for (int x = 0; x < AStarGrid.length; x++) {
            for (int y = 0; y < AStarGrid[0].length; y++) {
                if (AStarGrid[x][y] != null && AStarGrid[x][y] == 0) {
                    // Convert grid coordinates to world coordinates
                    Vector2 worldPos = new Vector2(
                        (x + 0.5f) * tileWidth,
                        (y + 0.5f) * tileHeight
                    );
                    spawnableAreas.add(worldPos);
                }
            }
        }

        return spawnableAreas;
    }

    public synchronized Integer[][] getAStarGridSnapshot() {
        Integer[][] copy = new Integer[AStarGrid.length][];
        for (int i = 0; i < AStarGrid.length; i++) {
            if (AStarGrid[i] != null) {
                copy[i] = Arrays.copyOf(AStarGrid[i], AStarGrid[i].length);
            }
        }
        return copy;
    }

    public Vector2 getEndPoint() {
        return endPoint;
    }

    public Set<Rectangle> getSubroomEntrance() {
        return subroomEntrance;
    }

    private Point findPipeStart() {

        Collections.shuffle(validPipeStarts);
        Point samplePoint = validPipeStarts.get(0);

        return samplePoint;
    }

    private void constructPipe(TiledMapTileLayer pipeLayer) {

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

                if (segment.second == DirectionOfTravel.NORTH) {
                    collisionVertexes.removeAll(TileLookup.getCollisionLines(3, segment.first, tileWidth / 32f, tileHeight / 32f));
                    subroomEntrance.add(new Rectangle(segment.first.x, segment.first.y + 11 / 16f, tileWidth / 32f, (tileHeight / 32f) * 5 / 16f));
                }
            }

        });

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

    public static Set<Line> loadCollisionLinesFromMap(TiledMap map) {
        Set<Line> lines = new HashSet<>();

        MapLayer collisionLayer = map.getLayers().get("Collisions");
        if (collisionLayer == null) {
            Gdx.app.log("MapGenerator", "No 'Collisions' layer found in map!");
            return lines;
        }
        float UNIT_SCALE = 1 / 32f;

        for (MapObject object : collisionLayer.getObjects()) {
            if (object instanceof PolylineMapObject) {
                Polyline polyline = ((PolylineMapObject) object).getPolyline();
                float[] vertices = polyline.getTransformedVertices();

                Gdx.app.log("Collision", "Polyline with " + vertices.length / 2 + " points");

                for (int i = 0; i < vertices.length - 2; i += 2) {
                    Gdx.app.log("Collision", "Raw vertex: " + vertices[i] + ", " + vertices[i + 1]);
                    float x1 = vertices[i] * UNIT_SCALE;
                    float y1 = vertices[i + 1] * UNIT_SCALE;
                    float x2 = vertices[i + 2] * UNIT_SCALE;
                    float y2 = vertices[i + 3] * UNIT_SCALE;

                    lines.add(new Line(new Vector2(x1, y1), new Vector2(x2, y2)));
                }

            } else {
                Gdx.app.log("Collision", "Non-polyline object: " + object);
                float x = object.getProperties().get("x", Float.class) / 32f;
                float y = object.getProperties().get("y", Float.class) / 32f;
                Gdx.app.log("MapGenerator", "Ignored object (Point?) at " + x + ", " + y);
            }
        } return lines;
    }
}
