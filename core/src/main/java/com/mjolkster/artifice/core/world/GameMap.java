package com.mjolkster.artifice.core.world;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mjolkster.artifice.core.entities.PlayableCharacter;
import com.mjolkster.artifice.core.world.generation.LineHandler;
import com.mjolkster.artifice.core.world.generation.MapGenerator;
import com.mjolkster.artifice.util.geometry.Line;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GameMap {
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final World world;
    private final RayHandler rayHandler;
    private final Set<Line> collisionBoxes;
    private final Vector2 spawnpoint;
    private final Vector2 endPointPosition;
    private final List<Vector2> spawnableAreas;
    private final MapGenerator mapGenerator;
    private final PointLight playerLight;

    public GameMap(long seed) {
        this.mapGenerator = new MapGenerator(32, 32, seed);
        this.map = mapGenerator.generate(64, 16, 6);

        this.spawnpoint = mapGenerator.getSpawnPoint();
        this.endPointPosition = mapGenerator.getEndPoint();
        this.spawnableAreas = mapGenerator.getSpawnableAreas();

        this.renderer = new OrthogonalTiledMapRenderer(map, 1f / 32f);

        this.world = new World(new Vector2(0, -9.8f), true);
        this.rayHandler = new RayHandler(world);
        this.rayHandler.setAmbientLight(0.0f);
        this.rayHandler.setBlur(true);
        this.rayHandler.setCulling(true);

        this.playerLight = new PointLight(rayHandler, 300);
        this.playerLight.setColor(Color.GOLDENROD);
        this.playerLight.setDistance(5f);
        this.playerLight.setSoft(true);
        this.playerLight.setSoftnessLength(0.5f);

        this.collisionBoxes = mapGenerator.getCollisionLines();
        List<List<Vector2>> outlines = LineHandler.orderOutline(collisionBoxes);
        createBodiesFromPolygons(world, outlines);

        Gdx.app.log("GameMap", "Initialisation complete");
    }

    // Rendering
    public void render(OrthographicCamera camera) {
        renderer.setView(camera);
        renderer.render();
    }

    public void renderLighting(OrthographicCamera camera, PlayableCharacter player) {
        playerLight.setPosition(
            player.x + 0.5f,
            player.y + 0.5f);
        playerLight.update();
        rayHandler.setCombinedMatrix(camera);
        rayHandler.updateAndRender();
    }

    public void step(float delta) {
        world.step(delta, 6, 2);
    }

    public void dispose() {
        map.dispose();
        renderer.dispose();
        rayHandler.dispose();
        world.dispose();
    }

    // Collision creation
    public List<Body> createBodiesFromPolygons(World world, List<List<Vector2>> polygons) {
        List<Body> bodies = new ArrayList<>();

        for (List<Vector2> loop : polygons) {
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            Body body = world.createBody(bodyDef);

            for (int i = 0; i < loop.size(); i++) {
                Vector2 current = loop.get(i);
                Vector2 next = loop.get((i + 1) % loop.size());

                if (current.epsilonEquals(next, 0.001f)) {
                    continue;
                }

                EdgeShape edgeShape = new EdgeShape();
                edgeShape.set(current, next);

                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.shape = edgeShape;
                fixtureDef.density = 0f;
                fixtureDef.friction = 0.5f;

                body.createFixture(fixtureDef);
                edgeShape.dispose();
            }

            bodies.add(body);
        }

        return bodies;
    }

    // Getters
    public TiledMap getMap() { return map; }
    public OrthogonalTiledMapRenderer getRenderer() { return renderer; }
    public RayHandler getRayHandler() { return rayHandler; }
    public Set<Line> getCollisionBoxes() { return collisionBoxes; }
    public World getWorld() { return world; }
    public Vector2 getPlayerSpawnpoint() { return spawnpoint; }
    public Vector2 getEndPointPosition() { return endPointPosition; }
    public List<Vector2> getSpawnableAreas() { return spawnableAreas; }
    public MapGenerator getMapGenerator() { return mapGenerator; }
}
