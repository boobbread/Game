package com.mjolkster.artifice.graphics.screen;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mjolkster.artifice.core.entities.PlayableCharacter;
import com.mjolkster.artifice.core.world.EntityManager;
import com.mjolkster.artifice.core.world.generation.LineHandler;
import com.mjolkster.artifice.graphics.viewports.AspectRatioViewport;
import com.mjolkster.artifice.util.geometry.Line;
import com.mjolkster.artifice.core.world.generation.MapGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A sublevel is a self-contained TMX map that the player
 * can enter via a portal (e.g. pipe) from the overworld.
 */
public class SublevelScreen implements Screen {
    private final OrthographicCamera camera;
    private final EntityManager entityManager;

    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final World world;
    private final RayHandler rayHandler;
    private final PointLight playerLight;
    private final Viewport viewport;

    private final SpriteBatch spriteBatch;

    private final Set<Line> collisionBoxes;

    private final Box2DDebugRenderer debugRenderer;

    public SublevelScreen(String tmxPath, EntityManager entityManager, Runnable onExitCallback) {
        this.entityManager = entityManager;

        this.camera = new OrthographicCamera();
        this.viewport = new AspectRatioViewport(6f, camera);

        this.map = new TmxMapLoader().load(tmxPath);
        this.renderer = new OrthogonalTiledMapRenderer(map, 1/32f);
        this.debugRenderer = new Box2DDebugRenderer(true, false, false, false, false, false);

        this.world = new World(new Vector2(0, -9.8f), true);
        this.rayHandler = new RayHandler(world);
        this.rayHandler.setAmbientLight(0.2f);

        this.spriteBatch = new SpriteBatch();

        this.playerLight = new PointLight(rayHandler, 300);
        this.playerLight.setColor(Color.GOLDENROD);
        this.playerLight.setDistance(5f);
        this.playerLight.setSoft(true);

        // Load collision from TMX
        this.collisionBoxes = MapGenerator.loadCollisionLinesFromMap(map);
        List<List<Vector2>> outlines = LineHandler.orderOutline(collisionBoxes);

        // TODO: place player at sublevel spawnpoint (parse from object layer)
        entityManager.getPlayer().x = 5;
        entityManager.getPlayer().y = 2;

        Gdx.app.log("Sublevel", "Loaded sublevel from " + tmxPath);
    }

    @Override
    public void render(float delta) {
        // Step physics
        world.step(delta, 6, 2);

        // Update player
        entityManager.update(delta, camera, collisionBoxes);

        // Update camera to follow player
        PlayableCharacter player = entityManager.getPlayer();
        float playerCenterX = player.x + player.collisionBox.getBounds().width / 2f;
        float playerCenterY = player.y + player.collisionBox.getBounds().height / 2f;
        camera.position.set(playerCenterX, playerCenterY, 0);
        camera.update();

        // Render map
        renderer.setView(camera);
        renderer.render();

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        entityManager.render(spriteBatch);
        spriteBatch.end();

        // Render lighting
        playerLight.setPosition(playerCenterX, playerCenterY);
        playerLight.update();
        rayHandler.setCombinedMatrix(camera);
        rayHandler.updateAndRender();

        debugRenderer.render(world, camera.combined);

        // Check exit condition (e.g. touching exit portal)
//        if (player.collisionBox.getBounds().overlaps()) {
//            onExitCallback.run(); // jump back to overworld
//        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.update();
    }

    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        rayHandler.dispose();
        world.dispose();
    }

    private static final float UNIT_SCALE = 1f / 32f;

    private List<Body> createBodiesFromPolygons(World world, List<List<Vector2>> polygons) {
        List<Body> bodies = new ArrayList<>();

        for (List<Vector2> loop : polygons) {
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            Body body = world.createBody(bodyDef);

            for (int i = 0; i < loop.size(); i++) {
                Vector2 current = loop.get(i).scl(UNIT_SCALE); // convert px → meters
                Vector2 next = loop.get((i + 1) % loop.size()).scl(UNIT_SCALE);

                if (current.epsilonEquals(next, 0.001f)) continue;

                EdgeShape edge = new EdgeShape();
                edge.set(current, next);

                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.shape = edge;
                fixtureDef.friction = 0.5f;

                body.createFixture(fixtureDef);
                edge.dispose();
            }

            bodies.add(body);
        }
        return bodies;
    }

}
