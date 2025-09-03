package com.mjolkster.artifice.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mjolkster.artifice.screen.GameScreen;
import com.mjolkster.artifice.util.Sprite;
import com.mjolkster.artifice.util.tools.AStarPathfinder;
import com.mjolkster.artifice.util.wrappers.Gaussian;
import com.mjolkster.artifice.util.wrappers.Line;

import java.lang.reflect.Array;
import java.util.*;

public abstract class NonPlayableCharacter extends Entity {

    protected final PlayableCharacter target;
    protected final Rectangle hitbox;
    protected final AStarPathfinder pathfinder = new AStarPathfinder();
    protected Queue<Vector2> currentPath = new LinkedList<>();
    protected float distanceTravelled = 0f;
    protected float blockTimer = 0f;

    protected boolean hasCompletedTurn = true;
    protected boolean hasDamagedPlayer = false;
    protected Vector2 averageDirection = new Vector2();

    // Life / death states
    protected NPCState state = NPCState.ALIVE;
    protected float deathTimer = 0f;

    public enum NPCState {
        ALIVE,
        DYING,
        DEAD
    }

    protected boolean hasSpawnedChest = false;

    /**
     * Constructor for generic enemy.
     * Subclasses define stats and sprite.
     */
    public NonPlayableCharacter(Vector2 spawnPoint, PlayableCharacter target, Sprite sprite, int health, float moveDistance, GameScreen gameScreen) {
        super(health, 0, moveDistance, 0, sprite, gameScreen);
        this.target = target;

        this.x = spawnPoint.x / 32f;
        this.y = spawnPoint.y / 32f;
        this.hitbox = new Rectangle(x, y + 1/32f, 1f, 16/32f);

        this.sprite.setDirection(Sprite.Direction.IDLE);
    }

    public Rectangle getHitbox() { return hitbox; }

    /** Main update loop */
    public void update(float delta, OrthographicCamera camera, Set<Line> collisionBoxes) {

        if (state == NPCState.ALIVE && health <= 0) {
            state = NPCState.DYING;
            deathTimer = 0f;
        }

        switch (state) {
            case ALIVE:
                followPath(delta);
                sprite.update(delta);
                sprite.getHitbox().setPosition(x, y);

                if (!hasCompletedTurn) {
                    performAction();
                }

                break;

            case DYING:
                deathTimer += delta;
                sprite.update(delta);
                if (deathTimer >= 1f) state = NPCState.DEAD;
                break;

            case DEAD:
                handleDeath();
                break;
        }
    }

    /** Abstract method: subclasses define custom actions (attack, skills, etc.) */
    public abstract void performAction();

    /** Default damage behavior for simple melee enemies */
    protected void damagePlayer(PlayableCharacter target) {
        if (hitbox.overlaps(target.collisionBox.getBounds()) && !hasDamagedPlayer) {
            target.changeHealth(-1);
            hasDamagedPlayer = true;
        }
    }

    /** Pathfinding recalculation */
    public void recalculatePath() {
        Vector2 start = new Vector2(x, y);
        Vector2 goal = new Vector2((int)Math.ceil(target.getX()), (int)Math.ceil(target.getY()));
        Queue<Vector2> proposedPath = pathfinder.createAStarPathfinder(start, goal, gameScreen);
        if (!proposedPath.isEmpty()) {
            currentPath = proposedPath;
        }
        currentPath.poll();
        distanceTravelled = 0f;
        hasCompletedTurn = false;
    }

    /** Follow the current A* path */
    public void followPath(float delta) {

        if (currentPath == null || currentPath.isEmpty() || distanceTravelled >= moveDistance) {
            hasCompletedTurn = true;
            return;
        }

        float moveSpeed = 1.5f;
        Vector2 nextPoint = currentPath.peek();
        Vector2 targetPos = new Vector2(nextPoint.x, nextPoint.y);

        boolean blocked = isBlocked(targetPos);

        if (blocked) {
            handleBlocking(delta);
        } else {
            if (moveTowards(targetPos, delta, moveSpeed)) {
                recalculatePath();
            }
        }

        hitbox.setPosition(x, y);
    }

    /** Movement helper */
    protected boolean moveTowards(Vector2 targetPos, float delta, float moveSpeed) {
        Vector2 direction = targetPos.cpy().sub(x, y);
        if (direction.len() < 0.05f) {
            x = targetPos.x;
            y = targetPos.y;
            currentPath.poll();
            return true;
        } else {
            direction.nor();
            float dx = direction.x * moveSpeed * delta;
            float dy = direction.y * moveSpeed * delta;
            x += dx;
            y += dy;
            distanceTravelled += (float)Math.sqrt(dx*dx + dy*dy);

            averageDirection.lerp(new Vector2(dx, dy).nor(), 0.2f);
            updateSpriteDirection();
        }
        return false;
    }

    protected void updateSpriteDirection() {
        if (averageDirection.isZero(0.05f)) {
            sprite.setDirection(Sprite.Direction.IDLE);
            return;
        }

        float angle = averageDirection.angleDeg();

        if (angle >= 45 && angle < 135) {
            sprite.setDirection(Sprite.Direction.UP);
        } else if (angle >= 135 && angle < 225) {
            sprite.setDirection(Sprite.Direction.DOWN);
        } else if (angle >= 225 && angle < 315) {
            sprite.setDirection(Sprite.Direction.RIGHT);
        } else {
            sprite.setDirection(Sprite.Direction.LEFT);
        }
    }


    /** Blocking / collision with other NPCs */
    protected void handleBlocking(float delta) {
        blockTimer += delta;
        if (blockTimer > 0.5f) {
            currentPath.poll();
            blockTimer = 0f;
        }

        Vector2 separation = new Vector2();
        for (NonPlayableCharacter other : gameScreen.getNPCs()) {
            if (other == this) continue;
            float dx = x - other.getX();
            float dy = y - other.getY();
            float dist2 = dx*dx + dy*dy;
            if (dist2 < 0.25f && dist2 > 0.01f) {
                separation.add(new Vector2(dx, dy).scl(0.2f/dist2));
            }
        }

        if (!separation.isZero()) {
            separation.nor();
            x += separation.x * 0.5f * delta;
            y += separation.y * 0.5f * delta;
        }
    }

    /** Check if tile is blocked by other NPCs or the player */
    protected boolean isBlocked(Vector2 targetPos) {
        for (NonPlayableCharacter other : gameScreen.getNPCs()) {
            if (other == this) continue;
            if ((int)other.getX() == (int)targetPos.x && (int)other.getY() == (int)targetPos.y) return true;
        }

        int ox = (int)target.getX();
        int oy = (int)target.getY();
        return ox == (int)targetPos.x && oy == (int)targetPos.y;
    }

    /** Death logic, e.g., spawning loot */
    protected void handleDeath() {
        if (!hasSpawnedChest) {
            if (Math.random() > 0.7f) {
                spawnChest();
            }
            hasSpawnedChest = true;
        }
    }

    protected void spawnChest() {
        ChestEntity chest = new ChestEntity(10, 20, 0, 0, gameScreen);
        Rectangle playerBounds = target.collisionBox.getBounds();
        Rectangle chestBox = new Rectangle(x, y, 0.5f, 0.5f);

        if (!chestBox.overlaps(playerBounds)) {
            chest.spawnChest(x, y);
        } else chest.spawnChest(x+1, y+1);

        gameScreen.chests.add(chest);
    }

    /** Draw A* path (debug) */
    public void drawPath(ShapeRenderer shapeRenderer) {
        if (currentPath == null || currentPath.isEmpty()) return;
        float lastX = x + 0.5f;
        float lastY = y + 0.5f;

        for (Vector2 point : currentPath) {
            shapeRenderer.line(lastX, lastY, point.x, point.y);
            lastX = point.x;
            lastY = point.y;
        }
    }

    public boolean hasDamagedPlayer() {
        return hasDamagedPlayer;
    }

    public void damagedPlayer(boolean result) {
        hasDamagedPlayer = result;
    }

    public boolean hasSpawnedChest() { return hasSpawnedChest; }
    public boolean hasCompletedMove() { return hasCompletedTurn; }
    public NPCState getState() { return state; }

}
