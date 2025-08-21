package com.mjolkster.artifice.entities;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mjolkster.artifice.screen.GameScreen;
import com.mjolkster.artifice.util.tools.AStarPathfinder;
import com.mjolkster.artifice.util.wrappers.Line;
import com.mjolkster.artifice.util.Sprite;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class NonPlayableCharacter extends Entity {

    private final PlayableCharacter target;
    private final Rectangle hitbox;
    private Queue<Vector2> currentPath = new LinkedList<>();
    private final AStarPathfinder pathfinder = new AStarPathfinder();
    private float distanceTravelled = 0f;

    public NonPlayableCharacter(Vector2 spawnPoint, PlayableCharacter player) {
        super(10, 0, 10f, 0, new Sprite("ChestSprite.png", 4, 5, 0.2f));
        this.x = spawnPoint.x / 32f;
        this.y = spawnPoint.y / 32f;
        this.target = player;

        this.hitbox = new Rectangle(x, y, 1f, 1f);

        sprite.setDirection(Sprite.Direction.IDLE);

    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    @Override
    public void update(float delta, OrthographicCamera camera, Set<Line> collisionBoxes) {

        if (health > 0){
            followPath(delta);
            sprite.update(delta);
            sprite.getHitbox().setPosition(x, y);
        }
    }

    public void recalculatePath() {
        Vector2 start = new Vector2((int) x, (int) y);
        Vector2 goal = new Vector2((int) target.getX(), (int) target.getY());
        currentPath = pathfinder.createAStarPathfinder(start, goal);
        System.out.println("NPC at " + start + " path to " + goal + " length " + currentPath.size());
        distanceTravelled = 0f;
    }

    private void followPath(float delta) {
        if (currentPath == null || currentPath.isEmpty()) return;
        if (distanceTravelled >= moveDistance) return; // stop for this turn

        float moveSpeed = 2f;

        Vector2 nextPoint = currentPath.peek();
        Vector2 targetPos = new Vector2(nextPoint.x, nextPoint.y);

        boolean blocked = false;
        for (NonPlayableCharacter other : GameScreen.NPCs) {
            if (other == this) continue;

            int ox = (int) other.getX();
            int oy = (int) other.getY();
            if (ox == (int) targetPos.x && oy == (int) targetPos.y) {
                blocked = true;
                break;
            }
        }

        if (blocked) {

            Vector2 separation = new Vector2(0, 0);
            for (NonPlayableCharacter other : GameScreen.NPCs) {
                if (other == this) continue;

                float dx = x - other.getX();
                float dy = y - other.getY();
                float dist2 = dx * dx + dy * dy;

                if (dist2 < .5f * .5f && dist2 > 0.01f) {
                    Vector2 push = new Vector2(dx, dy).scl(0.2f / dist2);
                    separation.add(push);
                }
            }

            if (!separation.isZero()) {
                separation.nor();
                x += separation.x * moveSpeed * delta * 0.5f;
                y += separation.y * moveSpeed * delta * 0.5f;
            }

        } else {

            Vector2 direction = targetPos.cpy().sub(x, y);

            if (direction.len() < 0.05f) {
                x = targetPos.x;
                y = targetPos.y;
                currentPath.poll();
            } else {
                direction.nor();
                float dx = direction.x * moveSpeed * delta;
                float dy = direction.y * moveSpeed * delta;

                x += dx;
                y += dy;
                distanceTravelled += (float) Math.sqrt(dx * dx + dy * dy);
            }
        }

        hitbox.setPosition(x, y);
    }

    public void drawPath(ShapeRenderer shapeRenderer) {
        if (currentPath == null || currentPath.isEmpty()) return;

        float lastX = x + 0.5f;
        float lastY = y + 0.5f;

        for (Vector2 point : currentPath) {
            float nextX = point.x;
            float nextY = point.y;
            shapeRenderer.line(lastX, lastY, nextX, nextY);
            lastX = nextX;
            lastY = nextY;
        }

    }

}
