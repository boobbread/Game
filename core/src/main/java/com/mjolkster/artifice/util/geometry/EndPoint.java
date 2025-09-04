package com.mjolkster.artifice.util.geometry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mjolkster.artifice.core.GameClass;
import com.mjolkster.artifice.io.FileHandler;
import com.mjolkster.artifice.registry.registries.ItemRegistry;
import com.mjolkster.artifice.graphics.screen.GameScreen;

public class EndPoint {

    private final GameScreen gameScreen;
    GameClass game;
    private final Vector2 position;
    private final TextureRegion closedFrame;
    private final Animation<TextureRegion> openingAnimation;
    private final TextureRegion openFrame;
    private final Rectangle hitbox;
    private State currentState;
    private float stateTime;
    private boolean playerCanInteract;
    public EndPoint(Vector2 position, GameScreen gameScreen) {
        this.position = new Vector2(position.x / 32, position.y / 32);
        this.stateTime = 0;
        this.currentState = State.CLOSED;
        this.playerCanInteract = false;

        game = GameScreen.game;
        this.gameScreen = gameScreen;

        // Create hitbox (adjust size as needed)
        this.hitbox = new Rectangle(this.position.x, this.position.y, 1f, 1f);

        // Load texture and create animations
        Texture texture = new Texture(Gdx.files.internal("endpoint.png"));
        TextureRegion[][] frames = TextureRegion.split(texture, 32, 32);

        closedFrame = frames[0][0];

        TextureRegion[] openingFrames = new TextureRegion[frames[0].length];
        System.arraycopy(frames[0], 0, openingFrames, 0, frames[0].length);

        openingAnimation = new Animation<>(0.05f, openingFrames);
        openingAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        openFrame = frames[0][frames[0].length - 1];
    }

    public void update(float delta) {
        stateTime += delta;

        // Handle state transitions
        if (currentState == State.OPENING) {
            // Check if opening animation is complete
            if (openingAnimation.isAnimationFinished(stateTime)) {
                currentState = State.OPEN;
                playerCanInteract = true;
                stateTime = 0; // Reset for open animation
            }
        }

        if (currentState == State.OPEN) {
            if (gameScreen.getPlayer().collisionBox.overlaps(hitbox)) {
                interact();
            }
        }
    }

    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = null;

        switch (currentState) {
            case CLOSED:
                currentFrame = closedFrame;
                break;
            case OPENING:
                currentFrame = openingAnimation.getKeyFrame(stateTime);
                break;
            case OPEN:
                currentFrame = openFrame;
                break;
        }

        if (currentFrame != null) {
            batch.draw(currentFrame, position.x, position.y, 1f, 1f);
        }
    }

    public void open() {
        if (currentState == State.CLOSED) {
            currentState = State.OPENING;
            stateTime = 0;
        }
    }

    public boolean canInteract() {
        return playerCanInteract;
    }

    public boolean interact() {
        if (playerCanInteract) {

            if (gameScreen.getPlayer().invPerm.getContents().contains(ItemRegistry.nest_feather.get())) {
                gameScreen.getPlayer().changeHealth(3);
            }

            FileHandler.CreateNewSave(gameScreen.getPlayer(), gameScreen.getSeed(), GameScreen.playerSlotNumber);
            gameScreen.requestRestart();

            return true;
        }
        return false;
    }

    // Getters
    public Vector2 getPosition() {
        return position;
    }

    public State getState() {
        return currentState;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public enum State {
        CLOSED,
        OPENING,
        OPEN
    }
}
