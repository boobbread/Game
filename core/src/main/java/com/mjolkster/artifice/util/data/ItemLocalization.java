package com.mjolkster.artifice.util.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

public class ItemLocalization {
    private static ObjectMap<String, ItemStrings> entries;

    public static void load() {
        Json json = new Json();
        entries = json.fromJson(ObjectMap.class, ItemStrings.class, Gdx.files.internal("items.json"));
    }

    public static ItemStrings get(String itemName) {
        if (entries == null) {
            load();
        }
        return entries.get(itemName);
    }

    public static class ItemStrings {
        public String displayName;
        public String description;
    }
}

