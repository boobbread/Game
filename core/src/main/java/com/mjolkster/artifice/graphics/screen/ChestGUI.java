package com.mjolkster.artifice.graphics.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.mjolkster.artifice.core.world.ChestEntity;
import com.mjolkster.artifice.core.items.Item;

import java.util.ArrayList;
import java.util.List;

public class ChestGUI {

    private final GameScreen gameScreen;
    private final Stage stage;
    private final Skin skin;
    private final Table table;
    private final List<TextButton> lootButtons = new ArrayList<>();
    private TextButton closeButton = null;
    private int slotNumber = 0;
    private InputListener inputListener;

    public ChestGUI(Stage stage, Skin skin, GameScreen gameScreen) {
        this.stage = stage;
        this.skin = skin;
        this.gameScreen = gameScreen;
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        inputListener = new InputListener() {
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.LEFT) {

                    if (slotNumber > 0) {
                        slotNumber --;
                        stage.setKeyboardFocus(lootButtons.get(slotNumber));

                    } else {
                        slotNumber = 2;
                        stage.setKeyboardFocus(lootButtons.get(slotNumber));
                    }

                    Gdx.app.log("ChestGUI", "Focus set to slot " + slotNumber);

                }
                if (keycode == Input.Keys.RIGHT) {

                    if (slotNumber < 2) {
                        slotNumber ++;
                        stage.setKeyboardFocus(lootButtons.get(slotNumber));
                    } else {
                        slotNumber = 0;
                        stage.setKeyboardFocus(lootButtons.get(slotNumber));
                    }

                    Gdx.app.log("ChestGUI", "Focus set to slot " + slotNumber);

                }
                if (keycode == Input.Keys.ENTER) {
                    TextButton slot = lootButtons.get(slotNumber);

                    // Fire touchDown
                    InputEvent down = new InputEvent();
                    down.setType(InputEvent.Type.touchDown);
                    down.setStage(stage);
                    down.setTarget(slot);
                    slot.fire(down);

                    // Fire touchUp
                    InputEvent up = new InputEvent();
                    up.setType(InputEvent.Type.touchUp);
                    up.setStage(stage);
                    up.setTarget(slot);
                    slot.fire(up);

                }
                if (keycode == Input.Keys.ESCAPE) {
                    InputEvent down = new InputEvent();
                    down.setType(InputEvent.Type.touchDown);
                    down.setStage(stage);
                    down.setTarget(closeButton);
                    closeButton.fire(down);

                    // Fire touchUp
                    InputEvent up = new InputEvent();
                    up.setType(InputEvent.Type.touchUp);
                    up.setStage(stage);
                    up.setTarget(closeButton);
                    closeButton.fire(up);
                }
                return true;
            }
        };

        this.stage.addListener(inputListener);
    }

    public void showLoot(List<Item> loot, ChestEntity chest, Runnable onClose) {
        table.clear();
        table.defaults().pad(10);

        Table row = new Table(); // horizontal row container

        for (Item item : loot) {
            String desc = item.getItemDescription() != null ? item.getItemDescription() : "";
            TextButton button = new TextButton("", skin, item.getRarity().toString());
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!gameScreen.getPlayer().addItemToTemporaryInv(item)) {
                        showReplaceDialog(item, gameScreen.getPlayer().invTemp.getContents(), () -> {
                        });
                    }
                    onClose.run();
                    chest.closeChest();
                    gameScreen.pauseGame();
                }
            });

            button.addListener(new FocusListener() {
                @Override
                public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                    button.setChecked(focused);
                }
            });

            button.clearChildren(); // remove default label

            Table content = new Table();
            Image icon = new Image(new TextureRegionDrawable(item.getItemTexture()));
            Label name = new Label(item.getDisplayName(), skin);
            String descriptionText = desc + " \n" + item.getFormattedBonus() + "\n" + item.isItemPoisonous();
            Label description = new Label(descriptionText, skin);
            description.setWrap(true);
            description.setAlignment(Align.center);

            content.add(icon).size(64).padBottom(30).row();
            content.add(name).minHeight(16).expandX().center().padBottom(30).row();
            content.add(description).minHeight(64).minWidth(220).padBottom(50).expandX().center();

            button.add(content).expand().fill();

            row.add(button).width(300).height(420).pad(16); // set consistent size
            lootButtons.add(button);
        }

        table.add(row).center();
        table.row();

        Table row2 = new Table();

        // Add a close button below the loot
        TextButton closeButton = new TextButton("Close", skin, "transparent");
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onClose.run();
                chest.closeChest();
                gameScreen.pauseGame();
            }
        });
        this.closeButton = closeButton;
        row2.add(closeButton).padTop(20);
        table.add(row2).center();

        table.pack();

        // Make sure the stage receives input
        Gdx.input.setInputProcessor(stage);
    }

    public void showReplaceDialog(Item newItem, List<Item> contents, Runnable onCancel) {

        Dialog dialog = new Dialog("Inventory full!", skin) {
            @Override
            public float getPrefWidth() {
                return 300f; // force fixed width
            }

            @Override
            public float getPrefHeight() {
                return 420f; // force fixed height
            }

            @Override
            protected void result(Object obj) {
                if (Boolean.FALSE.equals(obj)) {
                    onCancel.run();
                }
            }
        };

        dialog.getTitleLabel().setAlignment(Align.center);
        dialog.getTitleTable().padTop(40f);
        dialog.getButtonTable().padBottom(100f);
        dialog.text("Your temporary inventory is full!\nPick a slot to replace, or cancel.").pad(20);

        Table slotsTable = new Table();

        for (int i = 0; i < contents.size(); i++) {
            final int slotIndex = i;
            Item existing = contents.get(i);

            ImageButton slotButton = new ImageButton(skin, "GUIslot");

            // add icon if there is one
            if (existing != null && existing.getItemTexture() != null) {
                Image icon = new Image(new TextureRegionDrawable(existing.getItemTexture()));
                slotButton.add(icon).size(32).center();
            }

            slotButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Replace item in that slot
                    gameScreen.getPlayer().invTemp.replaceItemInSlot(newItem, slotIndex);
                    dialog.hide();
                }
            });

            slotsTable.add(slotButton).pad(10);
        }

        dialog.getContentTable().row();
        dialog.getContentTable().add(slotsTable);

        dialog.button(new TextButton("Cancel", skin, "transparent"), false);

        dialog.show(stage);

    }

    public void hide() {
        table.clear();
        gameScreen.getHud().resetFocus();
        gameScreen.getStage().removeListener(inputListener);
    }
}
