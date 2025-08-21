package com.mjolkster.artifice.generators;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class PerlinNoiseGenerator {

    private final Array<Vector2> gridVertices = new Array<>();
    private final HashMap<Vector2, Vector2> vertexMap = new HashMap<>();

    private float gridLength;
    private int scale;
    private int yCells;
    private Random random;

    // Convert (gridX, gridY) to index in gridVertices
    private int index(int gx, int gy) {
        return gy * (scale + 1) + gx;
    }

    public Array<Vector2> getGridVertices() {return gridVertices;}

    public float getGridLength() {return gridLength;}

    public Array<Float> generateFullNoiseMap(int width, int height) {
        Array<Float> noiseValues = new Array<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float noiseVal = sampleNoiseAt(x, y);
                noiseValues.add(noiseVal);
            }
        }

        return noiseValues;
    }

    public void init(float width, float height, int scale, long seed) {
        this.scale = scale;
        this.gridLength = width / scale;
        this.yCells = (int) Math.ceil(height / gridLength);

        gridVertices.clear();
        vertexMap.clear();

        this.random = new Random(seed);

        createGrid();
        createRandomVectors();
    }

    // Create grid vertices
    private void createGrid() {
        for (int j = 0; j <= yCells; j++) {
            for (int i = 0; i <= scale; i++) {
                Vector2 vertV2 = new Vector2(i * gridLength, j * gridLength);
                gridVertices.add(vertV2);
            }
        }
    }

    // Assign random gradient vectors to each vertex
    private void createRandomVectors() {
        for (Vector2 vertex : gridVertices) {
            float randomX = (random.nextFloat() * 2) - 1;
            float randomY = (random.nextFloat() * 2) - 1;
            vertexMap.put(vertex, new Vector2(randomX, randomY).nor());
        }
    }

    // Sample noise at a pixel coordinate
    public float sampleNoiseAt(float x, float y) {

        x = Math.min(x, scale * gridLength - 0.001f);
        y = Math.min(y, yCells * gridLength - 0.001f);

        int cellX = (int) (x / gridLength);
        int cellY = (int) (y / gridLength);

        // Get dot products for this cell and sample point
        List<Float> dotProducts = findDotProducts(cellX, cellY, x, y);

        // Relative position inside cell
        float u = (x - cellX * gridLength) / gridLength;
        float v = (y - cellY * gridLength) / gridLength;

        // Interpolate
        return interpolationHandler(dotProducts, u, v);
    }

    public float sampleFractalNoise(float x, float y, int octaves, float persistence, float lacunarity) {
        float total = 0f;
        float frequency = 1f;
        float amplitude = 1f;
        float maxValue = 0f;

        for (int i = 0; i < octaves; i++) {
            total += sampleNoiseAt(x * frequency, y * frequency) * amplitude;
            maxValue += amplitude;

            amplitude *= persistence;
            frequency *= lacunarity;
        }

        return total / maxValue;
    }

    // Find the dot products for the four corners of the cell
    private List<Float> findDotProducts(int cellX, int cellY, float x, float y) {

        // Get shared vertex objects
        Vector2 bottomL = gridVertices.get(index(cellX, cellY));
        Vector2 topL    = gridVertices.get(index(cellX, cellY + 1));
        Vector2 topR    = gridVertices.get(index(cellX + 1, cellY + 1));
        Vector2 bottomR = gridVertices.get(index(cellX + 1, cellY));

        // Vectors from corner to sample point
        Vector2 bLS = new Vector2(x - bottomL.x, y - bottomL.y);
        Vector2 tLS = new Vector2(x - topL.x,    y - topL.y);
        Vector2 tRS = new Vector2(x - topR.x,    y - topR.y);
        Vector2 bRS = new Vector2(x - bottomR.x, y - bottomR.y);

        // Gradient vectors
        Vector2 bLG = vertexMap.get(bottomL);
        Vector2 tLG = vertexMap.get(topL);
        Vector2 tRG = vertexMap.get(topR);
        Vector2 bRG = vertexMap.get(bottomR);

        // Dot products
        float bLD = bLG.dot(bLS);
        float tLD = tLG.dot(tLS);
        float tRD = tRG.dot(tRS);
        float bRD = bRG.dot(bRS);

        return List.of(bLD, tLD, tRD, bRD);
    }

    // Interpolate dot products
    public float interpolationHandler(List<Float> dotProducts, float u, float v) {

        float uFade = fade(u);
        float vFade = fade(v);

        float bottomInterp = lerp(dotProducts.get(0), dotProducts.get(3), uFade);
        float topInterp = lerp(dotProducts.get(1), dotProducts.get(2), uFade);

        return lerp(bottomInterp, topInterp, vFade);
    }

    // Perlin fade curve
    public float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    // Linear interpolation
    public float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }
}
