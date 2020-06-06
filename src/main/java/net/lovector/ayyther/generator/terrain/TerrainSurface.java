package net.lovector.ayyther.generator.terrain;

import java.util.Random;

import org.bukkit.Material;

/**
 * Describes a terrain surface. A TerrainSurface could be used to
 * generate terrain (if the appropriate methods were public),
 * but {@link TerrainSurfaceGenerator}s provide a friendy interface.
 */
public abstract class TerrainSurface {
    /**
     * Creates a new generator with this surface
     * 
     * @return A new generator with this surface
     */
    public TerrainSurfaceGenerator makeGenerator() {
        return new TerrainSurfaceGenerator(this);
    }

    /**
     * @param current The current surface material
     * @param currentCount The number of the current material so far
     * @param random The random generator to be used
     * @param blockFilled Whether or not the block is filled
     * @return The next material that should be generated
     */
    abstract Material getNext(Material current, int currentCount, Random random, boolean blockFilled);
}