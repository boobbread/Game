package com.mjolkster.artifice.util.ai;

import com.badlogic.gdx.math.Vector2;
import com.mjolkster.artifice.core.world.MapGenerator;
import com.mjolkster.artifice.graphics.screen.GameScreen;

import java.util.*;

public class AStarPathfinder {

    public Queue<Vector2> createAStarPathfinder(Vector2 start, Vector2 goal, GameScreen gameScreen) {
        MapGenerator mapGen = gameScreen.getGameMap().getMapGenerator();
        if (mapGen == null) return new LinkedList<>();

        Integer[][] grid = mapGen.getAStarGridSnapshot();
        int width = mapGen.width;
        int height = mapGen.height;

        // Validate coordinates
        if (start.x < 0 || start.y < 0 || goal.x < 0 || goal.y < 0 ||
            start.x >= width || start.y >= height ||
            goal.x >= width || goal.y >= height) {
            return new LinkedList<>();
        }

        // Initialize data structures
        PriorityQueue<Node> openQueue = new PriorityQueue<>(Comparator.comparingDouble(Node::getF));
        Map<Vector2, Node> openMap = new HashMap<>();
        Set<Vector2> closedSet = new HashSet<>();

        if (hasLineOfSight(start, goal, grid)) {

            Node startNode = new Node(start, 0, Math.hypot(goal.x - start.x, goal.y - start.y), null);
            openQueue.add(startNode);
            openMap.put(start, startNode);

            while (!openQueue.isEmpty()) {
                Node current = openQueue.poll();
                openMap.remove(current.position);

                if (current.position.epsilonEquals(goal, 0.1f)) {
                    return reconstructPath(current);
                }

                closedSet.add(current.position);

                for (Node neighbor : getValidNeighbours(current, grid, width, height, goal)) {
                    if (closedSet.contains(neighbor.position)) continue;

                    Node existing = openMap.get(neighbor.position);
                    if (existing == null) {
                        openQueue.add(neighbor);
                        openMap.put(neighbor.position, neighbor);
                    } else if (neighbor.getG() < existing.getG()) {
                        openQueue.remove(existing);
                        openQueue.add(neighbor);
                        openMap.put(neighbor.position, neighbor);
                    }
                }
            }
        }
        return new LinkedList<>(); // Empty path if none found
    }

    private List<Node> getValidNeighbours(Node node, Integer[][] grid, int width, int height, Vector2 goal) {
        List<Node> neighbors = new ArrayList<>(8); // Pre-allocate for 8 neighbors
        final int x = node.getX();
        final int y = node.getY();

        // Check all 8 directions
        int[][] directions = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1}, {0, 1},
            {1, -1}, {1, 0}, {1, 1}
        };

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                if (grid[nx][ny] == 0) {
                    double cost = (dir[0] == 0 || dir[1] == 0) ? 1.0 : 1.414; // Pre-calculated sqrt(2)
                    double hCost = Math.hypot(goal.x - nx, goal.y - ny);
                    neighbors.add(new Node(new Vector2(nx, ny), node.getG() + cost, hCost, node));
                }
            }
        }
        return neighbors;
    }

    private Queue<Vector2> reconstructPath(Node node) {
        LinkedList<Vector2> path = new LinkedList<>();
        while (node != null) {
            path.addFirst(node.position);
            node = node.getParent();
        }
        return path;
    }

    public boolean hasLineOfSight(Vector2 start, Vector2 end, Integer[][] grid) {
        int x0 = (int) start.x;
        int y0 = (int) start.y;
        int x1 = (int) end.x;
        int y1 = (int) end.y;

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            if (grid[x0][y0] != 0) return false; // blocked cell

            if (x0 == x1 && y0 == y1) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }

        return true;
    }

}
