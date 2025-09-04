package com.mjolkster.artifice.util.combat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

public class DamageIndicatorManager {

    private static Stage stage;
    private static Label.LabelStyle style;

    /**
     * Initialize the manager with a Stage and a font.
     * Call once at startup.
     */
    public static void init(Stage stage, BitmapFont font, float fontScale) {
        DamageIndicatorManager.stage = stage;


        style = new Label.LabelStyle();
        style.font = font;
        style.fontColor = Color.RED;

        font.getData().setScale(fontScale); // shrink or scale font
    }

    /**
     * Show floating damage at world coordinates x, y.
     * Damage will rise and fade out automatically.
     */
    public static void showDamage(float x, float y, int amount) {
        if (stage == null || style == null) return;
        Label damageLabel = new Label(String.valueOf(amount), style);
        damageLabel.setAlignment(Align.center);
        damageLabel.setPosition(x, y);

        // Animate rise and fade out, then remove from stage
        damageLabel.addAction(Actions.sequence(
            Actions.parallel(
                Actions.moveBy(0, 30, 1f), // rise 30 pixels over 1 second
                Actions.fadeOut(1f)
            ),
            Actions.removeActor()
        ));

        stage.addActor(damageLabel);
    }

    /**
     * Call this in your main render loop
     */
    public static void update(float delta) {
        if (stage != null) stage.act(delta);
    }

    /**
     * Call this in your main render loop after batch.draw() if needed
     */
    public static void render() {
        if (stage != null) stage.draw();
    }
}
