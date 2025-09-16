package com.mjolkster.artifice.core.world.generation;

import com.mjolkster.artifice.util.data.Pair;

import java.awt.*;
import java.util.*;
import java.util.List;

public class IslandManager {

    private final HashMap<Point, Integer> gridVertices;
    private final List<Set<Point>> islands;

    public IslandManager(HashMap<Point, Integer> gridVertices, List<Set<Point>> islands) {
        this.gridVertices = gridVertices;
        this.islands = islands;
    }

    void processIslands(int minSizeToKeep, int growthAmount) {
        Set<Point> visited = new HashSet<>();

        for (Point p : gridVertices.keySet()) {
            if (gridVertices.get(p) == 1 && !visited.contains(p)) {
                Set<Point> island = new HashSet<>();
                floodFill(p, island, visited);
                islands.add(island);
            }
        }

        for (Set<Point> island : islands) {
            if (island.size() < minSizeToKeep) {
                for (Point p : island) gridVertices.put(p, 0);
            }
        }

        for (Set<Point> island : islands) {
            if (island.size() >= minSizeToKeep) growIsland(island, growthAmount);
        }
    }

    private void floodFill(Point start, Set<Point> island, Set<Point> visited) {
        Queue<Point> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Point p = queue.poll();
            island.add(p);

            Point[] neighbors = {
                new Point(p.x + 1, p.y),
                new Point(p.x - 1, p.y),
                new Point(p.x, p.y + 1),
                new Point(p.x, p.y - 1)
            };

            for (Point n : neighbors) {
                if (gridVertices.containsKey(n) && gridVertices.get(n) == 1 && !visited.contains(n)) {
                    queue.add(n);
                    visited.add(n);
                }
            }
        }
    }

    private void growIsland(Set<Point> island, int growthAmount) {
        Set<Point> newCells = new HashSet<>();
        for (int i = 0; i < growthAmount; i++) {
            Set<Point> borderNeighbors = new HashSet<>();
            for (Point p : island) {
                Point[] neighbors = {
                    new Point(p.x + 1, p.y),
                    new Point(p.x - 1, p.y),
                    new Point(p.x, p.y + 1),
                    new Point(p.x, p.y - 1)
                };
                for (Point n : neighbors) {
                    if (gridVertices.containsKey(n) && gridVertices.get(n) == 0) {
                        borderNeighbors.add(n);
                    }
                }
            }
            island.addAll(borderNeighbors);
            newCells.addAll(borderNeighbors);
        }
        for (Point p : newCells) gridVertices.put(p, 1);
    }

    // Additional generation stuff

    void connectIslands() {

        updateIslandsList();

        Set<Set<Point>> connected = new HashSet<>();
        Set<Set<Point>> unconnected = new HashSet<>(islands);

        for (Set<Point> island : islands) {
            if (island.stream().anyMatch(p -> p.x == 0)) {
                connected.add(island);
                unconnected.remove(island);
            }
        }

        if (connected.isEmpty()) {
            Set<Point> leftmost = islands.stream()
                .min(Comparator.comparingInt(island -> island.stream().mapToInt(p -> p.x).min().orElse(Integer.MAX_VALUE)))
                .orElse(null);
            if (leftmost != null) {
                connected.add(leftmost);
                unconnected.remove(leftmost);
            }
        }

        while (!unconnected.isEmpty()) {
            double minDist = Double.MAX_VALUE;
            Pair<Point, Point> closestPair = null;
            Set<Point> closestIsland = null;

            for (Set<Point> connIsland : connected) {
                for (Set<Point> unconnIsland : unconnected) {
                    Pair<Pair<Point, Point>, Double> result = PathGenerator.closestPoints(connIsland, unconnIsland);
                    if (result.second < minDist) {
                        minDist = result.second;
                        closestPair = result.first;
                        closestIsland = unconnIsland;
                    }
                }
            }

            if (closestPair != null && closestIsland != null) {
                PathGenerator.createPath(closestPair, 3, gridVertices);
                connected.add(closestIsland);
                unconnected.remove(closestIsland);
            } else {
                break;
            }
        }
    }

    private void updateIslandsList() {
        islands.clear();
        Set<Point> visited = new HashSet<>();

        for (Point p : gridVertices.keySet()) {
            if (gridVertices.get(p) == 1 && !visited.contains(p)) {
                Set<Point> island = new HashSet<>();
                floodFill(p, island, visited);
                islands.add(island);
            }
        }
    }
}
