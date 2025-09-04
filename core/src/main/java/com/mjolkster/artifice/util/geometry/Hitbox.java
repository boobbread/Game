package com.mjolkster.artifice.util.geometry;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class Hitbox implements Iterable<Rectangle> {

    private final List<Rectangle> rectangles;

    public Hitbox(Rectangle rect) {
        this.rectangles = new ArrayList<>();
        rectangles.add(rect);
    }

    public Hitbox(List<Rectangle> rects) {
        this.rectangles = new ArrayList<>(rects);
    }

    public void add(Rectangle rect) {
        rectangles.add(rect);
    }

    public void remove(Rectangle rect) {
        rectangles.remove(rect);
    }

    /**
     * Iterate over all rectangles
     */
    @Override
    public Iterator<Rectangle> iterator() {
        return rectangles.iterator();
    }

    /**
     * Perform an action on each rectangle
     */
    public void forEachRect(Consumer<Rectangle> action) {
        rectangles.forEach(action);
    }

    /**
     * Translate all rectangles
     */
    public void translate(float dx, float dy) {
        forEachRect(rect -> {
            rect.x += dx;
            rect.y += dy;
        });
    }

    /**
     * Set absolute position relative to first rectangle
     */
    public void setPosition(float x, float y) {
        if (rectangles.isEmpty()) return;
        Rectangle first = rectangles.get(0);
        float dx = x - first.x;
        float dy = y - first.y;
        translate(dx, dy);
    }

    /**
     * Scale all rectangles around their origin
     */
    public void scale(float scaleX, float scaleY) {
        forEachRect(rect -> {
            rect.x *= scaleX;
            rect.y *= scaleY;
            rect.width *= scaleX;
            rect.height *= scaleY;
        });
    }

    /**
     * Get the minimal bounding rectangle containing all sub-rectangles
     */
    public Rectangle getBounds() {
        if (rectangles.isEmpty()) return new Rectangle();
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
        for (Rectangle r : rectangles) {
            minX = Math.min(minX, r.x);
            minY = Math.min(minY, r.y);
            maxX = Math.max(maxX, r.x + r.width);
            maxY = Math.max(maxY, r.y + r.height);
        }
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Check if any rectangle overlaps another hitbox
     */
    public boolean overlaps(Hitbox other) {
        for (Rectangle r1 : rectangles) {
            for (Rectangle r2 : other.rectangles) {
                if (r1.overlaps(r2)) return true;
            }
        }
        return false;
    }

    /**
     * Check if any rectangle overlaps a line
     */
    public boolean overlaps(Line line) {
        return Intersector.intersectSegmentRectangle(
            line.start, line.end, getBounds());
    }

    /**
     * Check if any rectangle overlaps another rectangle
     */
    public boolean overlaps(Rectangle other) {
        for (Rectangle r1 : rectangles) {
            if (r1.overlaps(other)) return true;
        }
        return false;
    }

    /**
     * Get number of rectangles
     */
    public int size() {
        return rectangles.size();
    }

    /**
     * Get rectangle at index
     */
    public Rectangle get(int index) {
        return rectangles.get(index);
    }
}
