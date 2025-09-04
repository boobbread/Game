package com.mjolkster.artifice.util.geometry;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HitboxExtractor {

    /**
     * Extracts a Hitbox from a sprite region of a texture.
     *
     * @param texture        the sprite sheet texture
     * @param tx             x of the region
     * @param ty             y of the region (bottom-left)
     * @param tdx            width of the region
     * @param tdy            height of the region
     * @param alphaThreshold minimum alpha to count as opaque (0-255)
     * @return a Hitbox with rectangles relative to the texture region
     */
    public static Hitbox extractHitbox(Texture texture, int tx, int ty, int tdx, int tdy, int alphaThreshold) {
        if (!texture.getTextureData().isPrepared()) {
            texture.getTextureData().prepare();
        }

        Pixmap pixmap = texture.getTextureData().consumePixmap();
        try {
            boolean[][] opaque = new boolean[tdx][tdy];
            boolean[][] visited = new boolean[tdx][tdy];

            // Fill opaque map
            for (int y = 0; y < tdy; y++) {
                for (int x = 0; x < tdx; x++) {
                    int px = tx + x;
                    int py = ty + y; // LibGDX Pixmap origin is bottom-left
                    int pixel = pixmap.getPixel(px, py);
                    int alpha = (pixel >>> 24) & 0xFF;
                    opaque[x][y] = alpha >= alphaThreshold;
                }
            }

            List<Rectangle> rects = new ArrayList<>();

            // Flood fill to find connected opaque regions
            for (int y = 0; y < tdy; y++) {
                for (int x = 0; x < tdx; x++) {
                    if (opaque[x][y] && !visited[x][y]) {
                        Rectangle r = floodFill(x, y, opaque, visited, tdx, tdy);
                        rects.add(r);
                    }
                }
            }

            // Optional: limit to top 3 largest rectangles
            rects.sort((a, b) -> Float.compare(b.width * b.height, a.width * a.height));
            if (rects.size() > 3) rects = rects.subList(0, 3);

            // Adjust rectangles relative to sprite region
            for (Rectangle r : rects) {
                r.x += tx;
                r.y += ty;
            }

            return new Hitbox(rects);
        } finally {
            pixmap.dispose();
        }
    }

    private static Rectangle floodFill(int startX, int startY, boolean[][] opaque, boolean[][] visited, int maxX, int maxY) {
        int minX = startX, maxXFound = startX;
        int minY = startY, maxYFound = startY;
        LinkedList<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startX, startY});
        visited[startX][startY] = true;

        while (!queue.isEmpty()) {
            int[] p = queue.poll();
            int x = p[0], y = p[1];

            minX = Math.min(minX, x);
            maxXFound = Math.max(maxXFound, x);
            minY = Math.min(minY, y);
            maxYFound = Math.max(maxYFound, y);

            checkNeighbor(x + 1, y, opaque, visited, queue, maxX, maxY);
            checkNeighbor(x - 1, y, opaque, visited, queue, maxX, maxY);
            checkNeighbor(x, y + 1, opaque, visited, queue, maxX, maxY);
            checkNeighbor(x, y - 1, opaque, visited, queue, maxX, maxY);
        }

        return new Rectangle(minX, minY, maxXFound - minX + 1, maxYFound - minY + 1);
    }

    private static void checkNeighbor(int x, int y, boolean[][] opaque, boolean[][] visited, LinkedList<int[]> queue, int maxX, int maxY) {
        if (x >= 0 && x < maxX && y >= 0 && y < maxY && opaque[x][y] && !visited[x][y]) {
            visited[x][y] = true;
            queue.add(new int[]{x, y});
        }
    }
}
