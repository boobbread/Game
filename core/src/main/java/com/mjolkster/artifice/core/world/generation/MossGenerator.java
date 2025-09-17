package com.mjolkster.artifice.core.world.generation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;

/**
 * A helper class for {@link com.mjolkster.artifice.core.world.generation.MapGenerator MapGenerator}
 * Generates a {@link com.badlogic.gdx.maps.tiled.TiledMapTileLayer TiledMapTileLayer} of randomised moss textures to add to the game map
 */
public class MossGenerator {

    private final StaticTiledMapTile[] mossTiles;
    private final int width;
    private final int height;
    private final double[][] moistureMap;
    private PerlinNoiseGenerator noise = new PerlinNoiseGenerator();

    public MossGenerator(int tileWidth, int tileHeight, int width, int height, long seed) {

        this.width = width;
        this.height = height;
        noise.init(width * 32, height * 32, 23, seed);

        Texture mossSet = new Texture(Gdx.files.internal("mosstexture.png"));
        TextureRegion[][] mossSplit = TextureRegion.split(mossSet, tileWidth, tileHeight);
        this.mossTiles = new StaticTiledMapTile[mossSplit.length * mossSplit[0].length];
        int index = 0;
        for (int row = 0; row < mossSplit.length; row++) {
            for (int col = 0; col < mossSplit[0].length; col++) {
                mossTiles[index] = new StaticTiledMapTile(mossSplit[row][col]);
                index++;
            }
        }

        this.moistureMap = new double[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                float nx = x / 32.0f;
                float ny = y / 32.0f;

                moistureMap[x][y] = noise.sampleNoiseAt(nx, ny);
            }
        }

    }

    void drawMossLayer(Integer[][] AStarGrid, TiledMapTileLayer mossLayer) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (AStarGrid[x][y] == 0 && moistureMap[x][y] > 0.6) {

                    double noiseValue = Math.random();

                    TiledMapTileLayer.Cell moss = new TiledMapTileLayer.Cell();
                    moss.setTile(mossTiles[(int) (noiseValue * 15)]);
                    mossLayer.setCell(x, y, moss);
                }
            }
        }
    }


}
