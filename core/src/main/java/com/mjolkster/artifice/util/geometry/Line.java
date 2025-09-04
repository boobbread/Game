package com.mjolkster.artifice.util.geometry;

import com.badlogic.gdx.math.Vector2;

public class Line {
    public final Vector2 start;
    public final Vector2 end;

    public Line(Vector2 start, Vector2 end) {
        this.start = start;
        this.end = end;
    }

    public Line(float x1, float y1, float x2, float y2) {
        this.start = new Vector2(x1, y1);
        this.end = new Vector2(x2, y2);
    }

    @Override
    public String toString() {
        return String.format("Line(%.1f,%.1f -> %.1f,%.1f)", start.x, start.y, end.x, end.y);
    }
}
