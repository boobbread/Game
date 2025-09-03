package com.mjolkster.artifice.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.mjolkster.artifice.entities.ChestEntity;
import com.mjolkster.artifice.items.Item;

import java.util.List;

public class ChestGUI {

    private final GameScreen gameScreen;
    private Stage stage;
    private Skin skin; // You need a Skin for UI
    private Table table;

    public ChestGUI(Stage stage, Skin skin, GameScreen gameScreen) {
        this.stage = stage;
        this.skin = skin;
        this.gameScreen = gameScreen;
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
    }

    public void showLoot(List<Item> loot, ChestEntity chest, Runnable onClose) {
        table.clear();
        table.defaults().pad(10);

        Table row = new Table(); // horizontal row container

        for (Item item : loot) {
            String desc = item.getItemDescription() != null ? item.getItemDescription() : "";
            TextButton button = new TextButton("", skin);
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!gameScreen.player.addItemToTemporaryInv(item)) {
                        showReplaceDialog(item, gameScreen.player.invTemp.getContents(), () -> {
                        });
                    }
                    onClose.run();
                    chest.closeChest();
                }
            });

            button.clearChildren(); // remove default label

            Table content = new Table();
            Image icon = new Image(new TextureRegionDrawable(item.getItemTexture()));
            Label label = new Label(item.getItemName() + "\n\n" + desc, skin);

            content.add(icon).size(64).padBottom(10).row();
            content.add(label).expandX().center();

            button.add(content).expand().fill();

            row.add(button).width(300).height(420).pad(16); // set consistent size
        }

        table.add(row).center(); // add the horizontal row to the main table
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
                    gameScreen.player.invTemp.replaceItemInSlot(newItem, slotIndex);
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
    }
}
