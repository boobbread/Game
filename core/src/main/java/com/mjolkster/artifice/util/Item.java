package com.mjolkster.artifice.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class Item {

    Texture texture;
    String name;
    boolean permanent;

    public Item(String name, boolean permanent) {
        this.name = name;
        this.texture = new Texture(Gdx.files.internal(name + ".png"));
        this.permanent = permanent;
    }

}
