package com.mjolkster.artifice.core.world.generation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
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
    private final Random random;
    public final HashMap<Point, Integer> gridVertices = new HashMap<>();
    private final List<Point> validPipeStarts = new ArrayList<>();
    private final int tileWidth;
    private final int tileHeight;
    public int width;
    public int height;
    public Set<Line> collisionVertexes = new HashSet<>();
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

        random = new Random(seed);
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

        IslandManager islandManager = new IslandManager(gridVertices, islands);
        islandManager.processIslands(20, 1);
        islandManager.connectIslands();

        determineSpawnPoint();
        draw(layer);

        MossGenerator mossGenerator = new MossGenerator(tileWidth, tileHeight, width, height, seed);
        mossGenerator.drawMossLayer(AStarGrid, mossLayer);

        PipeGenerator pipeGenerator = new PipeGenerator(validPipeStarts, collisionVertexes, pipeLayer, tileWidth, tileHeight, gridVertices);
        pipeGenerator.constructPipe();
        pipeGenerator.constructPipe();

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

    private int getState(int bl, int br, int tr, int tl) {
        return (bl) + (br << 1) + (tr << 2) + (tl << 3);
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

    public int getStateAt(int x, int y) {
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
        for (List<Vector2> lines : LineHandler.orderOutline(collisionVertexes)) {
            collisionLines.addAll(LineHandler.reconstructLines(LineHandler.unifySegments(lines, 1e-1)));
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
