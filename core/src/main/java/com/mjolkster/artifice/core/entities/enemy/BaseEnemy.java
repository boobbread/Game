package com.mjolkster.artifice.core.entities.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mjolkster.artifice.core.entities.Entity;
import com.mjolkster.artifice.core.entities.PlayableCharacter;
import com.mjolkster.artifice.core.world.ChestEntity;
import com.mjolkster.artifice.graphics.screen.GameScreen;
import com.mjolkster.artifice.util.geometry.Hitbox;
import com.mjolkster.artifice.util.graphics.Sprite;
import com.mjolkster.artifice.util.ai.AStarPathfinder;
import com.mjolkster.artifice.util.geometry.Line;
import com.mjolkster.artifice.util.math.Gaussian;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

// TODO add more enemies
public abstract class BaseEnemy extends Entity {

    protected final PlayableCharacter target;
    protected Hitbox hitbox;
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
    protected boolean hasSpawnedChest = false;

    /**
     * Constructor for generic enemy.
     * Subclasses define stats and sprite.
     * @param spawnPoint The spawn location, in world-pixel coordinates
     * @param target The target of the pathfinding algorithm e.g. the player
     * @param sprite The {@link com.mjolkster.artifice.util.graphics.Sprite Sprite} for the enemy
     * @param health The health of the enemy
     * @param moveDistance The maximum distance the enemy can move in a turn
     * @param gameScreen The {@link com.mjolkster.artifice.graphics.screen.GameScreen GameScreen} on which to spawn the enemy
     */

    public BaseEnemy(Vector2 spawnPoint, PlayableCharacter target, Sprite sprite, int health, float moveDistance, GameScreen gameScreen) {
        super(health, 0, moveDistance, 0, sprite, gameScreen);
        this.target = target;

        this.x = spawnPoint.x / 32f;
        this.y = spawnPoint.y / 32f;
    }

    public abstract Hitbox getHitbox();

    /**
     * Main update loop
     */
    public void update(float delta, OrthographicCamera camera, Set<Line> collisionBoxes) {

        this.hitbox = this.sprite.getHitbox();

        if (state == NPCState.ALIVE && health <= 0) {
            state = NPCState.DYING;
            deathTimer = 0f;
        }

        switch (state) {
            case ALIVE:
                followPath(delta);
                sprite.update(delta);
                sprite.getHitbox().setOrigin(x, y);

                if (!hasCompletedTurn) {
                    performAction();
                }

                break;

            case DYING:
                deathTimer += delta;
                sprite.flashRed();
                sprite.update(delta);
                if (deathTimer >= 1f) state = NPCState.DEAD;
                sprite.playDeathEffect(this.x + 0.5f, this.y + 0.5f);
                break;

            case DEAD:

                break;
        }
    }

    /**
     * Abstract method: subclasses define custom actions (attack, skills, etc.)
     */
    public abstract void performAction();

    /**
     * Default damage behavior for simple melee enemies
     */
    protected abstract void damagePlayer(PlayableCharacter target);

    public void recalculatePath() {
        Vector2 start = new Vector2(x, y);
        Vector2 goal = new Vector2(
            target.collisionBox.getBounds().x + target.collisionBox.getBounds().getWidth() / 2f,
            target.collisionBox.getBounds().y + target.collisionBox.getBounds().getHeight() / 2f
        );
        Vector2 goalTile = new Vector2(Math.round(goal.x), Math.round(goal.y));

        Queue<Vector2> proposedPath = pathfinder.createAStarPathfinder(start, goalTile, gameScreen);
        if (!proposedPath.isEmpty()) {
            currentPath = proposedPath;
        }
        currentPath.poll();
        distanceTravelled = 0f;
        hasCompletedTurn = false;
    }

    public void followPath(float delta) {

        if ((currentPath == null || currentPath.isEmpty() || distanceTravelled >= moveDistance)) {
            if (this instanceof WaspEnemy) {
                hasCompletedTurn = ((WaspEnemy) this).hasFinishedAttack;
            } else hasCompletedTurn = true;
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

        hitbox.setOrigin(x, y);
    }

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
            distanceTravelled += (float) Math.sqrt(dx * dx + dy * dy);

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
            sprite.setDirection(Sprite.Direction.DOWN);
        } else if (angle >= 135 && angle < 225) {
            sprite.setDirection(Sprite.Direction.LEFT);
        } else if (angle >= 225 && angle < 315) {
            sprite.setDirection(Sprite.Direction.UP);
        } else {
            sprite.setDirection(Sprite.Direction.RIGHT);
        }
    }

    protected void handleBlocking(float delta) {
        blockTimer += delta;
        if (blockTimer > 0.5f) {
            currentPath.poll();
            blockTimer = 0f;
        }

        Vector2 separation = new Vector2();
        for (BaseEnemy other : gameScreen.getNPCs()) {
            if (other == this) continue;
            float dx = x - other.getX();
            float dy = y - other.getY();
            float dist2 = dx * dx + dy * dy;
            if (dist2 < 0.25f && dist2 > 0.01f) {
                separation.add(new Vector2(dx, dy).scl(0.2f / dist2));
            }
        }

        if (!separation.isZero()) {
            separation.nor();
            x += separation.x * 0.5f * delta;
            y += separation.y * 0.5f * delta;
        }
    }

    protected boolean isBlocked(Vector2 targetPos) {
        for (BaseEnemy other : gameScreen.getNPCs()) {
            if (other == this) continue;
            if ((int) other.getX() == (int) targetPos.x && (int) other.getY() == (int) targetPos.y) return true;
        }

        int ox = (int) target.getX();
        int oy = (int) target.getY();
        return ox == (int) targetPos.x && oy == (int) targetPos.y;
    }

    /**
     * Debug feature for checking the pathing is correct
     */
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

    public void damagedPlayer(boolean result) {
        hasDamagedPlayer = result;
    }

    public boolean hasSpawnedChest() {
        return hasSpawnedChest;
    }

    public boolean hasCompletedMove() {
        return hasCompletedTurn;
    }

    public NPCState getState() {
        return state;
    }

    public enum NPCState {
        ALIVE,
        DYING,
        DEAD
    }

}
