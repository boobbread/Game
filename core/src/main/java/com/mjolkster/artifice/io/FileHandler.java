package com.mjolkster.artifice.io;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.mjolkster.artifice.core.entities.Archetype;
import com.mjolkster.artifice.core.entities.PlayableCharacter;
import com.mjolkster.artifice.core.items.Item;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileHandler {
    public static ArrayList<SaveSlot> tempSlots = new ArrayList<>(Arrays.asList(null, null, null));

    public static boolean CreateNewSave(PlayableCharacter player, long seed, int slotNumber) {
        final int MAX_SLOTS = 3;

        if (slotNumber < 0 || slotNumber >= MAX_SLOTS) {
            return false;
        }

        Save newSave = new Save();
        newSave.health = player.health;
        newSave.tempInv = player.invTemp.getContents();
        newSave.permanentInv = player.invPerm.getContents();
        newSave.currentWorldSeed = seed;
        newSave.archetype = player.archetype;
        newSave.roundsPassed = player.roundsPassed;

        SaveSlot saveSlot = new SaveSlot();
        saveSlot.slotNumber = slotNumber;
        saveSlot.saveData = newSave;
        saveSlot.timestamp = System.currentTimeMillis();
        saveSlot.isEmpty = false;

        try {
            File savesDir = new File("saves");
            if (!savesDir.exists()) {
                savesDir.mkdirs();
            }

            File saveFile = new File("saves/slot_" + slotNumber + ".json");
            if (saveFile.createNewFile()) {
                Gdx.app.log("FileHandler", "Created new file " + saveFile.getName());
            } else {
                Gdx.app.log("FileHandler", "Overwriting existing save in slot " + slotNumber);
            }
        } catch (IOException e) {
            Gdx.app.log("FileHandler: Error", "An error occurred creating save file.");
            e.printStackTrace();
            return false;
        }

        Json json = new Json();
        FileHandle file = Gdx.files.local("saves/slot_" + slotNumber + ".json");
        json.toJson(saveSlot, file);

        return true;
    }

    public static SaveSlot loadSave(int slotNumber) {
        FileHandle file = Gdx.files.local("saves/slot_" + slotNumber + ".json");
        if (!file.exists()) {
            // Return empty slot
            SaveSlot emptySlot = new SaveSlot();
            emptySlot.slotNumber = slotNumber;
            emptySlot.isEmpty = true;
            return emptySlot;
        }

        Json json = new Json();
        return json.fromJson(SaveSlot.class, file);
    }

    public static SaveSlot loadTempSave(int slotNumber) {
        if (slotNumber < 0 || slotNumber >= tempSlots.size()) return null;
        return tempSlots.get(slotNumber); // may be null if never saved
    }

    public static List<SaveSlot> getAllSaveSlots() {
        List<SaveSlot> slots = new ArrayList<>();
        final int MAX_SLOTS = 3;

        for (int i = 0; i < MAX_SLOTS; i++) {
            slots.add(loadSave(i));
        }

        return slots;
    }

    public static boolean deleteSave(int slotNumber) {
        FileHandle file = Gdx.files.local("saves/slot_" + slotNumber + ".json");
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public static void saveTemp(int slotNumber, PlayableCharacter player) {
        SaveSlot saveSlot = new SaveSlot();
        Save saveData = new Save();

        saveData.health = player.health;
        saveData.tempInv = player.invTemp.getContents();
        saveData.permanentInv = player.invPerm.getContents();
        saveData.archetype = player.archetype;
        saveData.roundsPassed = player.roundsPassed;

        saveSlot.saveData = saveData;

        // store in memory or write to disk as needed
        tempSlots.set(slotNumber, saveSlot);
    }

    public static class Save {
        public float health;
        public List<Item> tempInv;
        public List<Item> permanentInv;
        public long currentWorldSeed;
        public Archetype archetype;
        public int roundsPassed;
    }

    public static class SaveSlot {
        public int slotNumber;
        public Save saveData;
        public long timestamp;
        public boolean isEmpty;
    }
}
