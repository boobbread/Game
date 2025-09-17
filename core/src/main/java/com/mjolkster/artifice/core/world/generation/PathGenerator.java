package com.mjolkster.artifice.core.world.generation;

import com.mjolkster.artifice.util.data.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * A helper class for {@link com.mjolkster.artifice.core.world.generation.MapGenerator MapGenerator}
 * Carves a route through the map to ensure traversability
 */
public class PathGenerator {

    public static void createPath(Pair<Point, Point> points, int width, HashMap<Point, Integer> gridVertices) {
        List<Point> linePoints = new ArrayList<>();
        int x1 = points.first.x;
        int y1 = points.first.y;
        int x2 = points.second.x;
        int y2 = points.second.y;

        // Bresenham's line algorithm
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            linePoints.add(new Point(x1, y1));
            if (x1 == x2 && y1 == y2) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }

        for (Point p : linePoints) {
            for (int wx = -width / 2; wx <= width / 2; wx++) {
                for (int wy = -width / 2; wy <= width / 2; wy++) {
                    Point np = new Point(p.x + wx, p.y + wy);
                    if (gridVertices.containsKey(np)) {
                        gridVertices.put(np, 1); // Set to land
                    }
                }
            }
        }

    }

    public static Pair<Pair<Point, Point>, Double> closestPoints(Set<Point> island1, Set<Point> island2) {

        Point islandPoint1 = new Point();
        Point islandPoint2 = new Point();

        double minDistance = 10000;

        for (Point point1 : island1) {
            for (Point point2 : island2) {
                double dist = Math.sqrt(Math.pow(point2.x - point1.x, 2) + Math.pow(point2.y - point1.y, 2));
                if (dist < minDistance) {
                    minDistance = dist;
                    islandPoint1 = point1;
                    islandPoint2 = point2;
                }

            }
        }

        return new Pair<>(new Pair<>(islandPoint1, islandPoint2), minDistance);
    }
}
