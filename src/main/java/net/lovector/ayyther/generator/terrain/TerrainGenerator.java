package net.lovector.ayyther.generator.terrain;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.util.noise.SimplexOctaveGenerator;

/**
 * Generates terrain, i.e. the actual islands.
 */
public final class TerrainGenerator {
    private final SimplexOctaveGenerator noiseGenerator;
    private final double noiseFrequency;
    private final double noiseAmplitude;
    private final double baseScaleX;
    private final double baseScaleY;
    private final double baseScaleZ;
    private final double islandThreshold;
    private final int minY;
    private final int maxY;
    private final int fadeDistance;

    /**
     * Creates a new TerrainGenerator using the paramaters as its generation settings.
     * 
     * @param seed The seed of the world that the generator is being used in
     * @param noiseOctaves The noise octaves
     * @param noiseFrequency The noise frequency
     * @param noiseAmplitude The noise amplitude
     * @param baseScaleX The value the x is multiplied by before using it to generate noise
     * @param baseScaleY The value the y is multiplied by before using it to generate noise
     * @param baseScaleZ The value the z is multiplied by before using it to generate noise
     * @param islandThreshold The minimum noise value that is considered to be a solid block
     * @param minY The bottom of the generation range
     * @param maxY The top of the generation range
     * @param fadeDistance The fade distance of the noise on the top and bottom
     */
    public TerrainGenerator(long seed, int noiseOctaves, double noiseFrequency, double noiseAmplitude,
            double baseScaleX, double baseScaleY, double baseScaleZ,
            double islandThreshold, int minY, int maxY, int fadeDistance) {
        this.noiseGenerator = new SimplexOctaveGenerator(seed, noiseOctaves);
        this.noiseFrequency = noiseFrequency;
        this.noiseAmplitude = noiseAmplitude;
        this.baseScaleX = baseScaleX;
        this.baseScaleY = baseScaleY;
        this.baseScaleZ = baseScaleZ;
        this.islandThreshold = islandThreshold;
        this.minY = minY;
        this.maxY = maxY;
        this.fadeDistance = fadeDistance;
    }

    /**
     * Generates a column of terrain using this instance's generation settings
     * 
     * @param random The random generator to use
     * @param data The ChunkData to generate the terrain into
     * @param surfaceGenerator The surface generator to use to generate the surface of the terrain
     * @param worldX The x location of the column in world space. This should lie inside the chunk you're generating.
     * @param worldZ The y location of the column in world space. This should lie inside the chunk you're generating.
     * @return
     */
    public boolean generateColumn(Random random, ChunkData data, TerrainSurfaceGenerator surfaceGenerator, int worldX, int worldZ) {
        boolean didAnything = false;

        int localX = Math.floorMod(worldX, 16);
        int localZ = Math.floorMod(worldZ, 16);
        for (int y = maxY; y >= minY; --y) {
            // Use noiseMultiplier to fade the noise at the top and bottom
            double noiseMultiplier = 1.0;
            if (y <= minY + fadeDistance)
                noiseMultiplier *= (y - minY) / (double) fadeDistance;
            if (y >= minY - fadeDistance)
                noiseMultiplier *= (maxY - y) / (double) fadeDistance;

            double noiseX = worldX * baseScaleX;
            double noiseY = y * baseScaleY;
            double noiseZ = worldZ * baseScaleZ;

            boolean isBlockFilled = noiseGenerator.noise(noiseX, noiseY, noiseZ, noiseFrequency, noiseAmplitude)
                    * noiseMultiplier > islandThreshold;

            Material blockMaterial = surfaceGenerator.advance(random, isBlockFilled);

            if (blockMaterial != Material.AIR) {
                if (!didAnything)
                    didAnything = true;

                data.setBlock(localX, y, localZ, blockMaterial);
            }
        }

        return didAnything;
    }
}