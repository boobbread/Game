package com.mjolkster.artifice.util.geometry;

import com.badlogic.gdx.math.Vector2;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Line)) return false;
        Line other = (Line) o;
        return start.epsilonEquals(other.start, 0.001f)
            && end.epsilonEquals(other.end, 0.001f);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Math.round(start.x * 1000), Math.round(start.y * 1000),
            Math.round(end.x * 1000), Math.round(end.y * 1000));
    }

    @Override
    public String toString() {
        return String.format("Line(%.1f,%.1f -> %.1f,%.1f)", start.x, start.y, end.x, end.y);
    }
}
