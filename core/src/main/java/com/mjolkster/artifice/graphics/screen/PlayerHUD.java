package com.mjolkster.artifice.graphics.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mjolkster.artifice.core.entities.PlayableCharacter;
import com.mjolkster.artifice.core.items.ConsumableItem;
import com.mjolkster.artifice.core.items.Item;
import com.mjolkster.artifice.registry.RegistryManager;
import com.mjolkster.artifice.graphics.viewports.ExpandingViewport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerHUD {
    private final Stage stage;
    private final Skin skin;
    private final BitmapFont font;

    private final Label fpsLabel;
    private final ProgressBar healthBar;
    private final ProgressBar movementBar;
    private final Label apSlot;

    private final List<ImageButton> tempSlots = new ArrayList<>();
    private final Map<ImageButton, Item> tempSlotItems = new HashMap<>();
    private final List<ImageButton> permSlots = new ArrayList<>();

    private final PlayableCharacter player;

    public PlayerHUD(PlayableCharacter player) {
        this.player = player;

        // UI camera + stage
        Viewport uiViewport = new ExpandingViewport(90f, true, new OrthographicCamera());
        this.stage = new Stage(uiViewport);
        Gdx.input.setInputProcessor(stage);

        this.skin = new Skin(Gdx.files.internal("GUI/GUISkin.json"));
        this.font = new BitmapFont();

        // --- FPS Label ---
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        fpsLabel = new Label("FPS: 0", labelStyle);
        fpsLabel.setAlignment(Align.topLeft);
        fpsLabel.setPosition(20, Gdx.graphics.getHeight() - 20);
        stage.addActor(fpsLabel);

        // Root table
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Load background images
        Image leftColImage = new Image(skin, "GUILeft");
        Image centerColImage = new Image(skin, "GUICenter");
        Image rightColImage = new Image(skin, "GUIRight");

        // === LEFT COLUMN ===
        Table leftCol = new Table();
        leftCol.left().bottom();

        // Temporary inventory
        Table tempInv = new Table();
        for (int i = 0; i < 3; i++) {
            ImageButton slot = new ImageButton(skin, "GUIslot");
            final int slotIndex = i;
            slot.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
                @Override
                public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                    Item item = tempSlotItems.get(slot);
                    if (item != null) {
                        if (RegistryManager.ITEMS.get(item.getItemName()) instanceof ConsumableItem) {
                            player.applyBonus(item.getBonus().first, item.getBonus().second, 0);
                            player.invTemp.removeItemFromSlot(slotIndex);
                            tempSlotItems.replace(slot, null);
                        }
                    }
                }
            });
            tempInv.add(slot).size(46).padLeft(21).padRight(21);
            tempSlots.add(slot);
        }
        tempInv.padLeft(2).padBottom(46);
        leftCol.add(tempInv);
        leftCol.row();

        // Health bar
        healthBar = new ProgressBar(0f, 100f, 1f, false, skin, "red-bar");
        leftCol.add(healthBar).width(226).align(Align.left).padLeft(21).padBottom(24);

        // Stack backgrounds + content
        Stack leftStack = new Stack();
        leftStack.add(leftColImage);
        leftStack.add(leftCol);
        root.add(leftStack).expand().left().bottom();

        // === CENTER COLUMN ===
        Table permInv = new Table();
        for (int i = 0; i < 5; i++) {
            ImageButton slot = new ImageButton(skin, "GUIslot");
            permInv.add(slot).size(96).pad(18);
            permSlots.add(slot);
        }

        Stack centerStack = new Stack();
        centerStack.add(centerColImage);
        centerStack.add(permInv);
        root.add(centerStack).expandX().bottom();

        // === RIGHT COLUMN ===
        Table rightCol = new Table();
        rightCol.right().bottom();

        apSlot = new Label("0", skin, "default");
        rightCol.add(apSlot).size(46).align(Align.left).padLeft(23).padBottom(46);
        rightCol.row();

        movementBar = new ProgressBar(0f, 100f, 1f, false, skin, "blue-bar");
        rightCol.add(movementBar).width(226).align(Align.left).padLeft(21).padRight(21).padBottom(24);

        Stack rightStack = new Stack();
        rightStack.add(rightColImage);
        rightStack.add(rightCol);
        root.add(rightStack).expand().right().bottom();
    }

    public void update(float delta) {
        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
        healthBar.setValue(player.health / player.maxHealth * 100f);
        movementBar.setValue(100 - (player.distanceTraveledThisTurn / player.moveDistance * 100));
        apSlot.setText(String.valueOf(player.actionPoints));

        // update slots
        updateInventoryUI();
        stage.act(delta);
    }

    public void render() {
        stage.draw();
    }

    private void updateInventoryUI() {
        List<Item> tempInventory = player.invTemp.getContents();
        List<Item> permInventory = player.invPerm.getContents();

        for (int i = 0; i < tempSlots.size(); i++) {
            ImageButton slot = tempSlots.get(i);
            slot.clearChildren();
            if (i < tempInventory.size()) {
                Item item = tempInventory.get(i);
                if (item != null && item.getItemTexture() != null) {
                    Image icon = new Image(new TextureRegionDrawable(item.getItemTexture()));
                    tempSlotItems.put(slot, item);
                    slot.add(icon).size(64, 64).center();
                }
            }
        }

        for (int i = 0; i < permSlots.size(); i++) {
            ImageButton slot = permSlots.get(i);
            slot.clearChildren();
            if (i < permInventory.size()) {
                Item item = permInventory.get(i);
                if (item != null && item.getItemTexture() != null) {
                    Image icon = new Image(new TextureRegionDrawable(item.getItemTexture()));
                    slot.add(icon).size(64, 64).center();
                }
            }
        }
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
        font.dispose();
    }
}
