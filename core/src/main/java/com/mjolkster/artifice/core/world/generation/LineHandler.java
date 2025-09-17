package com.mjolkster.artifice.core.world.generation;

import com.badlogic.gdx.math.Vector2;
import com.mjolkster.artifice.util.geometry.Line;

import java.util.*;

/**
 * A helper class for {@link com.mjolkster.artifice.core.world.generation.MapGenerator MapGenerator}
 * Provides methods for creating a simplified set of lines for collision detection
 */

public class LineHandler {
    public static boolean isCollinear(Vector2 a, Vector2 b, Vector2 c, double e) {
        double cross = (b.x - a.x) * (c.y - b.y) - (b.y - a.y) * (c.x - b.x);
        return Math.abs(cross) < e;
    }

    // Base mapGen stuff

    public static List<Vector2> unifySegments(List<Vector2> points, double epsilon) {
        if (points.size() < 3) return points;

        List<Vector2> result = new ArrayList<>();
        result.add(points.get(0)); // first point always included

        for (int i = 1; i < points.size() - 1; i++) {
            Vector2 prev = result.get(result.size() - 1);
            Vector2 curr = points.get(i);
            Vector2 next = points.get(i + 1);

            if (isCollinear(prev, curr, next, epsilon)) {
                // Skip curr (it's redundant)
            } else {
                result.add(curr);
            }
        }

        result.add(points.get(points.size() - 1)); // last point
        return result;
    }

    public static List<List<Vector2>> orderOutline(Set<Line> lines) {
        Set<Line> working = new HashSet<>(lines);
        List<List<Vector2>> result = new ArrayList<>();

        while (!working.isEmpty()) {
            Line first = working.iterator().next();
            working.remove(first);

            List<Vector2> polyline = new ArrayList<>();
            polyline.add(first.start);
            polyline.add(first.end);

            boolean extended = true;
            while (!working.isEmpty() && extended) {
                extended = false;

                Vector2 last = polyline.get(polyline.size() - 1);
                for (Iterator<Line> it = working.iterator(); it.hasNext(); ) {
                    Line l = it.next();
                    if (last.equals(l.start)) {
                        polyline.add(l.end);
                        it.remove();
                        extended = true;
                        break;
                    } else if (last.equals(l.end)) {
                        polyline.add(l.start);
                        it.remove();
                        extended = true;
                        break;
                    }
                }
            }

            // extend backwards as well
            extended = true;
            while (!working.isEmpty() && extended) {
                extended = false;

                Vector2 firstPt = polyline.get(0);
                for (Iterator<Line> it = working.iterator(); it.hasNext(); ) {
                    Line l = it.next();
                    if (firstPt.equals(l.start)) {
                        polyline.add(0, l.end);
                        it.remove();
                        extended = true;
                        break;
                    } else if (firstPt.equals(l.end)) {
                        polyline.add(0, l.start);
                        it.remove();
                        extended = true;
                        break;
                    }
                }
            }

            result.add(polyline);
        }

        return result;
    }

    public static Set<Line> reconstructLines(List<Vector2> points) {
        Set<Line> result = new HashSet<>();
        for (int i = 0; i < points.size() - 1; i++) {
            result.add(new Line(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y));
        }
        return result;
    }
}
