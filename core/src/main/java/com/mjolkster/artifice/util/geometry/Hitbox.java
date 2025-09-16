package com.mjolkster.artifice.util.geometry;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class Hitbox implements Iterable<Rectangle> {

    // Entity space rectangles
    private final List<Rectangle> localRectangles;

    // World space rectangles
    private final List<Rectangle> worldRectangles;

    public Hitbox(Rectangle rect) {
        this.localRectangles = new ArrayList<>();
        this.localRectangles.add(new Rectangle(rect));
        this.worldRectangles = new ArrayList<>();
        this.worldRectangles.add(new Rectangle(rect));
    }

    public Hitbox(List<Rectangle> rects) {
        this.localRectangles = new ArrayList<>();
        for (Rectangle r : rects) {
            this.localRectangles.add(new Rectangle(r));
        }

        this.worldRectangles = new ArrayList<>();
        for (Rectangle r : rects) {
            this.worldRectangles.add(new Rectangle(r));
        }
    }

    public void add(Rectangle rect) {
        this.localRectangles.add(new Rectangle(rect));
        this.worldRectangles.add(new Rectangle(rect));
    }

    public void remove(Rectangle rect) {
        int index = this.localRectangles.indexOf(rect);
        if (index >= 0) {
            this.localRectangles.remove(index);
            this.worldRectangles.remove(index);
        }
    }

    /**
     * Iterate over all **world** rectangles
     */
    @Override
    public Iterator<Rectangle> iterator() {
        return worldRectangles.iterator();
    }

    /**
     * Perform an action on each rectangle (world coordinates)
     */
    public void forEachRect(Consumer<Rectangle> action) {
        worldRectangles.forEach(action);
    }

    /**
     * Translate all world rectangles (e.g., collision nudges)
     */
    public void translate(float dx, float dy) {
        for (Rectangle r : worldRectangles) {
            r.x += dx;
            r.y += dy;
        }
    }

    /**
     * Set world position using local rectangles as offsets
     */
    public void setOrigin(float ox, float oy) {
        for (int i = 0; i < localRectangles.size(); i++) {
            Rectangle local = localRectangles.get(i);
            Rectangle world = worldRectangles.get(i);
            world.set(ox + local.x, oy + local.y, local.width, local.height);
        }
    }

    /**
     * Get the minimal bounding rectangle containing all sub-rectangles
     */
    public Rectangle getBounds() {
        if (worldRectangles.isEmpty()) return new Rectangle();
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
        for (Rectangle r : worldRectangles) {
            minX = Math.min(minX, r.x);
            minY = Math.min(minY, r.y);
            maxX = Math.max(maxX, r.x + r.width);
            maxY = Math.max(maxY, r.y + r.height);
        }
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Check if this hitbox overlaps another hitbox
     */
    public boolean overlaps(Hitbox other) {
        for (Rectangle r1 : worldRectangles) {
            for (Rectangle r2 : other.worldRectangles) {
                if (r1.overlaps(r2)) return true;
            }
        }
        return false;
    }

    /**
     * Check if this hitbox overlaps a line
     */
    public boolean overlaps(Line line) {
        return Intersector.intersectSegmentRectangle(line.start, line.end, getBounds());
    }

    /**
     * Check if this hitbox overlaps a rectangle
     */
    public boolean overlaps(Rectangle other) {
        for (Rectangle r : worldRectangles) {
            if (r.overlaps(other)) return true;
        }
        return false;
    }

    public int size() {
        return worldRectangles.size();
    }

    public Rectangle get(int index) {
        return worldRectangles.get(index);
    }

    /**
     * Reset all rectangles to local coordinates (useful for scaling)
     */
    public void resetToLocal() {
        for (int i = 0; i < localRectangles.size(); i++) {
            Rectangle local = localRectangles.get(i);
            Rectangle world = worldRectangles.get(i);
            world.set(local);
        }
    }
}
