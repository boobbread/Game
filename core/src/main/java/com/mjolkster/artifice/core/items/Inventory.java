package com.mjolkster.artifice.core.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Inventory {

    private final int size;
    private List<Item> contents;

    public Inventory(int size) {
        this.size = size;
        this.contents = new ArrayList<>(Collections.nCopies(size, null));
    }

    public boolean addItem(Item item) {
        for (int i = 0; i < size; i++) {
            if (contents.get(i) == null) {
                contents.set(i, item);
                return true;
            }
        }
        return false;
    }

    public Item replaceItemInSlot(Item item, int slot) {
        checkSlot(slot);
        Item previousItem = contents.get(slot);
        contents.set(slot, item);
        return previousItem;
    }

    public void removeItemFromSlot(int slot) {
        checkSlot(slot);
        contents.set(slot, null);
    }

    public boolean isFull() {
        return contents.stream().noneMatch(Objects::isNull);
    }

    public Item getItemInSlot(int slot) {
        checkSlot(slot);
        return contents.get(slot);
    }

    public List<Item> getContents() {
        return contents;
    }

    public void setContents(List<Item> inptContents) {
        this.contents = inptContents;
    }

    private void checkSlot(int slot) {
        if (slot < 0 || slot >= size) {
            throw new IndexOutOfBoundsException("Slot " + slot + " is outside of index range 0-" + (size - 1));
        }
    }
}
